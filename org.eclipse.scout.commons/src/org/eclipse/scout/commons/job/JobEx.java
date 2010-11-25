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
package org.eclipse.scout.commons.job;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.scout.commons.exception.ProcessingException;

/**
 * Extended job
 * <ul>
 * <li>access to canceled property</li>
 * <li>access to progress monitor</li>
 * <li>run convenience methods</li>
 * </ul>
 */
public abstract class JobEx extends Job {
  private IStatus m_statusOfRunNow;

  public JobEx(String name) {
    super(name);
  }

  @Override
  public final boolean shouldRun() {
    m_statusOfRunNow = null;
    return super.shouldRun();
  }

  /**
   * Some times it is useful to run a job "manually", for example when the job
   * can be run immediately.
   */
  public IStatus runNow(IProgressMonitor monitor) {
    m_statusOfRunNow = null;
    m_statusOfRunNow = run(monitor);
    return m_statusOfRunNow;
  }

  /**
   * Convenience for evaluating the result and throwing an exception if the job
   * result contains an exception
   */
  public void throwOnError() throws ProcessingException {
    IStatus status = getResult();
    if (status == null) {
      status = m_statusOfRunNow;
    }
    if (status != null && status.getException() != null) {
      Throwable t = status.getException();
      if (t instanceof ProcessingException) {
        throw (ProcessingException) t;
      }
      else {
        throw new ProcessingException("Unexpected", t);
      }
    }
  }

  public IProgressMonitor getMonitor() {
    IProgressMonitor monitor = null;
    if (getState() == Job.RUNNING) {
      try {
        Method m = Job.class.getSuperclass().getDeclaredMethod("getProgressMonitor");
        m.setAccessible(true);
        Object result = m.invoke(this, new Object[0]);
        if (result instanceof IProgressMonitor) {
          monitor = (IProgressMonitor) result;
        }
      }
      catch (Throwable t) {
        // nop
      }
    }
    return monitor;
  }

  /**
   * @return {@link #isCanceled()} property iff the current job is of instance {@link ClientJob}
   */
  public static boolean isCurrentJobCanceled() {
    Job job = getJobManager().currentJob();
    if (job instanceof JobEx) {
      IProgressMonitor monitor = ((JobEx) job).getMonitor();
      if (monitor != null) {
        return monitor.isCanceled();
      }
    }
    return false;
  }

  /**
   * Similar to {@link #join()} but with the difference that it waits at most the specified time.
   * <p>
   * A value of &lt;= 0 is equivalent to calling {@link #join()}.
   * 
   * @throws InterruptedException
   */
  public final void join(final long millis) throws InterruptedException {
    if (millis <= 0) {
      join();
    }
    else {
      int state = getState();
      if (state == Job.NONE) {
        return;
      }
      //it's an error for a job to join itself
      if (state == Job.RUNNING && getThread() == Thread.currentThread()) {
        throw new IllegalStateException("Job attempted to join itself");
      }
      final AtomicBoolean jobDoneLock = new AtomicBoolean();
      JobChangeAdapter listener = new JobChangeAdapter() {
        public void done(IJobChangeEvent event) {
          synchronized (jobDoneLock) {
            jobDoneLock.set(true);
            jobDoneLock.notifyAll();
          }
        }
      };
      try {
        addJobChangeListener(listener);
        //
        if (getState() == Job.NONE) {
          return;
        }
        long endTime = System.currentTimeMillis() + millis;
        synchronized (jobDoneLock) {
          while (!jobDoneLock.get()) {
            long dt = endTime - System.currentTimeMillis();
            if (dt <= 0) {
              return;
            }
            try {
              jobDoneLock.wait(dt);
            }
            catch (InterruptedException e) {
              throw e;
            }
          }
        }
      }
      finally {
        removeJobChangeListener(listener);
      }
    }
  }
}
