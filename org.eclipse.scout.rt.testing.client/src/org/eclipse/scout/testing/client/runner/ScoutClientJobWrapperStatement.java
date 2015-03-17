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
package org.eclipse.scout.testing.client.runner;

import java.util.List;

import org.eclipse.scout.commons.job.IFuture;
import org.eclipse.scout.commons.job.IRunnable;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.job.ClientJobInput;
import org.eclipse.scout.rt.client.job.IModelJobManager;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.OBJ;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.eclipse.scout.rt.testing.shared.services.common.exceptionhandler.WrappingProcessingRuntimeExceptionHandlerService;
import org.junit.runners.model.Statement;

/**
 * JUnit statements that runs the JUnit test within a Scout client session.
 */
public class ScoutClientJobWrapperStatement extends Statement {

  private final IClientSession m_clientSession;
  private final Statement m_statement;

  public ScoutClientJobWrapperStatement(IClientSession clientSession, Statement statement) {
    m_clientSession = clientSession;
    m_statement = statement;
  }

  @Override
  public void evaluate() throws Throwable {
    if (ClientSessionProvider.currentSession() == m_clientSession) {
      doEvaluate();
    }
    else {
      IFuture<Void> future = OBJ.get(IModelJobManager.class).schedule(new IRunnable() {
        @Override
        public void run() throws Exception {
          try {
            doEvaluate();
          }
          catch (Throwable e) {
            throw new Exception(e);
          }
        }
      }, ClientJobInput.defaults().session(m_clientSession).name("JUnit Client Job Runner"));
      future.get();
    }
  }

  private void doEvaluate() throws Throwable {
    List<IBean<?>> serviceReg = null;
    try {
      WrappingProcessingRuntimeExceptionHandlerService handler = new WrappingProcessingRuntimeExceptionHandlerService();
      serviceReg = TestingUtility.registerServices(1000, handler);
      m_statement.evaluate();
    }
    finally {
      if (serviceReg != null) {
        TestingUtility.unregisterServices(serviceReg);
      }
    }
  }
}
