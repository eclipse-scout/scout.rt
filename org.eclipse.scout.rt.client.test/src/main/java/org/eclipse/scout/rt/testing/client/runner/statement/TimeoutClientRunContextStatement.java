/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.testing.client.runner.statement;

import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruptedException;
import org.eclipse.scout.rt.platform.util.concurrent.TimedOutException;
import org.eclipse.scout.rt.testing.platform.runner.SafeStatementInvoker;
import org.junit.Test;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestTimedOutException;

/**
 * Statement for executing tests with a timeout (i.e. the annotated test method is expected to complete within the
 * specified amount of time). The given next statement is executed in a new model job. Hence this statement cannot be
 * evaluated within a model job.
 *
 * @see Test#timeout()
 * @since 5.1
 */
public class TimeoutClientRunContextStatement extends Statement {

  protected final Statement m_next;
  private final long m_timeoutMillis;

  public TimeoutClientRunContextStatement(final Statement next, final long timeoutMillis) {
    m_timeoutMillis = timeoutMillis;
    m_next = Assertions.assertNotNull(next, "next statement must not be null");
  }

  @Override
  public void evaluate() throws Throwable {
    final SafeStatementInvoker invoker = new SafeStatementInvoker(m_next);

    final IFuture<Void> future = ModelJobs.schedule(invoker, ModelJobs.newInput(ClientRunContexts.copyCurrent()).withName("Running test with support for JUnit timeout"));

    try {
      if (m_timeoutMillis <= 0) {
        future.awaitDone();
      }
      else {
        future.awaitDone(m_timeoutMillis, TimeUnit.MILLISECONDS);
      }
    }
    catch (ThreadInterruptedException | TimedOutException e) {
      future.cancel(true);
      throw new TestTimedOutException(m_timeoutMillis, TimeUnit.MILLISECONDS); // JUnit timeout exception
    }

    invoker.throwOnError();
  }
}
