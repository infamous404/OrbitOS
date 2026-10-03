// SPDX-FileCopyrightText: Copyright The OrbitOS Authors
// SPDX-License-Identifier: Apache-2.0
package org.thingsboard.server.orbitos.i18n;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.server.common.data.AdminSettings;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.dao.settings.AdminSettingsService;

import java.util.List;

/**
 * The platform default language: used for emails to users who have not chosen a language
 * and for the default data created with each new organization (rule chain, notifications).
 * <p>
 * The first-run setup page saves the language chosen there. Until then, the value comes from
 * ORBITOS_DEFAULT_LANGUAGE (orbitos.i18n.default-language).
 */
@Slf4j
@Component
public class PlatformLanguage {

    public static final List<String> SUPPORTED = List.of("en_US", "en_CA", "fr_FR", "fr_CA");
    static final String SETTINGS_KEY = "orbitos";

    private final ObjectProvider<AdminSettingsService> adminSettings;
    private final String configured;
    private volatile String saved;

    public PlatformLanguage(ObjectProvider<AdminSettingsService> adminSettings,
                            @Value("${orbitos.i18n.default-language:en_US}") String configured) {
        this.adminSettings = adminSettings;
        this.configured = SUPPORTED.contains(configured) ? configured : "en_US";
    }

    public String get() {
        String value = saved;
        if (value == null) {
            value = load();
            if (value == null) {
                return configured; // database not reachable yet: try again next time
            }
            saved = value;
        }
        return value;
    }

    public void set(String language) {
        if (!SUPPORTED.contains(language)) {
            throw new IllegalArgumentException("Unsupported language: " + language);
        }
        AdminSettingsService service = adminSettings.getObject();
        AdminSettings settings = service.findAdminSettingsByKey(TenantId.SYS_TENANT_ID, SETTINGS_KEY);
        if (settings == null) {
            settings = new AdminSettings();
            settings.setTenantId(TenantId.SYS_TENANT_ID);
            settings.setKey(SETTINGS_KEY);
            settings.setJsonValue(JacksonUtil.newObjectNode());
        }
        ((ObjectNode) settings.getJsonValue()).put("defaultLanguage", language);
        service.saveAdminSettings(TenantId.SYS_TENANT_ID, settings);
        saved = language;
    }

    public boolean isFrench() {
        return get().startsWith("fr");
    }

    private String load() {
        try {
            AdminSettingsService service = adminSettings.getIfAvailable();
            AdminSettings settings = service != null ? service.findAdminSettingsByKey(TenantId.SYS_TENANT_ID, SETTINGS_KEY) : null;
            if (settings != null && settings.getJsonValue() != null && settings.getJsonValue().hasNonNull("defaultLanguage")) {
                String value = settings.getJsonValue().get("defaultLanguage").asText();
                if (SUPPORTED.contains(value)) {
                    return value;
                }
            }
        } catch (Exception e) {
            log.debug("Could not read the saved default language: {}", e.getMessage());
            return null;
        }
        return configured;
    }

}
