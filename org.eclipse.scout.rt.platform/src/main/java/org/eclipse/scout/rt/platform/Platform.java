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
package org.eclipse.scout.rt.platform;

import java.util.List;

import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.cdi.CDI;
import org.eclipse.scout.rt.platform.cdi.OBJ;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 *
 */
public final class Platform implements IPlatform {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(Platform.class);
  /**
   * Singleton instance.
   */
  private static final Platform platform = new Platform();

  private State m_state = State.Stopped;
  private EventListenerList m_platformListeners = new EventListenerList();
  private List<IModule> m_startedModules;
  private IApplication m_application;

  private Platform() {
  }

  /**
   * To access the platform.
   */
  public static IPlatform get() {
    return platform;
  }

  @Override
  public State getState() {
    return m_state;
  }

  @Override
  public synchronized void start() {
    if (getState() != State.Stopped) {
      throw new IllegalStateException("Platform is already started or starting.");
    }
    m_state = State.Starting;
    notifyListeners(new PlatformEvent(this, PlatformEvent.ABOUT_TO_START));
    CDI.start();
    startModules();
    // parse xml

    notifyListeners(new PlatformEvent(this, PlatformEvent.MODULES_STARTED));

    startApplications();
    notifyListeners(new PlatformEvent(this, PlatformEvent.STARTED));
    m_state = State.Running;
  }

  public synchronized void ensureStarted() {
    if (getState() != State.Running) {
      start();
    }
  }

  protected void startModules() {
    m_startedModules = CDI.getBeanContext().getInstances(IModule.class);
    for (IModule module : m_startedModules) {
      try {
        module.start();
      }
      catch (Exception e) {
        LOG.error(String.format("Could not start module '%s'.", module.getClass().getName()), e);
      }
    }
  }

  /**
   *
   */
  private void startApplications() {
    m_application = OBJ.oneOrNull(IApplication.class);
    if (m_application != null) {
      try {
        m_application.start();
      }
      catch (Exception e) {
        LOG.error(String.format("Could not start application '%s'.", m_application.getClass().getName()), e);
      }
    }
    else {
      LOG.warn("Start platform without an application. No application has been found.");
    }
  }

  @Override
  public synchronized void stop() {
    if (getState() != State.Running) {
      throw new IllegalStateException("Platform is already stopping or stopped.");
    }
    m_state = State.Stopping;
    stopApplications();
    // stop modules
    stopModules();
    m_state = State.Stopped;
  }

  /**
   *
   */
  protected void stopApplications() {
    if (m_application != null) {
      try {
        m_application.stop();
      }
      catch (Exception e) {
        LOG.error(String.format("Could not stop application '%s'.", m_application.getClass().getName()), e);
      }
    }
  }

  protected void stopModules() {
    for (IModule module : m_startedModules) {
      try {
        module.stop();
      }
      catch (Exception e) {
        LOG.error(String.format("Could not stop module '%s'.", module.getClass().getName()), e);
      }
    }
  }

  @Override
  public void addPlatformListener(IPlatformListener listener) {
    m_platformListeners.add(IPlatformListener.class, listener);
  }

  @Override
  public void removePlatformListener(IPlatformListener listener) {
    m_platformListeners.remove(IPlatformListener.class, listener);
  }

  protected void notifyListeners(PlatformEvent e) {
    for (IPlatformListener l : m_platformListeners.getListeners(IPlatformListener.class)) {
      try {
        l.platformChanged(e);
      }
      catch (Exception ex) {
        LOG.warn("Platform event listener notification.", e);
      }
    }
  }

  public static boolean isOsgiRunning() {
//    return StringUtility.hasText(System.getProperty("org.osgi.framework.version"));
    Bundle bundle = FrameworkUtil.getBundle(Platform.class);
    return bundle != null;
  }
}
