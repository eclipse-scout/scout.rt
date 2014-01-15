/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.spec.client;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.services.common.session.IClientSessionRegistryService;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.shared.ui.UiDeviceType;
import org.eclipse.scout.rt.shared.ui.UiLayer;
import org.eclipse.scout.rt.shared.ui.UserAgent;
import org.eclipse.scout.rt.testing.shared.ScoutJUnitPluginTestExecutor;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.eclipse.scout.rt.testing.shared.WaitCondition;
import org.eclipse.scout.service.SERVICES;

/**
 * Main job generating specification.
 */
public class SpecJob extends Job {
  private static IScoutLogger LOG = ScoutLogManager.getLogger(SpecJob.class);

  private static final long STARTUP_TIMEOUT_IN_MS = 60 * 1000L;
  private final long m_startupTimeout;
  protected final Class<? extends IClientSession> m_clientSessionClass;
  private final SpecPostProcessor m_postProcessor;

  public SpecJob(Class<? extends IClientSession> clientSessionClass, String pluginName) {
    this(clientSessionClass, STARTUP_TIMEOUT_IN_MS, new SpecPostProcessor(pluginName));
  }

  public SpecJob(Class<? extends IClientSession> clientSessionClass, long startupTimeout, SpecPostProcessor postProcessor) {
    super("Specification");
    setSystem(true);
    m_clientSessionClass = clientSessionClass;
    m_startupTimeout = startupTimeout;
    m_postProcessor = postProcessor;
  }

  @Override
  protected IStatus run(IProgressMonitor monitor) {
    tryStartup();
    final int code = runTests();
    postProcess();
    System.exit(code);
    return Status.OK_STATUS;
  }

  private void tryStartup() {
    try {
      waitForStartup();
    }
    catch (Throwable t) {
      System.err.println("Timeout waiting for SwingApplication to start: " + t);
      System.exit(0);
    }
  }

  protected void waitForStartup() throws Throwable {
    TestingUtility.waitUntil(getStartupTimeout(), new WaitCondition<Object>() {
      @Override
      public Object run() {
        try {
          IClientSession session = createClientSession();
          return isApplicationReady(session) ? true : null;
        }
        catch (Throwable t) {
          t.printStackTrace();
          System.exit(0);
        }
        return null;
      }
    });
  }

  protected IClientSession createClientSession() {
    UserAgent ua = UserAgent.create(UiLayer.SWING, UiDeviceType.DESKTOP);
    return SERVICES.getService(IClientSessionRegistryService.class).newClientSession(m_clientSessionClass, ua);
  }

  /**
   * The desktop must be open for screenshots.
   */
  protected boolean isApplicationReady(IClientSession session) {
    if (session != null) {
      IDesktop desktop = session.getDesktop();
      if (desktop != null) {
        if (desktop.isGuiAvailable() && desktop.isOpened()) {
          return true;
        }
      }
    }
    return false;
  }

  public long getStartupTimeout() {
    return m_startupTimeout;
  }

  private int runTests() {
    ScoutJUnitPluginTestExecutor e = new ScoutJUnitPluginTestExecutor();
    return e.runAllTests();
  }

  private void postProcess() {
    try {
      getPostProcessor().process();
    }
    catch (ProcessingException e) {
      LOG.error("Error during post Processing ", e);
    }
  }

  public SpecPostProcessor getPostProcessor() {
    return m_postProcessor;
  }

}
