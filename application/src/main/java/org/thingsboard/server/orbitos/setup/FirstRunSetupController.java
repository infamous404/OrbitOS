// SPDX-FileCopyrightText: Copyright The OrbitOS Authors
// SPDX-License-Identifier: Apache-2.0
package org.thingsboard.server.orbitos.setup;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.dao.settings.AdminSettingsService;
import org.thingsboard.server.orbitos.setup.FirstRunSetupService.SetupException;
import org.thingsboard.server.orbitos.setup.FirstRunSetupService.SetupRequest;

import java.util.Map;

/**
 * Public endpoints used before anyone can sign in:
 * the first-run setup page and the health check used by Docker.
 */
@RestController
@Profile("!install")
@RequestMapping("/api/noauth/orbitos")
public class FirstRunSetupController {

    private final FirstRunSetupService setupService;
    private final AdminSettingsService adminSettingsService;

    public FirstRunSetupController(FirstRunSetupService setupService, AdminSettingsService adminSettingsService) {
        this.setupService = setupService;
        this.adminSettingsService = adminSettingsService;
    }

    @GetMapping("/setup")
    public Map<String, Object> status() {
        return Map.of("required", setupService.isRequired());
    }

    @PostMapping("/setup")
    public ResponseEntity<?> complete(@RequestBody SetupRequest request) {
        try {
            return ResponseEntity.ok(setupService.complete(request));
        } catch (SetupException e) {
            HttpStatus status = switch (e.getMessage()) {
                case "already-done" -> HttpStatus.CONFLICT;
                case "code" -> HttpStatus.FORBIDDEN;
                case "failed" -> HttpStatus.INTERNAL_SERVER_ERROR;
                default -> HttpStatus.BAD_REQUEST;
            };
            return ResponseEntity.status(status).body(Map.of("error", e.getMessage()));
        }
    }

    /** UP when the server answers and the database can be read. */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        try {
            adminSettingsService.findAdminSettingsByKey(TenantId.SYS_TENANT_ID, "general");
            return ResponseEntity.ok(Map.of("status", "UP"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("status", "DOWN"));
        }
    }

}
