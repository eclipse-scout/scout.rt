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

import java.util.concurrent.TimeUnit;

import org.eclipse.scout.commons.filter.AndFilter;
import org.eclipse.scout.commons.filter.NotFilter;
import org.eclipse.scout.commons.job.IFuture;
import org.eclipse.scout.commons.job.IRunnable;
import org.eclipse.scout.commons.job.filter.FutureFilter;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.job.ClientJobInput;
import org.eclipse.scout.rt.client.job.IClientJobManager;
import org.eclipse.scout.rt.client.job.IModelJobManager;
import org.eclipse.scout.rt.client.job.filter.BlockedJobFilter;
import org.eclipse.scout.rt.client.services.common.session.IClientSessionRegistryService;
import org.eclipse.scout.rt.platform.cdi.OBJ;
import org.eclipse.scout.rt.shared.ui.UserAgent;
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
        IClientSession clientSession = service.newClientSession(session.getClass(), null, initUserAgent);
        final IGuiMock gui = guiMockService.createMock(clientSession);
        IModelJobManager modelJobManager = OBJ.one(IModelJobManager.class);
        gui.beforeTest();
        IFuture<Void> runModelJob = null;
        try {
          // run model job
          runModelJob = modelJobManager.schedule(new IRunnable() {
            @Override
            public void run() throws Exception {
              try {
                testStatement.evaluate();
              }
              catch (Throwable e) {
                throw new Exception(e);
              }
            }
          }, ClientJobInput.defaults().session(clientSession).name("Run"));

          modelJobManager.waitUntilDone(new AndFilter<IFuture<?>>(new FutureFilter(runModelJob), new NotFilter<>(BlockedJobFilter.INSTANCE)), 1, TimeUnit.HOURS);

          // gui script job
          IFuture<Void> guiScriptJob = OBJ.one(IClientJobManager.class).schedule(new IRunnable() {
            @Override
            public void run() throws Exception {
              gui.waitForIdle();
            }
          }, ClientJobInput.defaults().session(clientSession).name("Gui Script"));
          guiScriptJob.get();

          runModelJob.get();
        }
        finally {
          gui.afterTest();
        }
      }
    };

  }

}
