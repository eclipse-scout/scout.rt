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

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.EventListener;

import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.StringUtility;

/**
 * handler dealing with SwingProgressProvider events.
 */
public class SwingProgressHandler {
  private static SwingProgressHandler instance;

  public static synchronized void install() {
    if (instance == null) {
      instance = new SwingProgressHandler();
    }
  }

  public static synchronized void uninstall() {
    SwingProgressHandler oldHandler = instance;
    instance = null;
    if (oldHandler != null) {
      oldHandler.dispose();
    }
  }

  public static SwingProgressHandler getInstance() {
    return instance;
  }

  private final EventListenerList m_listenerList = new EventListenerList();
  private boolean m_jobRunning;
  private String m_taskName;
  private String m_subTaskName;
  private double m_worked;

  protected SwingProgressHandler() {
    SwingProgressProvider p = new SwingProgressProvider();
    Job.getJobManager().setProgressProvider(p);
    p.addPropertyChangeListener(SwingProgressProvider.PROP_ACTIVE_MONITOR, new PropertyChangeListener() {
      @Override
      public synchronized void propertyChange(PropertyChangeEvent evt) {
        final SwingProgressMonitor monitor = (SwingProgressMonitor) evt.getNewValue();
        SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run() {
            propertyChangeInSwing(monitor);
          }
        });
      }
    });
  }

  private void dispose() {
    Job.getJobManager().setProgressProvider(null);
  }

  private void propertyChangeInSwing(SwingProgressMonitor monitor) {
    if (monitor == null) {
      m_jobRunning = false;
      m_taskName = null;
      m_subTaskName = null;
      m_worked = 0;
    }
    else {
      m_jobRunning = true;
      m_taskName = monitor.getTaskName();
      m_subTaskName = monitor.getSubTaskName();
      m_worked = monitor.getWorked();
    }
    fireStateChanged();
  }

  /**
   * Add a state change listener whenever {@link #isJobRunning()} changes.
   * <p>
   * The call is done in the swing thread {@link SwingUtilities#isEventDispatchThread()}
   */
  public void addStateChangeListener(IStateChangeListener listener) {
    m_listenerList.add(IStateChangeListener.class, listener);
  }

  /**
   * Remove a state change listener whenever {@link #isJobRunning()} changes.
   * <p>
   * The call is done in the swing thread {@link SwingUtilities#isEventDispatchThread()}
   */
  public void removeStateChangeListener(IStateChangeListener listener) {
    m_listenerList.remove(IStateChangeListener.class, listener);
  }

  private void fireStateChanged() {
    IStateChangeListener[] listeners = m_listenerList.getListeners(IStateChangeListener.class);
    if (listeners == null || listeners.length == 0) {
      return;
    }
    for (IStateChangeListener listener : listeners) {
      listener.stateChanged(this);
    }
  }

  public boolean isJobRunning() {
    return m_jobRunning;
  }

  public double getJobWorked() {
    return m_worked;
  }

  public String getJobTaskName() {
    return StringUtility.concatenateTokens(m_taskName, " - ", m_subTaskName);
  }

  /**
   * Listener and Timer Implementation
   */
  public static class CancelJobsAction extends AbstractAction {
    private static final long serialVersionUID = 1L;

    @Override
    public void actionPerformed(ActionEvent a) {
      Job[] jobs = Job.getJobManager().find(null);
      if (jobs != null) {
        for (Job j : jobs) {
          if (!j.isSystem() && j.getState() == Job.RUNNING) {
            try {
              j.cancel();
            }
            catch (Throwable t) {
              //nop
            }
          }
        }
      }
    }
  }// end class

  public static interface IStateChangeListener extends EventListener {
    /**
     * This event is sent whenever the state of the handler changes.
     * The call is always in the ui thread with {@link SwingUtilities#isEventDispatchThread()}=true
     */
    void stateChanged(SwingProgressHandler h);
  }
}
