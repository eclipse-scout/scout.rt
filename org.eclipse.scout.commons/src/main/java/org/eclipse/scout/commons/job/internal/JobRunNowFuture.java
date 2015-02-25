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

/**
 * {@link RunnableScheduledFuture} representing a Future used for 'runNow'-style execution if not running in a
 * job execution yet.
 *
 * @see RunnableScheduledFuture
 * @see JobManager
 * @since 5.1
 */
public class JobRunNowFuture<RESULT> implements RunnableScheduledFuture<RESULT> {

  private final Thread m_workerThread;
  private boolean m_cancelled;

  public JobRunNowFuture(final Thread workerThread) {
    m_workerThread = workerThread;
  }

  @Override
  public boolean cancel(final boolean interruptIfRunning) {
    if (m_cancelled) {
      return false;
    }

    if (interruptIfRunning) {
      m_workerThread.interrupt();
    }

    return m_cancelled = true;
  }

  // === RunnableScheduledFuture - delegate methods ===

  @Override
  public boolean isCancelled() {
    return m_cancelled;
  }

  @Override
  public boolean isDone() {
    return false;
  }

  @Override
  public RESULT get() throws InterruptedException, ExecutionException {
    throw new UnsupportedOperationException();
  }

  @Override
  public RESULT get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void run() {
    throw new UnsupportedOperationException();
  }

  @Override
  public long getDelay(final TimeUnit unit) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int compareTo(final Delayed o) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isPeriodic() {
    throw new UnsupportedOperationException();
  }
}
