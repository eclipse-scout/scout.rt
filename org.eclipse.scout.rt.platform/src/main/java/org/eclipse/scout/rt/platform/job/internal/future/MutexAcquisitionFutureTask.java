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
package org.eclipse.scout.rt.platform.job.internal.future;

import org.eclipse.scout.commons.Callables;
import org.eclipse.scout.commons.annotations.Internal;
import org.eclipse.scout.rt.platform.job.IBlockingCondition;
import org.eclipse.scout.rt.platform.job.internal.MutexSemaphores;

/**
 * FutureTask used to re-acquire the mutex after waiting for a blocking condition to fall.
 *
 * @see IBlockingCondition
 * @since 5.1
 */
@Internal
public abstract class MutexAcquisitionFutureTask extends ScheduledFutureDelegate<Void> implements IFutureTask<Void> {

  private final MutexSemaphores m_mutexSemaphores;
  private final Object m_mutexObject;
  private volatile boolean m_awaitMutex;
  private final Job<Void> m_nullJob;

  public MutexAcquisitionFutureTask(final MutexSemaphores mutexSemaphores, final Object mutexObject) {
    m_mutexSemaphores = mutexSemaphores;
    m_mutexObject = mutexObject;
    m_awaitMutex = true;
    m_nullJob = new Job<>(this, Callables.nullCallable());
  }

  @Override
  public Object getMutexObject() {
    return m_mutexObject;
  }

  @Override
  public boolean isMutexTask() {
    return true;
  }

  @Override
  public boolean isMutexOwner() {
    return m_mutexSemaphores.isMutexOwner(this);
  }

  @Override
  public final void run() {
    mutexAcquired();
  }

  @Override
  public final void reject() {
    mutexAcquired();
  }

  /**
   * @return <code>true</code> if there is a thread waiting for the mutex to become available.
   */
  public boolean isAwaitMutex() {
    return m_awaitMutex;
  }

  /**
   * Invoke this method to no longer wait for the mutex to become available.
   */
  public void stopAwaitMutex() {
    m_awaitMutex = false;
  }

  @Override
  public void setBlocked(final boolean blocked) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Job<Void> getJob() {
    return m_nullJob;
  }

  /**
   * Method invoked once the mutex is acquired.
   */
  protected abstract void mutexAcquired();
}
