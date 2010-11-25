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
package org.eclipse.scout.rt.server.scheduler;

import java.util.Collection;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

/**
 * This accessor can be used to give individual sessions limited access to a
 * global {@link IScheduler} via their {@link com.bsiag.service.scheduler.ISchedulerService} This implementation is
 * a transparent proxy. Override the methods that are subject to access control
 * restrictions
 */
public class SchedulerAccessor implements IScheduler {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SchedulerAccessor.class);

  private IScheduler m_scheduler;

  public SchedulerAccessor(IScheduler s) {
    m_scheduler = s;
  }

  public void addJob(ISchedulerJob newJob) {
    m_scheduler.addJob(newJob);
  }

  public Collection<ISchedulerJob> getAllJobs() {
    return m_scheduler.getAllJobs();
  }

  public Collection<ISchedulerJob> getAllRunningJobs() {
    return m_scheduler.getAllRunningJobs();
  }

  public ISchedulerJob getJob(String jobId) {
    return m_scheduler.getJob(jobId);
  }

  public int getJobCount() {
    return m_scheduler.getJobCount();
  }

  public Collection<ISchedulerJob> getJobs(String groupId, String jobId) {
    return m_scheduler.getJobs(groupId, jobId);
  }

  public int getRunningJobCount() {
    return m_scheduler.getRunningJobCount();
  }

  public Collection<ISchedulerJob> getRunningJobs(String groupId, String jobId) {
    return m_scheduler.getRunningJobs(groupId, jobId);
  }

  public Ticker getTicker() {
    return m_scheduler.getTicker();
  }

  public void interruptAllJobs() {
    m_scheduler.interruptAllJobs();
  }

  public Collection<ISchedulerJob> interruptJobs(String groupId, String jobId) {
    return m_scheduler.interruptJobs(groupId, jobId);
  }

  public boolean isActive() {
    return m_scheduler.isActive();
  }

  public void removeAllJobs() {
    m_scheduler.removeAllJobs();
  }

  public Collection<ISchedulerJob> removeJobs(String groupId, String jobId) {
    return m_scheduler.removeJobs(groupId, jobId);
  }

  public void setActive(boolean b) {
    m_scheduler.setActive(b);
  }

  public void handleJobExecution(ISchedulerJob job, TickSignal signal) {
    // nop, is never called on an access wrapper
  }

  public void start() {
    m_scheduler.start();
  }

  public void stop() {
    m_scheduler.stop();
  }

}
