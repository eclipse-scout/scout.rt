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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.rt.shared.ScoutTexts;

/**
 * Job operating on a {@link IClientSession} Job may be sync or async. Sync jobs
 * use the {@link ClientRule}, async don't. Be careful when using async jobs and
 * use it only read-only. When using a {@link BlockingCondition} to wait on, the
 * lock is released during the blocking wait and re-acquired after. see {@link ClientSyncJob} and {@link ClientAsyncJob}
 */
public class ClientJob extends JobEx implements IClientSessionProvider {
  private final IClientSession m_session;
  private final EventListenerList m_listeners;
  private final Object m_waitForLock;
  private boolean m_waitFor;

  /**
   * @param name
   *          job name
   * @param session
   *          {@link IClientSession} in which the job is running
   * @param sync
   *          if true then {@link IClientSession#getJobSyncLock()} is acquired
   *          to run the job, if false the job runs without lock (careful,
   *          read-only-mode recommended)
   */
  public ClientJob(String name, IClientSession session, boolean sync) {
    this(name, session, sync, true);
  }

  /**
   * @param name
   *          job name
   * @param session
   *          {@link IClientSession} in which the job is running
   * @param sync
   *          if true then {@link IClientSession#getJobSyncLock()} is acquired
   *          to run the job, if false the job runs without lock (careful,
   *          read-only-mode recommended)
   * @param system
   *          see {@link Job#setSystem(boolean)}
   */
  public ClientJob(String name, IClientSession session, boolean sync, boolean system) {
    super(name);
    if (session == null) throw new IllegalArgumentException("session is null");
    m_session = session;
    m_listeners = new EventListenerList();
    setUser(false);
    setSystem(system);
    setProperty(ScoutTexts.JOB_PROPERTY_NAME, m_session.getNlsTexts());
    m_waitForLock = new Object();
    if (sync) {
      setRule(new ClientRule(session));
    }
  }

  public IClientSession getClientSession() {
    return m_session;
  }

  /**
   * {@link ClientJob}s belong to the family of type {@link ClientJob}.class
   */
  @Override
  public boolean belongsTo(Object family) {
    if (family == ClientJob.class) {
      return true;
    }
    return false;
  }

  /**
   * @return whether the job is running with the {@link ClientRule} and
   *         therefore runs in sequence with other jobs on the same session
   */
  public final boolean isSync() {
    return getRule() instanceof ClientRule;
  }

  /**
   * Set whether the job is running with the {@link ClientRule} and therefore
   * runs in sequence with other jobs on the same session. This method can only
   * be called before the job is scheduled
   */
  public final void setSync(boolean sync) {
    if (sync != isSync()) {
      if (getState() != NONE) throw new IllegalStateException("sync property cannot be changed once the job is scheduled");
      //
      if (sync) {
        setRule(new ClientRule(m_session));
      }
      else {
        setRule(null);
      }
    }
  }

  public final boolean isWaitFor() {
    return m_waitFor;
  }

  @Override
  public boolean shouldSchedule() {
    if (getClientSession() != null && getClientSession().isWebSession()) {
      runNow(new NullProgressMonitor());
      return false;
    }
    else {
      return super.shouldSchedule();
    }
  }

