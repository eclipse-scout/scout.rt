/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.busy;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

/**
 * This is the default busy job that runs the process of showing busy marker, blocking and canceling.
 * <p>
 * Subclass this job for custom handling.
 * <p>
 * The {@link #run(IProgressMonitor)} first calls {@link #runBusy(IProgressMonitor)} and then calls
 * {@link #runBlocking(IProgressMonitor)}
 * 
 * @author imo
 * @since 3.8
 */
public class BusyJob extends Job {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(BusyJob.class);

  private final IBusyHandler m_handler;
  private boolean m_cancelApplied;

  public BusyJob(String name, IBusyHandler handler) {
    super(name);
    m_handler = handler;
  }

  protected IBusyHandler getBusyHandler() {
    return m_handler;
  }

  @Override
  protected IStatus run(IProgressMonitor monitor) {
    if (!getBusyHandler().isBusy() || !getBusyHandler().isEnabled()) {
      return Status.OK_STATUS;
    }
    runBusy(monitor);
    if (!getBusyHandler().isBusy() || !getBusyHandler().isEnabled()) {
      return Status.OK_STATUS;
    }
    runBlocking(monitor);
    return Status.OK_STATUS;
  }

  protected void runBusy(IProgressMonitor monitor) {
    IBusyHandler h = getBusyHandler();
    Object lock = h.getStateLock();
    long longOpTimestamp = System.currentTimeMillis() + h.getLongOperationMillis() - h.getShortOperationMillis();
    while (true) {
      synchronized (lock) {
        if (!h.isBusy()) {
          return;
        }
        if (!m_cancelApplied && monitor.isCanceled()) {
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

  protected void runBlocking(IProgressMonitor monitor) {
    IBusyHandler h = getBusyHandler();
    Object lock = h.getStateLock();
    try {
      monitor.beginTask(null, IProgressMonitor.UNKNOWN);
      while (true) {
        synchronized (lock) {
          if (!h.isBusy()) {
            return;
          }
          if (!m_cancelApplied && monitor.isCanceled()) {
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
    finally {
      monitor.done();
    }
  }

}
