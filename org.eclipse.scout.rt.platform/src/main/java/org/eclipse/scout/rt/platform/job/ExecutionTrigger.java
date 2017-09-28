/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.job;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.quartz.Calendar;
import org.quartz.CalendarIntervalScheduleBuilder;
import org.quartz.CronScheduleBuilder;
import org.quartz.DailyTimeIntervalScheduleBuilder;
import org.quartz.ScheduleBuilder;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

/**
 * Component that defines the schedule upon which a job will commence execution.
 * <p>
 * A trigger can be as simple as a 'one-shot' execution at some specific point in time in the future, or represent a
 * schedule which executes a job on a repeatedly basis. The latter can be configured to run infinitely, or to end at a
 * specific point in time. It is further possible to define rather complex triggers, like to execute a job every second
 * Friday at noon, but with the exclusion of all the business's holidays.
 * <p>
 * See the various schedule builders provided by Quartz Scheduler:<br>
 * {@link SimpleScheduleBuilder}, {@link CronScheduleBuilder}, {@link CalendarIntervalScheduleBuilder},
 * {@link DailyTimeIntervalScheduleBuilder}.
 * <p>
 * The most powerful builder is {@link CronScheduleBuilder}. Cron is a UNIX tool with powerful and proven scheduling
 * capabilities.
 * <p>
 * Additionally, Scout provides you with {@link FixedDelayScheduleBuilder} to run a job with a fixed delay between the
 * termination of one execution and the commencement of the next execution.
 *
 * @see TriggerBuilder
 * @since 5.2
 */
@Bean
public class ExecutionTrigger {

  private long m_now = System.currentTimeMillis();

  private Date m_startTime;
  private boolean m_startTimeSet;
  private Date m_endTime;
  private boolean m_endTimeSet;
  private Calendar m_calendar;
  private ScheduleBuilder<?> m_scheduleBuilder;

  /**
   * Returns the current time at which this execution trigger was instantiated, and is the reference time to compute
   * absolute times for durations.
   */
  public Date getNow() {
    return new Date(m_now);
  }

  /**
   * Returns the point in time this trigger will fire for the first time, or the current time if not set.
   */
  public Date getStartTime() {
    return (m_startTime != null ? m_startTime : getNow());
  }

  /**
   * Instruments this trigger to wait for the specified duration before firing for the first time, and which allows for
   * delayed execution. However, the job may or may not be executed at this time, which depends upon the schedule
   * configured for this trigger, and whether having to compete for an execution permit first.
   * <p>
   * If not set or zero, the job will commence execution immediately.
   */
  public ExecutionTrigger withStartIn(final long duration, final TimeUnit unit) {
    Assertions.assertFalse(m_startTimeSet, "StartTime already set");
    assertDuration(m_now, duration, unit);
    m_startTime = new Date(m_now + unit.toMillis(duration));
    m_startTimeSet = true;
    return this;
  }

  /**
   * Sets the point in time this trigger will fire for the first time, and which allows for delayed execution. However,
   * the job may or may not be executed at this time, which depends upon the schedule configured for this trigger, and
   * whether having to compete for an execution permit first.
   * <p>
   * If not set, the job will commence execution immediately.
   */
  public ExecutionTrigger withStartAt(final Date startTime) {
    Assertions.assertFalse(m_startTimeSet, "StartTime already set");
    m_startTime = startTime;
    m_startTimeSet = true;
    return this;
  }

  /**
   * Returns the point in time this trigger will no longer fire, or <code>null</code> if not set.
   */
  public Date getEndTime() {
    return m_endTime;
  }

  /**
   * Instruments this trigger to not fire any longer than the duration specified - even if it's schedule has remaining
   * repeats.
   * <p>
   * If not set, the end time depends on the trigger's schedule, and might be infinitely.
   */
  public ExecutionTrigger withEndIn(final long duration, final TimeUnit unit) {
    Assertions.assertFalse(m_endTimeSet, "EndTime already set");
    assertDuration(m_now, duration, unit);
    m_endTime = new Date(m_now + unit.toMillis(duration));
    m_endTimeSet = true;
    return this;
  }

