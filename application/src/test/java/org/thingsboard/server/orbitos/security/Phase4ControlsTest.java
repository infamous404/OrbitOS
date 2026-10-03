// SPDX-FileCopyrightText: Copyright The OrbitOS Authors
// SPDX-License-Identifier: Apache-2.0
package org.thingsboard.server.orbitos.security;

import org.junit.jupiter.api.Test;
import org.thingsboard.server.common.data.security.UserCredentials;
import org.thingsboard.server.orbitos.audit.AuditChain;

import static org.assertj.core.api.Assertions.assertThat;

/** Unit checks of the Phase 4 controls; the end-to-end checks are in orbitos/scripts/smoke-test.sh. */
class Phase4ControlsTest {

    @Test
    void forcedChangeEndsWhenThePasswordChanges() {
        UserCredentials credentials = new UserCredentials();
        credentials.setPassword("$2a$10$hash-of-generated-password");
        assertThat(ForcedPasswordChange.isPending(credentials)).isFalse();

        ForcedPasswordChange.mark(credentials);
        assertThat(ForcedPasswordChange.isPending(credentials)).isTrue();

        credentials.setPassword("$2a$10$hash-of-the-new-password");
        assertThat(ForcedPasswordChange.isPending(credentials)).isFalse();
    }

    @Test
    void auditHashDependsOnContentAndOrder() {
        String first = AuditChain.hash(AuditChain.GENESIS, "entry-1");
        String second = AuditChain.hash(first, "entry-2");
        assertThat(first).hasSize(64).isEqualTo(AuditChain.hash(AuditChain.GENESIS, "entry-1"));
        assertThat(AuditChain.hash(AuditChain.GENESIS, "entry-1 changed")).isNotEqualTo(first);
        // Same content after a different predecessor gives a different link: entries cannot be moved or dropped.
        assertThat(AuditChain.hash(AuditChain.GENESIS, "entry-2")).isNotEqualTo(second);
    }

}
