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
package org.eclipse.scout.rt.ui.swing.window.desktop.status;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;

import javax.swing.SwingUtilities;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.core.runtime.jobs.ProgressProvider;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

public class SwingProgressProvider extends ProgressProvider implements PropertyChangeListener {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwingProgressProvider.class);

  private Object m_listLock;
  private LinkedList<SwingProgressMonitor> m_list;

  public SwingProgressProvider() {
    m_listLock = new Object();
    m_list = new LinkedList<SwingProgressMonitor>();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public IProgressMonitor createMonitor(Job job) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("new " + (job.isSystem() ? "system  " : job.isUser() ? "user    " : "default ") + "Job: " + job);
    }
    if (job.isSystem()) {
      return new NullProgressMonitor();
    }
    //
    final SwingProgressMonitor monitor = new SwingProgressMonitor();
    job.addJobChangeListener(new JobChangeAdapter() {
      @Override
      public void running(IJobChangeEvent event) {
        addInternal(monitor);
      }

      @Override
      public void done(IJobChangeEvent event) {
        removeInternal(monitor);
      }
    });
    return monitor;
  }

  private void addInternal(SwingProgressMonitor monitor) {
    synchronized (m_listLock) {
      m_list.add(0, monitor);
    }
    monitor.addPropertyChangeListener(this);
    activeMonitorChanged(monitor);
  }

  private void removeInternal(SwingProgressMonitor monitor) {
    monitor.removePropertyChangeListener(this);
    SwingProgressMonitor next;
    synchronized (m_listLock) {
      m_list.remove(monitor);
      next = (m_list.size() > 0 ? m_list.get(0) : null);
    }
    if (next != null) {
      activeMonitorChanged(next);
    }
    else {
      //delay by 100ms to make sure no further jobs arise
      Job j = new Job("ProgressProvider, double-check") {
        @Override
        protected IStatus run(IProgressMonitor m) {
          SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
              int count;
              synchronized (m_listLock) {
                count = m_list.size();
              }
              if (count == 0) {
                //still empty
                activeMonitorChanged(null);
              }
            }
          });
          return Status.OK_STATUS;
        }
      };
      j.setSystem(true);
      j.schedule(100);
    }
  }

  /**
   * Internal method used as property observer for progress monitors
   */
  @Override
  public final void propertyChange(PropertyChangeEvent e) {
    activeMonitorChanged((SwingProgressMonitor) e.getSource());
  }

  /**
   * Local observer, override in subclasses. Whenever a registered monitor
   * changes, this method is called. This leads to the behaviour that always the
   * just recently changed monitor is displayed as active monitor.
   */
  protected void activeMonitorChanged(SwingProgressMonitor monitor) {
  }

}
