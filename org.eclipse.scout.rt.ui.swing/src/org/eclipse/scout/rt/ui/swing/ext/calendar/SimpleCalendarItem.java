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
package org.eclipse.scout.rt.ui.swing.ext.calendar;

import java.awt.Color;
import java.util.Date;

public class SimpleCalendarItem {
  protected String m_label;
  protected String m_tooltip;
  protected Date m_from;
  protected Date m_to;
  protected Color m_color;
  protected boolean m_fullDay;
  protected boolean m_isDraggable;

  public SimpleCalendarItem(String label, Date from, Date to, Color color) {
    this(label, label, from, to, color);
  }

  public SimpleCalendarItem(String label, String tooltip, Date from, Date to, Color color) {
    this(label, tooltip, from, to, color, false);
  }

  public SimpleCalendarItem(String label, String tooltip, Date from, Date to, Color color, boolean fullDay) {
    this(label, tooltip, from, to, color, fullDay, false);
  }

  public SimpleCalendarItem(String label, String tooltip, Date from, Date to, Color color, boolean fullDay, boolean isDraggable) {
    m_label = label;
    m_tooltip = tooltip;
    m_from = from;
    m_to = to;
    m_color = color;
    m_fullDay = fullDay;
    m_isDraggable = isDraggable;
  }

  public String getLabel() {
    return m_label;
  }

  public String getTooltipText() {
    return m_tooltip;
  }

  public Date getFromDate() {
    return m_from;
  }

  public Date getToDate() {
    return m_to;
  }

  public Color getColor() {
    return m_color;
  }

  @Override
  public String toString() {
    return m_label;
  }

  public boolean isFullDay() {
    return m_fullDay;
  }

  public boolean isDraggable() {
    return m_isDraggable;
  }
}
