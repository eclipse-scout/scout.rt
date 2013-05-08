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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.ui.swt.basic.calendar.CalendarConstants;
import org.eclipse.scout.rt.ui.swt.basic.calendar.CalendarModel;
import org.eclipse.scout.rt.ui.swt.basic.calendar.CalendarViewEvent;
import org.eclipse.scout.rt.ui.swt.basic.calendar.DisplayMode;
import org.eclipse.scout.rt.ui.swt.basic.calendar.EmptyCalendarModel;
import org.eclipse.scout.rt.ui.swt.basic.calendar.ICalendarViewListener;
import org.eclipse.scout.rt.ui.swt.basic.calendar.SwtColors;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * This class represents an SWT calendar widget.<br>
 * <br>
 * <b>Date concept regarding this calendar:</b>
 * <ul>
 * <li>A SwtCalendar object has a viewDate, which represent the day which we want to visualize (within a certain viewing
 * interval)</li>
 * <li>There is a view date start and view date end (m_viewDateStart, m_viewDateEnd) representing the visualized period</li>
 * <li>We find additionnaly and optionnaly a selected date (m_selectedDate)</li>
 * <li>When a new JCalendar object is created, the view date is set to today</li>
 * <li>If the display mode is changed, the view date start/end is recalculated</li>
 * <li>If the current view date is changed, the view date start/end are also recalculated</li>
 * </ul>
 * 
 * @author Michael Rudolf, Andreas Hoegger
 */
public class SwtCalendar extends Composite implements PaintListener {
  protected static final IScoutLogger LOG = ScoutLogManager.getLogger(SwtCalendar.class);

  private ArrayList<ICalendarViewListener> calendarViewListeners;

  private int m_workDayCount = 5;

  private Map m_itemMap;

  /** selected date */
  private Calendar m_selectedDate;

  /** current calendar date */
  private Calendar m_viewDate;
  /** start calendar date */
  private Calendar m_viewDateStart;
  /** end calendar date */
  private Calendar m_viewDateEnd;

  private CalendarModel m_model;
  private Object m_selectedItem;
  private boolean m_calendarUpdating;

  /** date browser bar to set the date backward/forward */
  private DateBrowserBar m_browserBar;

  /** central composite to hold the calendar area */
  private Composite m_centralComposite;

  /** reference to the calendar area (child of m_centralComposite) */
  private CentralPanel m_calendarPanel;

  /** selection scope widget (month, week, work week, day */
  private SelectionScopeBar m_selectionScope;

  /** workingHour settings **/
  private int m_startHour = CalendarConstants.DAY_TIMELINE_START_TIME;
  private int m_endHour = CalendarConstants.DAY_TIMELINE_END_TIME;
  private boolean m_useOverflowCells = true;
  private boolean m_markNoonHour = true;
  private boolean m_markOutOfMonthDays = true;

  public SwtCalendar(Composite parent, int style) {
    super(parent, style);

    // just create a dummy model
    m_model = new EmptyCalendarModel();

    // create listener arrays
    calendarViewListeners = new ArrayList<ICalendarViewListener>();
    createControls();
    addPaintListener(this);
    // set date to now + refresh layout
    setToday();

  }

  protected void createControls() {

    // layout and layout data for this component
    GridLayout toplayout = new GridLayout();
    toplayout.numColumns = 1;
    toplayout.verticalSpacing = 1;
    toplayout.marginHeight = 2;
    toplayout.marginWidth = 2;
    this.setLayout(toplayout);
    //
    GridData gd = new GridData();
    gd.grabExcessHorizontalSpace = true;
    gd.grabExcessVerticalSpace = true;
    gd.horizontalAlignment = GridData.FILL;
    gd.verticalAlignment = GridData.FILL;
    this.setLayoutData(gd);

    // new date browser bar
    m_browserBar = new DateBrowserBar(this, SWT.NONE);

    // main calendar area
    m_centralComposite = new Composite(this, SWT.NONE);
    gd = new GridData();
    gd.grabExcessHorizontalSpace = true;
    gd.grabExcessVerticalSpace = true;
    gd.horizontalAlignment = GridData.FILL;
    gd.verticalAlignment = GridData.FILL;
    m_centralComposite.setLayoutData(gd);
    //
    GridLayout layout = new GridLayout();
    layout.numColumns = 1;
    layout.verticalSpacing = 1;
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    m_centralComposite.setLayout(layout);

    // scope selection at the bottom
    m_selectionScope = new SelectionScopeBar(this, SWT.NONE);

  }

