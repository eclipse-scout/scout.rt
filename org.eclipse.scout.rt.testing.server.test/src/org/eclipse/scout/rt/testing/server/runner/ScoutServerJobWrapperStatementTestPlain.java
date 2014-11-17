/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.testing.server.runner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.server.IServerJobFactory;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.ITransactionRunnable;
import org.eclipse.scout.rt.server.ServerJob;
import org.eclipse.scout.rt.server.ServerJobFactory;
import org.eclipse.scout.rt.testing.server.test.TestServerSession;
import org.junit.Test;
import org.junit.runners.model.Statement;

/**
 * Tests that {@link ScoutServerTestRunner} evaluates in ServerJob.
 * Has to be a plain junit test and not with {@link ScoutServerTestRunner} to be effective.
 */
public class ScoutServerJobWrapperStatementTestPlain {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ScoutServerJobWrapperStatementTestPlain.class);
  private static final String CUSTOM_ID_PREFIX = "Custom:";

  /**
   * Test for {@link ScoutServerJobWrapperStatement#evaluate()}
   */
  @Test
  public void testEvaluateInServerJob() throws Throwable {
    final TestServerSession session = new TestServerSession();
    ScoutServerJobWrapperStatement statement = new WrapperStatementNoActivator(new ServerJobFactory(session, session.getSubject()), new Statement() {
      @Override
      public void evaluate() throws Throwable {
        assertEquals(TestServerSession.class, ServerJob.getCurrentSession().getClass());
      }
    });
    statement.evaluate();
  }

  /**
   * Test for {@link ScoutServerJobWrapperStatement#evaluate()}
   */
  @Test
  public void testEvaluateInServerJobFactory() throws Throwable {
    final TestServerSession session = new TestServerSession();
    ScoutServerJobWrapperStatement statement = new WrapperStatementNoActivator(new TestFactory(session, session.getSubject()), new Statement() {
      @Override
      public void evaluate() throws Throwable {
        assertEquals(TestServerSession.class, ServerJob.getCurrentSession().getClass());
        assertTrue(ServerJob.getCurrentSession().getId().startsWith(CUSTOM_ID_PREFIX));
      }
    });
    statement.evaluate();
  }

  /**
   * A ScoutServerJobWrapperStatement running without OSGi environment
   */
  class WrapperStatementNoActivator extends ScoutServerJobWrapperStatement {

    public WrapperStatementNoActivator(IServerJobFactory factory, Statement statement) {
      super(factory, statement);
    }

    @Override
    protected void doEvaluateWrappingExceptions() {
      try {
        doEvaluate();
      }
      catch (Throwable e) {
        final String msg = "Error Executing test statement ";
        LOG.error(msg, e);
        fail(msg + e.getMessage());
      }
    }

  }

  class TestFactory extends ServerJobFactory {

    public TestFactory(IServerSession serverSession, Subject subject) {
      super(serverSession, subject);
    }

    @Override
    public ServerJob create(String name, ITransactionRunnable runnable) {
      final ServerJob job = super.create(name, runnable);
      final IServerSession serverSession = job.getServerSession();
      serverSession.setIdInternal(CUSTOM_ID_PREFIX + serverSession.getId());
      return job;
    }

  }
}
