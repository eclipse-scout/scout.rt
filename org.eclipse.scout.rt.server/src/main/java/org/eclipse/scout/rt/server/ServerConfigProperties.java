/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.platform.config.AbstractStringConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractSubjectConfigProperty;
import org.eclipse.scout.rt.server.services.common.file.RemoteFileService;

public final class ServerConfigProperties {

  private ServerConfigProperties() {
  }

  public static class ClusterSyncUserProperty extends AbstractSubjectConfigProperty {

    public static final String CLUSTER_SYNC_USER_NAME = "system";

    @Override
    public Subject getDefaultValue() {
      return convertToSubject(CLUSTER_SYNC_USER_NAME);
    }

    @Override
    public String getKey() {
      return "scout.clustersync.user";
    }

    @Override
    public String description() {
      return String.format("Technical subject under which received cluster sync notifications are executed. The default value is '%s'.", CLUSTER_SYNC_USER_NAME);
    }
  }

  public static class RemoteFilesRootDirProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "scout.remotefileRootPath";
    }

    @Override
    public String description() {
      return String.format("Absolute path to the root directory of the '%s'. The default value is null.", RemoteFileService.class.getSimpleName());
    }
  }
}
