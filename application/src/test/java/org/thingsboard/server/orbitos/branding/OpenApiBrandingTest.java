// SPDX-FileCopyrightText: Copyright The OrbitOS Authors
// SPDX-License-Identifier: Apache-2.0
package org.thingsboard.server.orbitos.branding;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiBrandingTest {

    @Test
    void replacesTheProductNameButNotUrlsOrPackages() {
        String text = "Creates a ThingsBoard device. See https://thingsboard.io/docs and org.thingsboard.server. "
                + "Use the ThingsBoard IoT Gateway or ThingsBoard Edge. Thingsboard's API.";
        assertThat(OpenApiBranding.rebrand(text, "Acme IoT"))
                .isEqualTo("Creates a Acme IoT device. See https://thingsboard.io/docs and org.thingsboard.server. "
                        + "Use the IoT Gateway or Edge. Acme IoT's API.");
    }

    @Test
    void brandNameWithSpecialCharactersIsInsertedLiterally() {
        assertThat(OpenApiBranding.rebrand("ThingsBoard", "A$1\\B")).isEqualTo("A$1\\B");
    }

}
