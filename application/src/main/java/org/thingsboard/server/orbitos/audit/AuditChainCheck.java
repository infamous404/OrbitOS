// SPDX-FileCopyrightText: Copyright The OrbitOS Authors
// SPDX-License-Identifier: Apache-2.0
package org.thingsboard.server.orbitos.audit;

import org.thingsboard.common.util.JacksonUtil;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Command-line integrity check of the audit log:  docker compose run --rm orbitos verify-audit
 * Prints the result as JSON. Exit code 0 = intact, 3 = broken, 1 = error.
 */
public final class AuditChainCheck {

    private AuditChainCheck() {
    }

    public static void main(String[] args) {
        String retention = System.getenv().getOrDefault("SQL_TTL_AUDIT_LOGS_SECS", "31536000");
        try {
            Class.forName("org.postgresql.Driver");
            try (Connection connection = DriverManager.getConnection(System.getenv("SPRING_DATASOURCE_URL"),
                    System.getenv("SPRING_DATASOURCE_USERNAME"), System.getenv("SPRING_DATASOURCE_PASSWORD"))) {
                AuditChain.Result result = AuditChain.verify(connection, Long.parseLong(retention.trim()), System.currentTimeMillis());
                System.out.println(JacksonUtil.toPrettyString(result.toMap()));
                System.exit(result.intact() ? 0 : 3);
            }
        } catch (Exception e) {
            System.out.println("Audit log check failed: " + e.getMessage());
            System.exit(1);
        }
    }

}
