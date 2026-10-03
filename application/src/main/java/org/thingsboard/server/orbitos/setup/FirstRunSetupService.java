// SPDX-FileCopyrightText: Copyright The OrbitOS Authors
// SPDX-License-Identifier: Apache-2.0
package org.thingsboard.server.orbitos.setup;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.server.common.data.Dashboard;
import org.thingsboard.server.common.data.Device;
import org.thingsboard.server.common.data.Tenant;
import org.thingsboard.server.common.data.User;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.security.Authority;
import org.thingsboard.server.common.data.security.UserCredentials;
import org.thingsboard.server.dao.dashboard.DashboardService;
import org.thingsboard.server.dao.device.DeviceCredentialsService;
import org.thingsboard.server.dao.device.DeviceProfileService;
import org.thingsboard.server.dao.device.DeviceService;
import org.thingsboard.server.dao.service.DataValidator;
import org.thingsboard.server.dao.user.UserService;
import org.thingsboard.server.orbitos.i18n.PlatformLanguage;
import org.thingsboard.server.service.entitiy.tenant.TbTenantService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Locale;
import java.util.Map;

/**
 * First-run setup. A fresh install has no usable account: the system administrator is created
 * with an unknown random password ({@link #PENDING_SYSADMIN_EMAIL}). The setup page then creates,
 * in one step: the organization, its administrator (the owner's daily account), the system
 * administrator's real login, the platform language, and optionally a first device with a dashboard.
 * <p>
 * To stop a stranger on the network from claiming a new server, the page asks for a one-time
 * setup code that is only printed in the server log.
 */
@Slf4j
@Service
@Profile("!install")
public class FirstRunSetupService {

    public static final String PENDING_SYSADMIN_EMAIL = "sysadmin@setup.invalid";
    public static final String DEFAULT_SYSADMIN_EMAIL = "sysadmin@local.invalid";
    static final int MIN_PASSWORD = 12;
    static final int MAX_PASSWORD = 72; // bcrypt limit
    static final int MAX_FAILED_CODES = 10;
    private static final String CODE_ALPHABET = "23456789ABCDEFGHJKMNPQRSTVWXYZ"; // no 0/O, 1/I/L, U
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserService userService;
    private final TbTenantService tenantService;
    private final DeviceService deviceService;
    private final DeviceProfileService deviceProfileService;
    private final DeviceCredentialsService deviceCredentialsService;
    private final DashboardService dashboardService;
    private final PlatformLanguage platformLanguage;
    private final PasswordEncoder passwordEncoder;

    private volatile Boolean required;
    private String setupCode;
    private int failedCodes;

    public FirstRunSetupService(UserService userService, TbTenantService tenantService, DeviceService deviceService,
                                DeviceProfileService deviceProfileService, DeviceCredentialsService deviceCredentialsService,
                                DashboardService dashboardService, PlatformLanguage platformLanguage,
                                PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.tenantService = tenantService;
        this.deviceService = deviceService;
        this.deviceProfileService = deviceProfileService;
        this.deviceCredentialsService = deviceCredentialsService;
        this.dashboardService = dashboardService;
        this.platformLanguage = platformLanguage;
        this.passwordEncoder = passwordEncoder;
    }

