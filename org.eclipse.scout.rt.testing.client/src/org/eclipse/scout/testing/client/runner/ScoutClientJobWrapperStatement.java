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

import org.eclipse.scout.rt.client.ClientJob;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.testing.shared.Activator;
import org.eclipse.scout.rt.testing.commons.ScoutAssert;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.eclipse.scout.rt.testing.shared.services.common.exceptionhandler.WrappingProcessingRuntimeExceptionHandlerService;
import org.junit.runners.model.Statement;
import org.osgi.framework.ServiceRegistration;

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
    if (ClientJob.getCurrentSession() != null) {
      doEvaluate();
    }
    else {
      ClientSyncJob job = new ClientSyncJob("JUnit Client Job Runner", m_clientSession) {
        @Override
        protected void runVoid(org.eclipse.core.runtime.IProgressMonitor monitor) throws Throwable {
          doEvaluate();
        }
      };
      job.schedule();
      job.join();
      ScoutAssert.jobSuccessfullyCompleted(job);
    }
  }

  private void doEvaluate() throws Throwable {
    List<ServiceRegistration> serviceReg = null;
    try {
      WrappingProcessingRuntimeExceptionHandlerService handler = new WrappingProcessingRuntimeExceptionHandlerService();
      serviceReg = TestingUtility.registerServices(Activator.getDefault().getBundle(), 1000, handler);
      m_statement.evaluate();
    }
    finally {
      if (serviceReg != null) {
        TestingUtility.unregisterServices(serviceReg);
      }
    }
  }
}
