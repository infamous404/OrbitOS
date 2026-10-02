// SPDX-FileCopyrightText: Copyright The OrbitOS Authors
// SPDX-License-Identifier: Apache-2.0
package org.thingsboard.server.orbitos.branding;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Loads the branding folder (branding.yml plus logo and favicon) once at startup.
 * Everything is read from the local disk; nothing is fetched from the network.
 * An invalid or missing value falls back to the OrbitOS default and logs a warning.
 */
@Slf4j
@Service
public class BrandingService {

    public static final String DEFAULT_NAME = "OrbitOS";
    static final String CONFIG_FILE = "branding.yml";
    static final long MAX_ASSET_BYTES = 1024 * 1024;
    static final Set<String> ASSET_KEYS = Set.of("logo", "favicon");
    private static final Map<String, String> CONTENT_TYPES = Map.of(
            "svg", "image/svg+xml",
            "png", "image/png",
            "ico", "image/x-icon");
    private static final Set<String> TEXT_SECTIONS = Set.of("login", "footer", "linkLabels");
    private static final Set<String> LINK_KEYS = Set.of("website", "privacy", "terms", "accessibility", "support");
    private static final Pattern SAFE_FILE_NAME = Pattern.compile("^[A-Za-z0-9][A-Za-z0-9._-]{0,99}$");
    private static final Pattern COLOR = Pattern.compile("^#[0-9A-Fa-f]{6}$");
    private static final Pattern LANG = Pattern.compile("^[a-z]{2}(_[A-Z]{2})?$");
    private static final int MAX_NAME_LENGTH = 64;
    private static final int MAX_TEXT_LENGTH = 500;

    @Value("${orbitos.branding.dir:/etc/orbitos/branding}")
    private String brandingDir;

    private volatile Branding branding = Branding.defaults();

    @PostConstruct
    public void init() {
        branding = load(Path.of(brandingDir));
        log.info("Branding loaded: name='{}', logo={}, favicon={}", branding.name(),
                branding.assets().containsKey("logo"), branding.assets().containsKey("favicon"));
    }

    public Branding getBranding() {
        return branding;
    }

    public Optional<BrandingAsset> getAsset(String key) {
        return Optional.ofNullable(branding.assets().get(key));
    }

    static Branding load(Path dir) {
        Path file = dir.resolve(CONFIG_FILE);
        if (!Files.isRegularFile(file)) {
            log.warn("Branding file {} not found, using defaults", file);
            return Branding.defaults();
        }
        Map<String, Object> raw;
        try (InputStream in = Files.newInputStream(file)) {
            Object parsed = new Yaml(new SafeConstructor(new LoaderOptions())).load(in);
            raw = parsed instanceof Map<?, ?> m ? castMap(m) : Map.of();
        } catch (Exception e) {
            log.warn("Branding file {} could not be read, using defaults: {}", file, e.getMessage());
            return Branding.defaults();
        }

        String name = DEFAULT_NAME;
        if (raw.get("name") instanceof String n && !n.isBlank() && n.length() <= MAX_NAME_LENGTH) {
            name = n.strip();
        } else if (raw.containsKey("name")) {
            log.warn("Branding 'name' must be 1-{} characters, using default", MAX_NAME_LENGTH);
        }

        Map<String, Object> publicConfig = new LinkedHashMap<>();
        publicConfig.put("name", name);
        publicConfig.put("colors", readColors(raw.get("colors")));
        for (String section : TEXT_SECTIONS) {
            publicConfig.put(section, readTexts(section, raw.get(section)));
        }
        publicConfig.put("links", readLinks(raw.get("links")));

        Map<String, BrandingAsset> assets = new LinkedHashMap<>();
        for (String key : ASSET_KEYS) {
            readAsset(dir, key, raw.get(key)).ifPresent(asset -> assets.put(key, asset));
        }
        return new Branding(name, publicConfig, assets);
    }

