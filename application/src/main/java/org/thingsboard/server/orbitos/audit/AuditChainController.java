// SPDX-FileCopyrightText: Copyright The OrbitOS Authors
// SPDX-License-Identifier: Apache-2.0
package org.thingsboard.server.orbitos.audit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.thingsboard.server.queue.util.TbCoreComponent;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Map;

/** Integrity check of the audit log, for the platform administrator. */
@RestController
@TbCoreComponent
@RequestMapping("/api/orbitos/audit")
public class AuditChainController {

    private final DataSource dataSource;
    private final long retentionSeconds;

    public AuditChainController(DataSource dataSource, @Value("${sql.ttl.audit_logs.ttl:0}") long retentionSeconds) {
        this.dataSource = dataSource;
        this.retentionSeconds = retentionSeconds;
    }

    @GetMapping("/verify")
    @PreAuthorize("hasAuthority('SYS_ADMIN')")
    public Map<String, Object> verify() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            return AuditChain.verify(connection, retentionSeconds, System.currentTimeMillis()).toMap();
        }
    }

}
