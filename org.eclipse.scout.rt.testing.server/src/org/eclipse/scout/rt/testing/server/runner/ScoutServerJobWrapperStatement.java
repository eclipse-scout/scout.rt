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
package org.eclipse.scout.rt.testing.server.runner;

import java.util.List;

import javax.security.auth.Subject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.ServerJob;
import org.eclipse.scout.rt.testing.shared.Activator;
import org.eclipse.scout.rt.testing.commons.ScoutAssert;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.eclipse.scout.rt.testing.shared.services.common.exceptionhandler.WrappingProcessingRuntimeExceptionHandlerService;
import org.junit.runners.model.Statement;
import org.osgi.framework.ServiceRegistration;

/**
 * JUnit statements that runs the JUnit test within a Scout server session.
 */
public class ScoutServerJobWrapperStatement extends Statement {

  private final IServerSession m_serverSession;
  private final Subject m_subject;
  private final Statement m_statement;

  public ScoutServerJobWrapperStatement(IServerSession serverSession, Subject subject, Statement statement) {
    m_serverSession = serverSession;
    m_subject = subject;
    m_statement = statement;
  }

  @Override
  public void evaluate() throws Throwable {
    if (ServerJob.getCurrentSession() != null) {
      doEvaluate();
    }
    else {
      ServerJob job = new ServerJob("JUnit Server Job Runner", m_serverSession, m_subject) {
        @Override
        protected IStatus runTransaction(IProgressMonitor monitor) throws Exception {
          doEvaluate();
          return Status.OK_STATUS;
        }
      };
      job.schedule();
      job.join();
      ScoutAssert.jobSuccessfullyCompleted(job);
    }
  }

  private void doEvaluate() throws Exception {
    List<ServiceRegistration> serviceReg = null;
    try {
      WrappingProcessingRuntimeExceptionHandlerService handler = new WrappingProcessingRuntimeExceptionHandlerService();
      serviceReg = TestingUtility.registerServices(Activator.getDefault().getBundle(), 1000, handler);
      try {
        m_statement.evaluate();
      }
      catch (Exception e) {
        throw e;
      }
      catch (Error e) {
        throw e;
      }
      catch (Throwable e) {
        throw new Exception(e);
      }
    }
    finally {
      if (serviceReg != null) {
        TestingUtility.unregisterServices(serviceReg);
      }
    }
  }
}
