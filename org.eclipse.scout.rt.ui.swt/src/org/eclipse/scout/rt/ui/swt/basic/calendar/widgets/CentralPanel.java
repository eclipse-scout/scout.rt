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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import org.eclipse.scout.rt.client.ui.basic.calendar.DateTimeFormatFactory;
import org.eclipse.scout.rt.ui.swt.basic.calendar.CalendarConstants;
import org.eclipse.scout.rt.ui.swt.basic.calendar.DisplayMode;
import org.eclipse.scout.rt.ui.swt.basic.calendar.SwtColors;
import org.eclipse.scout.rt.ui.swt.basic.calendar.layout.MonthCellLayout;
import org.eclipse.scout.rt.ui.swt.util.SwtUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * Central panel component of a calendar (i.e. holding calendar cells).
 * 
 * @author Michael Rudolf, Andreas Hoegger
 */
public class CentralPanel extends Composite {

  /** ref to calendar */
  private SwtCalendar m_calendar;

  /** empty composite to keep alignement */
  private Composite m_emptyComposite;

  /** widget representing the day names */
  private DayNamesBar m_dayNames;

  /** widget representing the timeline */
  private TimelineColumn m_timeline;

  /** cell list for week or day view */
  private ArrayList<WeekCell> m_weekCells = null;

  /** cell list list for month view */
  private ArrayList<ArrayList<MonthCell>> m_monthCells = null;

  /** to hold the existing cell according to their dates */
  private HashMap<Date, AbstractCell> m_dateMap;

  /** composite for holding the cell widgets */
  private Composite m_cells;

  /** panel width */
  private int m_panelWidth = 0;

  /** panel height */
  private int m_panelHeight = 0;

  /** max number of timeless items within any cell */
  private int m_timelessMaxCount;

  public CentralPanel(Composite parent, int style, SwtCalendar calendar) {
    super(parent, style);

    m_calendar = calendar;

    createControls();
  }

  protected void createControls() {

    GridData gd;

    gd = new GridData();
    gd.grabExcessHorizontalSpace = true;
    gd.grabExcessVerticalSpace = true;
    gd.horizontalAlignment = GridData.FILL;
    gd.verticalAlignment = GridData.FILL;
    this.setLayoutData(gd);

    GridLayout layout = new GridLayout();
    layout.marginWidth = 0;
    layout.marginHeight = 0;
    layout.horizontalSpacing = 2;
    layout.verticalSpacing = 2;
    layout.numColumns = 2;
    this.setLayout(layout);
    this.setBackground(SwtColors.getInstance().getWhite());

    // to make this composite focusable
    addListener(SWT.KeyDown, new Listener() {
      @Override
      public void handleEvent(Event e) {
      }
    });
  }

  public SwtCalendar getCalendar() {
    return m_calendar;
  }

  protected void setState() {

    // clear existing widgets
    disposeWidgets();

    // map to store date, cells
    m_dateMap = new HashMap<Date, AbstractCell>();

    switch (m_calendar.getDisplayMode()) {
      case DisplayMode.DAY:

        setupDay();
        break;

      case DisplayMode.WEEK:
      case DisplayMode.WORKWEEK:

        setupWeek();
        break;

      case DisplayMode.MONTH:

        setupMonth();
        break;
    }

    layout();
  }

