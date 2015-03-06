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

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.job.IRunnable;
import org.eclipse.scout.rt.platform.cdi.IBean;
import org.eclipse.scout.rt.platform.cdi.OBJ;
import org.eclipse.scout.rt.server.IServerJobFactory;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.job.IServerJobManager;
import org.eclipse.scout.rt.server.job.ServerJobInput;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.eclipse.scout.rt.testing.shared.services.common.exceptionhandler.WrappingProcessingRuntimeExceptionHandlerService;
import org.junit.runners.model.Statement;

/**
 * JUnit statements that runs the JUnit test within a server job.
 */
public class ScoutServerJobWrapperStatement extends Statement {
  private static final int EXCEPTIONHANDLER_SERVICE_RANKING = 1000;
  private final Statement m_statement;
  private final IServerJobFactory m_factory;

  public ScoutServerJobWrapperStatement(IServerJobFactory factory, Statement statement) {
    m_statement = statement;
    m_factory = factory;
  }

  @Override
  public void evaluate() throws Throwable {
    ISession session = ISession.CURRENT.get();
    if (session instanceof IServerSession) {
      doEvaluateWrappingExceptions();
    }
    else {
      OBJ.one(IServerJobManager.class).runNow(new IRunnable() {

        @Override
        public void run() throws Exception {
          doEvaluateWrappingExceptions();
        }
      }, ServerJobInput.defaults().name("JUnit Server Job Runner").session(m_factory.getServerSession()).subject(m_factory.getSubject()));
    }
  }

  protected void doEvaluateWrappingExceptions() throws ProcessingException {
    List<? extends IBean<?>> serviceReg = null;
    try {
      WrappingProcessingRuntimeExceptionHandlerService handler = new WrappingProcessingRuntimeExceptionHandlerService();
      serviceReg = TestingUtility.registerServices(EXCEPTIONHANDLER_SERVICE_RANKING, handler);
      doEvaluate();
    }
    finally {
      if (serviceReg != null) {
        TestingUtility.unregisterServices(serviceReg);
      }
    }
  }

  protected void doEvaluate() throws ProcessingException {
    try {
      m_statement.evaluate();
    }
    catch (Throwable e) {
      throw new ProcessingException(e.getMessage(), e);
    }
  }
}
