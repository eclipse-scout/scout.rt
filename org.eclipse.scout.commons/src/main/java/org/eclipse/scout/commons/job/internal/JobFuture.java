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
package org.eclipse.scout.commons.job.internal;

import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.scout.commons.job.IJobInput;

/**
 * {@link RunnableScheduledFuture} representing a task associated with a {@link IJobInput}.
 *
 * @see RunnableScheduledFuture
 * @see JobManager
 * @since 5.1
 */
public class JobFuture<RESULT> implements RunnableScheduledFuture<RESULT> {

  private final RunnableScheduledFuture<RESULT> m_delegate;
  private final IJobInput m_input;

  public JobFuture(final RunnableScheduledFuture<RESULT> delegate, final IJobInput input) {
    m_delegate = delegate;
    m_input = input;
  }

  /**
   * @return Job input that describes the executing job and contains instruction details about the job's
   *         execution.
   */
  public IJobInput getInput() {
    return m_input;
  }

  // === RunnableScheduledFuture - delegate methods ===

  @Override
  public boolean cancel(final boolean interruptIfRunning) {
    return m_delegate.cancel(interruptIfRunning);
  }

  @Override
  public boolean isCancelled() {
    return m_delegate.isCancelled();
  }

  @Override
  public boolean isDone() {
    return m_delegate.isDone();
  }

  @Override
  public RESULT get() throws InterruptedException, ExecutionException {
    return m_delegate.get();
  }

  @Override
  public RESULT get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
    return m_delegate.get(timeout, unit);
  }

  @Override
  public boolean isPeriodic() {
    return m_delegate.isPeriodic();
  }

  @Override
  public void run() {
    m_delegate.run();
  }

  @Override
  public long getDelay(final TimeUnit unit) {
    return m_delegate.getDelay(unit);
  }

  @Override
  public int compareTo(final Delayed o) {
    return m_delegate.compareTo(o);
  }
}
