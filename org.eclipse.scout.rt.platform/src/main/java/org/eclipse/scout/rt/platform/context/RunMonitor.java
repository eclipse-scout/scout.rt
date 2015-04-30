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
 * @since 5.0
 */
public class RunMonitor implements IRunMonitor {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(RunMonitor.class);

  private final Object m_lock = new Object();
  private final HashSet<ICancellable> m_set = new HashSet<>();
  private boolean m_cancelled;

  @Override
  public boolean isCancelled() {
    return m_cancelled;
  }

  @Override
  public boolean cancel(boolean interruptIfRunning) {
    Set<ICancellable> tmp;
    synchronized (m_lock) {
      m_cancelled = true;
      tmp = new HashSet<>(m_set);
    }
    boolean success = true;
    for (ICancellable c : tmp) {
      if (!invokeCancel(c, interruptIfRunning)) {
        success = false;
      }
    }
    return success;
  }

  @Override
  public void registerCancellable(ICancellable c) {
    synchronized (m_lock) {
      m_set.add(c);
    }
    if (isCancelled()) {
      invokeCancel(c, true);
    }
  }

  @Override
  public void unregisterCancellable(ICancellable c) {
    synchronized (m_lock) {
      m_set.remove(c);
    }
  }

  protected boolean invokeCancel(ICancellable c, boolean interruptIfRunning) {
    try {
      if (!c.isCancelled()) {
        return c.cancel(interruptIfRunning);
      }
      return true;
    }
    catch (Throwable t) {
      LOG.error("Cancelling " + c, t);
      return false;
    }
  }
}
