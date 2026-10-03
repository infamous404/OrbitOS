// SPDX-FileCopyrightText: Copyright The OrbitOS Authors
// SPDX-License-Identifier: Apache-2.0
package org.thingsboard.server.orbitos.privacy;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.thingsboard.server.common.data.User;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.id.UserId;
import org.thingsboard.server.common.data.security.Authority;
import org.thingsboard.server.dao.user.UserService;
import org.thingsboard.server.queue.util.TbCoreComponent;
import org.thingsboard.server.service.security.model.SecurityUser;

import java.util.Map;
import java.util.UUID;

/**
 * Privacy requests for one account. Platform administrators may handle any account except other
 * platform administrators' erasure; organization administrators only accounts of their organization.
 */
@RestController
@TbCoreComponent
@RequestMapping("/api/orbitos/privacy")
public class PrivacyController {

    private final PrivacyService privacyService;
    private final UserService userService;

    public PrivacyController(PrivacyService privacyService, UserService userService) {
        this.privacyService = privacyService;
        this.userService = userService;
    }

    @GetMapping("/users/{userId}/export")
    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
    public ResponseEntity<?> export(@PathVariable("userId") UUID userId) {
        SecurityUser me = currentUser();
        User target = findAllowed(userId, me);
        if (target == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "not-found"));
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"personal-data-" + userId + ".json\"")
                .contentType(MediaType.APPLICATION_JSON)
                .body(privacyService.export(target, me));
    }

    @PostMapping("/users/{userId}/erase")
    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
    public ResponseEntity<?> erase(@PathVariable("userId") UUID userId) {
        SecurityUser me = currentUser();
        User target = findAllowed(userId, me);
        if (target == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "not-found"));
        }
        if (!PrivacyService.canErase(target, me)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "not-allowed"));
        }
        return ResponseEntity.ok(privacyService.erase(target, me));
    }

    /** The account, or null when it does not exist or the caller may not see it (same answer, on purpose). */
    private User findAllowed(UUID id, SecurityUser me) {
        User target = userService.findUserById(TenantId.SYS_TENANT_ID, new UserId(id));
        if (target == null) {
            return null;
        }
        if (me.getAuthority() == Authority.SYS_ADMIN) {
            return target;
        }
        boolean sameOrganization = me.getTenantId() != null && me.getTenantId().equals(target.getTenantId());
        return sameOrganization && target.getAuthority() != Authority.SYS_ADMIN ? target : null;
    }

    private static SecurityUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (SecurityUser) authentication.getPrincipal();
    }

}
