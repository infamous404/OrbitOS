// SPDX-FileCopyrightText: Copyright The OrbitOS Authors
// SPDX-License-Identifier: Apache-2.0
package org.thingsboard.server.orbitos.i18n;

import freemarker.template.Configuration;
import freemarker.template.TemplateScalarModel;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * French emails: FreeMarker finds name_fr.ftl for fr_CA and fr_FR, English stays the default,
 * subjects and usage labels come from messages_fr.properties.
 */
class MailTranslationsTest {

    private static final List<String> TEMPLATES = List.of("test.ftl", "activation.ftl", "account.activated.ftl",
            "reset.password.ftl", "password.was.reset.ftl", "account.lockout.ftl", "2fa.verification.code.ftl",
            "state.enabled.ftl", "state.warning.ftl", "state.disabled.ftl");

    private Configuration freemarker() throws Exception {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_32);
        cfg.setClassForTemplateLoading(getClass(), "/templates");
        cfg.setDefaultEncoding("UTF-8");
        TemplateScalarModel brand = () -> "OrbitOS";
        cfg.setSharedVariable("brandName", brand);
        return cfg;
    }

    private Map<String, Object> model() {
        Map<String, Object> model = new HashMap<>();
        model.put("targetEmail", "user@example.ca");
        model.put("activationLink", "https://example.ca/activate");
        model.put("activationLinkTtlInHours", 24);
        model.put("loginLink", "https://example.ca/login");
        model.put("passwordResetLink", "https://example.ca/reset");
        model.put("passwordResetLinkTtlInHours", 24);
        model.put("lockoutAccount", "user@example.ca");
        model.put("maxFailedLoginAttempts", 5);
        model.put("code", "123456");
        model.put("expirationTimeSeconds", 120);
        model.put("apiFeature", "Envoi de courriels");
        model.put("apiLabel", "envoyer");
        model.put("apiValueLabel", "10 courriels sur 12 autorisés");
        model.put("apiLimitValueLabel", "12 courriels");
        return model;
    }

    @Test
    void frenchTemplatesAreUsedForFrenchLocales() throws Exception {
        Configuration cfg = freemarker();
        for (Locale locale : List.of(Locale.forLanguageTag("fr-CA"), Locale.forLanguageTag("fr-FR"))) {
            for (String name : TEMPLATES) {
                String html = FreeMarkerTemplateUtils.processTemplateIntoString(cfg.getTemplate(name, locale), model());
                assertThat(html).as(name + " " + locale).contains("lang=\"fr\"").contains("Ce courriel a été envoyé à");
                assertThat(html).as(name + " " + locale).doesNotContainIgnoringCase("thingsboard");
            }
        }
    }

    @Test
    void englishStaysTheDefault() throws Exception {
        Configuration cfg = freemarker();
        for (String name : TEMPLATES) {
            String html = FreeMarkerTemplateUtils.processTemplateIntoString(cfg.getTemplate(name, Locale.US), model());
            assertThat(html).as(name).contains("This email was sent to").doesNotContain("Ce courriel");
            assertThat(html.replace("Copyright The Thingsboard Authors", "")).as(name).doesNotContainIgnoringCase("thingsboard");
        }
    }

    @Test
    void subjectsAndLabelsAreTranslated() {
        ResourceBundleMessageSource messages = new ResourceBundleMessageSource();
        messages.setBasename("i18n/messages");
        messages.setDefaultEncoding("UTF-8");
        messages.setFallbackToSystemLocale(false);
        Locale fr = Locale.forLanguageTag("fr-CA");

        assertThat(messages.getMessage("activation.subject", null, fr)).isEqualTo("Activation de votre compte {brandName}");
        assertThat(messages.getMessage("activation.subject", null, Locale.US)).isEqualTo("Your account activation on {brandName}");
        assertThat(messages.getMessage("api.usage.warning.EMAIL_EXEC_COUNT", new Object[]{"10", "12"}, "x", fr))
                .isEqualTo("10 courriels sur 12 autorisés");
        // English keeps the upstream wording (default message) because no English key exists
        assertThat(messages.getMessage("api.usage.warning.EMAIL_EXEC_COUNT", new Object[]{"10", "12"},
                "sent 10 out of 12 allowed Email messages", Locale.US)).isEqualTo("sent 10 out of 12 allowed Email messages");
    }

    @Test
    void languageCodesBecomeLocales() {
        assertThat(MailLocaleResolver.toLocale("fr_CA")).isEqualTo(Locale.forLanguageTag("fr-CA"));
        assertThat(MailLocaleResolver.toLocale("en-CA")).isEqualTo(Locale.forLanguageTag("en-CA"));
        assertThat(MailLocaleResolver.toLocale("")).isEqualTo(Locale.US);
    }

}
