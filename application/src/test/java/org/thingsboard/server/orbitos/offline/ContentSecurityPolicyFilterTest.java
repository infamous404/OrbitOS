// SPDX-FileCopyrightText: Copyright The OrbitOS Authors
// SPDX-License-Identifier: Apache-2.0
package org.thingsboard.server.orbitos.offline;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ContentSecurityPolicyFilterTest {

    @Test
    void offlineByDefault() {
        String policy = ContentSecurityPolicyFilter.buildPolicy(false, "");
        assertThat(policy).contains("img-src 'self' data: blob:;", "connect-src 'self';", "object-src 'none'");
        assertThat(policy).doesNotContain("https://");
    }

    @Test
    void onlineMapsAddTileServersOnly() {
        String policy = ContentSecurityPolicyFilter.buildPolicy(true, "");
        assertThat(policy).contains("https://*.openstreetmap.org", "https://server.arcgisonline.com");
        assertThat(policy).doesNotContain(" https: ", " * ");
    }

    @Test
    void allowedHostsAreCheckedBeforeUse() {
        String policy = ContentSecurityPolicyFilter.buildPolicy(false,
                "https://maps.example.ca, wss://mqtt.example.ca:8084 bad;host 'unsafe-inline' https://x.example\r\nInjected: 1");
        assertThat(policy).contains("https://maps.example.ca", "wss://mqtt.example.ca:8084");
        assertThat(policy).doesNotContain("bad;host", "unsafe-inline", "Injected", "\n");
    }

}
