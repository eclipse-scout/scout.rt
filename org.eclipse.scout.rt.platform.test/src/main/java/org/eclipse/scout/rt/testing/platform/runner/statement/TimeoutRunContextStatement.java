/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.testing.platform.runner.statement;

import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.transaction.TransactionScope;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruptedError;
import org.eclipse.scout.rt.platform.util.concurrent.TimedOutError;
import org.eclipse.scout.rt.testing.platform.runner.SafeStatementInvoker;
import org.junit.Test;
import org.junit.internal.runners.statements.FailOnTimeout;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestTimedOutException;

/**
 * JUnit runs tests with a timeout in a separate thread. This statement is a replace for {@link FailOnTimeout}, and
 * provides the current {@link RunContext} to the executing thread.
 * <p>
 * Note: The thread runs with a new transaction, because a transaction is only allowed to be accessed by the same
 * thread.
 *
 * @see Test#timeout()
 * @see FailOnTimeout
 * @since 5.1
 */
public class TimeoutRunContextStatement extends Statement {

  private final Statement m_next;
  private final long m_timeoutMillis;

  public TimeoutRunContextStatement(final Statement next, final long timeoutMillis) {
    m_next = Assertions.assertNotNull(next, "next statement must not be null");
    m_timeoutMillis = timeoutMillis;
  }

  @Override
  public void evaluate() throws Throwable {
    final SafeStatementInvoker invoker = new SafeStatementInvoker(m_next);

    final IFuture<Void> future = Jobs.schedule(invoker, Jobs.newInput()
        .withRunContext(RunContext.CURRENT.get().copy().withTransactionScope(TransactionScope.REQUIRES_NEW)) // Run in new TX, because the same TX is not allowed to be used by multiple threads.
        .withName("Running test with support for JUnit timeout"));
    try {
      future.awaitDone(m_timeoutMillis, TimeUnit.MILLISECONDS);
    }
    catch (ThreadInterruptedError | TimedOutError e) { // NOSONAR
      future.cancel(true);
      throw new TestTimedOutException(m_timeoutMillis, TimeUnit.MILLISECONDS); // JUnit timeout exception
    }

    invoker.throwOnError();
  }
}
