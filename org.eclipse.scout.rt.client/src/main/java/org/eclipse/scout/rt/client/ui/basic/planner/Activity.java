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
package org.eclipse.scout.rt.client.ui.basic.planner;

import java.util.Date;

import org.eclipse.scout.rt.shared.data.basic.MemoryOptimizedObject;

/**
 * Representation of an activity containing an ActivityData
 */
public class Activity<RESOURCE_ID, ACTIVITY_ID> extends MemoryOptimizedObject {
  private static final long serialVersionUID = 1L;

  public static final int OBSERVER_BIT = 0;

  /**
   * long
   */
  public static final int RESOURCE_ID_BIT = 1;

  /**
   * long
   */
  public static final int ID_BIT = 2;

  /**
   * Date
   */
  public static final int BEGIN_TIME_BIT = 3;

  /**
   * Date
   */
  public static final int END_TIME_BIT = 4;

  /**
   * String
   */
  public static final int TEXT_BIT = 5;

  /**
   * String
   */
  public static final int BACKGROUND_COLOR_BIT = 6;

  /**
   * String
   */
  public static final int FOREGROUND_COLOR_BIT = 7;

  /**
   * float
   */
  public static final int LEVEL_BIT = 8;

  /**
   * String
   */
  public static final int LEVEL_COLOR_BIT = 9;

  /**
   * String
   */
  public static final int CSS_CLASS_BIT = 10;

  /**
   * int
   */
  public static final int DURATION_MINUTES_BIT = 12;

  /**
   * String
   */
  public static final int TOOLTIP_TEXT_BIT = 13;

  /**
   * String
   */
  public static final int CUSTOM_DATA = 15;

  public Activity(RESOURCE_ID resource, ACTIVITY_ID activityId) {
    setValueInternal(RESOURCE_ID_BIT, resource);
    setValueInternal(ID_BIT, activityId);
  }

  /**
   * @param resource
   * @param activityId
   * @param startTime
   * @param endTime
   * @param text
   * @param tooltipText
   * @param level
   */
  public Activity(RESOURCE_ID resource, ACTIVITY_ID activityId, Date startTime, Date endTime, String text, String tooltipText, float level) {
    setValueInternal(RESOURCE_ID_BIT, resource);
    setValueInternal(ID_BIT, activityId);
    setValueInternal(BEGIN_TIME_BIT, startTime);
    setValueInternal(END_TIME_BIT, endTime);
    setValueInternal(TEXT_BIT, text);
    setValueInternal(TOOLTIP_TEXT_BIT, tooltipText);
    setValueInternal(LEVEL_BIT, level);
  }

  /**
   * @param row
   *          <ul>
   *          <li>resource of type RI
   *          <li>activityId of type AI
   *          <li>startTime of type {@link Date}
   *          <li>endTime of type {@link Date}
   *          <li>text of type {@link String}
   *          <li>tooltipText of type {@link String}
   *          <li>value of type {@link Number}
   *          </ul>
   */
  public Activity(Object[] row) {
    if (row == null || row.length < 2) {
      throw new IllegalArgumentException("row must not be null or shorted than 2 elements");
    }
    if (row[0] == null) {
      throw new IllegalArgumentException("resource must not be null");
    }
    if (row[1] == null) {
      throw new IllegalArgumentException("activityId must not be null");
    }
    for (int i = 0; i < row.length; i++) {
      if (row[i] != null) {
        switch (i) {
          case 0: {
            setValueInternal(RESOURCE_ID_BIT, row[i]);
            break;
          }
          case 1: {
            setValueInternal(ID_BIT, row[i]);
            break;
          }
          case 2: {
            setValueInternal(BEGIN_TIME_BIT, row[i]);
            break;
          }
          case 3: {
            setValueInternal(END_TIME_BIT, row[i]);
            break;
          }
          case 4: {
            setText((String) row[i]);
            break;
          }
          case 5: {
            setTooltipText((String) row[i]);
            break;
          }
          case 6: {
            setLevel(((Number) row[i]).floatValue());
            break;
          }
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  public IActivityObserver<RESOURCE_ID, ACTIVITY_ID> getObserver() {
    return (IActivityObserver) getValueInternal(OBSERVER_BIT);
  }

  public void setObserver(IActivityObserver<RESOURCE_ID, ACTIVITY_ID> observer) {
    setValueInternal(OBSERVER_BIT, observer);
  }

  @Override
  protected boolean setValueInternal(int bitPos, Object o) {
    boolean b = super.setValueInternal(bitPos, o);
    if (getObserver() != null && bitPos != OBSERVER_BIT) {
      getObserver().activityChanged(this, bitPos);
    }
    return b;
  }

  @SuppressWarnings("unchecked")
  public ACTIVITY_ID getId() {
    return (ACTIVITY_ID) getValueInternal(ID_BIT);
  }

  @SuppressWarnings("unchecked")
  public RESOURCE_ID getResourceId() {
    return (RESOURCE_ID) getValueInternal(RESOURCE_ID_BIT);
  }

  public Date getBeginTime() {
    return (Date) getValueInternal(BEGIN_TIME_BIT);
  }

  public void setBeginTime(Date d) {
    setValueInternal(BEGIN_TIME_BIT, d);
  }

  public Date getEndTime() {
    return (Date) getValueInternal(END_TIME_BIT);
  }

  public void setEndTime(Date d) {
    setValueInternal(END_TIME_BIT, d);
  }

  public int getDurationMinutes() {
    Integer o = (Integer) getValueInternal(DURATION_MINUTES_BIT);
    return o != null ? o : 0;
  }

  public float getLevel() {
    Float o = (Float) getValueInternal(LEVEL_BIT);
    return o != null ? o : 0;
  }

  public void setLevel(float f) {
    setValueInternal(LEVEL_BIT, f);
  }

  public String getLevelColor() {
    return (String) getValueInternal(LEVEL_COLOR_BIT);
  }

  public void setLevelColor(String s) {
    setValueInternal(LEVEL_COLOR_BIT, s);
  }

  public String getCssClass() {
    return (String) getValueInternal(CSS_CLASS_BIT);
  }

  public void setCssClass(String s) {
    setValueInternal(CSS_CLASS_BIT, s);
  }

  public String getTooltipText() {
    return (String) getValueInternal(TOOLTIP_TEXT_BIT);
  }

  public void setTooltipText(String s) {
    setValueInternal(TOOLTIP_TEXT_BIT, s);
  }

  public String getText() {
    return (String) getValueInternal(TEXT_BIT);
  }

  public void setText(String s) {
    setValueInternal(TEXT_BIT, s);
  }

  public String getBackgroundColor() {
    return (String) getValueInternal(BACKGROUND_COLOR_BIT);
  }

  public void setBackgroundColor(String c) {
    setValueInternal(BACKGROUND_COLOR_BIT, c);
  }

  public String getForegroundColor() {
    return (String) getValueInternal(FOREGROUND_COLOR_BIT);
  }

  public void setForegroundColor(String c) {
    setValueInternal(FOREGROUND_COLOR_BIT, c);
  }

  public Object getCustomData() {
    return getValueInternal(CUSTOM_DATA);
  }

  public void setCustomData(Object o) {
    setValueInternal(CUSTOM_DATA, o);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + getText() + "]";
  }
}
