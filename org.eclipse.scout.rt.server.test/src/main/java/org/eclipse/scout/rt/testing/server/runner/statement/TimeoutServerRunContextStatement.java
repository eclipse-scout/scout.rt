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
package org.eclipse.scout.rt.testing.server.runner.statement;

import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.transaction.TransactionScope;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruptedException;
import org.eclipse.scout.rt.platform.util.concurrent.TimedOutException;
import org.eclipse.scout.rt.server.context.ServerRunContexts;
import org.eclipse.scout.rt.testing.platform.runner.SafeStatementInvoker;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestTimedOutException;

/**
 * Statement for executing tests with a timeout (i.e. the annotated test method is expected to complete within the
 * specified amount of time). If the given timeout is <code>0</code> the next statement is executed in the calling
 * thread. A positive timeout value requires a new job to be scheduled and awaited for. That job runs with a new
 * transaction, because a transaction is only allowed to be accessed by the same thread.
 *
 * @see Test#timeout()
 * @since 5.1
 */
public class TimeoutServerRunContextStatement extends Statement {

  private final Statement m_next;
  private final long m_timeoutMillis;

  public TimeoutServerRunContextStatement(final Statement next, final long timeoutMillis) {
    m_next = Assertions.assertNotNull(next, "next statement must not be null");
    m_timeoutMillis = timeoutMillis;
  }

  @Override
  public void evaluate() throws Throwable {
    if (m_timeoutMillis <= 0) {
      m_next.evaluate();
    }
    else {
      evaluateWithTimeout();
    }
  }

  protected void evaluateWithTimeout() throws Throwable {
    final SafeStatementInvoker invoker = new SafeStatementInvoker(m_next);

    final IFuture<Void> future = Jobs.schedule(invoker, Jobs.newInput()
        .withRunContext(ServerRunContexts.copyCurrent().withTransactionScope(TransactionScope.REQUIRES_NEW)) // Run in new TX, because the same TX is not allowed to be used by multiple threads.
        .withName("Running test with support for JUnit timeout"));

    try {
      future.awaitDone(m_timeoutMillis, TimeUnit.MILLISECONDS);
    }
    catch (ThreadInterruptedException | TimedOutException e) {
      future.cancel(true);
      throw new TestTimedOutException(m_timeoutMillis, TimeUnit.MILLISECONDS); // JUnit timeout exception
    }

    invoker.throwOnError();
  }
}
