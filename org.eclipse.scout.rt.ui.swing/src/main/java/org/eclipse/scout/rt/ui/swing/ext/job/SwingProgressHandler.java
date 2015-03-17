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
import java.util.EventListener;

import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;

import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.filter.AlwaysFilter;
import org.eclipse.scout.commons.job.IFuture;
import org.eclipse.scout.commons.job.IJobChangeEvent;
import org.eclipse.scout.commons.job.IJobChangeListener;
import org.eclipse.scout.commons.job.internal.JobChangeEvent;
import org.eclipse.scout.commons.job.internal.JobChangeListeners;
import org.eclipse.scout.rt.client.job.IClientJobManager;
import org.eclipse.scout.rt.client.job.IModelJobManager;
import org.eclipse.scout.rt.client.job.ModelJobFilter;
import org.eclipse.scout.rt.platform.OBJ;

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
  private final IJobChangeListener m_modelJobsListener;
  private volatile boolean m_jobRunning;
  private MonitorProperties m_monitorProps;

  protected SwingProgressHandler() {
    m_modelJobsListener = new IJobChangeListener() {
      @Override
      public void jobChanged(final IJobChangeEvent event) {
        if (event.getMode() == JobChangeEvent.EVENT_MODE_ASYNC) {
          if (event.getType() == JobChangeEvent.EVENT_TYPE_ABOUT_TO_RUN) {
            SwingUtilities.invokeLater(new Runnable() {
              @Override
              public void run() {
                propertyChangeInSwing(event.getFuture());
              }
            });
          }
          else if (event.getType() == JobChangeEvent.EVENT_TYPE_DONE) {
            SwingUtilities.invokeLater(new Runnable() {
              @Override
              public void run() {
                propertyChangeInSwing(null);
              }
            });
          }
        }
      }
    };

    JobChangeListeners.DEFAULT.add(m_modelJobsListener, ModelJobFilter.INSTANCE);
  }

  private void dispose() {
    JobChangeListeners.DEFAULT.remove(m_modelJobsListener, ModelJobFilter.INSTANCE);
  }

  private void propertyChangeInSwing(IFuture<?> future) {
    if (future == null) {
      m_jobRunning = false;
      m_monitorProps = MonitorProperties.NULL_INSTANCE;
    }
    else {
      m_jobRunning = true;
      m_monitorProps = new MonitorProperties(0, future.getJobInput().getIdentifier(), ""); //TODO: more progress required?
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
    return m_monitorProps.getWorked();
  }

  /**
   * Returns the task name of the active job.
   *
   * @since 3.10.0-M3.
   *        In earlier versions this method returned {@link #getJobFullName()}. This has been changed, since in earlier
   *        versions it was not possible to access "task" and "sub task" separately.
   * @return task name
   */
  public String getJobTaskName() {
    return m_monitorProps.getTaskName();
  }

  /**
   * Returns the sub task name of the active job.
   *
   * @return sub task name
   */
  public String getJobSubTaskName() {
    return m_monitorProps.getSubTaskName();
  }

  /**
   * Returns a concatenated string with "[task-name] - [subtask-name]" of the active job.
   *
   * @return full name
   */
  public String getJobFullName() {
    return StringUtility.concatenateTokens(getJobTaskName(), " - ", getJobSubTaskName());
  }

  /**
   * Listener and Timer Implementation
   */
  public static class CancelJobsAction extends AbstractAction {
    private static final long serialVersionUID = 1L;

    @Override
    public void actionPerformed(ActionEvent a) {
      OBJ.get(IClientJobManager.class).cancel(new AlwaysFilter<IFuture<?>>(), true);
      OBJ.get(IModelJobManager.class).cancel(new AlwaysFilter<IFuture<?>>(), true);
    }
  }// end class

  public interface IStateChangeListener extends EventListener {
    /**
     * This event is sent whenever the state of the handler changes.
     * The call is always in the ui thread with {@link SwingUtilities#isEventDispatchThread()}=true
     */
    void stateChanged(SwingProgressHandler h);
  }
}
