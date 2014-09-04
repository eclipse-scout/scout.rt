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
package org.eclipse.scout.rt.client;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.desktop.DesktopEvent;
import org.eclipse.scout.rt.client.ui.desktop.DesktopListener;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;

/**
 * <p>
 * Use this object to put the current job into waiting mode until the blocking condition falls. Thereby, multiple
 * threads can wait for the condition, which in turn are released all together. When being released, this condition does
 * not block subsequent threads unless <code>blocking</code> is set to <code>true</code> anew.
 * </p>
 * If the calling thread runs a {@link ClientJob}:
 * <ul>
 * <li>the blocking is delegated to the {@link ClientJob#waitFor()}.</li>
 * <li>waiting threads are released automatically when the desktop is closed.</li>
 * </ul>
 *
 * @see {@link ClientJob}
 */
public class BlockingCondition {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(BlockingCondition.class);

  /**
   * Indicates whether this condition is armed.
   */
  private boolean m_blocking;

  /**
   * Blocked callers that run a {@link ClientJob}.
   */
  private final List<ClientJob> m_blockingJobs;

  /**
   * Creates an instance of the blocking condition which multiple threads can wait for.
   *
   * @param blocking
   *          <code>true</code> to arm this condition and make threads wait when calling {@link #waitFor()}.
   */
  public BlockingCondition(boolean blocking) {
    m_blocking = blocking;
    m_blockingJobs = new ArrayList<ClientJob>();
  }

  /**
   * @return <code>true</code> if this condition is armed.
   */
  public boolean isBlocking() {
    return m_blocking;
  }

  /**
   * Arms this condition to block subsequent calls on {@link #waitFor()}. If being called with <code>false</code> and
   * threads are waiting for the condition, those threads are being released.
   *
   * @param blocking
   *          <code>true</code> to arm this condition or <code>false</code> to turn off the blocking and release waiting
   *          threads.
   */
  public void setBlocking(boolean blocking) {
    if (m_blocking != blocking) {
      if (blocking) {
        synchronized (this) {
          m_blocking = true;
        }
      }
      else {
        release();
      }
    }
  }

  /**
   * Causes the current thread to wait until {@link #release() released} or {@linkplain Thread#interrupt interrupted}.
   *
   * @throws InterruptedException
   */
  public void waitFor() throws InterruptedException {
    final IDesktop desktop = ClientJob.getCurrentSession() != null ? ClientJob.getCurrentSession().getDesktop() : null;

    if (desktop == null) {
      waitForInternal();
    }
    else {
      final DesktopListener desktopListener = new P_DesktopListener();
      desktop.addDesktopListener(desktopListener);
      try {
        waitForInternal();
      }
      finally {
        desktop.removeDesktopListener(desktopListener);
      }
    }
  }

  private void waitForInternal() throws InterruptedException {
    ClientJob clientJob = null;
    synchronized (this) {
      if (m_blocking) {
        Job job = ClientJob.getJobManager().currentJob();
        if (job instanceof ClientJob) {
          clientJob = (ClientJob) job;
          m_blockingJobs.add(clientJob);
        }
        else {
          while (m_blocking) { // Conditional guard against spurious wakeup.
            wait(); // block non-clientJob thread.
          }
          return;
        }
      }
    }
    // Block client job (outside sync)
    if (clientJob != null) {
      clientJob.waitFor();
    }
  }

  /**
   * Invalidates the condition and releases currently waiting threads. This call has no effect if already been
   * invalidated. Subsequent calls to {@link #waitFor()} do not block anymore unless <code>blocking</code> is set
   * to <code>true</code> anew.
   */
  public void release() {
    synchronized (this) {
      if (m_blocking) {
        m_blocking = false;

        // Release waiting threads.
        notifyAll();

        // Release waiting threads that run client jobs.
        for (ClientJob clientJob : m_blockingJobs) {
          try {
            clientJob.releaseWaitFor();
          }
          catch (RuntimeException e) {
            LOG.error(String.format("Failed to release lock on client job [job=%s].", clientJob), e);
          }
        }
        m_blockingJobs.clear();
      }
    }
  }

  private class P_DesktopListener implements DesktopListener {

    @Override
    public void desktopChanged(final DesktopEvent evt) {
      if (evt.getType() == DesktopEvent.TYPE_DESKTOP_CLOSED) {
        // Release waiting threads because the application is about to close.

        // Notice that if being called for multiple waiting threads, during the time of iteration a potential new thread requesting to wait for the condition is released too.
        // But as the application goes down when the desktop is closed, this can be neglected.
        release();
      }
    }
  }
}
