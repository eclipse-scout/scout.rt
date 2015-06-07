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
package org.eclipse.scout.rt.platform.job.internal;

import java.util.concurrent.FutureTask;

import org.eclipse.scout.commons.Callables;
import org.eclipse.scout.commons.annotations.Internal;

/**
 * This task is not a real task like {@link JobFutureTask} which represents a 'runnable' to be run. Instead, this task
 * is used to re-acquire the mutex for a {@link JobFutureTask} after entering a blocking condition, which in the
 * meantime, is fallen.
 *
 * @since 5.1
 */
@Internal
public abstract class MutexAcquisitionFutureTask extends FutureTask<Void> implements IMutexTask<Void>, IRejectable {

  private final Object m_mutexObject;
  private volatile boolean m_awaitMutex;

  public MutexAcquisitionFutureTask(final Object mutexObject) {
    super(Callables.nullCallable());
    m_mutexObject = mutexObject;
    m_awaitMutex = true;
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
  public final void run() {
    mutexAcquired();
  }

  @Override
  public void reject() {
    mutexAcquired();
  }

  /**
   * @return <code>true</code> if this task is still waiting for the mutex to become available.
   */
  public boolean isAwaitMutex() {
    return m_awaitMutex;
  }

  /**
   * Indicates that this task is no longer interested for the mutex to become available.
   */
  public void stopAwaitMutex() {
    m_awaitMutex = false;
  }

  /**
   * Method invoked once the mutex is acquired for this task.
   */
  protected abstract void mutexAcquired();
}
