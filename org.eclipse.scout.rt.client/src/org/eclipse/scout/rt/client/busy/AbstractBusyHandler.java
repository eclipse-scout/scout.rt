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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientJob;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.IClientSession;

/**
 * <p>
 * Shows blocking progress when {@link ClientSyncJob} or {@link ClientJob} with {@link ClientJob#isSync()} are doing a
 * long operation.
 * </p>
 * <p>
 * The decision whether or not the progress should be visible is made in the acceptor
 * {@link AbstractBusyHandler#acceptJob(Job)}
 * </p>
 * <p>
 * The strategy to display busy and blocking progress can be changed by overriding {@link #runBusy(Object)} and
 * {@link #runBusy(IRunnableWithProgress)}.
 * </p>
 * Implementations are ui specific an can be found in the swing, swt, rwt implementation sof scout.
 * <p>
 * This abstract implementation is Thread-safe.
 * 
 * @author imo
 * @since 3.8
 */
public abstract class AbstractBusyHandler implements IBusyHandler {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractBusyHandler.class);
  private static final QualifiedName TIMER_PROPERTY = new QualifiedName(AbstractBusyHandler.class.getName(), "timer");
  private static final QualifiedName BUSY_OPERATION_PROPERTY = new QualifiedName(AbstractBusyHandler.class.getName(), "busy");

  private final IClientSession m_session;
  private final Object m_stateLock = new Object();
  private final Set<Job> m_list = Collections.synchronizedSet(new HashSet<Job>());
  private long m_shortOperationMillis = 200L;
  private long m_longOperationMillis = 3000L;

  public AbstractBusyHandler(IClientSession session) {
    m_session = session;
  }

  @Override
  public boolean acceptJob(final Job job) {
    if (job == null) {
      return false;
    }
    if (job instanceof ClientJob && ((ClientJob) job).isSync()) {
      return true;
    }
    return false;
  }

  @Override
  public void onJobBegin(Job job) {
    addTimer(job);
  }

  @Override
  public void onJobEnd(Job job) {
    removeTimer(job);
    //avoid unnecessary locks
    if (isBusyOperationNoLock(job)) {
      removeBusyOperation(job);
    }
  }

  @Override
  public final Object getStateLock() {
    return m_stateLock;
  }

  @Override
  public void cancel() {
    synchronized (getStateLock()) {
      for (Job job : m_list) {
        try {
          job.cancel();
        }
        catch (Throwable t) {
          //nop
        }
      }
    }
  }

  /**
   * This method is called directly from the job listener after {@link #getShortOperationMillis()}.
   * <p>
   * You may call {@link #runDefaultBusy(Object, IProgressMonitor)} to use default handling.
   * <p>
   * {@link #getStateLock()} can be used synchronized to check {@link #isBusy()}
   * <p>
   * Be careful what to do, since this might be expensive. The default starts a {@link BusyJob} or a subclass of the
   * {@link BusyJob}
   */
  protected abstract void runBusy();

  /**
   * @retrun true if blocking is active
   */
  @Override
  public final boolean isBusy() {
    return m_list.size() > 0;
  }

  @Override
  public long getShortOperationMillis() {
    return m_shortOperationMillis;
  }

  public void setShortOperationMillis(long shortOperationMillis) {
    m_shortOperationMillis = shortOperationMillis;
  }

  @Override
  public long getLongOperationMillis() {
    return m_longOperationMillis;
  }

  public void setLongOperationMillis(long longOperationMillis) {
    m_longOperationMillis = longOperationMillis;
  }

  private void addTimer(Job job) {
    P_TimerJob t = new P_TimerJob(job);
    job.setProperty(TIMER_PROPERTY, t);
    t.schedule(getShortOperationMillis());
  }

  private void removeTimer(Job job) {
    P_TimerJob t = (P_TimerJob) job.getProperty(TIMER_PROPERTY);
    if (t != null) {
      t.cancel();
      job.setProperty(TIMER_PROPERTY, null);
    }
  }

  private P_TimerJob getTimer(Job job) {
    return (P_TimerJob) job.getProperty(TIMER_PROPERTY);
  }

  private void addBusyOperation(Job job) {
    int oldSize, newSize;
    synchronized (getStateLock()) {
      job.setProperty(BUSY_OPERATION_PROPERTY, "true");
      oldSize = m_list.size();
      m_list.add(job);
      newSize = m_list.size();
      getStateLock().notifyAll();
    }
    if (oldSize == 0 && newSize == 1) {
      runBusy();
    }
  }

  private void removeBusyOperation(Job job) {
    synchronized (getStateLock()) {
      job.setProperty(BUSY_OPERATION_PROPERTY, null);
      m_list.remove(job);
      getStateLock().notifyAll();
    }
  }

  private boolean isBusyOperationNoLock(Job job) {
    return "true".equals(job.getProperty(BUSY_OPERATION_PROPERTY));
  }

  private static boolean isJobActive(final Job job) {
    if (job.getState() != Job.RUNNING) {
      return false;
    }
    if (job instanceof ClientJob && ((ClientJob) job).isWaitFor()) {
      return false;
    }
    return true;
  }

  private class P_TimerJob extends Job {
    private final Job m_job;

    public P_TimerJob(Job job) {
      super("TimerJob");
      setSystem(true);
      m_job = job;
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
      if (P_TimerJob.this != getTimer(m_job)) {
        return Status.OK_STATUS;
      }
      removeTimer(m_job);
      if (isJobActive(m_job)) {
        addBusyOperation(m_job);
      }
      //double check after queuing (avoid unnecessary locks)
      if (!isJobActive(m_job)) {
        removeBusyOperation(m_job);
      }
      return Status.OK_STATUS;
    }
  }

}
