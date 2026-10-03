// SPDX-FileCopyrightText: Copyright The OrbitOS Authors
// SPDX-License-Identifier: Apache-2.0
package org.thingsboard.server.orbitos.i18n;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.notification.NotificationType;
import org.thingsboard.server.common.data.notification.rule.NotificationRule;
import org.thingsboard.server.common.data.notification.rule.NotificationRuleConfig;
import org.thingsboard.server.common.data.notification.targets.NotificationTarget;
import org.thingsboard.server.common.data.notification.targets.platform.PlatformUsersNotificationTargetConfig;
import org.thingsboard.server.common.data.notification.template.DeliveryMethodNotificationTemplate;
import org.thingsboard.server.common.data.notification.template.NotificationTemplate;
import org.thingsboard.server.common.data.notification.template.TemplatableValue;
import org.thingsboard.server.common.data.page.PageLink;
import org.thingsboard.server.dao.notification.NotificationRuleService;
import org.thingsboard.server.dao.notification.NotificationTargetService;
import org.thingsboard.server.dao.notification.NotificationTemplateService;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;

/**
 * Translates the default data the platform creates for each new organization (tenant):
 * rule chain and rule node names, notification templates, rules and recipient groups.
 * <p>
 * The data is created in English by upstream code. When the platform default language is French,
 * every text that exactly matches an entry of orbitos/default-data/fr.json is replaced.
 * Texts without an entry stay in English, so an upstream change can never break creation.
 */
@Slf4j
@Component
public class DefaultDataTranslator {

    static final String FRENCH_DICTIONARY = "/orbitos/default-data/fr.json";

    private final PlatformLanguage platformLanguage;
    private final ObjectProvider<NotificationTemplateService> templates;
    private final ObjectProvider<NotificationRuleService> rules;
    private final ObjectProvider<NotificationTargetService> targets;
    private final Map<String, String> french;

    public DefaultDataTranslator(PlatformLanguage platformLanguage,
                                 ObjectProvider<NotificationTemplateService> templates,
                                 ObjectProvider<NotificationRuleService> rules,
                                 ObjectProvider<NotificationTargetService> targets) {
        this.platformLanguage = platformLanguage;
        this.templates = templates;
        this.rules = rules;
        this.targets = targets;
        this.french = loadDictionary(FRENCH_DICTIONARY);
    }

    /** Called with a rule chain file (root, edge or device profile template) before it is saved. */
    public JsonNode translateRuleChain(JsonNode ruleChainJson) {
        if (!platformLanguage.isFrench()) {
            return ruleChainJson;
        }
        JsonNode copy = ruleChainJson.deepCopy();
        translateField(copy.get("ruleChain"), "name");
        JsonNode nodes = copy.path("metadata").path("nodes");
        nodes.forEach(node -> translateField(node, "name"));
        return copy;
    }

    /** Called once, right after a new organization and its default notification settings are created. */
    public void translateNewTenant(TenantId tenantId) {
        if (!platformLanguage.isFrench()) {
            return;
        }
        try {
            PageLink all = new PageLink(1000);
            NotificationTemplateService templateService = templates.getObject();
            templateService.findNotificationTemplatesByTenantIdAndNotificationTypes(tenantId, Arrays.asList(NotificationType.values()), all)
                    .getData().forEach(template -> {
                        if (translateTemplate(template)) {
                            templateService.saveNotificationTemplate(tenantId, template);
                        }
                    });
            NotificationRuleService ruleService = rules.getObject();
            ruleService.findNotificationRulesByTenantId(tenantId, all).getData().forEach(rule -> {
                if (translateRule(rule)) {
                    ruleService.saveNotificationRule(tenantId, rule);
                }
            });
            NotificationTargetService targetService = targets.getObject();
            targetService.findNotificationTargetsByTenantId(tenantId, all).getData().forEach(target -> {
                if (translateTarget(target)) {
                    targetService.saveNotificationTarget(tenantId, target);
                }
            });
        } catch (Exception e) {
            // The organization is usable in English; never fail its creation because of a translation.
            log.warn("[{}] Default notification settings left in English: {}", tenantId, e.getMessage());
        }
    }

    boolean translateTemplate(NotificationTemplate template) {
        boolean changed = false;
        String name = french.get(template.getName());
        if (name != null) {
            template.setName(name);
            changed = true;
        }
        if (template.getConfiguration() != null && template.getConfiguration().getDeliveryMethodsTemplates() != null) {
            for (DeliveryMethodNotificationTemplate method : template.getConfiguration().getDeliveryMethodsTemplates().values()) {
                for (TemplatableValue value : method.getTemplatableValues()) {
                    String translated = value.get() != null ? french.get(value.get()) : null;
                    if (translated != null) {
                        value.set(translated);
                        changed = true;
                    }
                }
            }
        }
        return changed;
    }

    boolean translateRule(NotificationRule rule) {
        boolean changed = false;
        String name = french.get(rule.getName());
        if (name != null) {
            rule.setName(name);
            changed = true;
        }
        NotificationRuleConfig config = rule.getAdditionalConfig();
        String description = config != null ? french.get(config.getDescription()) : null;
        if (description != null) {
            config.setDescription(description);
            changed = true;
        }
        return changed;
    }

    boolean translateTarget(NotificationTarget target) {
        boolean changed = false;
        String name = french.get(target.getName());
        if (name != null) {
            target.setName(name);
            changed = true;
        }
        if (target.getConfiguration() instanceof PlatformUsersNotificationTargetConfig config) {
            String description = french.get(config.getDescription());
            if (description != null) {
                config.setDescription(description);
                changed = true;
            }
        }
        return changed;
    }

    private boolean translateField(JsonNode node, String field) {
        if (node instanceof ObjectNode object && object.hasNonNull(field)) {
            String translated = french.get(object.get(field).asText());
            if (translated != null) {
                object.set(field, new TextNode(translated));
                return true;
            }
        }
        return false;
    }

    static Map<String, String> loadDictionary(String resource) {
        try (InputStream in = DefaultDataTranslator.class.getResourceAsStream(resource)) {
            if (in == null) {
                log.warn("Translation file {} not found; default data stays in English", resource);
                return Map.of();
            }
            return JacksonUtil.OBJECT_MAPPER.readValue(in, new TypeReference<Map<String, String>>() {});
        } catch (IOException e) {
            log.warn("Translation file {} could not be read: {}", resource, e.getMessage());
            return Map.of();
        }
    }

}
