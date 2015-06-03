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
package org.eclipse.scout.rt.platform.context;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.Bean;

/**
 * A <code>RunMonitor</code> provides cancellation support for operations typically running on behalf of a
 * {@link RunContext}. By default, when {@link RunContexts#copyCurrent() copying} the current RunContext to run
 * some operations on behalf, a new {@link RunMonitor} is created and registered as child monitor in the monitor of the
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

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(RunMonitor.class);

  /**
   * The monitor which is currently associated with the current thread; is never <code>null</code> if running within a
   * {@link RunContext} or job.
   */
  public static final ThreadLocal<RunMonitor> CURRENT = new ThreadLocal<>();

  private final Object m_lock = new Object();
  private final Set<ICancellable> m_cancellables = new HashSet<>();
  private volatile boolean m_cancelled;

  @Override
  public boolean isCancelled() {
    return m_cancelled;
  }

  @Override
  public boolean cancel(final boolean interruptIfRunning) {
    Set<ICancellable> cancellablesCopyList;
    synchronized (m_lock) {
      cancellablesCopyList = new HashSet<>(m_cancellables);
      m_cancelled = true;
    }
    boolean success = true;
    for (final ICancellable cancellable : cancellablesCopyList) {
      if (!invokeCancel(cancellable, interruptIfRunning)) {
        success = false;
      }
    }
    return success;
  }

  /**
   * Registers the given {@link ICancellable} to be cancelled once this monitor get cancelled.
   */
  public void registerCancellable(final ICancellable cancellable) {
    synchronized (m_lock) {
      m_cancellables.add(cancellable);
    }
    if (isCancelled()) {
      invokeCancel(cancellable, true);
    }
  }

  /**
   * Unregisters the given {@link ICancellable}.
   */
  public void unregisterCancellable(final ICancellable cancellable) {
    synchronized (m_lock) {
      m_cancellables.remove(cancellable);
    }
  }

  protected Set<ICancellable> getCancellables() {
    return m_cancellables;
  }

  protected boolean invokeCancel(final ICancellable cancellable, final boolean interruptIfRunning) {
    try {
      if (!cancellable.isCancelled()) {
        return cancellable.cancel(interruptIfRunning);
      }
      return true;
    }
    catch (final RuntimeException e) {
      LOG.error(String.format("Cancellation failed [cancellable=%s]", cancellable), e);
      return false;
    }
  }
}
