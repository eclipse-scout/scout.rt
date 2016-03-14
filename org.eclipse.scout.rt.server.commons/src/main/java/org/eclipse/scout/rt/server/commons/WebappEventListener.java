/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.commons;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBeanManager;
import org.eclipse.scout.rt.platform.IPlatform;
import org.eclipse.scout.rt.platform.IPlatformListener;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.PlatformEvent;

/**
 * Ensures that the platform is started, when the servlet context is ready. Likewise, the platform is stopped, when the
 * servlet context is destroyed.
 * <p>
 * If an even earlier platform start is required, check the javadoc of {@link Platform}.
 * <p>
 * Publishes the {@link ServletContext} to the {@link IBeanManager} allowing for
 * <code>BEANS.get(ServletContext.class)</code>
 */
public class WebappEventListener implements ServletContextListener {

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    ServletContextRegistration.servletContext = sce.getServletContext();
    // Accessing the class activates the platform if it is not yet initialized
    IPlatform platform = Platform.get();
    platform.awaitPlatformStarted();
    //double check
    if (BEANS.opt(ServletContext.class) == null) {
      BEANS.getBeanManager().registerBean(new BeanMetaData(ServletContext.class, sce.getServletContext()).withApplicationScoped(true));
    }
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    ServletContextRegistration.servletContext = null;
    IPlatform platform = Platform.get();
    if (platform != null) {
      platform.stop();
    }
  }

  public static final class ServletContextRegistration implements IPlatformListener {
    private static ServletContext servletContext;

    @Override
    public void stateChanged(PlatformEvent event) {
      switch (event.getState()) {
        case BeanManagerPrepared: {
          event.getSource().getBeanManager().registerBean(new BeanMetaData(ServletContext.class, servletContext).withApplicationScoped(true));
          break;
        }
      }
    }
  }
}
