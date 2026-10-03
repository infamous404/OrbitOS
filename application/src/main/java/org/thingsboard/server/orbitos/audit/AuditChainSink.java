// SPDX-FileCopyrightText: Copyright The OrbitOS Authors
// SPDX-License-Identifier: Apache-2.0
package org.thingsboard.server.orbitos.audit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.thingsboard.server.common.data.audit.AuditLog;
import org.thingsboard.server.dao.audit.sink.AuditLogSink;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Adds every saved audit entry to the hash chain (see {@link AuditChain}).
 * Selected with audit-log.sink.type=orbitos-chain, the OrbitOS default. Single-server design:
 * links are appended in order by this one process.
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "audit-log.sink", value = "type", havingValue = "orbitos-chain")
public class AuditChainSink implements AuditLogSink {

    private final DataSource dataSource;
    private String lastHash;

    public AuditChainSink(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public synchronized void logAction(AuditLog entry) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            if (lastHash == null) {
                lastHash = loadLastHash(connection);
            }
            String content;
            try (PreparedStatement row = connection.prepareStatement(
                    "SELECT " + AuditChain.AUDIT_COLUMNS + " FROM audit_log WHERE id = ? AND created_time = ?")) {
                row.setObject(1, entry.getId().getId());
                row.setLong(2, entry.getCreatedTime());
                try (ResultSet rs = row.executeQuery()) {
                    if (!rs.next()) {
                        log.warn("Audit entry {} not found; not added to the integrity chain", entry.getId());
                        return;
                    }
                    content = AuditChain.canonical(rs);
                }
            }
            String hash = AuditChain.hash(lastHash, content);
            try (PreparedStatement insert = connection.prepareStatement("INSERT INTO " + AuditChain.TABLE
                    + " (audit_log_id, created_time, prev_hash, hash) VALUES (?, ?, ?, ?)")) {
                insert.setObject(1, entry.getId().getId());
                insert.setLong(2, entry.getCreatedTime());
                insert.setString(3, lastHash);
                insert.setString(4, hash);
                insert.executeUpdate();
            }
            lastHash = hash;
        } catch (SQLException e) {
            lastHash = null; // reload from the database next time
            log.error("Audit entry {} not added to the integrity chain", entry.getId(), e);
        }
    }

    private static String loadLastHash(Connection connection) throws SQLException {
        AuditChain.createTable(connection);
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT hash FROM " + AuditChain.TABLE + " ORDER BY seq DESC LIMIT 1")) {
            return rs.next() ? rs.getString(1) : AuditChain.GENESIS;
        }
    }

}