  protected void setupDay() {

    Calendar viewDate = m_calendar.getViewDate();
    int weekNo = viewDate.get(Calendar.WEEK_OF_YEAR);

    // create a timeline
    m_timeline = new TimelineColumn(this, SWT.NONE, false);

    // create a composite for holding individual cells
    m_cells = new Composite(this, SWT.NONE);
    GridData gd = new GridData();
    gd.grabExcessHorizontalSpace = true;
    gd.grabExcessVerticalSpace = true;
    gd.horizontalAlignment = SWT.FILL;
    gd.verticalAlignment = SWT.FILL;
    m_cells.setLayoutData(gd);
    //
    // its layout
    GridLayout layout = new GridLayout();
    layout.marginWidth = 0;
    layout.marginHeight = 0;
    layout.horizontalSpacing = 2;
    layout.verticalSpacing = 0;
    layout.numColumns = 1;
    layout.makeColumnsEqualWidth = true;
    m_cells.setLayout(layout);
    //
    // white background
    m_cells.setBackground(SwtColors.getInstance().getWhite());

    // new week cell list
    m_weekCells = new ArrayList<WeekCell>();

    // create a cell for the day
    WeekCell c = new WeekCell(m_cells, SWT.NONE, m_calendar, viewDate.getTime(), false, true);
    // keep a ref in an array
    m_weekCells.add(c);
    // hold a ref regarding its date for quick retrieval
    m_dateMap.put(viewDate.getTime(), c);

    // set date browser bar header
    DateFormat weekDayFmt = new SimpleDateFormat("EEEEE", Locale.getDefault());
    DateFormat dateFmt = new DateTimeFormatFactory().getDayMonthYear(DateFormat.LONG);
    m_calendar.setDateBrowserHeader(weekDayFmt.format(viewDate.getTime()) + " " + dateFmt.format(viewDate.getTime())
        + " - " + SwtUtility.getNlsText(Display.getCurrent(), "Week") + " " + weekNo);

    addItemsToCells();
    calcTimelessMaxCount();
    setWeekItemsLayoutData();

    m_timeline.init();

    m_cells.layout();

  }

  protected void setupWeek() {

    Calendar viewDate = m_calendar.getViewDate();
    int weekNo = viewDate.get(Calendar.WEEK_OF_YEAR);

    // new week cell list
    m_weekCells = new ArrayList<WeekCell>();

    GridData gd;

    m_emptyComposite = new Composite(this, SWT.NONE);
    m_emptyComposite.setBackground(SwtColors.getInstance().getWhite());
    gd = new GridData();
    gd.widthHint = CalendarConstants.TIMELINE_WIDTH;
    gd.heightHint = 1;
    gd.horizontalAlignment = SWT.BEGINNING;
    m_emptyComposite.setLayoutData(gd);

    // how many days?
    int nbDays;
    if (m_calendar.getDisplayMode() == DisplayMode.WORKWEEK) {
      nbDays = 5;
    }
    else {
      nbDays = 7;
    }

    // how many columns
    int nbCols = nbDays;
    if (m_calendar.getCondensedMode() && m_calendar.getDisplayMode() == DisplayMode.WEEK) {
      nbCols--;
    }

    // create day name bar (not day mode)
    if (m_calendar.getDisplayMode() != DisplayMode.DAY) {
      m_dayNames = new DayNamesBar(this, SWT.NONE, m_calendar, nbDays, m_calendar.getCondensedMode());
    }

    // create a timeline
    m_timeline = new TimelineColumn(this, SWT.NONE);

    // create a composite for holding individual cells
    m_cells = new Composite(this, SWT.NONE);
    gd = new GridData();
    gd.grabExcessHorizontalSpace = true;
    gd.grabExcessVerticalSpace = true;
    gd.horizontalAlignment = SWT.FILL;
    gd.verticalAlignment = SWT.FILL;
    m_cells.setLayoutData(gd);
    //
    // its layout
    GridLayout layout3 = new GridLayout();
    layout3.marginWidth = 0;
    layout3.marginHeight = 0;
    layout3.horizontalSpacing = 2;
    layout3.verticalSpacing = 0;
    layout3.numColumns = nbCols;
    layout3.makeColumnsEqualWidth = true;
    m_cells.setLayout(layout3);
    //
    // white background
    m_cells.setBackground(SwtColors.getInstance().getWhite());
    //
    // to make this composite focusable
    m_cells.addListener(SWT.KeyDown, new Listener() {
      @Override
      public void handleEvent(Event e) {
      }
    });

    // which month is the viewdate?
    int selectedMonth = m_calendar.getViewDate().get(Calendar.MONTH);

    // create the cells for the week
    Calendar currentDate = Calendar.getInstance();
    currentDate.setTime(m_calendar.getViewDateStart().getTime());
    for (int d = 0; d < nbDays; d++) {
      boolean cellCurrentMonth = currentDate.get(Calendar.MONTH) == selectedMonth;

      // create cell
      WeekCell cell = new WeekCell(m_cells, SWT.NONE, m_calendar, currentDate.getTime(), d == 0, cellCurrentMonth);
      // keep a ref in an array
      m_weekCells.add(cell);
      // hold a ref regarding its date for quick retrieval
      m_dateMap.put(currentDate.getTime(), cell);

      // increment date
      currentDate.add(Calendar.DATE, 1);
    }

    // set date browser bar header
    m_calendar.setDateBrowserHeader(new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(viewDate.getTime()) + " - " + SwtUtility.getNlsText(Display.getCurrent(), "Week") + " " + weekNo);

    addItemsToCells();
    calcTimelessMaxCount();
    setWeekItemsLayoutData();

    m_timeline.init();

    m_cells.layout();
  }