  @Override
  public final IStatus runNow(IProgressMonitor monitor) {
    IStatus status = null;
    try {
      ClientSessionThreadLocal.set(getClientSession());
      status = super.runNow(monitor);
    }
    finally {
      ClientSessionThreadLocal.set(null);
    }
    return status;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected IStatus run(IProgressMonitor monitor) {
    try {
      runVoid(monitor);
      return Status.OK_STATUS;
    }
    catch (Throwable t) {
      return new Status(Status.ERROR, "<none>", 0, t.getMessage(), t);
    }
  }

  /**
   * Convenience that throws exceptions instead of returning a status
   */
  protected void runVoid(IProgressMonitor monitor) throws Throwable {
  }

  /**
   * Waits for the condition and blocks the execution of the current job.
   * Notifies job listeners with {@link IJobChangeListenerEx#blockingConditionStart(IJobChangeEvent)} and
   * {@link IJobChangeListenerEx#blockingConditionEnd(IJobChangeEvent)} about
   * the blocking condition.
   */
  void waitFor() throws InterruptedException {
    final ClientRule rule = (getRule() instanceof ClientRule ? (ClientRule) getRule() : null);
    //
    if (rule != null) {
      rule.setEnabled(false);
      rescheduleWaitingSyncJobs();
    }
    m_waitFor = true;
    fireBlockingConditionStart();
    scheduleDummyJob();
    synchronized (m_waitForLock) {
      m_waitForLock.wait();
    }
    m_waitFor = false;
    fireBlockingConditionEnd();
    scheduleDummyJob();
    // continue work
  }

  void releaseWaitFor() throws InterruptedException {
    final ClientRule rule = (getRule() instanceof ClientRule ? (ClientRule) getRule() : null);
    if (rule != null) {
      ClientSyncJob proxyJob = new ClientSyncJob("release waitFor lock on \"" + this + "\"", m_session) {
        @Override
        protected void runVoid(IProgressMonitor arg0) throws Throwable {
          rule.setEnabled(true);
          rescheduleWaitingSyncJobs();
          synchronized (m_waitForLock) {
            m_waitForLock.notifyAll();
          }
        }
      };
      proxyJob.schedule();
    }
  }

  private void rescheduleWaitingSyncJobs() {
    ArrayList<Job> jobList = new ArrayList<Job>();
    for (Job j : Job.getJobManager().find(ClientJob.class)) {
      if (j instanceof ClientJob) {
        ClientJob c = (ClientJob) j;
        if (c.isSync() && !c.isWaitFor()) {
          if (c.getState() == Job.WAITING) {
            jobList.add(j);
          }
        }
      }
    }
    for (int i = jobList.size() - 1; i >= 0; i--) {
      Job j = jobList.get(i);
      if (!j.sleep()) {
        jobList.remove(i);
      }
    }
    for (Job j : jobList) {
      j.wakeUp();
    }
  }

  /**
   * schedule a dummy job to support for busy handling with job manager listener
   */
  private void scheduleDummyJob() {
    new ClientAsyncJob("Dummy", m_session).schedule();
  }

  public final void addJobChangeListenerEx(IJobChangeListenerEx listener) {
    addJobChangeListener(listener);
    m_listeners.add(IJobChangeListenerEx.class, listener);
  }

  public final void removeJobChangeListenerEx(IJobChangeListenerEx listener) {
    removeJobChangeListener(listener);
    m_listeners.remove(IJobChangeListenerEx.class, listener);
  }

  private void fireBlockingConditionStart() {
    JobChangeEventEx e = new JobChangeEventEx(this);
    for (IJobChangeListenerEx listener : m_listeners.getListeners(IJobChangeListenerEx.class)) {
      try {
        listener.blockingConditionStart(e);
      }
      catch (Throwable t) {
        t.printStackTrace();
      }
    }
  }

  private void fireBlockingConditionEnd() {
    JobChangeEventEx e = new JobChangeEventEx(this);
    for (IJobChangeListenerEx listener : m_listeners.getListeners(IJobChangeListenerEx.class)) {
      try {
        listener.blockingConditionEnd(e);
      }
      catch (Throwable t) {
        t.printStackTrace();
      }
    }
  }

  private static class JobChangeEventEx implements IJobChangeEvent {
    Job job = null;

    public JobChangeEventEx(Job job) {
      this.job = job;
    }

    public long getDelay() {
      return 0L;
    }

    public Job getJob() {
      return job;
    }

    public IStatus getResult() {
      return null;
    }
  }

  /**
   * @return {@link IClientSession} if the current job is a {@link IClientSessionProvider}
   */
  public static final IClientSession getCurrentSession() {
    return getCurrentSession(IClientSession.class);
  }

  /**
   * @return true if {@link IJobManager#currentJob()} is a {@link ClientJob} and {@link ClientJob#isSync()} is true
   */
  public static final boolean isSyncClientJob() {
    Job j = Job.getJobManager().currentJob();
    return (j instanceof ClientJob) && ((ClientJob) j).isSync();
  }

  /**
   * @return {@link IClientSession} if the current job is a {@link IClientSessionProvider}
   */
  @SuppressWarnings("unchecked")
  public static final <T extends IClientSession> T getCurrentSession(Class<T> type) {
    Job job = getJobManager().currentJob();
    if (job instanceof IClientSessionProvider) {
      IClientSession s = ((IClientSessionProvider) job).getClientSession();
      if (s != null && type.isAssignableFrom(s.getClass())) {
        return (T) s;
      }
    }
    return (T) ClientSessionThreadLocal.get();
  }

}
