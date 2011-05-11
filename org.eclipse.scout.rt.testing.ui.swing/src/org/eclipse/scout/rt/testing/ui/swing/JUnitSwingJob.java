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
package org.eclipse.scout.rt.testing.ui.swing;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.services.common.session.IClientSessionRegistryService;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.testing.shared.ScoutJUnitPluginTestExecutor;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.eclipse.scout.rt.testing.shared.WaitCondition;
import org.eclipse.scout.service.SERVICES;
import org.eclipse.scout.testing.client.IGuiMock;

/**
 * Runs all @Test annotated methods in all classes and then exit
 * <p>
 * Normally this is called from within a swing application in the start method <code><pre>
 *   public Object start(IApplicationContext context) throws Exception {
 *     new JUnitSwingJob(ClientSession.class).schedule(2000);
 *     //
 *     ...
 *   }
 * </pre></code>
 */
public class JUnitSwingJob extends Job {
  private final Class<? extends IClientSession> m_clientSessionClass;

  public JUnitSwingJob(Class<? extends IClientSession> clientSessionClass) {
    super("JUnit Swing Job");
    setSystem(true);
    m_clientSessionClass = clientSessionClass;
  }

  @Override
  protected IStatus run(IProgressMonitor monitor) {
    //wait until the application is showing
    try {
      TestingUtility.waitUntil(IGuiMock.WAIT_TIMEOUT, new WaitCondition<Object>() {
        public Object run() {
          try {
            IClientSession session = SERVICES.getService(IClientSessionRegistryService.class).getClientSession(m_clientSessionClass);
            if (session != null) {
              IDesktop desktop = session.getDesktop();
              if (desktop != null) {
                if (desktop.isGuiAvailable() && desktop.isOpened()) {
                  return true;//non-null
                }
              }
            }
          }
          catch (Throwable t) {
            t.printStackTrace();
            System.exit(0);
          }
          return null;
        }
      });
    }
    catch (Throwable t) {
      System.err.println("Timeout waiting for SwingApplication to start: " + t);
      System.exit(0);
    }
    //
    ScoutJUnitPluginTestExecutor scoutJUnitPluginTestExecutor = new ScoutJUnitPluginTestExecutor();
    final int code = scoutJUnitPluginTestExecutor.runAllTests();
    System.exit(code);
    return Status.OK_STATUS;
  }
}
