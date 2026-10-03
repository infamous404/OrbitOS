// SPDX-FileCopyrightText: Copyright The OrbitOS Authors
// SPDX-License-Identifier: Apache-2.0
package org.thingsboard.server.orbitos.branding;

import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The API reference page (/swagger-ui.html) is generated from upstream code annotations that name the
 * upstream product about 2,600 times. Instead of editing hundreds of upstream files, the generated
 * document is rebranded once: the product name becomes the configured brand name, with the same rule as
 * orbitos/scripts/rebrand.py (URLs, package names and file names are left alone).
 */
@Slf4j
@Component
@Profile("!install")
public class OpenApiBranding implements GlobalOpenApiCustomizer {

    static final Pattern PRODUCT_NAME = Pattern.compile("(?<![\\w./@:-])(?:ThingsBoard|Thingsboard|THINGSBOARD)(?![\\w@/-]|\\.\\w)");
    static final Pattern NEUTRAL_NAMES = Pattern.compile("(?:ThingsBoard|Thingsboard) (IoT Gateway|Edge)\\b");

    private final BrandingService brandingService;

    public OpenApiBranding(BrandingService brandingService) {
        this.brandingService = brandingService;
    }

    @Override
    public void customise(OpenAPI openApi) {
        String brand = brandingService.getBranding().name();
        try {
            String json = rebrand(Json.mapper().writeValueAsString(openApi), brand);
            OpenAPI branded = Json.mapper().readValue(json, OpenAPI.class);
            openApi.setPaths(branded.getPaths());
            openApi.setComponents(branded.getComponents());
            openApi.setTags(branded.getTags());
            openApi.setExternalDocs(branded.getExternalDocs());
        } catch (Exception e) {
            log.warn("API reference left unbranded: {}", e.getMessage());
        }
        Info info = openApi.getInfo() != null ? openApi.getInfo() : new Info();
        info.setTitle(brand + " REST API");
        info.setDescription("Sign in with POST /api/auth/login, then click Authorize and paste the token as: Bearer <token>. "
                + "Device APIs (/api/v1/{token}/...) use the device access token instead.");
        info.setContact(null);
        info.setLicense(new License().name("Apache License 2.0").url("https://www.apache.org/licenses/LICENSE-2.0"));
        openApi.setInfo(info);
    }

    static String rebrand(String text, String brand) {
        String neutral = NEUTRAL_NAMES.matcher(text).replaceAll("$1");
        return PRODUCT_NAME.matcher(neutral).replaceAll(Matcher.quoteReplacement(brand));
    }

}
