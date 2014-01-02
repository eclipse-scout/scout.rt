package org.eclipse.scout.testing.client.runner;

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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.services.common.session.IClientSessionRegistryService;
import org.eclipse.scout.rt.shared.ui.UserAgent;
import org.eclipse.scout.rt.testing.commons.ScoutAssert;
import org.eclipse.scout.service.SERVICES;
import org.eclipse.scout.testing.client.IGuiMock;
import org.eclipse.scout.testing.client.IGuiMockService;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

/**
 *
 */
public class ScoutClientGUITestRunner extends ScoutClientTestRunner {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ScoutClientGUITestRunner.class);

  /**
   * @param klass
   * @throws InitializationError
   */
  public ScoutClientGUITestRunner(Class<?> klass) throws InitializationError {
    super(klass);
  }

  @Override
  protected Statement createWrappedStatement(final Statement testStatement, final IClientSession session) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        IGuiMockService guiMockService = SERVICES.getService(IGuiMockService.class);
        if (guiMockService == null) {
          LOG.error("Can not evaluate statement, no IGuiMockService available");
          return;
        }
        if (session == null) {
          LOG.error("Can not evaluate statement, no Session available");
          return;
        }
        IClientSessionRegistryService service = SERVICES.getService(IClientSessionRegistryService.class);
        UserAgent initUserAgent = guiMockService.initUserAgent();
        IClientSession clientSession = service.newClientSession(session.getClass(), initUserAgent);
        final IGuiMock gui = guiMockService.createMock(clientSession);
        gui.beforeTest();
        try {
          //
          final ClientSyncJob runModelJob = new ClientSyncJob("Run", clientSession) {
            @Override
            protected void runVoid(IProgressMonitor m) throws Throwable {
              testStatement.evaluate();
            }
          };
          runModelJob.setUser(false);
          runModelJob.setSystem(true);
          //
          final ClientSyncJob disposeModelJob = new ClientSyncJob("Dispose", clientSession) {
            @Override
            protected void runVoid(IProgressMonitor m) throws Throwable {
            }
          };
          disposeModelJob.setUser(false);
          disposeModelJob.setSystem(true);
          //
          JobEx guiScriptJob = new JobEx("Gui Script") {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
              try {
                gui.waitForIdle();
                return Status.OK_STATUS;
              }
              catch (Throwable t) {
                return new Status(Status.WARNING, ScoutClientGUITestRunner.this.getClass().getName(), t.getMessage(), t);
              }
            }
          };
          guiScriptJob.setUser(false);
          guiScriptJob.setSystem(true);
          //
          try {
            runModelJob.schedule();
            while (!runModelJob.isWaitFor() && runModelJob.getState() != Job.NONE) {
              runModelJob.join(100);
            }
            guiScriptJob.schedule();
            guiScriptJob.join();
          }
          finally {
            disposeModelJob.schedule();
            disposeModelJob.join();
          }
          runModelJob.join();
          ScoutAssert.jobSuccessfullyCompleted(runModelJob);
          ScoutAssert.jobSuccessfullyCompleted(guiScriptJob);
          ScoutAssert.jobSuccessfullyCompleted(disposeModelJob);
        }
        finally {
          gui.afterTest();
        }
      }
    };

  }

}
