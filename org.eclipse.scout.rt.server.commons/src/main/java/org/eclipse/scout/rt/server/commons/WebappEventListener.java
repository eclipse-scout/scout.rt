/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.commons;

import org.eclipse.scout.rt.platform.IPlatform;
import org.eclipse.scout.rt.platform.IPlatform.State;
import org.eclipse.scout.rt.platform.Platform;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

/**
 * Ensures that the platform is started, when the servlet context is ready. Likewise, the platform is stopped, when the
 * servlet context is destroyed.
 * <p>
 * If an even earlier platform start is required, check the javadoc of {@link Platform}.
 *
 * @deprecated Deploying to an application server is a legacy operation only, this class might be removed in a future
 *             release.
 */
@Deprecated
public class WebappEventListener implements ServletContextListener {

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    // Accessing the class activates the platform if it is not yet initialized
    IPlatform platform = Platform.get();
    platform.awaitPlatformStarted();
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    IPlatform platform = Platform.peek();
    if (platform != null && platform.getState() != State.PlatformStopped) {
      platform.stop();
    }
  }
}