  /**
   * Setup the layout data of the items within each existing week cell.
   * This has to be done separately to avoid a circular dependency.
   * (the layout data depends on the max timeless count, which depends on all
   * items being present)
   */
  protected void setWeekItemsLayoutData() {
    if (m_weekCells != null) {
      for (WeekCell day : m_weekCells) {
        day.setItemsLayoutData();
      }
    }
  }

  protected void setupMonth() {

    Calendar viewDate = m_calendar.getViewDate();

    // create a first array list
    m_monthCells = new ArrayList<ArrayList<MonthCell>>();

    // nb of columns
    int nbCols;
    if (m_calendar.getCondensedMode()) {
      nbCols = 6;
    }
    else {
      nbCols = 7;
    }

    // create day name bar
    m_dayNames = new DayNamesBar(this, SWT.NONE, m_calendar, 7, m_calendar.getCondensedMode());
    ((GridData) m_dayNames.getLayoutData()).horizontalSpan = 2;

    // create a composite for holding individual cells
    m_cells = new Composite(this, SWT.NONE);
    GridData gd = new GridData();
    gd.grabExcessHorizontalSpace = true;
    gd.grabExcessVerticalSpace = true;
    gd.horizontalAlignment = SWT.FILL;
    gd.verticalAlignment = SWT.FILL;
    gd.horizontalSpan = 2;
    m_cells.setLayoutData(gd);
    //
    // its layout
    MonthCellLayout layout = new MonthCellLayout();
    layout.setNumColumns(nbCols);
    layout.setNumLines(12);
    m_cells.setLayout(layout);
    //
    // white background
    m_cells.setBackground(SwtColors.getInstance().getWhite());

    // grab starting date
    Calendar currentDate = Calendar.getInstance();
    currentDate.setTime(m_calendar.getViewDateStart().getTime());

    // which month is the viewdate?
    int selectedMonth = m_calendar.getViewDate().get(Calendar.MONTH);

    for (int w = 0; w < 6; w++) {
      ArrayList<MonthCell> week = new ArrayList<MonthCell>();

      for (int d = 0; d < 7; d++) {
        boolean cellCurrentMonth = currentDate.get(Calendar.MONTH) == selectedMonth;

        // create new cell
        MonthCell mc = new MonthCell(m_cells, SWT.NONE, m_calendar, currentDate.getTime(), d == 0, cellCurrentMonth);
        // keep a ref in an array
        week.add(mc);
        // hold a ref regarding its date for quick retrieval
        m_dateMap.put(currentDate.getTime(), mc);

        // increment date
        currentDate.add(Calendar.DATE, 1);
      }

      m_monthCells.add(week);
    }

    // set date browser bar header
    m_calendar.setDateBrowserHeader(new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(viewDate.getTime()));

    m_cells.layout();

  }

