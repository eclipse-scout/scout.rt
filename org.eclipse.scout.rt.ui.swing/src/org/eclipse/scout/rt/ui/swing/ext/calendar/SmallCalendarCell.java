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
import java.awt.Font;
import java.awt.Point;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.swing.UIManager;

public class SmallCalendarCell extends AbstractCalendarCell {
  private static final long serialVersionUID = 1L;
  private DateChooser m_dateChooser;
  private Date m_repDate; // use setter to set, since setter is overridden to
  // keep trakc of changes to this field.
  protected boolean m_isMajor;// in current month resp. in current week
  protected boolean m_selected;
  protected Color m_majorColorForWork;
  protected Color m_minorColorForWork;
  protected Color m_majorColorForWeekend;
  protected Color m_minorColorForWeekend;
  protected Color m_selectedForeground;
  protected Color m_selectedBackground;
  protected Font m_defaultFont;
  protected Font m_selectedFont;

  public SmallCalendarCell(DateChooser dateChooser) {
    m_dateChooser = dateChooser;
    setHorizontalAlignment(CENTER);
    m_majorColorForWork = UIManager.getColor("Calendar.date.work"); // getForeground();
    m_minorColorForWork = UIManager.getColor("Calendar.date.workMinor"); // ColorUtility.mergeColors(m_majorColorForWork, 0.5f, Color.white, 0.5f);
    m_majorColorForWeekend = UIManager.getColor("Calendar.date.weekend"); // ColorUtility.mergeColors(getForeground(), 0.5f, Color.red, 0.5f);
    m_minorColorForWeekend = UIManager.getColor("Calendar.date.weekendMinor"); //ColorUtility.mergeColors(m_majorColorForWeekend, 0.5f, Color.white, 0.5f);
    m_selectedForeground = UIManager.getColor("Calendar.date.selected.foreground");
    m_selectedBackground = UIManager.getColor("Calendar.date.selected.background");
    m_defaultFont = UIManager.getFont("Calendar.date.font");
    m_selectedFont = UIManager.getFont("Calendar.date.selected.font");
  }

  @Override
  public void setWorkingHours(int startHour, int endHour, boolean useOverflowCells) {
  }

  @Override
  protected void onSpacePressed() {
    m_dateChooser.setSelectedDate(getRepresentedDate());
  }

  @Override
  public void refresh() {
    repaint();
  }

  @Override
  public boolean isSelected() {
    return m_selected;
  }

  @Override
  public void setSelected(boolean b) {
    m_selected = b;
    if (b) {
      setFont(m_selectedFont);
      setForeground(m_selectedForeground);
      setBackground(m_selectedBackground);
      setOpaque(true);
    }
    else {
      setFont(m_defaultFont);
      updateGui();
      setBackground(null);
      setOpaque(false);
    }
  }

  @Override
  public Date getRepresentedDate() {
    return m_repDate;
  }

  @Override
  public void setRepresentedState(Calendar c, boolean isMajor, boolean firstColumn, int displayType) {
    setRepDate(new Date(c.getTime().getTime()));
    m_isMajor = isMajor;
    // gui
    updateGui();
  }

  /**
   * @since 09.02.2006 - tha@bsiag.com
   * @rn extracted from {@link #setRepresentedState(Calendar, boolean, boolean, int)}.
   */
  protected void updateGui() {
    setText(new SimpleDateFormat("dd").format(m_repDate));
    Color c;
    if (m_dateChooser != null && !m_dateChooser.isWorkDay(getRepresentedDate())) {
      // weekend
      if (m_isMajor) {
        c = m_majorColorForWeekend;
      }
      else {
        c = m_minorColorForWeekend;
      }
    }
    else {
      // work
      if (m_isMajor) {
        c = m_majorColorForWork;
      }
      else {
        c = m_minorColorForWork;
      }
    }
    setForeground(c);
  }

  @Override
  public int getTimelessItemCount() {
    return 0;
  }

  @Override
  public int getTimedItemCount() {
    return 0;
  }

  @Override
  public Object getItemAt(Point p) {
    return null;
  }

  @Override
  public void resetItemCache() {
  }

  protected void setRepDate(Date repDate) {
    m_repDate = repDate;
  }

}
