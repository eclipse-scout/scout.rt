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

import java.util.Date;
import java.util.TreeMap;

/**
 * Model to represent a day, a week, a month, ...
 */
public class MajorTimeColumn {
  private TimeScale m_scale;
  private String m_smallText = "";
  private String m_mediumText = "";
  private String m_largeText = "";
  private String m_tooltipText;
  private final TreeMap<Date, MinorTimeColumn> m_children = new TreeMap<Date, MinorTimeColumn>();

  public MajorTimeColumn(TimeScale scale) {
    m_scale = scale;
    m_scale.addMajorColumnNotify(this);
  }

  public TimeScale geTimeScale() {
    return m_scale;
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

  protected void addMinorColumnNotify(MinorTimeColumn c) {
    m_children.put(c.getBeginTime(), c);
    m_scale.addMinorColumnNotify(this, c);
  }

  public MinorTimeColumn[] getMinorTimeColumns() {
    return m_children.values().toArray(new MinorTimeColumn[m_children.size()]);
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
    for (MinorTimeColumn c : getMinorTimeColumns()) {
      b.append(" ");
      b.append(c.toString(size));
    }
    b.append(" ]");
    return b.toString();
  }

}
