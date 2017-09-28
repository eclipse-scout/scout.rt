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
package org.eclipse.scout.rt.client.ui.basic.planner;

/**
 * Encapsulates options for {@link IPlanner}.
 *
 * @since 5.2
 */
public class DisplayModeOptions {

  private long m_interval = 0;
  private int m_labelPeriod = 1;
  private int m_firstHourOfDay = 0;
  private int m_lastHourOfDay = 23;

  /**
   * @return the interval of the small time line. <br>
   *         For {@link DAY}, {@link WEEK}, {@link WORK_WEEK} the value is expected to be minutes. <br>
   *         For {@link MONTH}, {@link YEAR} and {@link CALENDAR_WEEK} the parameter has no effect.
   */
  public long getInterval() {
    return m_interval;
  }

  /**
   * @return in which period labels are drawn. For value n every n-th label is drawn.
   */
  public int getLabelPeriod() {
    return m_labelPeriod;
  }

  /**
   * @return the first hour of a day<br>
   *         When a working day starts at 08:00 and ends at 17:00, this value is 8.
   */
  public int getFirstHourOfDay() {
    return m_firstHourOfDay;
  }

  /**
   * @return the last hour of a day<br>
   *         When a working day starts at 08:00 and ends at 17:00, this value is 16 since the last hour starts at 16:00
   *         and ends at 16:59.
   */
  public int getLastHourOfDay() {
    return m_lastHourOfDay;
  }

  public DisplayModeOptions withInterval(long interval) {
    m_interval = interval;
    return this;
  }

  public DisplayModeOptions withLabelPeriod(int labelPeriod) {
    m_labelPeriod = labelPeriod;
    return this;
  }

  public DisplayModeOptions withFirstHourOfDay(int firstHourOfDay) {
    m_firstHourOfDay = firstHourOfDay;
    return this;
  }

  public DisplayModeOptions withLastHourOfDay(int lastHourOfDay) {
    m_lastHourOfDay = lastHourOfDay;
    return this;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + m_firstHourOfDay;
    result = prime * result + (int) (m_interval ^ (m_interval >>> 32));
    result = prime * result + m_labelPeriod;
    result = prime * result + m_lastHourOfDay;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    DisplayModeOptions other = (DisplayModeOptions) obj;
    if (m_firstHourOfDay != other.m_firstHourOfDay) {
      return false;
    }
    if (m_interval != other.m_interval) {
      return false;
    }
    if (m_labelPeriod != other.m_labelPeriod) {
      return false;
    }
    if (m_lastHourOfDay != other.m_lastHourOfDay) {
      return false;
    }
    return true;
  }

}
