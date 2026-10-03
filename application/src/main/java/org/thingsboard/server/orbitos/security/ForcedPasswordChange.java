// SPDX-FileCopyrightText: Copyright The OrbitOS Authors
// SPDX-License-Identifier: Apache-2.0
package org.thingsboard.server.orbitos.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.server.common.data.security.UserCredentials;

/**
 * A password chosen by the platform (not by the person) must be replaced at the first sign-in.
 * <p>
 * The mark stores the (bcrypt) hash of that password. As soon as the password is changed, by any
 * path (expired-password page, profile, reset link), the hash differs and the mark has no effect;
 * nothing has to remember to clear it.
 */
public final class ForcedPasswordChange {

    static final String KEY = "orbitosMustChangePassword";

    private ForcedPasswordChange() {
    }

    /** Call after the password hash is set on the credentials. */
    public static void mark(UserCredentials credentials) {
        ObjectNode info = credentials.getAdditionalInfo() instanceof ObjectNode o ? o : JacksonUtil.newObjectNode();
        info.put(KEY, credentials.getPassword());
        credentials.setAdditionalInfo(info);
    }

    public static boolean isPending(UserCredentials credentials) {
        JsonNode info = credentials.getAdditionalInfo();
        return info != null && info.hasNonNull(KEY) && credentials.getPassword() != null
                && info.get(KEY).asText().equals(credentials.getPassword());
    }

}