  /**
   * Sets the point in time at which this trigger will no longer fire - even if it's schedule has remaining repeats.
   * <p>
   * If not set, the end time depends on the trigger's schedule, and might be infinitely.
   */
  public ExecutionTrigger withEndAt(final Date endTime) {
    Assertions.assertFalse(m_endTimeSet, "EndTime already set");
    m_endTime = endTime;
    m_endTimeSet = true;
    return this;
  }

  /**
   * Returns the calendar if set.
   */
  public Calendar getCalendar() {
    return m_calendar;
  }

  /**
   * Sets the {@link Calendar} to be applied to this trigger's schedule. This is useful to exclude firing for some
   * temporal periods, e.g. to not fire on business's holidays.
   */
  public ExecutionTrigger withModifiedByCalendar(final Calendar calendar) {
    m_calendar = calendar;
    return this;
  }

  /**
   * Returns the schedule used to define this trigger's schedule, or <code>null</code> to run the job once.
   */
  public ScheduleBuilder<?> getSchedule() {
    return m_scheduleBuilder;
  }

  /**
   * Sets the {@link ScheduleBuilder} that defines the schedule upon which the job will commence execution. If not set,
   * the job will commence execution immediately, and be run exactly once.
   * <p>
   * See the various schedule builders provided by Quartz Scheduler:<br>
   * {@link SimpleScheduleBuilder}, {@link CronScheduleBuilder}, {@link CalendarIntervalScheduleBuilder},
   * {@link DailyTimeIntervalScheduleBuilder}.
   * <p>
   * The most powerful builder is {@link CronScheduleBuilder}. Cron is a UNIX tool with powerful and proven scheduling
   * capabilities.
   * <p>
   * Additionally, Scout provides you with {@link FixedDelayScheduleBuilder} to run a job with a fixed delay between the
   * termination of one execution and the commencement of the next execution.
   * <p>
   * To create a schedule, use the builder's static factory methods:
   *
   * <pre>
   * CronScheduleBuilder.cronSchedule("0 15 10 ? * *") // to commence execution every day at 10:15am
   * SimpleScheduleBuilder.repeatMinutelyForever() // to commence execution every minute
   * FixedDelayScheduleBuilder.repeatForever(1, TimeUnit.MINUTES) // to run a job infinitely with a fixed delay of 1 minute between successive runs
   * </pre>
   */
  public ExecutionTrigger withSchedule(final ScheduleBuilder<? extends Trigger> scheduleBuilder) {
    m_scheduleBuilder = scheduleBuilder;
    return this;
  }

  /**
   * Returns a copy of this trigger.
   */
  public ExecutionTrigger copy() {
    final ExecutionTrigger copy = BEANS.get(ExecutionTrigger.class);
    copy.m_now = m_now;
    copy.m_startTime = (m_startTime != null ? new Date(m_startTime.getTime()) : null);
    copy.m_endTime = (m_endTime != null ? new Date(m_endTime.getTime()) : null);
    copy.m_calendar = (m_calendar != null ? (Calendar) m_calendar.clone() : null);
    copy.m_scheduleBuilder = TriggerBuilder.newTrigger()
        .withSchedule(m_scheduleBuilder)
        .build()
        .getScheduleBuilder();
    return copy;
  }

  private static void assertDuration(final long now, final long duration, final TimeUnit unit) {
    Assertions.assertNotEquals(unit, TimeUnit.NANOSECONDS, "Quartz Trigger does not support NANOSECONDS as temporal granularity");
    Assertions.assertGreaterOrEqual(duration, 0L, "Invalid duration; must be >= 0 [duration={}]", duration);
    Assertions.assertGreaterOrEqual(now + duration, now, "Duration caused temporal overflow [duration={}, date={}]", duration, now + duration);
  }
}
