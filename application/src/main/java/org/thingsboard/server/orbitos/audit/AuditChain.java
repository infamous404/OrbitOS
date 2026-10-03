// SPDX-FileCopyrightText: Copyright The OrbitOS Authors
// SPDX-License-Identifier: Apache-2.0
package org.thingsboard.server.orbitos.audit;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Tamper-evident audit log: a hash chain over the upstream audit_log table.
 * <p>
 * Each audit entry gets a link in orbitos_audit_chain holding SHA-256(previous hash + entry content).
 * Changing, deleting or inserting an entry afterwards breaks the chain, and {@link #verify} reports where.
 * <p>
 * What the hash covers: entry ID, time, organization, customer, target entity (type and ID), user ID,
 * action, action data, result and failure details. Not covered on purpose: the user name and the
 * target name, so a privacy erasure can replace them with a pseudonym without breaking the chain.
 * <p>
 * Limits (documented for administrators): it is tamper-evident, not tamper-proof. Someone with full
 * database access could rebuild the whole chain; keep a copy of the latest hash elsewhere to detect that.
 * Entries removed by the audit-log retention period are reported as "pruned", not as tampering.
 * Plain JDBC, so the same code runs in the server and in the command-line check.
 */
public final class AuditChain {

    public static final String GENESIS = "0".repeat(64);
    static final String TABLE = "orbitos_audit_chain";
    static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE + " ("
            + "seq bigserial PRIMARY KEY, "
            + "audit_log_id uuid NOT NULL, "
            + "created_time bigint NOT NULL, "
            + "prev_hash char(64) NOT NULL, "
            + "hash char(64) NOT NULL)";
    static final String AUDIT_COLUMNS = "id, created_time, tenant_id, customer_id, entity_type, entity_id, user_id, "
            + "action_type, action_data, action_status, action_failure_details";
    private static final char SEPARATOR = '\u001f';
    private static final int BATCH = 500;
    /** Entries younger than this may not be chained yet (audit logs are saved asynchronously). */
    static final long GRACE_MILLIS = 60_000;

    private AuditChain() {
    }

    /** Content of one audit_log row, in column order of {@link #AUDIT_COLUMNS}. */
    public static String canonical(ResultSet rs) throws SQLException {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 11; i++) {
            if (i > 1) {
                sb.append(SEPARATOR);
            }
            Object value = rs.getObject(i);
            sb.append(value == null ? "" : value.toString());
        }
        return sb.toString();
    }

    public static String hash(String previousHash, String canonical) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(previousHash.getBytes(StandardCharsets.US_ASCII));
            digest.update((byte) '\n');
            digest.update(canonical.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public static void createTable(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(CREATE_TABLE);
        }
    }

    /**
     * Checks the whole chain.
     *
     * @param retentionSeconds audit-log retention (0 = keep forever); older missing entries count as pruned
     */
    public static Result verify(Connection connection, long retentionSeconds, long now) throws SQLException {
        createTable(connection);
        long pruneBefore = retentionSeconds > 0 ? now - retentionSeconds * 1000 : Long.MIN_VALUE;
        Result result = new Result();
        String expectedPrevious = null;
        long firstTime = -1;

        try (PreparedStatement links = connection.prepareStatement(
                "SELECT seq, audit_log_id, created_time, prev_hash, hash FROM " + TABLE + " ORDER BY seq")) {
            links.setFetchSize(BATCH);
            try (ResultSet rs = links.executeQuery()) {
                List<Link> batch = new ArrayList<>(BATCH);
                while (rs.next()) {
                    Link link = new Link(rs.getLong(1), (UUID) rs.getObject(2), rs.getLong(3), rs.getString(4), rs.getString(5));
                    if (firstTime < 0) {
                        firstTime = link.createdTime();
                        result.firstHash = link.prevHash();
                    }
                    batch.add(link);
                    if (batch.size() == BATCH) {
                        expectedPrevious = checkBatch(connection, batch, expectedPrevious, pruneBefore, result);
                        batch.clear();
                    }
                }
                expectedPrevious = checkBatch(connection, batch, expectedPrevious, pruneBefore, result);
            }
        }
        result.latestHash = expectedPrevious;

        if (firstTime >= 0) {
            try (PreparedStatement unchained = connection.prepareStatement(
                    "SELECT a.id FROM audit_log a WHERE a.created_time >= ? AND a.created_time < ? "
                            + "AND NOT EXISTS (SELECT 1 FROM " + TABLE + " c WHERE c.audit_log_id = a.id) LIMIT 1")) {
                unchained.setLong(1, firstTime);
                unchained.setLong(2, now - GRACE_MILLIS);
                try (ResultSet rs = unchained.executeQuery()) {
                    if (rs.next()) {
                        result.problem("added", null, rs.getObject(1).toString());
                    }
                }
            }
        }
        return result;
    }

    private static String checkBatch(Connection connection, List<Link> batch, String expectedPrevious,
                                     long pruneBefore, Result result) throws SQLException {
        if (batch.isEmpty()) {
            return expectedPrevious;
        }
        Map<UUID, String> contents = new HashMap<>();
        String placeholders = String.join(",", java.util.Collections.nCopies(batch.size(), "?"));
        try (PreparedStatement rows = connection.prepareStatement(
                "SELECT " + AUDIT_COLUMNS + " FROM audit_log WHERE id IN (" + placeholders + ")")) {
            for (int i = 0; i < batch.size(); i++) {
                rows.setObject(i + 1, batch.get(i).auditLogId());
            }
            try (ResultSet rs = rows.executeQuery()) {
                while (rs.next()) {
                    contents.put((UUID) rs.getObject(1), canonical(rs));
                }
            }
        }
        for (Link link : batch) {
            result.checked++;
            if (expectedPrevious != null && !expectedPrevious.equals(link.prevHash())) {
                result.problem("link-removed", link.seq(), link.auditLogId().toString());
            }
            String content = contents.get(link.auditLogId());
            if (content == null) {
                if (link.createdTime() < pruneBefore) {
                    result.pruned++;
                } else {
                    result.problem("deleted", link.seq(), link.auditLogId().toString());
                }
            } else if (!hash(link.prevHash(), content).equals(link.hash())) {
                result.problem("changed", link.seq(), link.auditLogId().toString());
            }
            expectedPrevious = link.hash();
        }
        return expectedPrevious;
    }

    record Link(long seq, UUID auditLogId, long createdTime, String prevHash, String hash) {
    }

    /** Outcome of a check. "intact" only if no problem was found. */
    public static final class Result {
        public long checked;
        public long pruned;
        public String firstHash;
        public String latestHash;
        public final List<Map<String, String>> problems = new ArrayList<>();

        void problem(String kind, Long seq, String auditLogId) {
            if (problems.size() < 100) {
                Map<String, String> p = new HashMap<>();
                p.put("kind", kind);
                p.put("auditLogId", auditLogId);
                if (seq != null) {
                    p.put("link", seq.toString());
                }
                problems.add(p);
            }
        }

        public boolean intact() {
            return problems.isEmpty();
        }

        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("status", intact() ? "intact" : "broken");
            map.put("checked", checked);
            map.put("pruned", pruned);
            map.put("latestHash", latestHash);
            map.put("problems", problems);
            return map;
        }
    }

}
