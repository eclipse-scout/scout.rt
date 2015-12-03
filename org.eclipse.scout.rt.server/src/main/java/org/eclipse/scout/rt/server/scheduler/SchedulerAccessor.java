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

/**
 * This accessor can be used to give individual sessions limited access to a global {@link IScheduler} via their
 * {@link com.bsiag.service.scheduler.ISchedulerService} This implementation is a transparent proxy. Override the
 * methods that are subject to access control restrictions
 */
public class SchedulerAccessor implements IScheduler {

  private IScheduler m_scheduler;

  public SchedulerAccessor(IScheduler s) {
    m_scheduler = s;
  }

  @Override
  public void addJob(ISchedulerJob newJob) {
    m_scheduler.addJob(newJob);
  }

  @Override
  public Collection<ISchedulerJob> getAllJobs() {
    return m_scheduler.getAllJobs();
  }

  @Override
  public Collection<ISchedulerJob> getAllRunningJobs() {
    return m_scheduler.getAllRunningJobs();
  }

  @Override
  public ISchedulerJob getJob(String jobId) {
    return m_scheduler.getJob(jobId);
  }

  @Override
  public int getJobCount() {
    return m_scheduler.getJobCount();
  }

  @Override
  public Collection<ISchedulerJob> getJobs(String groupId, String jobId) {
    return m_scheduler.getJobs(groupId, jobId);
  }

  @Override
  public int getRunningJobCount() {
    return m_scheduler.getRunningJobCount();
  }

  @Override
  public Collection<ISchedulerJob> getRunningJobs(String groupId, String jobId) {
    return m_scheduler.getRunningJobs(groupId, jobId);
  }

  @Override
  public Ticker getTicker() {
    return m_scheduler.getTicker();
  }

  @Override
  public void interruptAllJobs() {
    m_scheduler.interruptAllJobs();
  }

  @Override
  public Collection<ISchedulerJob> interruptJobs(String groupId, String jobId) {
    return m_scheduler.interruptJobs(groupId, jobId);
  }

  @Override
  public boolean isActive() {
    return m_scheduler.isActive();
  }

  @Override
  public void removeAllJobs() {
    m_scheduler.removeAllJobs();
  }

  @Override
  public Collection<ISchedulerJob> removeJobs(String groupId, String jobId) {
    return m_scheduler.removeJobs(groupId, jobId);
  }

  @Override
  public void setActive(boolean b) {
    m_scheduler.setActive(b);
  }

  @Override
  public void handleJobExecution(ISchedulerJob job, TickSignal signal) {
    // nop, is never called on an access wrapper
  }

  @Override
  public void start() {
    m_scheduler.start();
  }

  @Override
  public void stop() {
    m_scheduler.stop();
  }

}
