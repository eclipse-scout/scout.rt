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
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Locale;
import java.util.TreeSet;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.MatteBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.rt.client.ui.basic.calendar.DateTimeFormatFactory;
import org.eclipse.scout.rt.client.ui.basic.calendar.ICalendar;
import org.eclipse.scout.rt.ui.swing.SwingUtility;
import org.eclipse.scout.rt.ui.swing.ext.FlowLayoutEx;
import org.eclipse.scout.rt.ui.swing.ext.JHyperlink;
import org.eclipse.scout.rt.ui.swing.ext.JPanelEx;

public class DateChooser {
  public static final int DISPLAY_MODE_DAY = ICalendar.DISPLAY_MODE_DAY;
  public static final int DISPLAY_MODE_WEEK = ICalendar.DISPLAY_MODE_WEEK;
  public static final int DISPLAY_MODE_MONTH = ICalendar.DISPLAY_MODE_MONTH;
  public static final int DISPLAY_MODE_WORKWEEK = ICalendar.DISPLAY_MODE_WORKWEEK;

  public static final int MIN_CELLPANEL_HEIGHT = 150;

  private int m_displayMode = DISPLAY_MODE_MONTH;
  private int m_timelessHeight = 0;
  private boolean m_largeVersion;
  private boolean m_multiSelect = false;
  private final int m_firstDayOfWeek = Calendar.getInstance().getFirstDayOfWeek();
  private int m_workDayCount = 5;
  private EventListenerList m_listenerList;
  private JPanel m_container;
  private JPanel m_headerPanel;//buttons and month label
  private JLabel m_monthLabel;
  private JPanel m_daysPanel;// 7 items
  private JPanel m_cellsPanel;// 7-42 items
  private AbstractCalendarCell[][] m_cell;// 7*6=42 items
  private TreeSet<Date> m_selectedDates = new TreeSet<Date>();
  private Date m_viewDate, m_viewDateStart, m_viewDateEnd;
  private HashMap<Date, AbstractCalendarCell> m_dateMap;
  private CalendarModel m_model;
  // large calendar control
  private JPanel m_controlPanel;//radio buttons
  private JHyperlink m_linkDay;
  private JHyperlink m_linkWorkWeek;
  private JHyperlink m_linkWeek;
  private JHyperlink m_linkMonth;
  //
  private Object m_selectedItem;
  protected Border m_cellBorder;
  private DateChooser m_parentCalendar;
  private DateChooser m_childCalendar;
  private boolean m_calendarUpdating;

  private int m_startHour = 6;
  private int m_endHour = 19;
  private boolean m_useOverflowCells = true;
  private DateFormat m_formatHHMM;
  private boolean m_markOutOfMonthDays;
  private boolean m_markNoonHour = true;

  public DateChooser() {
    this(false);
  }

  public DateChooser(Object[] params) {
    this(false, DISPLAY_MODE_MONTH, false, params);
  }

  public DateChooser(boolean largeVersion) {
    this(largeVersion, DISPLAY_MODE_MONTH);
  }

  public DateChooser(boolean largeVersion, int displayMode) {
    this(largeVersion, displayMode, false);
  }

  public DateChooser(boolean largeVersion, int displayMode, boolean displayCondensed) {
    this(largeVersion, displayMode, displayCondensed, null);
  }

  public DateChooser(boolean largeVersion, int displayMode, boolean displayCondensed, Object[] params) {
    super();
    m_listenerList = new EventListenerList();
    preConstructorInitialization(params);
    initializationByConstructor(largeVersion, displayMode, displayCondensed);
    m_formatHHMM = new DateTimeFormatFactory().getHourMinute();
  }

  /**
   * is overridden by subclasses for pre-constructor initialization.
   * 
   * @param params
   *          may be null!
   * @since 07.02.2006 - tha@bsiag.com
   */
  protected void preConstructorInitialization(Object[] params) {
  }

  /**
   * @param largeVersion
   * @param displayMode
   * @param displayCondensed
   * @since 07.02.2006 - tha@bsiag.com
   */
  private void initializationByConstructor(boolean largeVersion, int displayMode, boolean displayCondensed) {
    m_container = new JPanelEx(createLayout(largeVersion));
    m_container.setBorder(null);
    m_viewDate = truncDate(new Date());
    m_model = new EmptyCalendarModel();

    displayMode = Math.min(3, Math.max(displayMode, 1));
    m_displayMode = displayMode;
    m_largeVersion = largeVersion;
    m_dateMap = new HashMap<Date, AbstractCalendarCell>();

    Color focusColor = UIManager.getColor("Table.focusCellForeground");
    if (largeVersion) {
      m_cellBorder = new LargeCalendarCellBorder(focusColor);
    }
    else {
      m_cellBorder = new SmallCalendarCellBorder(focusColor);
    }

    m_headerPanel = createHeaderPanel();
    m_daysPanel = createDaysPanel();
    m_cellsPanel = createCellsPanel();

    m_container.add(m_headerPanel);
    m_container.add(m_daysPanel);
    m_container.add(m_cellsPanel);

    if (m_largeVersion) {
      m_controlPanel = createControlPanel();
      m_container.add(m_controlPanel);
      updateControlPanelState();
    }

    reconfigureLayout();
    // date click and update controller
    m_container.addMouseListener(new DateController());
    // set Date to now
    setDate(new Date());
  }

