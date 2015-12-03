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

import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractSchedulerJob implements ISchedulerJob {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractSchedulerJob.class);

  private final String m_groupId;
  private final String m_jobId;
  private volatile boolean m_disposed;
  private volatile boolean m_interrupted;

  public AbstractSchedulerJob(String groupId, String jobId) {
    m_groupId = groupId;
    m_jobId = jobId;
  }

  @Override
  public String getGroupId() {
    return m_groupId;
  }

  @Override
  public String getJobId() {
    return m_jobId;
  }

  @Override
  public boolean acceptTick(TickSignal signal) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("task " + getJobId() + " at " + signal);
    }
    int second = signal.getSecond();
    int minute = signal.getMinute();
    int hour = signal.getHour();
    int day = signal.getDay();
    int week = signal.getWeek();
    int month = signal.getMonth();
    int year = signal.getYear();
    int dayOfWeek = signal.getDayOfWeek();
    int dayOfMonthReverse = signal.getDayOfMonthReverse();
    int dayOfYear = signal.getDayOfYear();
    int secondOfDay = signal.getSecondOfDay();
    boolean accepted = execAcceptTick(signal, second, minute, hour, day, week, month, year, dayOfWeek, dayOfMonthReverse, dayOfYear, secondOfDay);
    if (accepted) {
      if (LOG.isInfoEnabled()) {
        LOG.info("accepted task " + getJobId() + " triggered at " + signal);
      }
    }
    return accepted;
  }

  @Override
  @ConfigOperation
  @Order(20)
  public void run(IScheduler scheduler, TickSignal signal) {
  }

  @ConfigOperation
  @Order(10)
  protected boolean execAcceptTick(TickSignal signal, int second, int minute, int hour, int day, int week, int month, int year, int dayOfWeek, int dayOfMonthReverse, int dayOfYear, int secondOfDay) {
    return false;
  }

  @Override
  public boolean isInterrupted() {
    return m_interrupted;
  }

  @Override
  public void setInterrupted(boolean b) {
    m_interrupted = b;
  }

  @Override
  public boolean isDisposed() {
    return m_disposed;
  }

  @Override
  public void setDisposed(boolean b) {
    m_disposed = b;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + m_groupId + "." + m_jobId + "]";
  }

}
