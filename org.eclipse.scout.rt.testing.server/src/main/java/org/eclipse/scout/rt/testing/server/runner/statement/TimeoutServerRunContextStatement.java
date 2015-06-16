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

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.server.context.ServerRunContext;
import org.eclipse.scout.rt.server.context.ServerRunContexts;
import org.eclipse.scout.rt.server.job.ServerJobs;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestTimedOutException;

/**
 * Statement for executing tests with a timeout (i.e. the annotated test method is expected to complete within the
 * specified amount of time). If the given timeout is <code>0</code> the next statement is executed using a copy of the
 * current {@link ServerRunContext}. A positive timeout value requires a new server job to be scheduled. In both cases,
 * the next statement is executed in its own transaction.
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
        catch (final Exception e) {
          throw new WrappingError(e);
        }
        catch (final Error e) {
          throw e;
        }
        catch (final Throwable e) {
          throw new Error(e);
        }
      }
    };

    if (m_timeoutMillis <= 0) {
      // no timeout specified. Hence run in a nested transaction that uses the calling thread
      try {
        ServerRunContexts.copyCurrent().run(nestedRunnable);
      }
      catch (WrappingError e) {
        throw e.getCause();
      }
    }
    else {
      // timeout specified. Run statement in a new server job and wait the amount specified.
      IFuture<Void> future = ServerJobs.schedule(nestedRunnable);
      try {
        future.awaitDoneAndGet(m_timeoutMillis, TimeUnit.MILLISECONDS);
      }
      catch (WrappingError e) {
        throw e.getCause();
      }
      catch (ProcessingException e) {
        if (e.isTimeout()) {
          // waiting on the job to complete timed out. Try to cancel the job and translate exception into junit counterpart.
          future.cancel(true);
          throw new TestTimedOutException(m_timeoutMillis, TimeUnit.MILLISECONDS);
        }

        // re-throw any other job exception
        throw e;
      }
    }
  }

  private static class WrappingError extends Error {
    private static final long serialVersionUID = 1L;

    public WrappingError(Throwable paramThrowable) {
      super(paramThrowable);
    }
  }
}
