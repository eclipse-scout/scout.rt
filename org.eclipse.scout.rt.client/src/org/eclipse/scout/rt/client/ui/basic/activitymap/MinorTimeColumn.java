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
package org.eclipse.scout.rt.client.ui.basic.activitymap;

import java.util.Calendar;
import java.util.Date;

/**
 * ui model to represent an intraday column (hour, half hour, quarter hour,
 * etc..)
 */
public class MinorTimeColumn {
  private Date m_beginTime;
  private Date m_endTime;
  private String m_smallText = "";
  private String m_mediumText = "";
  private String m_largeText = "";
  private String m_tooltipText;
  private MajorTimeColumn m_majorTimeColumn;

  public MinorTimeColumn(MajorTimeColumn parent, Date beginTime, Date endTime) {
    m_beginTime = beginTime;
    m_endTime = endTime;
    m_majorTimeColumn = parent;
    m_majorTimeColumn.addMinorColumnNotify(this);
  }

  public MajorTimeColumn getMajorTimeColumn() {
    return m_majorTimeColumn;
  }

  public Date getBeginTime() {
    return m_beginTime;
  }

  public Date getEndTime() {
    return m_endTime;
  }

  public Calendar getBeginCalendar() {
    Calendar cal = Calendar.getInstance();
    cal.setTime(m_beginTime);
    return cal;
  }

  public String getSmallText() {
    return m_smallText;
  }

  public void setSmallText(String s) {
    m_smallText = s;
  }

  public String getMediumText() {
    return m_mediumText;
  }

  public void setMediumText(String s) {
    m_mediumText = s;
  }

  public String getLargeText() {
    return m_largeText;
  }

  public void setLargeText(String s) {
    m_largeText = s;
  }

  public String getTooltipText() {
    return m_tooltipText;
  }

  public void setTooltipText(String s) {
    m_tooltipText = s;
  }

  public String getText(int size) {
    switch (size) {
      case TimeScale.SMALL: {
        return getSmallText();
      }
      case TimeScale.MEDIUM: {
        return getMediumText();
      }
      case TimeScale.LARGE: {
        return getLargeText();
      }
      default: {
        return "<UNKNOWN SIZE " + size + ">";
      }
    }
  }

  @Override
  public String toString() {
    return toString(TimeScale.LARGE);
  }

  public String toString(int size) {
    StringBuilder b = new StringBuilder();
    b.append(getClass().getSimpleName());
    b.append("[");
    b.append(getText(size));
    b.append(" ]");
    return b.toString();
  }

}