  /** force a reload of all items within calendar cells */
  protected void reloadCalendarItems() {
    m_calendarPanel.reloadCalendarItems();
  }

  public void refreshLayout() {
    //logger.debug("Refreshing layout...");

    // get rid of old panel
    if (m_calendarPanel != null && !m_calendarPanel.isDisposed()) {
      m_calendarPanel.dispose();
    }

    // create a new one
    m_calendarPanel = new CentralPanel(m_centralComposite, SWT.NONE, this);

    // set state of panel
    m_calendarPanel.setState();

    // update selection
    m_calendarPanel.updateSelection(m_selectedDate);

    // redraw calendar panel
    m_centralComposite.layout();
    layout(true);

  }

  public void setModel(CalendarModel model) {
    m_model = model;
    modelChanged();
  }

  public CalendarModel getModel() {
    return m_model;
  }

  public Composite getCentralComposite() {
    return m_centralComposite;
  }

  public CentralPanel getCentralPanel() {
    return m_calendarPanel;
  }

  protected void fireSelectionChanged() {
    if (calendarViewListeners != null && calendarViewListeners.size() > 0) {
      Date viewDate = getViewDate().getTime();
      Date viewDateStart = getViewDateStart().getTime();
      Date viewDateEnd = getViewDateEnd().getTime();
      CalendarViewEvent e = new CalendarViewEvent(this, CalendarViewEvent.TYPE_SELECTION_CHANGED, viewDate, viewDateStart, viewDateEnd);
      for (int i = 0; i < calendarViewListeners.size(); i++) {
        (calendarViewListeners.get(i)).viewChanged(e);
      }
    }
  }

  protected void fireViewDateChanged() {
    if (calendarViewListeners != null && calendarViewListeners.size() > 0) {
      Date viewDate = getViewDate().getTime();
      Date viewDateStart = getViewDateStart().getTime();
      Date viewDateEnd = getViewDateEnd().getTime();
      CalendarViewEvent e = new CalendarViewEvent(this, CalendarViewEvent.TYPE_VISIBLE_RANGE_CHANGED, viewDate, viewDateStart, viewDateEnd);
      for (int i = 0; i < calendarViewListeners.size(); i++) {
        (calendarViewListeners.get(i)).viewChanged(e);
      }
    }
  }

  /**
   * @param item
   *          some calendar item type, e.g. CalendarItem
   */
  public void setSelectedItem(Object item) {
    Object oldItem = m_selectedItem;
    m_selectedItem = item;
    CalendarModel model = getModel();
    // do we need to repaint the old item range?
    if (oldItem != null) {
      Date a = model.getFromDate(oldItem);
      Date b = model.getToDate(oldItem);
      repaintCellRange(a, b);
      // do we need to repaint the new item range?
    }
    if (item != null) {
      Date a = model.getFromDate(item);
      Date b = model.getToDate(item);
      repaintCellRange(a, b);
    }

    fireSelectionChanged();
  }

  protected void repaintCellRange(Date a, Date b) {
    if (b == null) {
      b = a;
    }
    Calendar cal = Calendar.getInstance();
    cal.setTime(truncDate(a));
    while (cal.getTime().compareTo(b) <= 0) {
      AbstractCell cell = m_calendarPanel.getCellFromDate(cal.getTime());
      // repaint?
      if (cell != null) {
        cell.redraw();
      }

      cal.add(Calendar.DATE, 1);
    }
  }

  /** @return a Calendar object corresponding to the given date */
  public static Calendar getCalendarFromDate(Date d) {
    Calendar c = Calendar.getInstance();
    c.setTime(d);
    return c;
  }

  /** @return some calendar item type, e.g. CalendarItem */
  public Object getSelectedItem() {
    return m_selectedItem;
  }

  public void modelChanged() {
    LOG.debug("Reloading items...");

    // reset
    if (m_calendarPanel != null) {
      m_calendarPanel.reloadCalendarItems();
    }
  }

  /**
   * @param showDisplayModeSelection
   */
  public void setShowDisplayModeSelection(boolean showDisplayModeSelection) {
    m_selectionScope.setVisible(showDisplayModeSelection);
    ((GridData) m_selectionScope.getLayoutData()).exclude = !showDisplayModeSelection;
  }

  public void setWorkingHours(int startHour, int endHour, boolean useOverflowCells) {
    m_startHour = startHour;
    // to keep swt in sync with swing, swt needs an extra hour to draw timeline
    m_endHour = endHour + 1;
    m_useOverflowCells = useOverflowCells;
  }

