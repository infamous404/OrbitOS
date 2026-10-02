// SPDX-FileCopyrightText: Copyright The OrbitOS Authors
// SPDX-License-Identifier: Apache-2.0
package org.thingsboard.server.orbitos.branding;

import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.thingsboard.server.queue.util.TbCoreComponent;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Public (no login) branding endpoints, so the login page can be branded too.
 * Only display settings are exposed: no secrets live in branding.yml.
 */
@RestController
@TbCoreComponent
@RequiredArgsConstructor
@RequestMapping("/api/noauth/branding")
public class BrandingController {

    private final BrandingService brandingService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getBranding() {
        BrandingService.Branding branding = brandingService.getBranding();
        Map<String, Object> body = new LinkedHashMap<>(branding.publicConfig());
        branding.assets().forEach((key, asset) ->
                body.put(key + "Url", "/api/noauth/branding/" + key + "?v=" + asset.version()));
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache())
                .body(body);
    }

    @GetMapping("/{key}")
    public ResponseEntity<byte[]> getAsset(@PathVariable String key) {
        if (!BrandingService.ASSET_KEYS.contains(key)) {
            return ResponseEntity.notFound().build();
        }
        return brandingService.getAsset(key)
                .map(asset -> ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(asset.contentType()))
                        .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
                        // An SVG opened directly must never run scripts.
                        .header("Content-Security-Policy", "default-src 'none'; style-src 'unsafe-inline'; sandbox")
                        .header("X-Content-Type-Options", "nosniff")
                        .body(asset.content()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

}
