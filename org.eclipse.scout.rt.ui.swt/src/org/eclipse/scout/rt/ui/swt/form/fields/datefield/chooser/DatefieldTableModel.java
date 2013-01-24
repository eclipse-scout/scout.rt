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
package org.eclipse.scout.rt.ui.swt.form.fields.datefield.chooser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.scout.commons.DateUtility;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;

public class DatefieldTableModel implements IStructuredContentProvider, ITableLabelProvider, ITableColorProvider, ITableFontProvider {

  private List<DateRow> m_rows = new ArrayList<DateRow>();
  private Date m_navigationDate = null;
  private Date m_highLightDate = null;
  private SimpleDateFormat m_monthYearFormat = new SimpleDateFormat("MMMMM yyyy");

  // ui
  private Color m_weekendBackground;
  private Color m_outMonthForeground;
  private Color m_highlightBackground;
  private Color m_highlightForeground;
  private Font m_highlightFont;
  private final ISwtEnvironment m_environment;

  public DatefieldTableModel(ISwtEnvironment environment) {
    m_environment = environment;
    // m_weekendBackground = new Color(Display.getDefault(),238,238,238);
    // m_outMonthForeground = new Color(Display.getDefault(),180,180,180);
    // m_highlightBackground = new Color(Display.getDefault(),1,42,195);
    // m_highlightForeground =
    // Display.getDefault().getSystemColor(SWT.COLOR_WHITE);
    m_weekendBackground = getEnvironment().getColor(new RGB(238, 238, 238));
    m_outMonthForeground = getEnvironment().getColor(new RGB(180, 180, 180));
    m_highlightBackground = getEnvironment().getColor(new RGB(1, 42, 195));
    m_highlightForeground = getEnvironment().getColor(new RGB(255, 255, 255));

    m_highlightFont = getEnvironment().getFont(new FontSpec(null, FontSpec.STYLE_BOLD, -1), JFaceResources.getDefaultFont());
  }

  @Override
  public void dispose() {
  }

  public DateRow getHighLightRow() {
    for (DateRow row : m_rows) {
      Date min = row.getDate(0);
      Date max = row.getDate(8);
      if (min.before(m_highLightDate) && max.after(m_highLightDate)) {
        return row;
      }
    }
    return null;
  }

  public int indexOf(DateRow row) {
    return m_rows.indexOf(row);
  }

  public void setRows(ArrayList<DateRow> rows) {
    m_rows.clear();
    m_rows.addAll(rows);
  }

  public void setHighLightDate(Date date) {
    if (date == null) {
      date = new Date();
    }
    m_highLightDate = date;
  }

  public void setNavigationDate(Date date) {
    if (date == null) {
      date = new Date();
    }
    m_navigationDate = date;
    m_rows.clear();
    Calendar c = Calendar.getInstance();
    c.setTime(date);
    // Calculate Startdate; go back to 1st of month, then go back to monday
    // (1=sunday)
    int monday = Calendar.MONDAY;
    c.add(Calendar.DAY_OF_MONTH, -(c.get(Calendar.DAY_OF_MONTH) - 1));
    c.add(Calendar.DAY_OF_WEEK, -((c.get(Calendar.DAY_OF_WEEK) - monday + 7) % 7));
    for (int iRows = 0; iRows < 6; iRows++) {
      DateRow row = new DateRow(c.getTime());
      m_rows.add(row);
      c.add(Calendar.DATE, 7);
    }
  }

  public Date getNavigationDate() {
    return m_navigationDate;
  }

  public String getMonthYearLabel() {
    return m_monthYearFormat.format(m_navigationDate);
  }

  @Override
  public Object[] getElements(Object inputElement) {
    return m_rows.toArray();
  }

  @Override
  public Image getColumnImage(Object element, int columnIndex) {
    return null;
  }

  @Override
  public String getColumnText(Object element, int columnIndex) {
    Calendar c = Calendar.getInstance();
    c.setTime(((DateRow) element).getDate(columnIndex - 1));
    int dayOfMonth = c.get(Calendar.DAY_OF_MONTH);
    return "" + dayOfMonth;
  }

  @Override
  public Color getBackground(Object element, int columnIndex) {

    Date date = ((DateRow) element).getDate(columnIndex - 1);
    // check hightlight
    if (DateUtility.isSameDay(date, m_highLightDate)) {
      return m_highlightBackground;
    }
    if (DateUtility.isWeekend(date)) {
      return m_weekendBackground;
    }
    return m_highlightForeground;
  }

  @Override
  public Color getForeground(Object element, int columnIndex) {
    Date date = ((DateRow) element).getDate(columnIndex - 1);
    // check hightlight
    if (DateUtility.isSameDay(date, m_highLightDate)) {

      return m_highlightForeground;
    }
    if (!DateUtility.isSameMonth(date, m_navigationDate)) {
      return m_outMonthForeground;
    }
    return null;
  }

  @Override
  public Font getFont(Object element, int columnIndex) {
    Date date = ((DateRow) element).getDate(columnIndex - 1);
    // check hightlight
    if (DateUtility.isSameDay(date, m_highLightDate)) {
      return m_highlightFont;
    }
    return null;
  }

  @Override
  public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
  }

  @Override
  public void addListener(ILabelProviderListener listener) {
  }

  @Override
  public boolean isLabelProperty(Object element, String property) {
    return false;
  }

  @Override
  public void removeListener(ILabelProviderListener listener) {
  }

  public ISwtEnvironment getEnvironment() {
    return m_environment;
  }
}
