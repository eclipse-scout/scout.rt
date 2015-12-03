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
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
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
    if (ModelJobs.isModelThread()) {
      throw new IllegalStateException("Already running in a model job. but tests with max allowed runtime (i.e. @Test(timeout=...)) "
          + "cannot be nested into a model job. Check your test setup or remove timeout.");
    }
    else {
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

      IFuture<Void> future = ModelJobs.schedule(nestedRunnable, ModelJobs.newInput(ClientRunContexts.copyCurrent()).withName("nested JUnit model job"));
      try {
        if (m_timeoutMillis <= 0) {
          future.awaitDoneAndGet();
        }
        else {
          future.awaitDoneAndGet(m_timeoutMillis, TimeUnit.MILLISECONDS);
        }
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
