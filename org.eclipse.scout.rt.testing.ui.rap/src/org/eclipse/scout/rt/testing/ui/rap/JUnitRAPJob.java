/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.testing.ui.rap;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.testing.shared.ScoutJUnitPluginTestExecutor;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.eclipse.scout.rt.testing.shared.WaitCondition;
import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.scout.testing.client.IGuiMock;
import org.osgi.framework.ServiceRegistration;

/**
 * Runs all @Test annotated methods in all classes and then exit
 * <p>
 * Normally this is called from within a swing application in the start method <code><pre>
 *   public Object start(IApplicationContext context) throws Exception {
 *     new JUnitRAPJob(env).schedule(2000);
 *     //
 *     ...
 *   }
 * </pre></code>
 */
public class JUnitRAPJob extends Job {
  private final IRwtEnvironment m_env;

  public JUnitRAPJob(IRwtEnvironment env) {
    super("JUnit RAP Job");
    m_env = env;
  }

  @Override
  protected IStatus run(IProgressMonitor monitor) {
    //wait until the application is showing
    try {
      TestingUtility.waitUntil(IGuiMock.WAIT_TIMEOUT, new WaitCondition<Object>() {
        @Override
        public Object run() {
          try {
            m_env.getDisplay().asyncExec(new Runnable() {
              @Override
              public void run() {
                m_env.ensureInitialized();
              }
            });
            IClientSession session = m_env.getClientSession();
            if (session != null) {
              IDesktop desktop = session.getDesktop();
              if (desktop != null) {
                if (desktop.isGuiAvailable() && desktop.isOpened()) {
                  return true;//not null
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
      System.err.println("Timeout waiting for RAPApplication to start: " + t);
      System.exit(0);
    }
    //
    RapClientSessionRegistryService csrs = new RapClientSessionRegistryService(m_env.getClientSession());
    List<ServiceRegistration> regs = null;
    try {
      regs = TestingUtility.registerServices(Activator.getDefault().getBundle(), 1000, csrs);
      //
      ScoutJUnitPluginTestExecutor scoutJUnitPluginTestExecutor = new ScoutJUnitPluginTestExecutor();
      final int code = scoutJUnitPluginTestExecutor.runAllTests();
      System.exit(code);
      return Status.OK_STATUS;
    }
    finally {
      TestingUtility.unregisterServices(regs);
    }
  }
}