  public void setMarkNoonHour(boolean markNoonHour) {
    m_markNoonHour = markNoonHour;
  }

  public void setMarkOutOfMonthDays(boolean markOutOfMonthDays) {
    m_markOutOfMonthDays = markOutOfMonthDays;
  }

  public int getStartHour() {
    return m_startHour;
  }

  public int getEndHour() {
    return m_endHour;
  }

  public boolean getUseOverflowCells() {
    return m_useOverflowCells;
  }

  public boolean getMarkNoonHour() {
    return m_markNoonHour;
  }

  public boolean getMarkOutOfMonthDays() {
    return m_markOutOfMonthDays;
  }

  public void setDisplayMode(int newMode) {
    // check argument
    if (newMode != DisplayMode.MONTH &&
        newMode != DisplayMode.WEEK &&
        newMode != DisplayMode.WORKWEEK &&
        newMode != DisplayMode.DAY) {
      throw new IllegalArgumentException("illegal display mode: " + newMode);
    }

    // changed?
    int oldMode = m_selectionScope.getDisplayMode();
    if (oldMode == newMode) {
      return;
    }

    // set new mode
    m_selectionScope.setDisplayMode(newMode);

    // calc start/end date
    calcViewDateInterval();

    LOG.debug("displayMode set to " + newMode);
  }

  public int getDisplayMode() {
    return m_selectionScope.getDisplayMode();
  }

  public void setCondensedMode(boolean mode) {

    // changed?
    boolean oldMode = m_selectionScope.getCondensedMode();
    if (oldMode == mode) {
      return;
    }

    // set new condensed mode
    m_selectionScope.setCondensedMode(mode);

    LOG.debug("set to condensed mode " + mode);
  }

  public boolean getCondensedMode() {
    return m_selectionScope.getCondensedMode();
  }

  public void setFirstDayOfWeek(int day) {

    if (day < 1 || day > 7) {
      throw new IllegalArgumentException("Illegal first day of week: " + day);
    }

    // changed?
    int oldDay = m_selectionScope.getFirstDayOfWeek();
    if (oldDay == day) {
      return;
    }

    // set new day
    //m_selectionScope.setFirstDayOfWeek(day);

    // calc start/end date
    calcViewDateInterval();

    LOG.debug("first day of week set to " + day);
  }

  /** set the header of the browser bar */
  public void setDateBrowserHeader(String header) {
    if (m_browserBar != null) {
      m_browserBar.setHeaderText(header);
      m_browserBar.layout(true);
    }
  }

  public int getFirstDayOfWeek() {
    return m_selectionScope.getFirstDayOfWeek();
  }

  public void addCalendarViewListener(ICalendarViewListener listener) {
    calendarViewListeners.add(listener);
  }

  public void removeCalendarViewListener(ICalendarViewListener listener) {
    calendarViewListeners.remove(listener);
  }

  // --- end listener handling

  /**
   * do nothing in the JCalendar version, needs to be overriden to do smth
   * e.g. in SWTScoutCalendar
   */
  public void showGeneralContextMenu(IMenuManager manager) {
  }

  /**
   * do nothing in the JCalendar version, needs to be overriden to do smth
   * e.g. in SWTScoutCalendar
   */
  public void showItemContextMenu(IMenuManager manager, Object item) {
  }

  @Override
  public void paintControl(PaintEvent e) {
    setBackground(SwtColors.getInstance().getWhite());
    setForeground(SwtColors.getInstance().getDarkgray());

    drawBorders(e);
  }

  protected void drawBorders(PaintEvent e) {
    Rectangle bounds = getBounds();
    e.gc.drawRectangle(0, 0, bounds.width - 1, bounds.height - 1);
  }

