// SPDX-FileCopyrightText: Copyright The OrbitOS Authors
// SPDX-License-Identifier: Apache-2.0
package org.thingsboard.server.orbitos.offline;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Offline by default: tells the browser that pages from this server may only load images, fonts,
 * media and data from this server. Nothing in the web interface can call another host unless the
 * administrator allows it:
 * <ul>
 *   <li>ORBITOS_ONLINE_MAPS=true allows the free map tile servers used by the map widgets;</li>
 *   <li>ORBITOS_ALLOWED_HOSTS lists other hosts (for example a company map server).</li>
 * </ul>
 * Scripts and styles are not restricted here: widgets rely on them, and they are served by this server.
 */
@Slf4j
@Component
@Profile("!install")
public class ContentSecurityPolicyFilter extends OncePerRequestFilter {

    static final List<String> MAP_HOSTS = List.of(
            "https://*.openstreetmap.org", "https://*.tile.openstreetmap.fr",
            "https://server.arcgisonline.com", "https://*.basemaps.cartocdn.com");
    private static final Pattern HOST = Pattern.compile("^(https?://|wss?://)?[A-Za-z0-9*.-]+(:[0-9]{1,5})?$");

    private final String policy;

    public ContentSecurityPolicyFilter(@Value("${orbitos.offline.online-maps:false}") boolean onlineMaps,
                                       @Value("${orbitos.offline.allowed-hosts:}") String allowedHosts) {
        this.policy = buildPolicy(onlineMaps, allowedHosts);
        log.info("Browser access to other hosts: {}", onlineMaps || !allowedHosts.isBlank() ? "limited to the allowed list" : "none (offline)");
    }

    static String buildPolicy(boolean onlineMaps, String allowedHosts) {
        List<String> extra = new ArrayList<>();
        if (onlineMaps) {
            extra.addAll(MAP_HOSTS);
        }
        Arrays.stream(allowedHosts.split("[,\\s]+")).filter(h -> !h.isBlank()).forEach(host -> {
            if (HOST.matcher(host).matches()) {
                extra.add(host);
            } else {
                log.warn("ORBITOS_ALLOWED_HOSTS: '{}' ignored (expected a host such as https://maps.example.com)", host);
            }
        });
        String hosts = extra.isEmpty() ? "" : " " + String.join(" ", extra);
        return "img-src 'self' data: blob:" + hosts
                + "; font-src 'self' data:" + hosts
                + "; media-src 'self' data: blob:" + hosts
                + "; connect-src 'self'" + hosts
                + "; object-src 'none'; base-uri 'self'";
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        response.setHeader("Content-Security-Policy", policy);
        chain.doFilter(request, response);
    }

}