  public JPanel getContainer() {
    return m_container;
  }

  /**
   * Returns the layout provided by the L/F or the default layout of JCalendar.
   * 
   * @param largeVersion
   * @param displayMode
   * @return
   */
  protected LayoutManager2 createLayout(boolean largeVersion) {
    if (largeVersion) {
      return new LargeCalendarLayout(this);
    }
    else {
      return new SmallCalendarLayout(this);
    }
  }

  protected JPanel createCellsPanel() {
    JPanel cellsPanel = new JPanelEx();
    cellsPanel.setBackground(new Color(0xf8f8f8));
    return cellsPanel;
  }

  protected JPanel createDaysPanel() {
    JPanel daysPanel = new JPanelEx();
    daysPanel.setLayout(new GridLayout(1, -1));
    if (UIManager.getColor("Calendar.weekDays.foreground") != null) {
      daysPanel.setForeground(UIManager.getColor("Calendar.weekDays.foreground"));
    }
    if (UIManager.getColor("Calendar.weekDays.background") != null) {
      daysPanel.setBackground(UIManager.getColor("Calendar.weekDays.background"));
    }
    daysPanel.setOpaque(false);
    return daysPanel;
  }

  protected JLabel createDayLabel(String text) {
    JLabel label = createCenterLabel(text);
    Font f = label.getFont();
    if (f != null) {
      label.setFont(new Font(f.getFamily(), Font.BOLD, f.getSize()));
    }
    return label;
  }

