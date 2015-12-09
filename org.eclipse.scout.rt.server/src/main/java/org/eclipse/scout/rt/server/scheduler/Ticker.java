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

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.util.SleepUtil;

public class Ticker {
  private Calendar m_cal;
  private int m_tickField = Calendar.MINUTE;

  /**
   * create a ticker with {@link Calendar#MINUTE} interval
   */
  public Ticker() {
    this(Calendar.MINUTE);
  }

  /**
   * @param tickMode
   *          one of {@link Calendar#DATE} {@link Calendar#HOUR_OF_DAY} {@link Calendar#MINUTE} {@link Calendar#SECOND}
   */
  public Ticker(int tickMode) {
    m_cal = Calendar.getInstance();
    setTickMode(tickMode);
  }

  /**
   * create a copy of a ticker
   */
  public Ticker(Ticker other) {
    setTickMode(other.getTickMode());
    setTime(other.getTime());
  }

  /**
   * @param calendarField
   *          one of {@link Calendar#DATE} {@link Calendar#HOUR_OF_DAY} {@link Calendar#MINUTE} {@link Calendar#SECOND}
   */
  public void setTickMode(int calendarField) {
    switch (calendarField) {
      case Calendar.DATE:
      case Calendar.HOUR_OF_DAY:
      case Calendar.MINUTE:
      case Calendar.SECOND: {
        m_tickField = calendarField;
        break;
      }
      default: {
        throw new IllegalArgumentException("tickMode must be one of: Calendar.DATE,Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND");
      }
    }
    adjustTime();
  }

  /**
   * one of {@link Calendar#DATE} {@link Calendar#HOUR_OF_DAY} {@link Calendar#MINUTE} {@link Calendar#SECOND}
   */
  public int getTickMode() {
    return m_tickField;
  }

  public void setTime(long time) {
    m_cal.setTimeInMillis(time);
    adjustTime();
  }

  private void adjustTime() {
    switch (m_tickField) {
      case Calendar.DATE: {
        m_cal.set(Calendar.HOUR_OF_DAY, 0);
        m_cal.set(Calendar.HOUR, 0);
        m_cal.set(Calendar.MINUTE, 0);
        m_cal.set(Calendar.SECOND, 0);
        m_cal.set(Calendar.MILLISECOND, 0);
        break;
      }
      case Calendar.HOUR_OF_DAY: {
        m_cal.set(Calendar.MINUTE, 0);
        m_cal.set(Calendar.SECOND, 0);
        m_cal.set(Calendar.MILLISECOND, 0);
        break;
      }
      case Calendar.MINUTE: {
        m_cal.set(Calendar.SECOND, 0);
        m_cal.set(Calendar.MILLISECOND, 0);
        break;
      }
      case Calendar.SECOND: {
        m_cal.set(Calendar.MILLISECOND, 0);
        break;
      }
    }
  }

  /**
   * Wait for next tick
   */
  public TickSignal waitForNextTick() {
    Calendar nextCal = new GregorianCalendar();
    nextCal.setTime(m_cal.getTime());
    nextCal.add(m_tickField, 1);
    // wait until this tick is reached
    long t = nextCal.getTimeInMillis();
    long dt = t - System.currentTimeMillis();
    while (dt > 0) {
      SleepUtil.sleepSafe(Math.min(dt, 15000), TimeUnit.MILLISECONDS);
      dt = t - System.currentTimeMillis();
    }
    // apply next time
    m_cal.add(m_tickField, 1);
    return getCurrentTick();
  }

  public long getTime() {
    return m_cal.getTimeInMillis();
  }

  public TickSignal getCurrentTick() {
    return new TickSignal(
        m_cal.get(Calendar.SECOND),
        m_cal.get(Calendar.MINUTE),
        m_cal.get(Calendar.HOUR_OF_DAY),
        ((7 + m_cal.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY) % 7) + 1,
        m_cal.get(Calendar.DAY_OF_MONTH),
        m_cal.getActualMaximum(Calendar.DAY_OF_MONTH) - m_cal.get(Calendar.DAY_OF_MONTH),
        m_cal.get(Calendar.DAY_OF_YEAR),
        m_cal.get(Calendar.WEEK_OF_YEAR),
        ((12 + m_cal.get(Calendar.MONTH) - Calendar.JANUARY) % 12) + 1,
        m_cal.get(Calendar.YEAR));
  }

}
