/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.session.services.common.clustersync;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.server.context.ServerRunContext;
import org.eclipse.scout.rt.server.services.common.clustersync.ClusterSynchronizationService;
import org.eclipse.scout.rt.server.session.ServerSessionProviderWithCache;
import org.eclipse.scout.rt.server.session.context.ServerSessionRunContext;

@Replace
public class SessionClusterSynchronizationService extends ClusterSynchronizationService {

  @Override
  protected ServerRunContext createRunContext() {
    ServerSessionRunContext serverRunContext = (ServerSessionRunContext) super.createRunContext();
    return serverRunContext.withSession(BEANS.get(ServerSessionProviderWithCache.class).provide(serverRunContext.copy()));
  }
}
