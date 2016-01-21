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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.util.concurrent.ICancellable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A <code>RunMonitor</code> allows the registration of {@link ICancellable} objects, which are cancelled upon
 * cancellation of this monitor.
 * <p>
 * A <code>RunMonitor</code> is associated with every {@link RunContext} and {@link IFuture}, meaning that executing
 * code can always query its current cancellation status via <code>RunMonitor.CURRENT.get().isCancelled()</code>.
 * <p>
 * When registering a {@link ICancellable} and this monitor is already cancelled, the {@link ICancellable} is cancelled
 * immediately.
 * <p>
 * A <code>RunMonitor</code> implements {@link ICancellable} interface, meaning that monitors can be nested to propagate
 * cancellation to child monitors.
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

  private final ReadWriteLock m_registrationLock = new ReentrantReadWriteLock();
  private final List<ICancellable> m_cancellables = new ArrayList<>(); // Do not use a Set to ensure cancellation in the order as registered.

  private volatile boolean m_cancelled;
  private final Object m_cancellationLock = new Object();

  /**
   * Returns <code>true</code> if this {@link RunMonitor} was cancelled, or else <code>false</code>.
   */
  @Override
  public boolean isCancelled() {
    return m_cancelled;
  }

  /**
   * Cancels this monitor and all registered {@link ICancellable}s. If already cancelled, this cancellation request has
   * no effect.
   *
   * @param interruptIfRunning
   *          <code>true</code> to interrupt the executing thread, or else <code>false</code>.
   * @return <code>true</code> if this monitor was not cancelled yet and all registered {@link ICancellable}s could be
   *         cancelled successfully.
   */
  @Override
  public boolean cancel(final boolean interruptIfRunning) {
    if (m_cancelled) {
      return false;
    }

    synchronized (m_cancellationLock) {
      if (m_cancelled) { // double-checked locking
        return false;
      }

      m_cancelled = true;
    }

    // Cancel the Cancellables outside the lock.
    boolean success = true;
    for (final ICancellable cancellable : getCancellables()) {
      if (!cancel(cancellable, interruptIfRunning)) {
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
    if (m_cancelled) {
      cancel(cancellable, true);
      return;
    }

    boolean cancel;
    synchronized (m_cancellationLock) {
      if (m_cancelled) { // double-checked locking
        cancel = true;
      }
      else {
        cancel = false;

        m_registrationLock.writeLock().lock();
        try {
          if (!m_cancellables.contains(cancellable)) {
            m_cancellables.add(cancellable);
          }
        }
        finally {
          m_registrationLock.writeLock().unlock();
        }
      }
    }

    // Cancel the Cancellable outside the lock.
    if (cancel) {
      cancel(cancellable, true);
    }
  }

  /**
   * Unregisters the given {@link ICancellable}.
   */
  public void unregisterCancellable(final ICancellable cancellable) {
    m_registrationLock.writeLock().lock();
    try {
      m_cancellables.remove(cancellable);
    }
    finally {
      m_registrationLock.writeLock().unlock();
    }
  }

  protected List<ICancellable> getCancellables() {
    m_registrationLock.readLock().lock();
    try {
      return new ArrayList<>(m_cancellables);
    }
    finally {
      m_registrationLock.readLock().unlock();
    }
  }

  protected boolean cancel(final ICancellable cancellable, final boolean interruptIfRunning) {
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
