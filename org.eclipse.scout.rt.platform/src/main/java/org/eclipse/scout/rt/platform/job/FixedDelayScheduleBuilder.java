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

import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.job.internal.FixedDelayTrigger;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.quartz.ScheduleBuilder;
import org.quartz.spi.MutableTrigger;

/**
 * Represents a schedule to run a job periodically with a fixed delay between the termination of one execution and the
 * commencement of the next execution.
 *
 * @since 5.2
 */
public final class FixedDelayScheduleBuilder extends ScheduleBuilder<IFixedDelayTrigger> {

  private final long m_fixedDelay;
  private final long m_repeatCount;

  private FixedDelayScheduleBuilder(final long repeatCount, final long fixedDelay, final TimeUnit unit) {
    Assertions.assertNotEquals(unit, TimeUnit.NANOSECONDS, "Quartz Scheduler does not support NANOSECONDS as temporal granularity");
    Assertions.assertGreater(fixedDelay, 0L, "Invalid fixed delay; must be > 0 [fixedDelay={}]", fixedDelay);

    m_repeatCount = repeatCount;
    m_fixedDelay = unit.toMillis(fixedDelay);
  }

  /**
   * Returns the fixed delay between successive runs.
   */
  public long getFixedDelay() {
    return m_fixedDelay;
  }

  /**
   * Returns the maximal repetition count.
   */
  public long getRepeatCount() {
    return m_repeatCount;
  }

  @Override
  protected MutableTrigger build() {
    return new FixedDelayTrigger()
        .withFixedDelay(m_fixedDelay)
        .withRepeatCount(m_repeatCount);
  }

  /**
   * Creates an infinite schedule to run a job periodically with a fixed delay between the termination of one execution
   * and the commencement of the next execution.
   * <p>
   * The job runs forever, unless the trigger's end time elapses, or the job is cancelled, or the job throws an
   * exception, and which in turn is not swallowed by the job's installed exception handling mechanism. See
   * {@link JobInput#withExceptionHandling(Class, boolean)}.
   *
   * @param fixedDelay
   *          the delay between successive runs.
   * @param unit
   *          the time unit of the <code>delay</code> argument.
   */
  public static FixedDelayScheduleBuilder repeatForever(final long fixedDelay, final TimeUnit unit) {
    return new FixedDelayScheduleBuilder(IFixedDelayTrigger.REPEAT_INDEFINITELY, fixedDelay, unit);
  }

  /**
   * Creates a finite schedule to run a job periodically with a fixed delay between the termination of one execution and
   * the commencement of the next execution.
   * <p>
   * At maximum, the job repeats as many times as specified. However, repeating execution is stopped once being
   * cancelled, or the job throws an exception, and which in turn is not swallowed by the job's installed exception
   * handling mechanism. See {@link JobInput#withExceptionHandling(Class, boolean)}.
   *
   * @param repeatCount
   *          the maximum number of repetitions.
   * @param fixedDelay
   *          the delay between successive runs.
   * @param unit
   *          the time unit of the <code>delay</code> argument.
   */
  public static FixedDelayScheduleBuilder repeatForTotalCount(final long repeatCount, final long fixedDelay, final TimeUnit unit) {
    return new FixedDelayScheduleBuilder(repeatCount, fixedDelay, unit);
  }
}
