// SPDX-FileCopyrightText: Copyright The OrbitOS Authors
// SPDX-License-Identifier: Apache-2.0
package org.thingsboard.server.orbitos.privacy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.thingsboard.server.common.data.Customer;
import org.thingsboard.server.common.data.HasName;
import org.thingsboard.server.common.data.audit.ActionType;
import org.thingsboard.server.common.data.User;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.id.UserId;
import org.thingsboard.server.common.data.notification.NotificationDeliveryMethod;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.page.PageLink;
import org.thingsboard.server.common.data.page.TimePageLink;
import org.thingsboard.server.common.data.security.Authority;
import org.thingsboard.server.common.data.security.UserCredentials;
import org.thingsboard.server.common.data.settings.UserSettings;
import org.thingsboard.server.common.data.settings.UserSettingsType;
import org.thingsboard.server.dao.audit.AuditLogService;
import org.thingsboard.server.dao.customer.CustomerService;
import org.thingsboard.server.dao.notification.NotificationService;
import org.thingsboard.server.dao.user.UserService;
import org.thingsboard.server.dao.user.UserSettingsService;
import org.thingsboard.server.orbitos.branding.BrandingService;
import org.thingsboard.server.queue.util.TbCoreComponent;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Privacy requests about one person who has an account (Law 25 / PIPEDA access and erasure):
 * export everything the platform holds about the account, or erase the account.
 * <p>
 * Erasure deletes the account, its sign-in data, settings and notifications. Audit entries stay
 * (security record, kept for the audit-log retention period) but the person's name in them is
 * replaced by a pseudonym. Text the person wrote inside audit details is not rewritten; it goes
 * away when the retention period ends. This is documented for privacy officers.
 */
@Slf4j
@Service
@TbCoreComponent
public class PrivacyService {

    static final int MAX_PAGES = 50;
    static final int PAGE_SIZE = 1000;

    private final UserService userService;
    private final UserSettingsService userSettingsService;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;
    private final CustomerService customerService;
    private final BrandingService brandingService;
    private final JdbcTemplate jdbc;

    public PrivacyService(UserService userService, UserSettingsService userSettingsService,
                          AuditLogService auditLogService, NotificationService notificationService,
                          CustomerService customerService, BrandingService brandingService, JdbcTemplate jdbc) {
        this.userService = userService;
        this.userSettingsService = userSettingsService;
        this.auditLogService = auditLogService;
        this.notificationService = notificationService;
        this.customerService = customerService;
        this.brandingService = brandingService;
        this.jdbc = jdbc;
    }

    public Map<String, Object> export(User user, User requestedBy) {
        TenantId tenantId = user.getTenantId();
        UserId userId = user.getId();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("about", Map.of(
                "platform", brandingService.getBranding().name(),
                "generatedAt", Instant.now().toString(),
                "generatedBy", requestedBy.getEmail(),
                "content", "Account, sign-in status, preferences, actions performed by this person and about this "
                        + "account (audit log), notifications received, and the customer record if the account belongs to one. "
                        + "Device data is not attributed to people by the platform and is not included."));
        result.put("account", user);

        UserCredentials credentials = userService.findUserCredentialsByUserId(tenantId, userId);
        Map<String, Object> signIn = new LinkedHashMap<>();
        signIn.put("enabled", credentials != null && credentials.isEnabled());
        signIn.put("lastSignIn", user.getAdditionalInfo() != null ? user.getAdditionalInfo().get("lastLoginTs") : null);
        signIn.put("passwordStored", "only as a one-way hash; never exported");
        result.put("signIn", signIn);

        Map<String, Object> settings = new LinkedHashMap<>();
        for (UserSettingsType type : UserSettingsType.values()) {
            UserSettings value = userSettingsService.findUserSettings(tenantId, userId, type);
            if (value != null) {
                settings.put(type.name(), value.getSettings());
            }
        }
        result.put("preferences", settings);

        result.put("actionsByThisPerson", collect(page -> auditLogService.findAuditLogsByTenantIdAndUserId(
                tenantId, userId, null, new TimePageLink(PAGE_SIZE, page))));
        result.put("actionsAboutThisAccount", collect(page -> auditLogService.findAuditLogsByTenantIdAndEntityId(
                tenantId, userId, null, new TimePageLink(PAGE_SIZE, page))));
        result.put("notifications", collect(page -> notificationService.findNotificationsByRecipientIdAndReadStatus(
                tenantId, NotificationDeliveryMethod.WEB, userId, false, new PageLink(PAGE_SIZE, page))));

        if (user.getCustomerId() != null && !user.getCustomerId().isNullUid()) {
            Customer customer = customerService.findCustomerById(tenantId, user.getCustomerId());
            result.put("customer", customer);
        }
        return result;
    }

    /** Returns a short summary. The caller has already checked that the account may be erased. */
    public Map<String, Object> erase(User user, User requestedBy) {
        UserId userId = user.getId();
        String pseudonym = pseudonym(userId);
        // Deletes the account, its sign-in data, two-factor settings, API keys and preferences.
        userService.deleteUser(user.getTenantId(), user);
        // Audit entry without the person's details (the upstream "deleted" entry would copy the whole account).
        auditLogService.logEntityAction(user.getTenantId(), user.getCustomerId(), requestedBy.getId(), requestedBy.getName(),
                userId, (HasName) () -> pseudonym, ActionType.DELETED, null, userId.getId().toString());
        int notifications = jdbc.update("DELETE FROM notification WHERE recipient_id = ?", userId.getId());
        int asActor = jdbc.update("UPDATE audit_log SET user_name = ? WHERE user_id = ?", pseudonym, userId.getId());
        int asTarget = jdbc.update("UPDATE audit_log SET entity_name = ? WHERE entity_id = ?", pseudonym, userId.getId());
        log.info("Privacy erasure of account {} by {}: {} audit entries pseudonymized, {} notifications deleted",
                userId, requestedBy.getId(), asActor + asTarget, notifications);
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("erased", userId.getId().toString());
        summary.put("pseudonym", pseudonym);
        summary.put("auditEntriesPseudonymized", asActor + asTarget);
        summary.put("notificationsDeleted", notifications);
        return summary;
    }

    /** Stable for one account, so audit entries of the same person stay linked, but not reversible to the email. */
    static String pseudonym(UserId userId) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(userId.getId().toString().getBytes(StandardCharsets.UTF_8));
            return "erased-user-" + HexFormat.of().formatHex(digest).substring(0, 10);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    static boolean canErase(User target, User requestedBy) {
        return target.getAuthority() != Authority.SYS_ADMIN && !target.getId().equals(requestedBy.getId());
    }

    private static <T> List<T> collect(Function<Integer, PageData<T>> fetch) {
        List<T> all = new ArrayList<>();
        for (int page = 0; page < MAX_PAGES; page++) {
            PageData<T> data = fetch.apply(page);
            all.addAll(data.getData());
            if (!data.hasNext()) {
                break;
            }
        }
        return all;
    }

}