  /** has the view date interval changed, i.e. view date start/end */
  protected boolean changedViewDateInterval(Calendar newViewDate) {

    switch (getDisplayMode()) {
      case DisplayMode.DAY:
        return !newViewDate.equals(m_viewDate);

      case DisplayMode.WEEK:
      case DisplayMode.WORKWEEK:
        // date start
        Calendar viewDateStart = Calendar.getInstance();
        viewDateStart.setTime(newViewDate.getTime());
        // go back to first day of week
        viewDateStart.add(Calendar.DAY_OF_WEEK, -((viewDateStart.get(Calendar.DAY_OF_WEEK) - getFirstDayOfWeek() + 7) % 7));

        // date start changed?
        return !viewDateStart.equals(m_viewDateStart);

      case DisplayMode.MONTH:
        // date start
        viewDateStart = Calendar.getInstance();
        viewDateStart.setTime(newViewDate.getTime());
        //Calculate Startdate; go back to 1st of month, then back to 1st day of week (1=sunday)
        viewDateStart.add(Calendar.DAY_OF_MONTH, -(viewDateStart.get(Calendar.DAY_OF_MONTH) - 1));
        viewDateStart.add(Calendar.DAY_OF_WEEK, -((viewDateStart.get(Calendar.DAY_OF_WEEK) - getFirstDayOfWeek() + 7) % 7));

        // date start changed?
        return !viewDateStart.equals(m_viewDateStart);

      default:
        LOG.error("Should not reach this case");
        return false;
    }

  }

  /** calculate view date start/end corresponding to the truncated current viewdate */
  protected void calcViewDateInterval() {

    switch (getDisplayMode()) {
      case DisplayMode.DAY:
        m_viewDateStart = Calendar.getInstance();
        m_viewDateStart.setTime(m_viewDate.getTime());
        m_viewDateEnd = (Calendar) m_viewDateStart.clone();
        m_viewDateEnd.set(Calendar.HOUR, 23);
        m_viewDateEnd.set(Calendar.MINUTE, 59);

        break;
      case DisplayMode.WEEK:
      case DisplayMode.WORKWEEK:

        // date start
        m_viewDateStart = Calendar.getInstance();
        m_viewDateStart.setTime(m_viewDate.getTime());
        // go back to first day of week
        m_viewDateStart.add(Calendar.DAY_OF_WEEK, -((m_viewDateStart.get(Calendar.DAY_OF_WEEK) - getFirstDayOfWeek() + 7) % 7));

        // date end
        m_viewDateEnd = Calendar.getInstance();
        m_viewDateEnd.setTime(m_viewDateStart.getTime());
        // add the corresponding number of days
        m_viewDateEnd.add(Calendar.DATE, getDisplayMode() == DisplayMode.WEEK ? 7 : 5);

        break;
      case DisplayMode.MONTH:

        // date start
        m_viewDateStart = Calendar.getInstance();
        m_viewDateStart.setTime(m_viewDate.getTime());
        //Calculate Startdate; go back to 1st of month, then back to 1st day of week (1=sunday)
        m_viewDateStart.add(Calendar.DAY_OF_MONTH, -(m_viewDateStart.get(Calendar.DAY_OF_MONTH) - 1));
        m_viewDateStart.add(Calendar.DAY_OF_WEEK, -((m_viewDateStart.get(Calendar.DAY_OF_WEEK) - getFirstDayOfWeek() + 7) % 7));

        // date end
        m_viewDateEnd = Calendar.getInstance();
        m_viewDateEnd.setTime(m_viewDateStart.getTime());
        // the month view shows 42 days, so add 41 days to start date
        m_viewDateEnd.add(Calendar.DATE, 41);

        break;
    }
  }

  /** shift view date, same semantic than Calendar.add */
  protected void shiftViewDate(int type, int amount) {
    shiftViewDate(type, amount, true);
  }

  /**
   * @param type
   * @param amount
   * @param fireNotification
   *          set false to avoid circular firing.
   * @since 07.02.2006 - tha@bsiag.com
   */
  protected void shiftViewDate(int type, int amount, boolean fireNotification) {
    m_viewDate.add(type, amount);

    // calc new start/end dates
    calcViewDateInterval();

    LOG.debug("new date: " + m_viewDate.getTime().toString());

  }

  /** set view date to the truncated date of now */
  public void setToday() {
    // set view date to now
    m_viewDate = truncDate(Calendar.getInstance());

    LOG.debug("new date: " + m_viewDate.getTime().toString());

    // calculate view date start/end
    calcViewDateInterval();

    // set selected date
    setSelectedDate(m_viewDate);

    refreshLayout();
  }

  /** set view date to the given calendar date, truncate it */
  public void setViewDate(Calendar c) {

    // null check
    if (c == null) {
      if (m_viewDate != null) {
        return;
      }
      c = Calendar.getInstance();
    }

    // changed?
    c = truncDate(c);
    if (m_viewDate.equals(c)) {
      return;
    }

    // for refresh
    boolean showingScopeChanged = changedViewDateInterval(c);

    // set view date
    m_viewDate = c;

    //logger.debug("new date: " + m_viewDate.getTime().toString());

    // calculate view date start/end
    calcViewDateInterval();

    if (showingScopeChanged) {
      refreshLayout();
    }

  }

