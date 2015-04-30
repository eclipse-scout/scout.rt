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

/**
 * Default implementation of a {@link IRunMonitor}
 *
 * @since 5.1
 */
public class RunMonitor implements IRunMonitor {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(RunMonitor.class);

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
      m_cancelled = true;
      cancellablesCopyList = new HashSet<>(m_cancellables);
    }
    boolean success = true;
    for (final ICancellable cancellable : cancellablesCopyList) {
      if (!invokeCancel(cancellable, interruptIfRunning)) {
        success = false;
      }
    }
    return success;
  }

  @Override
  public void registerCancellable(final ICancellable cancellable) {
    synchronized (m_lock) {
      m_cancellables.add(cancellable);
    }
    if (isCancelled()) {
      invokeCancel(cancellable, true);
    }
  }

  @Override
  public void unregisterCancellable(final ICancellable cancellable) {
    synchronized (m_lock) {
      m_cancellables.remove(cancellable);
    }
  }

  protected boolean invokeCancel(final ICancellable c, final boolean interruptIfRunning) {
    try {
      if (!c.isCancelled()) {
        return c.cancel(interruptIfRunning);
      }
      return true;
    }
    catch (final Throwable t) {
      LOG.error("Cancelling " + c, t);
      return false;
    }
  }

  /**
   * Used for testing purpose.
   */
  public boolean isEmpty() {
    return m_cancellables.isEmpty();
  }
}
