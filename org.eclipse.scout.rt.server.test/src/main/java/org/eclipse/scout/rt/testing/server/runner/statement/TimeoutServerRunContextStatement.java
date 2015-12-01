/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
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

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.exception.ThrowableTranslator;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.server.context.ServerRunContext;
import org.eclipse.scout.rt.server.context.ServerRunContexts;
import org.eclipse.scout.rt.server.transaction.TransactionScope;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestTimedOutException;

/**
 * Statement for executing tests with a timeout (i.e. the annotated test method is expected to complete within the
 * specified amount of time). If the given timeout is <code>0</code> the next statement is executed using a copy of the
 * current {@link ServerRunContext}. A positive timeout value requires a new server job to be scheduled. In both cases,
 * the next statement is executed in the callers transaction, or in a new transaction if not available.
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
    IRunnable nestedRunnable = new IRunnable() {
      @Override
      public void run() throws Exception {
        try {
          m_next.evaluate();
        }
        catch (final Error e) {
          throw e;
        }
        catch (final Throwable t) {
          throw new ProcessingException("Wrapper", t);
        }
      }
    };

    // Create a copy of the calling RunContext and re-use its transaction if available.
    // TODO [abr/dwi]: Should we use the current TX if available? (REQUIRED)
    ServerRunContext runContext = ServerRunContexts.copyCurrent().withTransactionScope(TransactionScope.REQUIRES_NEW);

    if (m_timeoutMillis <= 0) {
      // no timeout specified. Hence run in a nested transaction that uses the calling thread
      runContext.run(nestedRunnable, BEANS.get(ThrowableTranslator.class));
    }
    else {
      // timeout specified. Run statement in a new server job and wait the amount specified.
      IFuture<Void> future = Jobs.schedule(nestedRunnable, Jobs.newInput()
          .withRunContext(runContext));
      try {
        future.awaitDoneAndGet(m_timeoutMillis, TimeUnit.MILLISECONDS);
      }
      catch (ProcessingException e) {
        if (e.isTimeout() || e.isInterruption()) {
          // Timeout or interruption: Try to cancel the job and translate exception into JUnit counterpart.
          future.cancel(true);
          throw new TestTimedOutException(m_timeoutMillis, TimeUnit.MILLISECONDS);
        }

        throw e.getCause(); // re-throw wrapped exception
      }
    }
  }
}
