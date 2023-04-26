/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.testing.platform.runner.statement;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
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
      public void run() {
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
    catch (TimedOutError e) { // NOSONAR
      System.err.println("Test has timed out, cancelling future and printing stack trace");
      StackTraceElement[] stackTrace = runnable.getStackTrace();
      future.cancel(true);
      printStackTrace(stackTrace);
      throw new TestTimedOutException(m_timeoutMillis, TimeUnit.MILLISECONDS); // JUnit timeout exception
    }

    invoker.throwOnError();
  }

  protected void printStackTrace(StackTraceElement[] stackTrace) {
    System.err.println(StringUtility.box("Trace:\n", Arrays.stream(stackTrace)
        .map(stackTraceElement -> StringUtility.box("\tat ", Objects.toString(stackTraceElement), null))
        .collect(Collectors.joining("\n")), null));
  }
}
