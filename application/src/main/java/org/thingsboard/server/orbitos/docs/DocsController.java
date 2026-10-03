// SPDX-FileCopyrightText: Copyright The OrbitOS Authors
// SPDX-License-Identifier: Apache-2.0
package org.thingsboard.server.orbitos.docs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Serves the OrbitOS guides (orbitos/docs/{en,fr}/*.md) to the in-app documentation page, so help
 * links work offline and never send people to another site. Public: the guides contain no secrets.
 */
@RestController
@Profile("!install")
@RequestMapping("/api/noauth/orbitos/docs")
public class DocsController {

    private static final Set<String> LANGUAGES = Set.of("en", "fr");
    private static final Pattern PAGE = Pattern.compile("^[a-z0-9-]{1,40}$");
    private static final MediaType MARKDOWN = new MediaType("text", "markdown", StandardCharsets.UTF_8);

    private final Path dir;

    public DocsController(@Value("${orbitos.docs.dir:/usr/share/orbitos/docs}") String dir) {
        this.dir = Path.of(dir).toAbsolutePath().normalize();
    }

    @GetMapping("/{lang}/{page}")
    public ResponseEntity<String> page(@PathVariable("lang") String lang, @PathVariable("page") String page) throws IOException {
        if (!LANGUAGES.contains(lang) || !PAGE.matcher(page).matches()) {
            return ResponseEntity.notFound().build();
        }
        Path file = dir.resolve(lang).resolve(page + ".md").normalize();
        if (!file.startsWith(dir) || !Files.isRegularFile(file)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MARKDOWN)
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(10)))
                .body(Files.readString(file, StandardCharsets.UTF_8));
    }

}
