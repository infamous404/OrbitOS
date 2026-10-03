// SPDX-FileCopyrightText: Copyright The OrbitOS Authors
// SPDX-License-Identifier: Apache-2.0
package org.thingsboard.server.orbitos.i18n;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.server.common.data.notification.template.DeliveryMethodNotificationTemplate;
import org.thingsboard.server.common.data.notification.template.NotificationTemplate;
import org.thingsboard.server.common.data.notification.template.TemplatableValue;
import org.thingsboard.server.dao.notification.DefaultNotifications;
import org.thingsboard.server.dao.notification.DefaultNotifications.DefaultNotification;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class DefaultDataTranslatorTest {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\{[^}]+}");
    private static final Path DATA = Path.of("src/main/data/json");

    /** The notifications every new organization gets (see DefaultNotificationSettingsService). */
    private static final List<DefaultNotification> TENANT_NOTIFICATIONS = List.of(
            DefaultNotifications.maintenanceWork, DefaultNotifications.newAlarm, DefaultNotifications.alarmUpdate,
            DefaultNotifications.entityAction, DefaultNotifications.deviceActivity, DefaultNotifications.alarmComment,
            DefaultNotifications.alarmAssignment, DefaultNotifications.ruleEngineComponentLifecycleFailure,
            DefaultNotifications.edgeConnection, DefaultNotifications.edgeCommunicationFailures);

    @SuppressWarnings("unchecked")
    private static DefaultDataTranslator translator(String language) {
        PlatformLanguage platformLanguage = new PlatformLanguage(mock(ObjectProvider.class), language);
        return new DefaultDataTranslator(platformLanguage, mock(ObjectProvider.class), mock(ObjectProvider.class), mock(ObjectProvider.class));
    }

    @Test
    void frenchTextsOnlyUseExistingPlaceholders() {
        Map<String, String> french = DefaultDataTranslator.loadDictionary(DefaultDataTranslator.FRENCH_DICTIONARY);
        assertThat(french).isNotEmpty();
        // A translation may leave out a placeholder (e.g. a button that just says "Ouvrir"), never invent one.
        french.forEach((english, translated) ->
                assertThat(placeholders(translated)).as(english).isSubsetOf(placeholders(english)));
    }

    @Test
    void rootRuleChainIsCreatedInFrench() throws Exception {
        for (String file : List.of("tenant/rule_chains/root_rule_chain.json", "edge/rule_chains/edge_root_rule_chain.json",
                "tenant/device_profile/rule_chain_template.json")) {
            JsonNode original = JacksonUtil.toJsonNode(Files.readString(DATA.resolve(file)));
            JsonNode french = translator("fr_CA").translateRuleChain(original);
            for (JsonNode node : french.path("metadata").path("nodes")) {
                assertThat(original.toString()).as(file).doesNotContain("\"" + node.get("name").asText() + "\"");
            }
            assertThat(translator("en_US").translateRuleChain(original)).isEqualTo(original);
        }
    }

    @Test
    void everyTenantNotificationIsCreatedInFrench() {
        DefaultDataTranslator translator = translator("fr_FR");
        Map<String, String> french = DefaultDataTranslator.loadDictionary(DefaultDataTranslator.FRENCH_DICTIONARY);
        for (DefaultNotification notification : TENANT_NOTIFICATIONS) {
            NotificationTemplate template = notification.toTemplate();
            translator.translateTemplate(template);
            assertThat(french).as(notification.getName()).containsValue(template.getName());
            for (DeliveryMethodNotificationTemplate method : template.getConfiguration().getDeliveryMethodsTemplates().values()) {
                for (TemplatableValue value : method.getTemplatableValues()) {
                    if (value.get() != null && !value.get().startsWith("/")) { // links stay as they are
                        assertThat(french).as(notification.getName()).containsValue(value.get());
                    }
                }
            }
            if (notification.getRule() != null) {
                assertThat(french).containsKey(notification.getRule().getName());
                assertThat(french).containsKey(notification.getRule().getDescription());
            }
        }
    }

    @Test
    void englishPlatformLeavesTemplatesUnchanged() {
        NotificationTemplate template = DefaultNotifications.newAlarm.toTemplate();
        String before = JacksonUtil.toString(template);
        translator("en_CA").translateNewTenant(null); // no service calls in English
        assertThat(JacksonUtil.toString(template)).isEqualTo(before);
    }

    private static Set<String> placeholders(String text) {
        Set<String> result = new TreeSet<>();
        Matcher m = PLACEHOLDER.matcher(text);
        while (m.find()) {
            result.add(m.group());
        }
        return result;
    }

}
