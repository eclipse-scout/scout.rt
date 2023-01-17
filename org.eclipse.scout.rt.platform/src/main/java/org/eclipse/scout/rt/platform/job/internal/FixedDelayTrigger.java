/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.job.internal;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.job.FixedDelayScheduleBuilder;
import org.eclipse.scout.rt.platform.job.IFixedDelayTrigger;
import org.quartz.Calendar;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.ScheduleBuilder;
import org.quartz.impl.triggers.AbstractTrigger;

/**
 * Default implementation of {@link IFixedDelayTrigger}.
 *
 * @since 5.2
 */
@SuppressWarnings("squid:S2160")
public class FixedDelayTrigger extends AbstractTrigger<IFixedDelayTrigger> implements IFixedDelayTrigger {

  private static final long serialVersionUID = 1L;

  private static final Date TIME_IN_FUTURE = new Date(Long.MAX_VALUE);

  private long m_fixedDelay;
  private long m_repeatCount;

  private Date m_startTime;
  private Date m_endTime;

  private Date m_previousFireTime;
  private Date m_nextFireTime;

  private long m_triggeredCount;

  @Override
  public long getRepeatCount() {
    return m_repeatCount;
  }

  /**
   * Sets the number of times this trigger should repeat, after which it will be automatically deleted.
   */
  public FixedDelayTrigger withRepeatCount(final long repeatCount) {
    m_repeatCount = repeatCount;
    return this;
  }

  @Override
  public long getFixedDelay() {
    return m_fixedDelay;
  }

  /**
   * Sets the fixed delay between successive runs.
   */
  public FixedDelayTrigger withFixedDelay(final long fixedDelay) {
    m_fixedDelay = fixedDelay;
    return this;
  }

  @Override
  public void triggered(final Calendar calendar) {
    m_triggeredCount++;
    m_previousFireTime = m_nextFireTime;

    if (m_triggeredCount == m_repeatCount) {
      m_nextFireTime = null; // null causes Quartz to not fire anymore
    }
    else if (m_endTime != null && m_endTime.before(new Date(System.currentTimeMillis()))) {
      m_nextFireTime = null; // null causes Quartz to not fire anymore
    }
    else {
      m_nextFireTime = TIME_IN_FUTURE; // will fire some time in the future
    }
  }

  @Override
  public CompletedExecutionInstruction executionComplete(final JobExecutionContext context, final JobExecutionException result) {
    if (TIME_IN_FUTURE.equals(m_nextFireTime)) {
      // Compute the next trigger fire time.
      m_nextFireTime = new Date(System.currentTimeMillis() + m_fixedDelay);
    }
    return CompletedExecutionInstruction.NOOP;
  }

  @Override
  public Date computeFirstFireTime(final Calendar calendar) {
    m_nextFireTime = m_startTime;
    return m_startTime;
  }

  @Override
  public Date getFinalFireTime() {
    if (m_repeatCount == 0) {
      return m_startTime;
    }
    else {
      return null;
    }
  }

  @Override
  public boolean mayFireAgain() {
    return m_nextFireTime != null;
  }

  @Override
  public Date getFireTimeAfter(final Date afterTime) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Date getNextFireTime() {
    return m_nextFireTime;
  }

  @Override
  public void setNextFireTime(final Date nextFireTime) {
    m_nextFireTime = nextFireTime;
  }

  @Override
  public Date getPreviousFireTime() {
    return m_previousFireTime;
  }

  @Override
  public void setPreviousFireTime(final Date previousFireTime) {
    m_previousFireTime = previousFireTime;
  }

  @Override
  public Date getStartTime() {
    return m_startTime;
  }

  @Override
  public void setStartTime(final Date startTime) {
    assertNotNull(startTime, "Start time cannot be null");
    assertStartEndTime(startTime, m_endTime);
    m_startTime = startTime;
  }

  @Override
  public Date getEndTime() {
    return m_endTime;
  }

  @Override
  public void setEndTime(final Date endTime) {
    assertStartEndTime(m_startTime, endTime);
    m_endTime = endTime;
  }

  @Override
  protected boolean validateMisfireInstruction(final int candidateMisfireInstruction) {
    // NOOP
    return false;
  }

  @Override
  public void updateAfterMisfire(final Calendar cal) {
    // NOOP
  }

  @Override
  public void updateWithNewCalendar(final Calendar cal, final long misfireThreshold) {
    // NOOP
  }

  @Override
  public ScheduleBuilder<IFixedDelayTrigger> getScheduleBuilder() {
    return FixedDelayScheduleBuilder.repeatForTotalCount(m_repeatCount, m_fixedDelay, TimeUnit.MILLISECONDS);
  }

  private static void assertStartEndTime(final Date startTime, final Date endTime) {
    if (startTime != null && endTime != null && endTime.before(startTime)) {
      throw new IllegalArgumentException("End time cannot be before start time");
    }
  }

  private static void assertNotNull(final Object object, final String message) {
    if (object == null) {
      throw new IllegalArgumentException(message);
    }
  }
}
