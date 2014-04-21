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
package org.eclipse.scout.rt.ui.swing.ext.job;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.LinkedList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.core.runtime.jobs.ProgressProvider;
import org.eclipse.scout.commons.beans.IPropertyObserver;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientJob;

/**
 * Job observer.
 * Whenever a registered monitor or its properties changes, the property {@link #PROP_MONITOR_PROPERTIES} is changed.
 * <p>
 * This leads to the behavior that always the most recently changed monitor is displayed as active monitor.
 */
public class SwingProgressProvider extends ProgressProvider implements IPropertyObserver {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwingProgressProvider.class);

  /**
   * @deprecated replaced by {@link #PROP_MONITOR_PROPERTIES}. Will be removed in the 5.0 Release.
   */
  @Deprecated
  public static final String PROP_ACTIVE_MONITOR = "activeMonitor";

  /**
   * Key for property to indicate that {@link MonitorProperties} have changed.
   */
  public static final String PROP_MONITOR_PROPERTIES = "monitorProps";

  private final Object m_listLock;
  private final LinkedList<SwingProgressMonitor> m_list;
  private final PropertyChangeSupport m_propertySupport;
  private final PropertyChangeListener m_jobListener;
  private SwingProgressMonitor m_activeMonitor;
  private MonitorProperties m_monitorProps;

  public SwingProgressProvider() {
    m_listLock = new Object();
    m_list = new LinkedList<SwingProgressMonitor>();
    m_propertySupport = new PropertyChangeSupport(this);
    m_jobListener = new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent e) {
        setActiveMonitor((SwingProgressMonitor) e.getSource());
      }
    };
  }

  @Override
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    m_propertySupport.addPropertyChangeListener(listener);
  }

  @Override
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    m_propertySupport.removePropertyChangeListener(listener);
  }

  @Override
  public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    m_propertySupport.addPropertyChangeListener(propertyName, listener);
  }

  @Override
  public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    m_propertySupport.removePropertyChangeListener(propertyName, listener);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public IProgressMonitor createMonitor(Job job) {
    if (!accept(job)) {
      return getDefaultMonitor();
    }
    if (LOG.isDebugEnabled()) {
      LOG.debug("new " + (job.isSystem() ? "system  " : job.isUser() ? "user    " : "default ") + "Job: " + job);
    }
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

  private boolean accept(Job job) {
    if (job.isSystem()) {
      return false;
    }
    if (job instanceof ClientJob && ((ClientJob) job).isSync()) {
      return false;
    }
    return true;
  }

  private void addInternal(SwingProgressMonitor monitor) {
    synchronized (m_listLock) {
      m_list.add(monitor);
    }
    monitor.addPropertyChangeListener(m_jobListener);
    setActiveMonitor(monitor);
  }

  private void removeInternal(SwingProgressMonitor monitor) {
    monitor.removePropertyChangeListener(m_jobListener);
    SwingProgressMonitor next;
    synchronized (m_listLock) {
      m_list.remove(monitor);
      next = (m_list.size() > 0 ? m_list.get(m_list.size() - 1) : null);
    }
    setActiveMonitor(next);
  }

  /**
   * This method fires when one of the properties of the active monitor have changed (worked, task-name, sub-task-name).
   * 
   * @param newMonitor
   */
  final synchronized void setActiveMonitor(SwingProgressMonitor newMonitor) {
    fireActiveMonitor(newMonitor);
    fireMonitorProps(newMonitor);
  }

  private void fireActiveMonitor(SwingProgressMonitor newMonitor) {
    SwingProgressMonitor oldMonitor = m_activeMonitor;
    m_activeMonitor = newMonitor;
    m_propertySupport.firePropertyChange(PROP_ACTIVE_MONITOR, oldMonitor, newMonitor);
  }

  private void fireMonitorProps(SwingProgressMonitor newMonitor) {
    MonitorProperties oldProps = m_monitorProps;
    MonitorProperties newProps = null;
    if (newMonitor != null) {
      newProps = newMonitor.createMonitorProperties();
    }
    m_monitorProps = newProps;
    m_propertySupport.firePropertyChange(PROP_MONITOR_PROPERTIES, oldProps, newProps);
  }

  public SwingProgressMonitor getActiveMonitor() {
    return m_activeMonitor;
  }
}
