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
package org.eclipse.scout.rt.platform.context;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.util.concurrent.ICancellable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A <code>RunMonitor</code> provides cancellation support for operations typically running on behalf of a
 * {@link RunContext}. By default, when {@link RunContexts#copyCurrent() copying} the current RunContext to run some
 * operations on behalf, a new {@link RunMonitor} is created and registered as child monitor in the monitor of the
 * current calling context to allow nested cancellation. However, if that child monitor is cancelled, cancellation is
 * not propagated to the parent monitors.
 * <p>
 * Any {@link ICancellable}s registered with this {@link RunMonitor} are cancelled as well once this {@link RunMonitor}
 * is cancelled.
 *
 * @since 5.1
 */
@Bean
public class RunMonitor implements ICancellable {

  private static final Logger LOG = LoggerFactory.getLogger(RunMonitor.class);

  /**
   * The monitor which is currently associated with the current thread; is never <code>null</code> if running within a
   * {@link RunContext} or job.
   */
  public static final ThreadLocal<RunMonitor> CURRENT = new ThreadLocal<>();

  private final Set<ICancellable> m_cancellables = new HashSet<>(1);
  private final AtomicBoolean m_cancelled = new AtomicBoolean(false);

  private final ReadWriteLock m_lock = new ReentrantReadWriteLock();

  /**
   * @return <code>true</code> if this {@link RunMonitor} was cancelled.
   */
  @Override
  public boolean isCancelled() {
    return m_cancelled.get();
  }

  /**
   * Cancels this monitor and all registered {@link ICancellable}s like associated {@link RunContext}s and
   * {@link IFuture}s. If already cancelled, this cancellation request has no effect.
   *
   * @param interruptIfRunning
   *          <code>true</code> to interrupt the executing thread, or else <code>false</code>.
   * @return <code>true</code> if this monitor was not cancelled yet and all registered {@link ICancellable}s could be
   *         cancelled successfully.
   */
  @Override
  public boolean cancel(final boolean interruptIfRunning) {
    if (!m_cancelled.compareAndSet(false, true)) {
      return false; // same behavior like Java Future.
    }

    boolean success = true;
    for (final ICancellable cancellable : getCancellables()) {
      if (!invokeCancel(cancellable, interruptIfRunning)) {
        success = false;
      }
    }
    return success;
  }

  /**
   * Registers the given {@link ICancellable} to be cancelled once this monitor get cancelled. If the monitor is already
   * cancelled, the given {@link ICancellable} is cancelled immediately with <code>interruptIfRunning=true</code>.
   */
  public void registerCancellable(final ICancellable cancellable) {
    boolean cancel = true;

    if (!isCancelled()) {
      m_lock.writeLock().lock();
      try {
        if (!isCancelled()) { // double-checked locking
          m_cancellables.add(cancellable);
          cancel = false;
        }
      }
      finally {
        m_lock.writeLock().unlock();
      }
    }

    if (cancel) {
      invokeCancel(cancellable, true);
    }
  }

  /**
   * Unregisters the given {@link ICancellable}.
   */
  public void unregisterCancellable(final ICancellable cancellable) {
    m_lock.writeLock().lock();
    try {
      m_cancellables.remove(cancellable);
    }
    finally {
      m_lock.writeLock().unlock();
    }
  }

  protected Set<ICancellable> getCancellables() {
    m_lock.readLock().lock();
    try {
      return new HashSet<>(m_cancellables);
    }
    finally {
      m_lock.readLock().unlock();
    }
  }

  protected boolean invokeCancel(final ICancellable cancellable, final boolean interruptIfRunning) {
    try {
      if (!cancellable.isCancelled()) {
        return cancellable.cancel(interruptIfRunning);
      }
      return false; // same behavior like Java Future.
    }
    catch (final RuntimeException e) {
      LOG.error("Cancellation failed [cancellable={}]", cancellable, e);
      return false;
    }
  }
}
