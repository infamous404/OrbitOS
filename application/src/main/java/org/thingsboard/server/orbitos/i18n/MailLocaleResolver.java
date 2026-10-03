// SPDX-FileCopyrightText: Copyright The OrbitOS Authors
// SPDX-License-Identifier: Apache-2.0
package org.thingsboard.server.orbitos.i18n;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.thingsboard.server.common.data.User;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.dao.user.UserService;

import java.util.Locale;

/**
 * Chooses the language of an email: the recipient's language from their profile,
 * otherwise the platform default language (see {@link PlatformLanguage}).
 */
@Slf4j
@Component
public class MailLocaleResolver {

    private final ObjectProvider<UserService> userService;
    private final PlatformLanguage platformLanguage;

    public MailLocaleResolver(ObjectProvider<UserService> userService, PlatformLanguage platformLanguage) {
        this.userService = userService;
        this.platformLanguage = platformLanguage;
    }

    public Locale forEmail(String email) {
        try {
            UserService users = userService.getIfAvailable();
            User user = users != null && email != null ? users.findUserByEmail(TenantId.SYS_TENANT_ID, email) : null;
            JsonNode info = user != null ? user.getAdditionalInfo() : null;
            if (info != null && info.hasNonNull("lang") && !info.get("lang").asText().isBlank()) {
                return toLocale(info.get("lang").asText());
            }
        } catch (Exception e) {
            log.debug("Could not read the language of {}: {}", email, e.getMessage());
        }
        return getDefaultLocale();
    }

    public Locale getDefaultLocale() {
        return toLocale(platformLanguage.get());
    }

    /** "fr_CA" or "fr-CA" -> Locale fr_CA. */
    public static Locale toLocale(String code) {
        if (code == null || code.isBlank()) {
            return Locale.US;
        }
        return Locale.forLanguageTag(code.strip().replace('_', '-'));
    }

}
