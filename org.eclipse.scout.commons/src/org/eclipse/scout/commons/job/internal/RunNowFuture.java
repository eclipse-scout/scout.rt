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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.scout.commons.job.IJob;

/**
 * {@link Future} used for {@link IJob#runNow()} invocations.
 *
 * @since 5.0
 */
public class RunNowFuture<R> implements Future<R> {

  private final Thread m_workerThread;
  private boolean m_cancelled;

  public RunNowFuture(final Thread workerThread) {
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

  @Override
  public boolean isCancelled() {
    return m_cancelled;
  }

  @Override
  public boolean isDone() {
    return false;
  }

  @Override
  public R get() throws InterruptedException, ExecutionException {
    throw new UnsupportedOperationException();
  }

  @Override
  public R get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
    throw new UnsupportedOperationException();
  }
}