  protected JPanel createHeaderPanel() {
    JPanel headerPanel = new JPanelEx(new FlowLayoutEx(FlowLayoutEx.HORIZONTAL, FlowLayoutEx.CENTER, 0, 0));
    ((FlowLayoutEx) headerPanel.getLayout()).setFillHorizontal(true);
    // <<,<,>,>>
    JButton b = createIconButton(new ArrowIcon(-2));
    b.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (m_displayMode == DISPLAY_MODE_MONTH) {
          prevYear();
        }
        else if (m_displayMode == DISPLAY_MODE_WEEK) {
          prevMonth();
        }
        else if (m_displayMode == DISPLAY_MODE_WORKWEEK) {
          prevMonth();
        }
        else {
          prevWeek();
        }
      }
    });
    headerPanel.add(b);
    //
    b = createIconButton(new ArrowIcon(-1));
    b.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (m_displayMode == DISPLAY_MODE_MONTH) {
          prevMonth();
        }
        else if (m_displayMode == DISPLAY_MODE_WEEK) {
          prevWeek();
        }
        else if (m_displayMode == DISPLAY_MODE_WORKWEEK) {
          prevWeek();
        }
        else {
          prevDay();
        }
      }
    });
    headerPanel.add(b);
    //
    headerPanel.add(SwingUtility.createGlue(0, 0, true, false));
    //
    m_monthLabel = createCenterLabel(null);
    m_monthLabel.setFont(UIManager.getFont("Calendar.monthYear.font"));
    m_monthLabel.setForeground(UIManager.getColor("Calendar.monthYear.foreground"));
    m_monthLabel.setOpaque(false);
    headerPanel.add(m_monthLabel);
    //
    headerPanel.add(SwingUtility.createGlue(0, 0, true, false));
    //
    b = createIconButton(new ArrowIcon(1));
    b.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (m_displayMode == DISPLAY_MODE_MONTH) {
          nextMonth();
        }
        else if (m_displayMode == DISPLAY_MODE_WEEK) {
          nextWeek();
        }
        else if (m_displayMode == DISPLAY_MODE_WORKWEEK) {
          nextWeek();
        }
        else {
          nextDay();
        }
      }
    });
    headerPanel.add(b);
    //
    b = createIconButton(new ArrowIcon(2));
    b.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (m_displayMode == DISPLAY_MODE_MONTH) {
          nextYear();
        }
        else if (m_displayMode == DISPLAY_MODE_WEEK) {
          nextMonth();
        }
        else if (m_displayMode == DISPLAY_MODE_WORKWEEK) {
          nextMonth();
        }
        else {
          nextWeek();
        }
      }
    });
    headerPanel.add(b);
    return headerPanel;
  }

  protected JPanel createControlPanel() {
    JPanel controlPanel = new JPanelEx(new FlowLayoutEx(FlowLayoutEx.HORIZONTAL, FlowLayoutEx.LEFT, 0, 0));
    ((FlowLayoutEx) controlPanel.getLayout()).setFillHorizontal(false);
    //
    m_linkDay = new JHyperlink();
    m_linkDay.setText(SwingUtility.getNlsText("Day"));
    m_linkDay.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        setDisplayMode(DISPLAY_MODE_DAY);
      }
    });
    controlPanel.add(m_linkDay);
    //
    m_linkWorkWeek = new JHyperlink();
    m_linkWorkWeek.setText(SwingUtility.getNlsText("WorkWeek"));
    m_linkWorkWeek.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        setDisplayMode(DISPLAY_MODE_WORKWEEK);
      }
    });
    controlPanel.add(m_linkWorkWeek);
    //
    m_linkWeek = new JHyperlink();
    m_linkWeek.setText(SwingUtility.getNlsText("Week"));
    m_linkWeek.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        setDisplayMode(DISPLAY_MODE_WEEK);
      }
    });
    controlPanel.add(m_linkWeek);
    //
    m_linkMonth = new JHyperlink();
    m_linkMonth.setText(SwingUtility.getNlsText("Month"));
    m_linkMonth.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        setDisplayMode(DISPLAY_MODE_MONTH);
      }
    });
    controlPanel.add(m_linkMonth);
    //
    return controlPanel;
  }

  protected JButton createIconButton(Icon icon) {
    JButton button = new JButton(icon);
    button.setContentAreaFilled(false);
    button.setMargin(new Insets(2, 2, 2, 2));
    button.setOpaque(false);
    button.setFocusPainted(false);
    button.setName("Synth.IconButton");
    return button;
  }

  protected JLabel createCenterLabel(String text) {
    JLabel label = new JLabel(text);
    label.setHorizontalAlignment(SwingConstants.CENTER);
    return label;
  }

  public void setModel(CalendarModel newModel) {
    m_model = newModel;
    modelChanged();
  }

  public CalendarModel getModel() {
    return m_model;
  }

  public void modelChanged() {
    // reset all cells item cache
    for (int i = 0; i < m_cell.length; i++) {
      for (int k = 0; k < m_cell[i].length; k++) {
        m_cell[i][k].resetItemCache();
      }
    }
    m_container.revalidate();
    m_container.repaint();
  }

  public DateChooser getParentCalendar() {
    return m_parentCalendar;
  }

  public void setParentCalendar(DateChooser cal) {
    if (this.m_parentCalendar != null) {
      m_parentCalendar.m_childCalendar = null;
    }
    this.m_parentCalendar = cal;
    if (cal != null) {
      cal.m_childCalendar = this;
    }
    fireParentAndChildCalendarShift();
  }

  public DateChooser getChildCalendar() {
    return m_childCalendar;
  }

  public void setChildCalendar(DateChooser cal) {
    if (this.m_childCalendar != null) {
      m_childCalendar.m_parentCalendar = null;
    }
    this.m_childCalendar = cal;
    if (cal != null) {
      cal.m_parentCalendar = this;
    }
    fireParentAndChildCalendarShift();
  }

  public boolean isCalendarUpdating() {
    return m_calendarUpdating;
  }

  public void setSelectedItem(Object item) {
    if (item != m_selectedItem) {
      Object oldItem = m_selectedItem;
      m_selectedItem = item;
      CalendarModel model = getModel();
      if (oldItem != null) {
        repaintCellRange(model.getFromDate(oldItem), model.getToDate(oldItem));
      }
      if (item != null) {
        repaintCellRange(model.getFromDate(item), model.getToDate(item));
      }
      fireSelectionChanged();
    }
  }

  public Object getSelectedItem() {
    return m_selectedItem;
  }

  public boolean isLargeVersion() {
    return m_largeVersion;
  }

  public AbstractCalendarCell createCalendarCell() {
    if (m_largeVersion) {
      LargeCalendarCell cell = new LargeCalendarCell(this);
      return cell;
    }
    else {
      return new SmallCalendarCell(this);
    }
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

  public void setDate(Date d) {
    Date d2 = truncDate(d);
    setViewDate(d2);
    setSelectedDate(d2);
  }

  public Date getDate() {
    if (m_selectedDates.size() > 0) {
      return m_selectedDates.first();
    }
    else {
      return null;
    }
  }

  public Date[] getSelectedDates() {
    return m_selectedDates.toArray(new Date[0]);
  }

  public int getTimelessSectionHeightHint() {
    return m_timelessHeight;
  }

  public void updateTimelessSectionHeightHint() {
    if (getDisplayMode() != DISPLAY_MODE_MONTH) {
      int timelessMaxCount = 0;
      if (m_cell.length > 0) {
        for (AbstractCalendarCell cell : m_cell[0]) {
          if (cell != null) {
            timelessMaxCount = Math.max(timelessMaxCount, cell.getTimelessItemCount());
          }
        }
      }
      m_timelessHeight = Math.min(33 * m_cellsPanel.getHeight() / 100, 24 * timelessMaxCount);
    }
  }

  public AbstractCalendarCell getCalendarCellFor(MouseEvent e) {
    Component c = SwingUtilities.getDeepestComponentAt((Component) e.getSource(), e.getX(), e.getY());
    if (c instanceof AbstractCalendarCell) {
      return (AbstractCalendarCell) c;
    }
    c = SwingUtilities.getAncestorOfClass(AbstractCalendarCell.class, c);
    return (AbstractCalendarCell) c;
  }

  public Object getCalendarItemFor(MouseEvent e) {
    AbstractCalendarCell cell = getCalendarCellFor(e);
    if (cell != null) {
      Point p = SwingUtilities.convertPoint((Component) e.getSource(), e.getPoint(), (Component) cell);
      return cell.getItemAt(p);
    }
    return null;
  }

  public void setDisplayMode(int newMode) {
    if (newMode != DISPLAY_MODE_MONTH &&
        newMode != DISPLAY_MODE_WEEK &&
        newMode != DISPLAY_MODE_WORKWEEK &&
        newMode != DISPLAY_MODE_DAY) {
      throw new IllegalArgumentException("illegal display mode: " + newMode);
    }
    int oldMode = m_displayMode;
    if (oldMode == newMode) {
      return;
    }
    m_displayMode = newMode;
    Date base = getDate();
    if (base != null) {
      m_viewDate = base;
    }
    updateControlPanelState();
    // recalculate and relayout
    reconfigureLayout();
    fireSetupChanged();
  }

  protected void updateControlPanelState() {
    m_linkDay.setEnabled(m_displayMode != DISPLAY_MODE_DAY);
    m_linkWorkWeek.setEnabled(m_displayMode != DISPLAY_MODE_WORKWEEK);
    m_linkWeek.setEnabled(m_displayMode != DISPLAY_MODE_WEEK);
    m_linkMonth.setEnabled(m_displayMode != DISPLAY_MODE_MONTH);
    //
    m_linkDay.setBold(!m_linkDay.isEnabled());
    m_linkWorkWeek.setBold(!m_linkWorkWeek.isEnabled());
    m_linkWeek.setBold(!m_linkWeek.isEnabled());
    m_linkMonth.setBold(!m_linkMonth.isEnabled());
  }

  public int getDisplayMode() {
    return m_displayMode;
  }

  public void setWorkDayCount(int n) {
    if (n < 0 || n > 7) {
      return;// ignore it
    }
    m_workDayCount = n;
    updateStates();
    m_container.repaint();
  }

  public int getWorkDayCount() {
    return m_workDayCount;
  }

  /**
   * fires whenever a date is picked, even if it is the current selection
   */
  public void addChangeListener(ChangeListener listener) {
    m_listenerList.add(ChangeListener.class, listener);
  }

  public void removeChangeListener(ChangeListener listener) {
    m_listenerList.remove(ChangeListener.class, listener);
  }

  /**
   * force changed event
   */
  public void doChanged() {
    fireChangedEvent();
  }

  private void fireChangedEvent() {
    EventListener[] a = m_listenerList.getListeners(ChangeListener.class);
    if (a != null && a.length > 0) {
      ChangeEvent e = new ChangeEvent(this);
      for (int i = 0; i < a.length; i++) {
        ((ChangeListener) a[i]).stateChanged(e);
      }
    }
  }

  public void addCalendarViewListener(CalendarViewListener listener) {
    m_listenerList.add(CalendarViewListener.class, listener);
  }

  public void removeCalendarViewListener(CalendarViewListener listener) {
    m_listenerList.remove(CalendarViewListener.class, listener);
  }

  protected void fireVisibleRangeChanged() {
    EventListener[] a = m_listenerList.getListeners(CalendarViewListener.class);
    if (a != null && a.length > 0) {
      CalendarViewEvent e = new CalendarViewEvent(this, CalendarViewEvent.TYPE_VISIBLE_RANGE_CHANGED);
      for (int i = 0; i < a.length; i++) {
        ((CalendarViewListener) a[i]).viewChanged(e);
      }
    }
  }

  protected void fireSetupChanged() {
    EventListener[] a = m_listenerList.getListeners(CalendarViewListener.class);
    if (a != null && a.length > 0) {
      CalendarViewEvent e = new CalendarViewEvent(this, CalendarViewEvent.TYPE_SETUP_CHANGED);
      for (int i = 0; i < a.length; i++) {
        ((CalendarViewListener) a[i]).viewChanged(e);
      }
    }
  }

  protected void fireSelectionChanged() {
    EventListener[] a = m_listenerList.getListeners(CalendarViewListener.class);
    if (a != null && a.length > 0) {
      CalendarViewEvent e = new CalendarViewEvent(this, CalendarViewEvent.TYPE_SELECTION_CHANGED);
      for (int i = 0; i < a.length; i++) {
        ((CalendarViewListener) a[i]).viewChanged(e);
      }
    }
  }

  protected void repaintCellRange(Date a, Date b) {
    if (b == null) {
      b = a;
    }
    Calendar cal = Calendar.getInstance();
    cal.setTime(truncDate(a));
    while (cal.getTime().compareTo(b) <= 0) {
      AbstractCalendarCell cell = m_dateMap.get(cal.getTime());
      if (cell != null) {
        cell.refresh();
      }
      cal.add(Calendar.DATE, 1);
    }
  }

  protected AbstractCalendarCell getCellOn(Date d) {
    Date d2 = truncDate(d);
    return m_dateMap.get(d2);
  }

  private void shiftViewDate(int type, int amount) {
    boolean fireNotification = true;
    shiftViewDate(type, amount, fireNotification);
  }

  /**
   * @param type
   * @param amount
   * @param fireNotification
   *          set false to avoid circular firing.
   * @since 07.02.2006 - tha@bsiag.com
   */
  protected void shiftViewDate(int type, int amount, boolean fireNotification) {
    Calendar c = Calendar.getInstance();
    //set time to center of current view range to avoid border effects such as when oct/30 is view date then the previous
    //42 days still include the same view and the view is not shifted to the previous month.
    if (m_viewDateStart != null && m_viewDateEnd != null) {
      c.setTimeInMillis((m_viewDateStart.getTime() + m_viewDateEnd.getTime()) / 2L);
    }
    else {
      c.setTimeInMillis(m_viewDate.getTime());
    }
    c.add(type, amount);
    setViewDate(truncDate(c.getTime()));
    if (fireNotification) {
      notifyShiftViewDateListeners(type, amount);
    }
    fireParentAndChildCalendarShift();
    updateSelections();
  }

  protected void fireParentAndChildCalendarShift() {
    try {
      m_calendarUpdating = true;
      if (getParentCalendar() != null && !getParentCalendar().isCalendarUpdating()) {
        Date prevMonth = new Date(this.getViewDateStart().getTime() - 15 * 24 * 3600 * 1000L);
        getParentCalendar().setViewDate(prevMonth);
      }
      if (getChildCalendar() != null && !getChildCalendar().isCalendarUpdating()) {
        Date nextMonth = new Date(this.getViewDateEnd().getTime() + 15 * 24 * 3600 * 1000L);
        getChildCalendar().setViewDate(nextMonth);
      }
    }
    finally {
      m_calendarUpdating = false;
    }
  }

  /**
   * overridden in subclass to notify subscribers.
   * 
   * @param type
   * @param amount
   * @since 07.02.2006 - tha@bsiag.com
   */
  protected void notifyShiftViewDateListeners(int type, int amount) {
  }

  public boolean isMultiSelect() {
    return m_multiSelect;
  }

  public void setMultiSelect(boolean b) {
    m_multiSelect = b;
  }

  public void setSelectedDate(Date d) {
    d = truncDate(d);
    TreeSet<Date> newSet = new TreeSet<Date>();
    if (d != null) {
      newSet.add(d);
    }
    setSelectedDatesInternal(newSet);
  }

  private void setSelectedDatesInternal(TreeSet<Date> newSet) {
    if (!newSet.equals(m_selectedDates)) {
      m_selectedDates = newSet;
      updateSelections();
      fireSelectionChanged();
      try {
        m_calendarUpdating = true;
        // update parent calendar
        if (getParentCalendar() != null && !getParentCalendar().isCalendarUpdating()) {
          getParentCalendar().setSelectedDates(newSet.toArray(new Date[newSet.size()]));
        }
        // update child calendar
        if (getChildCalendar() != null && !getChildCalendar().isCalendarUpdating()) {
          getChildCalendar().setSelectedDates(newSet.toArray(new Date[newSet.size()]));
        }
      }
      finally {
        m_calendarUpdating = false;
      }

    }
  }

  public void toggleSelectedDate(Date d) {
    if (d == null) {
      return;
    }
    d = truncDate(d);
    if (isMultiSelect()) {
      if (m_selectedDates.contains(d)) {
        removeSelectedDate(d);
      }
      else {
        addSelectedDate(d);
      }
    }
  }

  public void addSelectedDate(Date d) {
    if (d == null) {
      return;
    }
    d = truncDate(d);
    if (isMultiSelect()) {
      TreeSet<Date> newSet = new TreeSet<Date>();
      newSet.addAll(m_selectedDates);
      newSet.add(d);
      setSelectedDatesInternal(newSet);
      try {
        m_calendarUpdating = true;
        // update parent calendar
        if (getParentCalendar() != null && !getParentCalendar().isCalendarUpdating()) {
          getParentCalendar().addSelectedDate(d);
        }
        // update child calendar
        if (getChildCalendar() != null && !getChildCalendar().isCalendarUpdating()) {
          getChildCalendar().addSelectedDate(d);
        }
      }
      finally {
        m_calendarUpdating = false;
      }
    }
    else {
      setSelectedDate(d);
    }
  }

  public void removeSelectedDate(Date d) {
    if (d == null) {
      return;
    }
    d = truncDate(d);
    if (isMultiSelect()) {
      TreeSet<Date> newSet = new TreeSet<Date>();
      newSet.addAll(m_selectedDates);
      newSet.remove(d);
      setSelectedDatesInternal(newSet);
      try {
        m_calendarUpdating = true;
        // update parent calendar
        if (getParentCalendar() != null && !getParentCalendar().isCalendarUpdating()) {
          getParentCalendar().removeSelectedDate(d);
        }
        // update child calendar
        if (getChildCalendar() != null && !getChildCalendar().isCalendarUpdating()) {
          getChildCalendar().removeSelectedDate(d);
        }
      }
      finally {
        m_calendarUpdating = false;
      }
    }
  }

  public void setSelectedDates(Date[] dates) {
    if (isMultiSelect()) {
      TreeSet<Date> newSet = new TreeSet<Date>();
      for (Date d : dates) {
        if (d != null) {
          newSet.add(truncDate(d));
        }
      }
      setSelectedDatesInternal(newSet);
    }
    else if (dates.length > 0) {
      setSelectedDate(dates[0]);
    }
  }

  protected final void setViewDate(Date d) {
    if (d == null) {
      if (m_viewDate != null) {
        return;
      }
      d = new Date();
    }
    Date newDate = truncDate(d);
    if (newDate.compareTo(m_viewDateStart) < 0 || newDate.compareTo(m_viewDateEnd) >= 0) {
      m_viewDate = newDate;
      updateStates();
      updateSelections();
      fireVisibleRangeChanged();
    }

  }

  public Date getViewDate() {
    return m_viewDate;
  }

  public Date getViewDateStart() {
    return m_viewDateStart;
  }

  public Date getViewDateEnd() {
    return m_viewDateEnd;
  }

  private void reconfigureLayout() {
    reconfigureCells();
    updateStates();
    updateSelections();
    m_container.revalidate();
    m_container.repaint();
  }

  private void reconfigureCells() {
    // 7 days (in condesed for the 7th is void)
    m_daysPanel.removeAll();
    String[] wdStartingMonday = new DateFormatSymbols(Locale.getDefault()).getShortWeekdays();
    // weekdays starting at 0 (as first day of week)
    String[] wd = new String[7];
    for (int i = 0; i < 7; i++) {
      wd[i] = wdStartingMonday[((m_firstDayOfWeek - 1 + i) % 7) + 1];
    }
    if (m_displayMode == DISPLAY_MODE_DAY) {
      m_daysPanel.setVisible(false);
    }
    else if (m_displayMode == DISPLAY_MODE_WORKWEEK) {
      m_daysPanel.setVisible(true);
      for (String s : new String[]{wdStartingMonday[Calendar.MONDAY], wdStartingMonday[Calendar.TUESDAY], wdStartingMonday[Calendar.WEDNESDAY], wdStartingMonday[Calendar.THURSDAY], wdStartingMonday[Calendar.FRIDAY]}) {
        m_daysPanel.add(createDayLabel(s));
      }
    }
    else {// month or full week
      m_daysPanel.setVisible(true);
      for (int i = 0; i < 7; i++) {
        m_daysPanel.add(createDayLabel(wd[i]));
      }
    }

    //
    MouseProxyAdapter proxy = new MouseProxyAdapter();
    m_cellsPanel.removeAll();
    if (m_displayMode == DISPLAY_MODE_MONTH) {
      m_cell = new AbstractCalendarCell[6][7];
    }
    else if (m_displayMode == DISPLAY_MODE_WEEK) {
      m_cell = new AbstractCalendarCell[1][7];
    }
    else if (m_displayMode == DISPLAY_MODE_WORKWEEK) {
      m_cell = new AbstractCalendarCell[1][5];
    }
    if (m_displayMode == DISPLAY_MODE_DAY) {
      m_cell = new AbstractCalendarCell[1][1];
    }
    //
    m_cellsPanel.setLayout(new GridLayout(m_cell.length, 1));
    Color gridColor = UIManager.getColor("Calendar.gridColor");
    if (gridColor == null) {
      gridColor = Color.lightGray;
    }
    for (int y = 0; y < m_cell.length; y++) {
      JPanelEx rowPanel = new JPanelEx(new GridLayout(1, m_cell[y].length));
      if (!isLargeVersion()) {
        rowPanel.setBorder(new MatteBorder(new Insets(1, 0, 0, 0), gridColor));
      }
      m_cellsPanel.add(rowPanel);
      for (int x = 0; x < m_cell[y].length; x++) {
        m_cell[y][x] = createCalendarCell();
        m_cell[y][x].setBorder(m_cellBorder);
        m_cell[y][x].addMouseListener(proxy);
        m_cell[y][x].setWorkingHours(m_startHour, m_endHour, m_useOverflowCells);
        rowPanel.add(m_cell[y][x]);
      }
    }
  }

  private String formatHour(int h) {
    Calendar cal = Calendar.getInstance();
    cal.clear();
    cal.set(2000, 01, 01, h, 0, 0);
    String s = m_formatHHMM.format(cal.getTime());
    if (s.charAt(1) == ':') {
      s = "0" + s;
    }
    return s;
  }

  private void updateSelections() {
    for (int y = 0; y < m_cell.length; y++) {
      for (int x = 0; x < m_cell[y].length; x++) {
        AbstractCalendarCell cell = m_cell[y][x];
        if (cell != null) {
          Date d = cell.getRepresentedDate();
          if (m_selectedDates.size() > 0 && d != null && m_selectedDates.contains(d)) {
            if (!cell.isSelected()) {
              cell.setSelected(true);
            }
            cell.repaint();
          }
          else {
            if (cell.isSelected()) {
              cell.setSelected(false);
            }
            cell.repaint();
          }
        }
      }
    }
  }

  private void updateStates() {
    Calendar c = Calendar.getInstance();
    Date vd = getViewDate();
    if (vd == null) {
      vd = truncDate(new Date());
    }
    c.setTime(vd);
    int monthNo = c.get(Calendar.MONTH);
    int weekNo = c.get(Calendar.WEEK_OF_YEAR);
    if (m_displayMode == DISPLAY_MODE_DAY) {
      DateFormat weekDayFmt = new SimpleDateFormat("EEEEE", Locale.getDefault());
      DateFormat dateFmt = new DateTimeFormatFactory().getDayMonthYear(DateFormat.LONG);
      m_monthLabel.setText(weekDayFmt.format(vd) + " " + dateFmt.format(vd) + " - " + SwingUtility.getNlsText("Week") + " " + weekNo);
    }
    else if (m_displayMode == DISPLAY_MODE_WEEK) {
      // Calculate Startdate; go back to 1st day of week (1=sunday)
      c.add(Calendar.DAY_OF_WEEK, -((c.get(Calendar.DAY_OF_WEEK) - m_firstDayOfWeek + 7) % 7));
      m_monthLabel.setText(new SimpleDateFormat("MMMMM yyyy", Locale.getDefault()).format(vd) + " - " + SwingUtility.getNlsText("Week") + " " + weekNo);
    }
    else if (m_displayMode == DISPLAY_MODE_WORKWEEK) {
      c.add(Calendar.DAY_OF_WEEK, -((c.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY + 7) % 7));
      m_monthLabel.setText(new SimpleDateFormat("MMMMM yyyy", Locale.getDefault()).format(vd) + " - " + SwingUtility.getNlsText("Week") + " " + weekNo);
    }
    else if (m_displayMode == DISPLAY_MODE_MONTH) {
      // Calculate Startdate; go back to 1st of month, then back to 1st day of
      // week (1=sunday)
      c.add(Calendar.DAY_OF_MONTH, -(c.get(Calendar.DAY_OF_MONTH) - 1));
      c.add(Calendar.DAY_OF_WEEK, -((c.get(Calendar.DAY_OF_WEEK) - m_firstDayOfWeek + 7) % 7));
      m_monthLabel.setText(new SimpleDateFormat("MMMMM yyyy", Locale.getDefault()).format(vd));
    }
    Date newViewDateStart = truncDate(c.getTime());
    c.add(Calendar.DATE, m_cell.length * m_cell[0].length);
    c.setTime(newViewDateStart);
    m_dateMap.clear();
    m_viewDateStart = newViewDateStart;
    for (int y = 0; y < m_cell.length; y++) {
      for (int x = 0; x < m_cell[y].length; x++) {
        boolean isInMonth = c.get(Calendar.MONTH) == monthNo;
        AbstractCalendarCell cc = m_cell[y][x];
        boolean firstCol;
        if (m_displayMode == DateChooser.DISPLAY_MODE_MONTH) {
          firstCol = (c.get(Calendar.DAY_OF_WEEK) == m_firstDayOfWeek);
        }
        else if (m_displayMode == DateChooser.DISPLAY_MODE_WEEK) {
          firstCol = (c.get(Calendar.DAY_OF_WEEK) == m_firstDayOfWeek);
        }
        else if (m_displayMode == DateChooser.DISPLAY_MODE_WORKWEEK) {
          firstCol = (c.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY);
        }
        else {
          /* day */firstCol = true;
        }
        cc.setRepresentedState(c, isInMonth, firstCol, getDisplayMode());
        m_dateMap.put(cc.getRepresentedDate(), cc);
        // next
        c.add(Calendar.DATE, 1);
      }
    }
    m_viewDateEnd = truncDate(c.getTime());
  }

  public static Date truncDate(Date d) {
    if (d == null) {
      return null;
    }
    Calendar c = Calendar.getInstance();
    c.setTime(d);
    c.set(Calendar.HOUR, 0);
    c.set(Calendar.HOUR_OF_DAY, 0);
    c.set(Calendar.MINUTE, 0);
    c.set(Calendar.SECOND, 0);
    c.set(Calendar.MILLISECOND, 0);
    return new Date(c.getTime().getTime());
  }

  public boolean isWorkDay(Date d) {
    Calendar c = Calendar.getInstance();
    c.setTime(d);
    switch (c.get(Calendar.DAY_OF_WEEK)) {
      case Calendar.MONDAY:
      case Calendar.TUESDAY:
      case Calendar.WEDNESDAY:
      case Calendar.THURSDAY:
      case Calendar.FRIDAY: {
        return true;
      }
      default: {
        return false;
      }
    }
  }

  public void setWorkHours(int startHour, int endHour, boolean useOverflowCells) {
    m_startHour = startHour;
    m_endHour = endHour;
    m_useOverflowCells = useOverflowCells;
    for (int i = 0; i < m_cell.length; i++) {
      for (int k = 0; k < m_cell[i].length; k++) {
        m_cell[i][k].setWorkingHours(startHour, endHour, useOverflowCells);
      }
    }
    reconfigureLayout();
  }

  public void setShowDisplayModeSelectionPanel(boolean visible) {
    if (m_controlPanel != null) {
      m_controlPanel.setVisible(visible);
    }
  }

  public boolean getMarkNoonHour() {
    return m_markNoonHour;
  }

  public void setMarkNoonHour(boolean markNoonHour) {
    m_markNoonHour = markNoonHour;
  }

  public void setMarkOutOfMonthDays(boolean markOutOfMonthDays) {
    m_markOutOfMonthDays = markOutOfMonthDays;
  }

  public boolean getMarkOutOfMonthDays() {
    return m_markOutOfMonthDays;
  }

  public static Date nextDay(Date d) {
    if (d == null) {
      return null;
    }
    Calendar c = Calendar.getInstance();
    c.setTime(d);
    c.add(Calendar.DATE, 1);
    Date dNew = new Date(c.getTime().getTime());
    return dNew;
  }

  public JPanel getHeaderPanel() {
    return m_headerPanel;
  }

  public JPanel getControlPanel() {
    return m_controlPanel;
  }

  public JPanel getDaysPanel() {
    return m_daysPanel;
  }

  public JPanel getCellsPanel() {
    return m_cellsPanel;
  }

  public JComponent[][] getCells() {
    return m_cell;
  }

  /**
   * relocate all mouse events inside component to one point
   */
  private class MouseProxyAdapter extends MouseAdapter {
    @Override
    public void mouseClicked(MouseEvent e) {
      fireMouseEvent(relocateEvent(e));
    }

    @Override
    public void mousePressed(MouseEvent e) {
      fireMouseEvent(relocateEvent(e));
    }

    @Override
    public void mouseReleased(MouseEvent e) {
      fireMouseEvent(relocateEvent(e));
    }

    @Override
    public void mouseEntered(MouseEvent e) {
      fireMouseEvent(relocateEvent(e));
    }

    @Override
    public void mouseExited(MouseEvent e) {
      fireMouseEvent(relocateEvent(e));
    }

    private MouseEvent relocateEvent(MouseEvent e) {
      Component newSource = m_container;
      return SwingUtilities.convertMouseEvent((Component) e.getSource(), e, newSource);
    }

    private void fireMouseEvent(MouseEvent me) {
      m_container.dispatchEvent(me);
    }
  }// end class

  private class DateController extends MouseAdapter {
    private boolean m_pressedInsideThisAdapter;
    private Date m_lastClicked;

    @Override
    public void mousePressed(MouseEvent e) {
      m_pressedInsideThisAdapter = true;
      AbstractCalendarCell cell = getCalendarCellFor(e);
      if (cell != null) {
        // set focus on cell
        cell.requestFocus();
        if (isMultiSelect()) {
          if (e.isShiftDown()) {
            if (m_lastClicked != null) {
              Date a = cell.getRepresentedDate();
              Date b = m_lastClicked;
              if (a.after(b)) {
                Date tmp = a;
                a = b;
                b = tmp;
              }
              Calendar cal = Calendar.getInstance();
              cal.setTime(a);
              while (a.compareTo(b) <= 0) {
                addSelectedDate(a);
                cal.add(Calendar.DATE, 1);
                a = cal.getTime();
              }
            }
          }
          else if (e.isControlDown() || (e.isMetaDown())) {// also behave like
            // 'control' when
            // right clicked
            toggleSelectedDate(cell.getRepresentedDate());
          }
          else {
            setSelectedDate(cell.getRepresentedDate());
          }
        }
        else {
          setSelectedDate(cell.getRepresentedDate());
        }
        m_lastClicked = cell.getRepresentedDate();
      }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
      if (m_pressedInsideThisAdapter) {
        if (e.getButton() == MouseEvent.BUTTON1 && !e.isMetaDown()) {
          fireChangedEvent();
        }
      }
      m_pressedInsideThisAdapter = false;
    }

    @Override
    public void mouseEntered(MouseEvent e) {
      if (m_pressedInsideThisAdapter) {
        if ((e.getModifiers() & (MouseEvent.BUTTON1_MASK | MouseEvent.BUTTON3_MASK)) != 0) {
          AbstractCalendarCell cell = getCalendarCellFor(e);
          if (cell != null) {
            if (isMultiSelect()) {
              addSelectedDate(cell.getRepresentedDate());
            }
            else {
              setSelectedDate(cell.getRepresentedDate());
            }
          }
        }
      }
    }
  }// end class

  private class ArrowIcon implements Icon {
    private int m_steps;

    public ArrowIcon(int steps) {
      m_steps = steps;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
      g.translate(x, y);
      g.setColor(c.getForeground());
      if (m_steps == +2) {
        for (int i = 0; i <= 3; i++) {
          g.drawLine(i, i, i, 6 - i);
        }
        for (int i = 0; i <= 3; i++) {
          g.drawLine(i + 3, i, i + 3, 6 - i);
        }
      }
      else if (m_steps == +1) {
        for (int i = 0; i <= 3; i++) {
          g.drawLine(i + 1, i, i + 1, 6 - i);
        }
      }
      else if (m_steps == -1) {
        for (int i = 0; i <= 3; i++) {
          g.drawLine(5 - i, i, 5 - i, 6 - i);
        }
      }
      else if (m_steps == -2) {
        for (int i = 0; i <= 3; i++) {
          g.drawLine(6 - i, i, 6 - i, 6 - i);
        }
        for (int i = 0; i <= 3; i++) {
          g.drawLine(3 - i, i, 3 - i, 6 - i);
        }
      }
      g.translate(-x, -y);
    }

    @Override
    public int getIconWidth() {
      return 7;
    }

    @Override
    public int getIconHeight() {
      return 7;
    }
  }// end class

  private class EmptyCalendarModel implements CalendarModel {
    @Override
    public Collection<Object> getItemsAt(Date dateTruncatedToDay) {
      return null;
    }

    @Override
    public String getTooltip(Object item, Date d) {
      return null;
    }

    @Override
    public String getLabel(Object item, Date d) {
      return null;
    }

    @Override
    public Date getFromDate(Object item) {
      return null;
    }

    @Override
    public Date getToDate(Object item) {
      return null;
    }

    @Override
    public Color getColor(Object item) {
      return null;
    }

    @Override
    public boolean isFullDay(Object item) {
      return false;
    }

    @Override
    public boolean isDraggable(Object item) {
      return false;
    }

    @Override
    public void moveItem(Object item, Date newDate) {
    }
  }
}