  /** get view date, truncated */
  public Calendar getViewDate() {
    return m_viewDate;
  }

  /** get view date start, truncated */
  public Calendar getViewDateStart() {
    return m_viewDateStart;
  }

  /** get view date end, truncated */
  public Calendar getViewDateEnd() {
    return m_viewDateEnd;
  }

  public static Date truncDate(Date d) {
    Calendar c = Calendar.getInstance();
    c.setTime(d);
    return truncDate(c).getTime();
  }

  public static Calendar truncDate(Calendar c) {
    if (c == null) {
      return null;
    }
    c.set(Calendar.HOUR, 0);
    c.set(Calendar.HOUR_OF_DAY, 0);
    c.set(Calendar.MINUTE, 0);
    c.set(Calendar.SECOND, 0);
    c.set(Calendar.MILLISECOND, 0);
    return c;
  }

  public void prevDay() {
    shiftViewDate(Calendar.DATE, -1);
  }

  public void prevWeek() {
    shiftViewDate(Calendar.WEEK_OF_YEAR, -1);
  }

  public void prevMonth() {
    shiftViewDate(Calendar.MONTH, -1);
  }

  public void prevYear() {
    shiftViewDate(Calendar.YEAR, -1);
  }

  public void nextDay() {
    shiftViewDate(Calendar.DATE, 1);
  }

  public void nextWeek() {
    shiftViewDate(Calendar.WEEK_OF_YEAR, 1);
  }

  public void nextMonth() {
    shiftViewDate(Calendar.MONTH, 1);
  }

  public void nextYear() {
    shiftViewDate(Calendar.YEAR, 1);
  }

  /** set selection to the given date c */
  public void setSelectedDate(Calendar c) {
    c = truncDate(c);
    if (c != null) {
      m_selectedDate = c;
    }
  }

  public void setSelectedDateFromUI(Calendar c) {
    setSelectedDate(c);
  }

  /** update selection with the set selected date */
  public void updateSelection() {
    if (m_calendarPanel != null) {
      m_calendarPanel.updateSelection(m_selectedDate);
    }
  }

  public Calendar getSelectedDate() {
    return m_selectedDate;
  }

  public void fastBackward() {
    LOG.debug("fast backward");

    switch (m_selectionScope.getDisplayMode()) {
      case DisplayMode.MONTH:
        prevYear();
        break;
      case DisplayMode.WEEK:
      case DisplayMode.WORKWEEK:
        prevMonth();
        break;
      case DisplayMode.DAY:
      default:
        prevMonth();
    }
  }

  public void backward() {
    LOG.debug("backward");

    switch (m_selectionScope.getDisplayMode()) {
      case DisplayMode.MONTH:
        prevMonth();
        break;
      case DisplayMode.WEEK:
      case DisplayMode.WORKWEEK:
        prevWeek();
        break;
      case DisplayMode.DAY:
      default:
        prevDay();
    }
  }

  public void forward() {
    LOG.debug("forward");

    switch (m_selectionScope.getDisplayMode()) {
      case DisplayMode.MONTH:
        nextMonth();
        break;
      case DisplayMode.WEEK:
      case DisplayMode.WORKWEEK:
        nextWeek();
        break;
      case DisplayMode.DAY:
      default:
        nextDay();
    }
  }

  public void fastForward() {
    LOG.debug("fast forward");

    switch (m_selectionScope.getDisplayMode()) {
      case DisplayMode.MONTH:
        nextYear();
        break;
      case DisplayMode.WEEK:
      case DisplayMode.WORKWEEK:
        nextMonth();
        break;
      case DisplayMode.DAY:
      default:
        nextMonth();
    }
  }

  @Override
  public void dispose() {
    // dispose all contained swt widgets
    if (m_browserBar != null && !m_browserBar.isDisposed()) {
      m_browserBar.dispose();
    }

    if (m_centralComposite != null && !m_centralComposite.isDisposed()) {
      m_centralComposite.dispose();
    }

    if (m_calendarPanel != null && !m_calendarPanel.isDisposed()) {
      m_calendarPanel.dispose();
    }

    if (m_selectionScope != null && !m_selectionScope.isDisposed()) {
      m_selectionScope.dispose();
    }

    super.dispose();
  }
}
