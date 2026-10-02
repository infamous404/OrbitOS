// SPDX-FileCopyrightText: Copyright The OrbitOS Authors
// SPDX-License-Identifier: Apache-2.0
package org.thingsboard.server.orbitos.branding;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class BrandingServiceTest {

    @TempDir
    Path dir;

    private BrandingService.Branding load(String yaml) throws Exception {
        Files.writeString(dir.resolve("branding.yml"), yaml);
        return BrandingService.load(dir);
    }

    @Test
    void missingFileUsesDefaults() {
        BrandingService.Branding branding = BrandingService.load(dir);
        assertThat(branding.name()).isEqualTo("OrbitOS");
        assertThat(branding.assets()).isEmpty();
    }

    @Test
    void readsNameTextsLinksAndColors() throws Exception {
        BrandingService.Branding branding = load("""
                name: Acme IoT
                colors: { primary: "#112233", accent: "not-a-color" }
                login:
                  title: { en: "Welcome", fr: "Bienvenue", fr_CA: "Bienvenue au Québec" }
                footer: { en: "© Acme" }
                links: { privacy: "https://acme.example/privacy", terms: "javascript:alert(1)", unknown: "https://x" }
                """);
        assertThat(branding.name()).isEqualTo("Acme IoT");
        assertThat(branding.publicConfig().get("colors")).isEqualTo(Map.of("primary", "#112233"));
        assertThat(branding.publicConfig().get("links")).isEqualTo(Map.of("privacy", "https://acme.example/privacy"));
        @SuppressWarnings("unchecked")
        Map<String, Object> login = (Map<String, Object>) branding.publicConfig().get("login");
        assertThat(login.get("title")).isEqualTo(Map.of("en", "Welcome", "fr", "Bienvenue", "fr_CA", "Bienvenue au Québec"));
    }

    @Test
    void rejectsBlankOrTooLongName() throws Exception {
        assertThat(load("name: \"   \"").name()).isEqualTo("OrbitOS");
        assertThat(load("name: " + "x".repeat(65)).name()).isEqualTo("OrbitOS");
    }

    @Test
    void invalidYamlUsesDefaults() throws Exception {
        assertThat(load("name: [unclosed").name()).isEqualTo("OrbitOS");
    }

    @Test
    void yamlTagsAreNotExecuted() throws Exception {
        // SafeConstructor must refuse arbitrary Java types.
        assertThat(load("name: !!javax.script.ScriptEngineManager [!!java.net.URLClassLoader [[!!java.net.URL [\"http://x\"]]]]").name())
                .isEqualTo("OrbitOS");
    }

    @Test
    void servesOnlyPlainFilesInsideTheFolder() throws Exception {
        Files.writeString(dir.resolve("logo.svg"), "<svg/>");
        Path outside = dir.getParent().resolve("secret.svg");
        Files.writeString(outside, "<svg/>");
        try {
            BrandingService.Branding branding = load("""
                    logo: ../secret.svg
                    favicon: logo.svg
                    """);
            assertThat(branding.assets()).containsOnlyKeys("favicon");
            assertThat(branding.assets().get("favicon").contentType()).isEqualTo("image/svg+xml");
        } finally {
            Files.deleteIfExists(outside);
        }
    }

    @Test
    void rejectsWrongTypeAndOversizedFiles() throws Exception {
        Files.writeString(dir.resolve("logo.html"), "<script>alert(1)</script>");
        Files.write(dir.resolve("big.png"), new byte[(int) BrandingService.MAX_ASSET_BYTES + 1]);
        BrandingService.Branding branding = load("""
                logo: logo.html
                favicon: big.png
                """);
        assertThat(branding.assets()).isEmpty();
    }

}