    private static Map<String, String> readColors(Object value) {
        Map<String, String> colors = new LinkedHashMap<>();
        if (value instanceof Map<?, ?> map) {
            map.forEach((k, v) -> {
                if (v instanceof String s && COLOR.matcher(s).matches()) {
                    colors.put(String.valueOf(k), s);
                }
            });
        }
        return colors;
    }

    /**
     * A text section is either {lang: text} or {key: {lang: text}}. Anything else is dropped.
     */
    private static Map<String, Object> readTexts(String section, Object value) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (!(value instanceof Map<?, ?> map)) {
            return result;
        }
        map.forEach((k, v) -> {
            String key = String.valueOf(k);
            if (v instanceof String text && LANG.matcher(key).matches()) {
                putText(result, key, text);
            } else if (v instanceof Map<?, ?> byLang) {
                Map<String, Object> texts = new LinkedHashMap<>();
                byLang.forEach((lang, t) -> {
                    if (t instanceof String text && LANG.matcher(String.valueOf(lang)).matches()) {
                        putText(texts, String.valueOf(lang), text);
                    }
                });
                result.put(key, texts);
            } else {
                log.warn("Branding '{}.{}' ignored: expected text per language", section, key);
            }
        });
        return result;
    }

    private static void putText(Map<String, Object> target, String key, String text) {
        target.put(key, text.length() > MAX_TEXT_LENGTH ? text.substring(0, MAX_TEXT_LENGTH) : text);
    }

    private static Map<String, String> readLinks(Object value) {
        Map<String, String> links = new LinkedHashMap<>();
        if (value instanceof Map<?, ?> map) {
            map.forEach((k, v) -> {
                String key = String.valueOf(k);
                if (!LINK_KEYS.contains(key) || !(v instanceof String url) || url.isBlank()) {
                    return;
                }
                String lower = url.toLowerCase(Locale.ROOT);
                if (lower.startsWith("https://") || lower.startsWith("http://") || lower.startsWith("mailto:")) {
                    links.put(key, url.strip());
                } else {
                    log.warn("Branding link '{}' ignored: must start with https://, http:// or mailto:", key);
                }
            });
        }
        return links;
    }

    private static Optional<BrandingAsset> readAsset(Path dir, String key, Object value) {
        if (!(value instanceof String fileName) || fileName.isBlank()) {
            return Optional.empty();
        }
        if (!SAFE_FILE_NAME.matcher(fileName).matches()) {
            log.warn("Branding '{}' ignored: '{}' must be a plain file name in the branding folder", key, fileName);
            return Optional.empty();
        }
        String ext = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        String contentType = CONTENT_TYPES.get(ext);
        if (contentType == null) {
            log.warn("Branding '{}' ignored: only .svg, .png and .ico files are allowed", key);
            return Optional.empty();
        }
        Path path = dir.resolve(fileName).normalize();
        try {
            if (!path.startsWith(dir.normalize()) || !Files.isRegularFile(path)) {
                log.warn("Branding '{}' ignored: file {} not found", key, path);
                return Optional.empty();
            }
            if (Files.size(path) > MAX_ASSET_BYTES) {
                log.warn("Branding '{}' ignored: file is larger than 1 MB", key);
                return Optional.empty();
            }
            byte[] content = Files.readAllBytes(path);
            return Optional.of(new BrandingAsset(content, contentType, sha256(content).substring(0, 12)));
        } catch (IOException e) {
            log.warn("Branding '{}' ignored: {}", key, e.getMessage());
            return Optional.empty();
        }
    }

    private static String sha256(byte[] content) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> castMap(Map<?, ?> map) {
        return (Map<String, Object>) map;
    }

    public record Branding(String name, Map<String, Object> publicConfig, Map<String, BrandingAsset> assets) {
        static Branding defaults() {
            Map<String, Object> config = new LinkedHashMap<>();
            config.put("name", DEFAULT_NAME);
            config.put("colors", Map.of());
            config.put("login", Map.of());
            config.put("footer", Map.of());
            config.put("linkLabels", Map.of());
            config.put("links", Map.of());
            return new Branding(DEFAULT_NAME, config, Map.of());
        }
    }

    public record BrandingAsset(byte[] content, String contentType, String version) {
    }

}