    public static String randomSecret(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CODE_ALPHABET.charAt(RANDOM.nextInt(CODE_ALPHABET.length())));
        }
        return sb.toString();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void announce() {
        if (isRequired()) {
            newCode();
        }
    }

    public boolean isRequired() {
        Boolean value = required;
        if (value == null || value) {
            value = userService.findUserByEmail(TenantId.SYS_TENANT_ID, PENDING_SYSADMIN_EMAIL) != null;
            required = value;
        }
        return value;
    }

    public synchronized SetupResult complete(SetupRequest request) {
        if (!isRequired()) {
            throw new SetupException("already-done");
        }
        checkCode(request.setupCode());

        String language = request.language();
        if (!PlatformLanguage.SUPPORTED.contains(language)) {
            throw new SetupException("language");
        }
        String organization = trim(request.organizationName());
        if (organization.isEmpty() || organization.length() > 255) {
            throw new SetupException("organization");
        }
        String email = trim(request.email()).toLowerCase(Locale.ROOT);
        String sysadminEmail = trim(request.sysadminEmail()).toLowerCase(Locale.ROOT);
        if (sysadminEmail.isEmpty()) {
            sysadminEmail = DEFAULT_SYSADMIN_EMAIL;
        }
        checkEmail(email, "email");
        checkEmail(sysadminEmail, "sysadmin-email");
        if (email.equals(sysadminEmail) || email.equals(PENDING_SYSADMIN_EMAIL)) {
            throw new SetupException("same-email");
        }
        String password = request.password() == null ? "" : request.password();
        if (password.length() < MIN_PASSWORD || password.length() > MAX_PASSWORD || password.isBlank()) {
            throw new SetupException("password");
        }
        String deviceName = trim(request.deviceName());
        if (deviceName.length() > 255) {
            throw new SetupException("device");
        }

        User sysadmin = userService.findUserByEmail(TenantId.SYS_TENANT_ID, PENDING_SYSADMIN_EMAIL);
        Tenant tenant = null;
        try {
            // The language comes first: the organization's default data is created in it.
            platformLanguage.set(language);

            tenant = new Tenant();
            tenant.setTitle(organization);
            tenant = tenantService.save(tenant);
            createUser(Authority.TENANT_ADMIN, tenant.getId(), email, password, language);

            SetupResult result = new SetupResult(null, null, null);
            if (!deviceName.isEmpty()) {
                result = createFirstDevice(tenant.getId(), deviceName, language);
            }

            // Last step: giving the system administrator its real login ends the setup.
            sysadmin.setEmail(sysadminEmail);
            setLanguage(sysadmin, language);
            sysadmin = userService.saveUser(TenantId.SYS_TENANT_ID, sysadmin);
            setPassword(sysadmin, password);

            required = false;
            setupCode = null;
            log.info("First-run setup completed: organization '{}' created", organization);
            return result;
        } catch (SetupException e) {
            rollback(tenant);
            throw e;
        } catch (Exception e) {
            rollback(tenant);
            log.error("First-run setup failed", e);
            throw new SetupException("failed");
        }
    }

    private void checkCode(String code) {
        String expected = setupCode;
        String given = code == null ? "" : code.replaceAll("[\\s-]", "").toUpperCase(Locale.ROOT);
        if (expected == null || !MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), given.getBytes(StandardCharsets.UTF_8))) {
            if (++failedCodes >= MAX_FAILED_CODES) {
                log.warn("Too many wrong setup codes: a new code was created");
                newCode();
            }
            throw new SetupException("code");
        }
    }

    private void newCode() {
        setupCode = randomSecret(12);
        failedCodes = 0;
        String shown = setupCode.substring(0, 4) + "-" + setupCode.substring(4, 8) + "-" + setupCode.substring(8);
        log.warn("""

                ================================================================
                  First-run setup / Configuration initiale
                  Open the web page and enter this setup code:
                  Ouvrez la page Web et entrez ce code de configuration :

                      {}

                ================================================================""", shown);
    }

    private SetupResult createFirstDevice(TenantId tenantId, String deviceName, String language) throws IOException {
        Device device = new Device();
        device.setTenantId(tenantId);
        device.setName(deviceName);
        device.setDeviceProfileId(deviceProfileService.findDefaultDeviceProfile(tenantId).getId());
        device = deviceService.saveDeviceWithAccessToken(device, null);
        String token = deviceCredentialsService.findDeviceCredentialsByDeviceId(tenantId, device.getId()).getCredentialsId();

        boolean fr = language.startsWith("fr");
        Map<String, String> texts = Map.of(
                "__TITLE__", fr ? "Mon premier tableau de bord" : "My first dashboard",
                "__CHART__", fr ? "Historique" : "History",
                "__TEMPERATURE__", fr ? "Température" : "Temperature",
                "__HUMIDITY__", fr ? "Humidité" : "Humidity",
                "__DEVICE_ID__", device.getId().getId().toString());
        String json;
        try (InputStream in = getClass().getResourceAsStream("/orbitos/first-run/dashboard.json")) {
            json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        for (Map.Entry<String, String> e : texts.entrySet()) {
            json = json.replace(e.getKey(), e.getValue()); // fixed texts only: no quotes to escape
        }
        JsonNode template = JacksonUtil.toJsonNode(json);
        template.path("configuration").path("entityAliases").forEach(alias ->
                ((ObjectNode) alias).put("alias", deviceName)); // user text: set as a JSON value, never spliced
        Dashboard dashboard = new Dashboard();
        dashboard.setTenantId(tenantId);
        dashboard.setTitle(template.get("title").asText());
        dashboard.setConfiguration(template.get("configuration"));
        dashboard = dashboardService.saveDashboard(dashboard);
        return new SetupResult(device.getId().getId().toString(), token, dashboard.getId().getId().toString());
    }

    private void createUser(Authority authority, TenantId tenantId, String email, String password, String language) {
        User user = new User();
        user.setAuthority(authority);
        user.setTenantId(tenantId);
        user.setEmail(email);
        setLanguage(user, language);
        user = userService.saveUser(tenantId, user);
        setPassword(user, password);
    }

    private void setPassword(User user, String password) {
        UserCredentials credentials = userService.findUserCredentialsByUserId(TenantId.SYS_TENANT_ID, user.getId());
        credentials.setPassword(passwordEncoder.encode(password));
        credentials.setEnabled(true);
        credentials.setActivateToken(null);
        userService.saveUserCredentials(TenantId.SYS_TENANT_ID, credentials);
    }

    private static void setLanguage(User user, String language) {
        ObjectNode info = user.getAdditionalInfo() instanceof ObjectNode o ? o : JacksonUtil.newObjectNode();
        info.put("lang", language);
        user.setAdditionalInfo(info);
    }

    private void rollback(Tenant tenant) {
        if (tenant != null && tenant.getId() != null) {
            try {
                tenantService.delete(tenant);
            } catch (Exception e) {
                log.warn("Could not remove the partly created organization: {}", e.getMessage());
            }
        }
    }

    private static void checkEmail(String email, String field) {
        try {
            DataValidator.validateEmail(email);
        } catch (Exception e) {
            throw new SetupException(field);
        }
    }

    private static String trim(String value) {
        return value == null ? "" : value.strip();
    }

    public record SetupRequest(String setupCode, String language, String organizationName, String email,
                               String password, String sysadminEmail, String deviceName) {
    }

    public record SetupResult(String deviceId, String accessToken, String dashboardId) {
    }

    /** The message is a stable error code; the setup page shows the translated text. */
    public static class SetupException extends RuntimeException {
        public SetupException(String code) {
            super(code);
        }
    }

}
