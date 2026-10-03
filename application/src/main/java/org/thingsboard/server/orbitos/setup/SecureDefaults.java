// SPDX-FileCopyrightText: Copyright The OrbitOS Authors
// SPDX-License-Identifier: Apache-2.0
package org.thingsboard.server.orbitos.setup;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.thingsboard.server.common.data.TenantProfile;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.security.model.SecuritySettings;
import org.thingsboard.server.common.data.security.model.UserPasswordPolicy;
import org.thingsboard.server.common.data.security.model.mfa.PlatformTwoFaSettings;
import org.thingsboard.server.common.data.security.model.mfa.provider.BackupCodeTwoFaProviderConfig;
import org.thingsboard.server.common.data.security.model.mfa.provider.TotpTwoFaProviderConfig;
import org.thingsboard.server.common.data.tenant.profile.DefaultTenantProfileConfiguration;
import org.thingsboard.server.common.data.tenant.profile.TenantProfileData;
import org.thingsboard.server.dao.settings.SecuritySettingsService;
import org.thingsboard.server.dao.tenant.TenantProfileService;
import org.thingsboard.server.orbitos.branding.BrandingService;
import org.thingsboard.server.service.security.auth.mfa.config.TwoFaConfigManager;

import java.util.List;

/**
 * Privacy- and security-protective settings applied once, by the first-run setup.
 * Every value can be changed later by the platform administrator in the web interface.
 * <ul>
 *   <li>Two-factor sign-in available to everyone: authenticator app (TOTP) and backup codes.
 *       Not forced on anyone (Security settings > Two-factor authentication).</li>
 *   <li>Passwords: at least 12 characters; account locked after 10 failed sign-ins.</li>
 *   <li>Retention in the default organization profile: telemetry and alarms 365 days,
 *       remote-command (RPC) records 30 days. Audit log and event retention are server settings
 *       (orbitos.yml).</li>
 * </ul>
 */
@Slf4j
@Component
@Profile("!install")
public class SecureDefaults {

    static final int MIN_PASSWORD_LENGTH = 12;
    static final int MAX_FAILED_SIGN_INS = 10;
    static final int TELEMETRY_DAYS = 365;
    static final int ALARM_DAYS = 365;
    static final int RPC_DAYS = 30;

    private final TwoFaConfigManager twoFaConfigManager;
    private final SecuritySettingsService securitySettingsService;
    private final TenantProfileService tenantProfileService;
    private final BrandingService brandingService;

    public SecureDefaults(TwoFaConfigManager twoFaConfigManager, SecuritySettingsService securitySettingsService,
                          TenantProfileService tenantProfileService, BrandingService brandingService) {
        this.twoFaConfigManager = twoFaConfigManager;
        this.securitySettingsService = securitySettingsService;
        this.tenantProfileService = tenantProfileService;
        this.brandingService = brandingService;
    }

    public void apply() throws Exception {
        applyTwoFactor();
        applyPasswordPolicy();
        applyRetention();
        log.info("Secure defaults applied: two-factor sign-in available, {}-character passwords, {}-day telemetry retention",
                MIN_PASSWORD_LENGTH, TELEMETRY_DAYS);
    }

    private void applyTwoFactor() throws Exception {
        TotpTwoFaProviderConfig totp = new TotpTwoFaProviderConfig();
        totp.setIssuerName(brandingService.getBranding().name());
        BackupCodeTwoFaProviderConfig backupCodes = new BackupCodeTwoFaProviderConfig();
        backupCodes.setCodesQuantity(10);

        PlatformTwoFaSettings settings = new PlatformTwoFaSettings();
        settings.setProviders(List.of(totp, backupCodes));
        settings.setMinVerificationCodeSendPeriod(30);
        settings.setVerificationCodeCheckRateLimit("3:900");
        settings.setMaxVerificationFailuresBeforeUserLockout(10);
        settings.setTotalAllowedTimeForVerification(3600);
        settings.setEnforceTwoFa(false);
        twoFaConfigManager.savePlatformTwoFaSettings(TenantId.SYS_TENANT_ID, settings);
    }

    private void applyPasswordPolicy() {
        SecuritySettings settings = securitySettingsService.getSecuritySettings();
        UserPasswordPolicy policy = settings.getPasswordPolicy() != null ? settings.getPasswordPolicy() : new UserPasswordPolicy();
        if (policy.getMinimumLength() == null || policy.getMinimumLength() < MIN_PASSWORD_LENGTH) {
            policy.setMinimumLength(MIN_PASSWORD_LENGTH);
        }
        settings.setPasswordPolicy(policy);
        if (settings.getMaxFailedLoginAttempts() == null || settings.getMaxFailedLoginAttempts() <= 0) {
            settings.setMaxFailedLoginAttempts(MAX_FAILED_SIGN_INS);
        }
        securitySettingsService.saveSecuritySettings(settings);
    }

    private void applyRetention() {
        TenantProfile profile = tenantProfileService.findDefaultTenantProfile(TenantId.SYS_TENANT_ID);
        TenantProfileData data = profile != null ? profile.getProfileData() : null;
        if (data == null || !(data.getConfiguration() instanceof DefaultTenantProfileConfiguration config)) {
            log.warn("Default organization profile not found: retention left unchanged");
            return;
        }
        if (config.getDefaultStorageTtlDays() <= 0) {
            config.setDefaultStorageTtlDays(TELEMETRY_DAYS);
        }
        if (config.getAlarmsTtlDays() <= 0) {
            config.setAlarmsTtlDays(ALARM_DAYS);
        }
        if (config.getRpcTtlDays() <= 0) {
            config.setRpcTtlDays(RPC_DAYS);
        }
        profile.setProfileData(data);
        tenantProfileService.saveTenantProfile(TenantId.SYS_TENANT_ID, profile);
    }

}
