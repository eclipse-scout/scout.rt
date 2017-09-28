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
package org.eclipse.scout.rt.client.ui.desktop;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.platform.job.IBlockingCondition;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.concurrent.TimedOutError;

/**
 * {@link Future} implementation intended for issuing a call to a client.
 *
 * @since 6.1
 */
public class ClientCallback<T> implements Future<T> {
  private boolean m_cancelled = false;
  private boolean m_done = false;
  private T m_result;
  private ExecutionException m_failure;
  private final IBlockingCondition m_blockingCondition = Jobs.newBlockingCondition(true);

  @Override
  public boolean cancel(boolean mayInterruptIfRunning) {
    boolean alreadyCancelled = m_cancelled;
    m_cancelled = !m_done;
    return !alreadyCancelled && m_cancelled;
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
  public T get() throws InterruptedException, ExecutionException {
    m_blockingCondition.waitForUninterruptibly(ModelJobs.EXECUTION_HINT_UI_INTERACTION_REQUIRED);
    return report();
  }

  @Override
  public T get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
    try {
      m_blockingCondition.waitForUninterruptibly(timeout, unit, ModelJobs.EXECUTION_HINT_UI_INTERACTION_REQUIRED);
    }
    catch (TimedOutError t) { // NOSONAR
      timedOut();
      throw new TimeoutException();
    }
    return report();
  }

  /**
   * This method might be overwritten in order to intercept a timed out wait on get.
   */
  protected void timedOut() {
    //nop
  }

  public void done(T result) {
    if (m_cancelled || m_done) {
      return;
    }
    m_done = true;
    m_result = result;
    m_blockingCondition.setBlocking(false);
  }

  public void failed(Throwable t) {
    if (m_cancelled || m_done) {
      return;
    }
    m_done = true;
    m_failure = new ExecutionException(t);
    m_blockingCondition.setBlocking(false);
  }

  private T report() throws InterruptedException, ExecutionException {
    if (!m_done) {
      throw new InterruptedException();
    }
    if (m_failure != null) {
      throw m_failure;
    }
    return m_result;
  }
}
