/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.desktop.hybrid;

import java.util.Locale;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.platform.job.IBlockingCondition;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.concurrent.TimedOutError;

/**
 * {@link Future} implementation intended for issuing a call to the browser.
 *
 * @since 6.1
 */
public class BrowserCallback<T> implements Future<T> {
  private volatile boolean m_cancelled = false;
  private volatile boolean m_done = false;
  private volatile T m_result;
  private volatile ExecutionException m_failure;
  private final IBlockingCondition m_blockingCondition = Jobs.newBlockingCondition(true);

  @Override
  public boolean cancel(boolean mayInterruptIfRunning) {
    boolean alreadyCancelled = m_cancelled;
    m_cancelled = !m_done;
    boolean hasBeenCancelled = !alreadyCancelled && m_cancelled;
    if (hasBeenCancelled) {
      m_blockingCondition.setBlocking(false);
    }
    return hasBeenCancelled;
  }

  @Override
  public boolean isCancelled() {
    return m_cancelled;
  }

  @Override
  public boolean isDone() {
    return m_done;
  }

  @Override
  public T get() throws InterruptedException, ExecutionException, CancellationException {
    m_blockingCondition.waitFor(ModelJobs.EXECUTION_HINT_UI_INTERACTION_REQUIRED);
    return report();
  }

  @Override
  public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException, CancellationException {
    try {
      m_blockingCondition.waitFor(timeout, unit, ModelJobs.EXECUTION_HINT_UI_INTERACTION_REQUIRED);
    }
    catch (TimedOutError t) { // NOSONAR
      timedOut();
      throw new TimeoutException("BrowserCallback timed out after " + timeout + " " + unit.toString().toLowerCase(Locale.US) + ".");
    }
    return report();
  }

  /**
   * This method might be overwritten in order to intercept a timed out wait on get.
   */
  protected void timedOut() {
    // nop
  }

  public boolean done(T result) {
    if (m_cancelled || m_done) {
      return false;
    }
    m_done = true;
    m_result = result;
    m_blockingCondition.setBlocking(false);
    return true;
  }

  public boolean failed(Throwable t) {
    if (m_cancelled || m_done) {
      return false;
    }
    m_done = true;
    m_failure = new ExecutionException(t);
    m_blockingCondition.setBlocking(false);
    return true;
  }

  private T report() throws InterruptedException, ExecutionException, CancellationException {
    if (m_cancelled) {
      throw new CancellationException();
    }
    if (!m_done) {
      throw new InterruptedException();
    }
    if (m_failure != null) {
      throw m_failure;
    }
    return m_result;
  }
}
