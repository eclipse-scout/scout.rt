/*
 * Copyright (c) 2010-2023 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.testing.platform.runner.statement;

import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruptedError;
import org.eclipse.scout.rt.platform.util.concurrent.TimedOutError;
import org.eclipse.scout.rt.testing.platform.runner.SafeStatementInvoker;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestTimedOutException;

public abstract class AbstractTimeoutRunContextStatement extends Statement {

  protected final Statement m_next;
  protected final long m_timeoutMillis;

  public AbstractTimeoutRunContextStatement(final Statement next, final long timeoutMillis) {
    m_next = Assertions.assertNotNull(next, "next statement must not be null");
    m_timeoutMillis = timeoutMillis;
  }

  protected abstract IFuture<Void> createFuture(IRunnable runnable);

  @Override
  public void evaluate() throws Throwable {
    final SafeStatementInvoker invoker = new SafeStatementInvoker(m_next);
    class TimeoutFutureRunnable implements IRunnable {

      protected Thread m_thread;

      @Override
      public void run() throws Exception {
        try {
          m_thread = Thread.currentThread();
          invoker.run();
        }
        finally {
          m_thread = null;
        }
      }

      public StackTraceElement[] getStackTrace() {
        Thread t = m_thread;
        if (t != null) {
          return t.getStackTrace();
        }
        return new StackTraceElement[0];
      }
    }
    final TimeoutFutureRunnable runnable = new TimeoutFutureRunnable();

    final IFuture<Void> future = createFuture(runnable);
    try {
      if (m_timeoutMillis <= 0) {
        future.awaitDone();
      }
      else {
        future.awaitDone(m_timeoutMillis, TimeUnit.MILLISECONDS);
      }
    }
    catch (ThreadInterruptedError | TimedOutError e) { // NOSONAR
      System.err.println("Test has timed out, cancelling future (if test threw an exception, original exception is still thrown)");
      StackTraceElement[] stackTrace = runnable.getStackTrace();
      future.cancel(true);
      printStackTrace(stackTrace);
      invoker.throwOnError(); // throw if test itself has thrown an error
      throw new TestTimedOutException(m_timeoutMillis, TimeUnit.MILLISECONDS); // JUnit timeout exception
    }

    invoker.throwOnError();
  }

  protected void printStackTrace(StackTraceElement[] stackTrace) {
    StringBuilder trace = new StringBuilder();
    for (StackTraceElement traceElement : stackTrace) {
      if (trace.length() != 0) {
        trace.append('\n');
      }
      trace.append("\tat ");
      trace.append(traceElement);
    }
    if (trace.length() > 0) {
      System.err.println("Test timed out\n" + trace);
    }
  }
}