  /** get the cell which date is date */
  public AbstractCell getCellFromDate(Date d) {
    // first truncate date
    d = SwtCalendar.truncDate(d);

    // get corresponding element or null
    return m_dateMap.get(d);
  }

  /** calc the max nb of timeless items within all cells */
  protected void calcTimelessMaxCount() {

    m_timelessMaxCount = 0;

    // week or day mode
    if (m_weekCells != null) {
      for (WeekCell day : m_weekCells) {
        // update timelessMaxCount
        m_timelessMaxCount = Math.max(m_timelessMaxCount, day.getCountTimelessItems());
      }

      // month mode
    }
    else {
      for (ArrayList<MonthCell> week : m_monthCells) {
        for (MonthCell day : week) {
          // update timelessMaxCount
          m_timelessMaxCount = Math.max(m_timelessMaxCount, day.getCountTimelessItems());
        }
      }
    } // end else
  }

  /** only for week or day mode */
  protected void addItemsToCells() {
    if (m_weekCells != null) {
      for (WeekCell day : m_weekCells) {
        day.addCalendarItems();
      }
    }
  }

  /** returns the max number of timeless item in any cell */
  public int getTimelessMaxCount() {
    return m_timelessMaxCount;
  }

  /** dispose children widgets */
  protected void disposeWidgets() {

    // dispose all cells from the month cells collection
    if (m_monthCells != null) {
      for (ArrayList<MonthCell> week : m_monthCells) {
        for (MonthCell cell : week) {
          if (cell != null && !cell.isDisposed()) {
            cell.dispose();
          }
        }
      }
    }

    // same from the week cells one
    if (m_weekCells != null) {
      for (WeekCell cell : m_weekCells) {
        if (cell != null && !cell.isDisposed()) {
          cell.dispose();
        }
      }
    }

    if (m_emptyComposite != null && !m_emptyComposite.isDisposed()) {
      m_emptyComposite.dispose();
    }

    if (m_dayNames != null && !m_dayNames.isDisposed()) {
      m_dayNames.dispose();
    }

    if (m_timeline != null && !m_timeline.isDisposed()) {
      m_timeline.dispose();
    }

    m_weekCells = null;
    m_monthCells = null;
    m_dateMap = null;

    if (m_cells != null && !m_cells.isDisposed()) {
      m_cells.dispose();
    }

  }

  /** reset the item cache of each cell */
  public void reloadCalendarItems() {

    if (m_weekCells != null) {
      for (WeekCell day : m_weekCells) {
        day.reloadCalendarItems();
      }

      calcTimelessMaxCount();
      setWeekItemsLayoutData();
      m_timeline.init();
    }

    if (m_monthCells != null) {
      for (ArrayList<MonthCell> week : m_monthCells) {
        for (MonthCell day : week) {
          day.reloadCalendarItems();
        }
      }
    }

    // redo the layout
    m_cells.layout();

    // redraw
//    redraw();

    if (m_weekCells != null) {
      for (WeekCell day : m_weekCells) {
        day.layout();
      }
    }

    if (m_monthCells != null) {
      for (ArrayList<MonthCell> week : m_monthCells) {
        for (MonthCell day : week) {
          day.layout();
        }
      }
    }
  }

  /** update state of selected cell */
  public void updateSelection(Calendar selectedDate) {

    // week or day mode
    if (m_weekCells != null) {
      for (WeekCell day : m_weekCells) {
        if (selectedDate.getTime().equals(day.getDate().getTime())) {
          // set selected
          day.setSelected(true);
        }
        else {
          day.setSelected(false);
        }
      }
      // month mode
    }
    else {
      for (ArrayList<MonthCell> week : m_monthCells) {
        for (MonthCell day : week) {
          if (selectedDate.getTime().equals(day.getDate().getTime())) {
            // set selected
            day.setSelected(true);
          }
          else {
            day.setSelected(false);
          }
        }
      }
    } // end else
  }

  @Override
  public void dispose() {
    // widgets
    disposeWidgets();

    super.dispose();
  }

}
