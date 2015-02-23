/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.swt.basic.calendar.widgets;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.eclipse.scout.rt.client.ui.basic.calendar.DateTimeFormatFactory;
import org.eclipse.scout.rt.ui.swt.basic.calendar.layout.MonthCellData;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * Cells within a monthly calendar.
 * 
 * @author Michael Rudolf, Andreas Hoegger
 */
public class MonthCell extends AbstractCell implements TraverseListener {

  public MonthCell(Composite parent, int style, SwtCalendar calendar, Date cellDate, boolean isFirstColumn, boolean isCurrentMonth) {
    super(parent, style);

    setCalendar(calendar);
    setDate(Calendar.getInstance());
    getDate().setTime(cellDate);
    setFirstColumn(isFirstColumn);
    setCurrentPeriod(isCurrentMonth);
    setSelectedInternal(false);

    createControls();
    setVisualState();
    addCalendarItems();
    hookListeners();
  }

  // TODO: need this?
  // TraverseListener
  @Override
  public void keyTraversed(TraverseEvent e) {
  }

  protected void createControls() {
    // calc vertical span for this cell
    int vertSpan = 2; // per default 2
    int day = getDate().get(Calendar.DAY_OF_WEEK);
    int firstDay = getCalendar().getFirstDayOfWeek();
    int weekEndDay1 = (firstDay - 1 + 5) % 7 + 1;
    int weekEndDay2 = (firstDay - 1 + 6) % 7 + 1;
    if (getCalendar().getCondensedMode()
        && (day == weekEndDay1 || day == weekEndDay2)) {
      vertSpan = 1;
    }

    // data object for custom layout manager
    MonthCellData cd = new MonthCellData();
    cd.setVerticalSpan(vertSpan);
    this.setLayoutData(cd);

    // create new grid layout
    GridLayout layout = new GridLayout();
    // TODO: let marginTop depend of the font ascent
    layout.marginTop = 12;
    layout.marginWidth = 2;
    layout.marginHeight = 3;
    layout.horizontalSpacing = 0;
    layout.verticalSpacing = 1;
    layout.numColumns = 2;
    this.setLayout(layout);
  }

  @Override
  protected void drawLabels(PaintEvent e) {
    // week label only if first column
    if (isFirstColumn()) {
      drawWeekLabel(e);
    }

    // day label
    drawDayLabel(e);
  }

  @Override
  public String toString() {
    DateFormat weekDayFmt = new SimpleDateFormat("EEEEE", Locale.getDefault());
    DateFormat dateFmt = new DateTimeFormatFactory().getDayMonthYear(DateFormat.LONG);
    return "MonthCell {" + weekDayFmt.format(getDate().getTime()) + " " + dateFmt.format(getDate().getTime()) + "}";
  }

  @Override
  public void dispose() {
    super.dispose();
  }
}
