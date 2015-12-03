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
package org.eclipse.scout.rt.client.busy;

import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;

/**
 * This is the default busy job that runs the process of showing busy marker, blocking and canceling.
 * <p>
 * Subclass this job for custom handling.
 * <p>
 * The {@link #run()} first calls {@link #runBusy()} and then calls {@link #runBlocking()}
 *
 * @author imo
 * @since 3.8
 */
public class BusyJob implements IRunnable {
  private final IBusyHandler m_handler;
  private boolean m_cancelApplied;

  public BusyJob(IBusyHandler handler) {
    m_handler = handler;
  }

  protected IBusyHandler getBusyHandler() {
    return m_handler;
  }

  @Override
  public void run() throws Exception {
    if (!getBusyHandler().isBusy() || !getBusyHandler().isEnabled()) {
      return;
    }
    runBusy();
    if (!getBusyHandler().isBusy() || !getBusyHandler().isEnabled()) {
      return;
    }

    m_handler.onBlockingBegin();
    try {
      runBlocking();
    }
    finally {
      m_handler.onBlockingEnd();
    }
  }

  protected void runBusy() {
    IBusyHandler h = getBusyHandler();
    Object lock = h.getStateLock();
    long longOpTimestamp = System.currentTimeMillis() + h.getLongOperationMillis() - h.getShortOperationMillis();
    while (true) {
      synchronized (lock) {
        if (!h.isBusy()) {
          return;
        }
        if (!m_cancelApplied && RunMonitor.CURRENT.get().isCancelled()) {
          m_cancelApplied = true;
          h.cancel();
        }
        if (System.currentTimeMillis() > longOpTimestamp) {
          return;
        }
        try {
          lock.wait(100);
        }
        catch (InterruptedException e) {
          return;
        }
      }
    }
  }

  protected void runBlocking() {
    IBusyHandler h = getBusyHandler();
    Object lock = h.getStateLock();
    while (true) {
      synchronized (lock) {
        if (!h.isBusy()) {
          return;
        }
        if (!m_cancelApplied && RunMonitor.CURRENT.get().isCancelled()) {
          m_cancelApplied = true;
          h.cancel();
        }
        try {
          lock.wait(100);
        }
        catch (InterruptedException e) {
          return;
        }
      }
    }
  }
}
