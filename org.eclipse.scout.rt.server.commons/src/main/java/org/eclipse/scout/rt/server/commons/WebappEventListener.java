/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.commons;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.eclipse.scout.rt.platform.IPlatform;
import org.eclipse.scout.rt.platform.Platform;

/**
 * Start the platform when the servlet context starts.
 * <p>
 * If an even earlier platform start is required, check the javadoc of {@link Platform#get()} which uses the
 * META-INF/services/org.eclipse.scout.rt.platform.IPlatform
 */
public class WebappEventListener implements ServletContextListener {
  @Override
  public void contextInitialized(ServletContextEvent sce) {
    Platform.get();
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    IPlatform platform = Platform.get();
    if (platform != null) {
      platform.stop();
    }
  }
}
