// SPDX-FileCopyrightText: Copyright The OrbitOS Authors
// SPDX-License-Identifier: Apache-2.0
package org.thingsboard.server.orbitos.privacy;

import org.junit.jupiter.api.Test;
import org.thingsboard.server.common.data.User;
import org.thingsboard.server.common.data.id.UserId;
import org.thingsboard.server.common.data.security.Authority;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PrivacyServiceTest {

    private static User user(Authority authority) {
        User user = new User(new UserId(UUID.randomUUID()));
        user.setAuthority(authority);
        user.setEmail(UUID.randomUUID() + "@example.com");
        return user;
    }

    @Test
    void pseudonymIsStableAndHidesTheEmail() {
        User person = user(Authority.CUSTOMER_USER);
        String pseudonym = PrivacyService.pseudonym(person.getId());
        assertThat(pseudonym).startsWith("erased-user-").hasSize("erased-user-".length() + 10)
                .isEqualTo(PrivacyService.pseudonym(person.getId()))
                .doesNotContain(person.getEmail());
    }

    @Test
    void noSelfErasureAndNoPlatformAdminErasure() {
        User admin = user(Authority.TENANT_ADMIN);
        assertThat(PrivacyService.canErase(user(Authority.CUSTOMER_USER), admin)).isTrue();
        assertThat(PrivacyService.canErase(admin, admin)).isFalse();
        assertThat(PrivacyService.canErase(user(Authority.SYS_ADMIN), user(Authority.SYS_ADMIN))).isFalse();
    }

}
