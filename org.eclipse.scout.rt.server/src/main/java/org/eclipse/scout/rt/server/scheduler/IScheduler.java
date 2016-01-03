/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.scheduler;

import java.util.Collection;

import org.eclipse.scout.rt.platform.job.IJobManager;

/**
 * @deprecated will be removed in release 6.1; use {@link IJobManager} instead, which provides you support for triggered
 *             execution via Quartz schedule plans; see {@link JobInput#withExecutionTrigger(...)};
 */
@SuppressWarnings("deprecation")
@Deprecated
public interface IScheduler {

  boolean isActive();

  void setActive(boolean b);

  Ticker getTicker();

  /**
   * Add a new job to the scheduler. All jobs with matching groupId || jobId are disposed (removed). A null value for
   * groupId or jobId is interpreted as wildcard when no existing matching job is currently running, then this new job
   * is immediately scheduled when ready otherwiese it is just queued and the existing jobs are finished (not
   * interrupted)
   */
  void addJob(ISchedulerJob newJob);

  /**
   * convenience for removeJobs(null,null)
   */
  void removeAllJobs();

  /**
   * @param groupId
   *          filter value or null as wildcard
   * @param jobId
   *          filter value or null as wildcard
   * @return the list of removed jobs
   */
  Collection<ISchedulerJob> removeJobs(String groupId, String jobId);

  /**
   * convenience for interruptJobs(null,null)
   */
  void interruptAllJobs();

  /**
   * @param groupId
   *          filter value or null as wildcard
   * @param jobId
   *          filter value or null as wildcard
   * @return the list of interrupted jobs
   */
  Collection<ISchedulerJob> interruptJobs(String groupId, String jobId);

  int getJobCount();

  int getRunningJobCount();

  /**
   * convenience for getJobs(null,jobId) Note that this will return the first found job with that id even though there
   * might be other jobs with that same id
   */
  ISchedulerJob getJob(String jobId);

  /**
   * convenience for getJobs(null,null)
   */
  Collection<ISchedulerJob> getAllJobs();

  Collection<ISchedulerJob> getJobs(String groupId, String jobId);

  /**
   * convenience for getRunningJobs(null,null)
   */
  Collection<ISchedulerJob> getAllRunningJobs();

  Collection<ISchedulerJob> getRunningJobs(String groupId, String jobId);

  /**
   * Override this filter to control the running of scheduler jobs This method is synchronous and must wait until the
   * call to {@link ISchedulerJob#run(TickSignal)} has returned The default implementation just calls job.run(signal);
   */
  void handleJobExecution(ISchedulerJob job, TickSignal signal);

  /**
   * start scheduler if already running, this method does nothing
   */
  void start();

  /**
   * stop scheduler if not running, this method does nothing else it interrupts all jobs, disposes all jobs and stops
   * scheduler
   */
  void stop();
}
