// SPDX-FileCopyrightText: Copyright The OrbitOS Authors
// SPDX-License-Identifier: Apache-2.0
package org.thingsboard.server.orbitos.offline;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Service;
import org.thingsboard.server.common.data.UpdateMessage;
import org.thingsboard.server.queue.util.TbCoreComponent;
import org.thingsboard.server.service.update.UpdateService;

/**
 * Replaces the upstream update checker, which contacted an external server every hour
 * and sent a persistent instance ID. OrbitOS never checks for updates over the network:
 * administrators follow the project's release notes instead.
 */
@Service
@TbCoreComponent
public class OfflineUpdateService implements UpdateService {

    private final UpdateMessage noUpdate;

    public OfflineUpdateService(@Autowired(required = false) BuildProperties buildProperties) {
        String version = buildProperties != null ? buildProperties.getVersion() : "unknown";
        this.noUpdate = new UpdateMessage(false, version, version, "", "", "");
    }

    @Override
    public UpdateMessage checkUpdates() {
        return noUpdate;
    }

}
