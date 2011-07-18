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
package org.eclipse.scout.rt.ui.swing.window.desktop;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.rt.client.ClientJob;
import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;
import org.eclipse.scout.rt.ui.swing.window.desktop.status.SwingProgressMonitor;
import org.eclipse.scout.rt.ui.swing.window.desktop.status.SwingProgressProvider;

/**
 * Base class dealing with SwingProgressProvider events.
 */
public class ProgressHandler {

  private ISwingEnvironment m_env;

  private IProgressMonitor m_activeProgressMonitor;
  private String m_activeTaskName;
  private String m_activeSubTaskName;
  private double m_activeWorked;

  public ProgressHandler(ISwingEnvironment env) {
    m_env = env;
    initialize();
  }

  private void initialize() {
    Job.getJobManager().setProgressProvider(new SwingProgressProvider() {
      private Object dataLock = new Object();

      @Override
      protected void activeMonitorChanged(SwingProgressMonitor monitor) {
        boolean handle = false;
        synchronized (dataLock) {
          if (monitor == null) {
            m_activeProgressMonitor = null;
            m_activeTaskName = null;
            m_activeSubTaskName = null;
            m_activeWorked = 0;
            handle = true;
          }
          else if (monitor != m_activeProgressMonitor) {
            m_activeProgressMonitor = monitor;
            m_activeTaskName = monitor.getTaskName();
            m_activeSubTaskName = monitor.getSubTaskName();
            m_activeWorked = monitor.getWorked();
            handle = true;
          }
          else if (!CompareUtility.equals(m_activeTaskName, monitor.getTaskName()) ||
                   !CompareUtility.equals(m_activeSubTaskName, monitor.getSubTaskName()) ||
                   !CompareUtility.equals((int) (m_activeWorked * 100), (int) (monitor.getWorked() * 100))) {
            m_activeProgressMonitor = monitor;
            m_activeTaskName = monitor.getTaskName();
            m_activeSubTaskName = monitor.getSubTaskName();
            m_activeWorked = monitor.getWorked();
            handle = true;
          }
        }
        if (handle) {
          Runnable r = new Runnable() {
            @Override
            public void run() {
              handleProgressChangeInSwingThread();
            }
          };
          m_env.postImmediateSwingJob(r);
          SwingUtilities.invokeLater(r);
        }
      }
    });
  }

  protected void handleProgressChangeInSwingThread() {
    // nop
  }

  public AbstractAction createStopAction() {
    return new P_SwingStopButtonAction();
  }

  public boolean hasProgressMonitor() {
    return m_activeProgressMonitor != null;
  }

  public double getActiveWorked() {
    return m_activeWorked;
  }

  public String getTaskName() {
    return StringUtility.concatenateTokens(m_activeTaskName, " - ", m_activeSubTaskName);
  }

  /**
   * Listener and Timer Implementation
   */
  private class P_SwingStopButtonAction extends AbstractAction {
    private static final long serialVersionUID = 1L;

    @Override
    public void actionPerformed(ActionEvent a) {
      Job[] jobs = Job.getJobManager().find(ClientJob.class);
      if (jobs != null) {
        for (Job j : jobs) {
          try {
            j.cancel();
          }
          catch (Throwable t) {
            //nop
          }
        }
      }
    }
  }// end class
}
