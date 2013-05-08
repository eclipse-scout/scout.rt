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
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.Locale;
import java.util.TreeSet;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.basic.calendar.CalendarComponent;
import org.eclipse.scout.rt.client.ui.basic.calendar.DateTimeFormatFactory;
import org.eclipse.scout.rt.ui.swt.basic.calendar.CalendarItemContainer;
import org.eclipse.scout.rt.ui.swt.basic.calendar.SwtColors;
import org.eclipse.scout.rt.ui.swt.util.SwtUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;

/**
 * Abstract SWT component for a calendar (week or month) cell.
 * 
 * @author Michael Rudolf, Andreas Hoegger
 */
public abstract class AbstractCell extends Composite implements PaintListener {
  protected static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractCell.class);

  public static final Color BACKGROUND = SwtColors.getInstance().getWhite();
  public static final Color BORDER_SELECTED_COLOR = SwtColors.getInstance().getBlue();
  public static final Color BORDER_UNSELECTED_COLOR = SwtColors.getInstance().getGray();

  private Color background = BACKGROUND;

  /** main Calendar object */
  private SwtCalendar m_calendar;

  /** cell calendar date */
  private Calendar m_cellDate;

  /** is first column? */
  private boolean m_isFirstColumn;

  /** is this cell part of the current month/week? */
  private boolean m_isCurrentPeriod;
  /** cell selected? */
  private boolean m_isSelected;

  private String m_weekText = null;
  private String m_dayText = null;

  /** manager for context menu regarding this cell */
  private MenuManager m_menuManager;

  /** cached CalendarItemContainer */
  private TreeSet<CalendarItemContainer> m_itemsCached;

  /** widget celendar items contained within this cell */
  private ArrayList<AbstractCalendarItem> m_widgetItems;

  /** number of timeless items within that cell */
  private int m_countTimeless;

  public AbstractCell(Composite parent, int style) {
    super(parent, style);

    m_widgetItems = new ArrayList<AbstractCalendarItem>();

    m_isSelected = false;

    setupMenu();
  }

  /** create context menu (dynamically, gets filled when used) */
  protected void setupMenu() {
    m_menuManager = new MenuManager();
    m_menuManager.setRemoveAllWhenShown(true);
    Menu contextMenu = m_menuManager.createContextMenu(this);
    this.setMenu(contextMenu);
  }

  /** get calendar items corresponding to that cell */
  public synchronized Collection<CalendarItemContainer> getItems() {
    if (m_itemsCached == null) {
      // cache empty -> fetch data from model
      m_itemsCached = new TreeSet<CalendarItemContainer>();
      m_countTimeless = 0;
      Collection<CalendarComponent> items = m_calendar.getModel().getItemsAt(getDate().getTime());
      // if items found
      if (items != null) {
        try {
          // add them to the cache
          for (Object item : items) {
            m_itemsCached.add(new CalendarItemContainer(item, this));
          }
        }
        catch (ConcurrentModificationException cme) {
          LOG.warn("ConcurrentModificationException on getItems");
          return m_itemsCached;
        }
      }
      // if cache not empty
      if (!m_itemsCached.isEmpty()) {
        // *** calculate distribution of time-dependent items
        ArrayList<ItemBoundsOrganizer> seen = new ArrayList<ItemBoundsOrganizer>();
        ArrayList<ItemBoundsOrganizer> conflict = new ArrayList<ItemBoundsOrganizer>();
        int horizontalSlotCount = 0;
        for (CalendarItemContainer item : m_itemsCached) {
          // for all timed items
          if (item.isTimed()) {
            ItemBoundsOrganizer newNode = new ItemBoundsOrganizer(item);
            // clear the conflict list
            conflict.clear();
            int minLevel = 0;
            // for each seen items
            for (ItemBoundsOrganizer oldNode : seen) {
              // if there is a conflict with the new one
              if (oldNode.getCC().intersects(newNode.getCC())) {
                // enter the seen item in the conflict list
                conflict.add(oldNode);
                minLevel = Math.max(minLevel, oldNode.getMinLevel() + 1);
              }
            }
            newNode.setMinLevel(minLevel);
            horizontalSlotCount = Math.max(horizontalSlotCount, minLevel + 1);
            seen.add(newNode);
            // process the conflict list
            for (ItemBoundsOrganizer node : conflict) {
              // set the max level
              /**
               * @rn imo, 06.07.2007, Ticket 57375
               */
              node.limitMaxLevel(minLevel - 1);
            }
          }
        }
        // save bounds to the seen items according to the pre-calculated values
        for (ItemBoundsOrganizer item : seen) {
          item.saveHorizontalExtents(horizontalSlotCount);
        }

        // *** calculate distribution of time-independent items
        for (CalendarItemContainer cc : m_itemsCached) {
          if (!cc.isTimed()) {
            cc.setHorizontalExtents(0f, 1f);
            m_countTimeless++;
          }
        }
        return m_itemsCached;
      }
      else {
        return null;
      }
    } // -- end cach null
    else {
      // cache not null, returns it
      return m_itemsCached;
    }
  }

  /** get the number of timeless items for that cell */
  public int getCountTimelessItems() {
    return m_countTimeless;
  }

  /** reset the item cache for that cell */
  public void reloadCalendarItems() {

    // dispose existing calendar items
    disposeCalendarItems();

    // force a reload from the model
    m_itemsCached = null;

    addCalendarItems();

  }

  public SwtCalendar getCalendar() {
    return m_calendar;
  }

  protected void setCalendar(SwtCalendar calendar) {
    m_calendar = calendar;
  }

  protected ArrayList<AbstractCalendarItem> getWidgetItems() {
    return m_widgetItems;
  }

  /** method getting called by the PaintListener */
  @Override
  public void paintControl(PaintEvent e) {

    setBackground(background);

    drawLabels(e);
    drawTimeline(e);
    //redrawChildren ();
    drawBorder(e);
  }

  /** need to be implemented by a subclass */
  protected abstract void drawLabels(PaintEvent e);

  /** draw day label */
  protected void drawDayLabel(PaintEvent e) {
    if (m_dayText != null) {
      // foreground to black
      e.gc.setForeground(SwtColors.getInstance().getBlack());

      // get cell bounds
      Rectangle bounds = getBounds();
      Point ext = e.gc.stringExtent(m_dayText);
      e.gc.drawString(m_dayText, bounds.width - 2 - ext.x, 1, true);
    }
  }

  /** draw week label */
  protected void drawWeekLabel(PaintEvent e) {
    if (m_dayText != null) {
      // foreground to black
      e.gc.setForeground(SwtColors.getInstance().getBlack());

      // test draw a test
      e.gc.drawString(m_weekText, 3, 1, true);
    }
  }

  /** draw composite border with the current (unselected/selected) border color */
  protected void drawBorder(PaintEvent e) {
    /** set right color */
    if (m_isSelected) {
      e.gc.setForeground(BORDER_SELECTED_COLOR);
    }
    else {
      e.gc.setForeground(BORDER_UNSELECTED_COLOR);
    }

    Rectangle bounds = getBounds();
    e.gc.drawRectangle(0, 0, bounds.width - 1, bounds.height - 1);

    e.gc.setForeground(BORDER_UNSELECTED_COLOR);
  }

  /** per default, no timeline */
  protected void drawTimeline(PaintEvent e) {
  }

  /** scheduled a paint event for every child, i.e. item within this cell */
//  protected void redrawChildren () {
//    if (m_widgetItems != null)
//      for (final AbstractCalendarItem child: m_widgetItems)
//        child.redraw();
//  }

  /** the current cell gets selected */
  public void setSelected() {
    // do we need to change the view date
    if (!m_isCurrentPeriod) {
      m_calendar.setViewDate(m_cellDate);
    }

    // report selection to main calendar
    m_calendar.setSelectedItem(null);
    m_calendar.setSelectedDateFromUI(m_cellDate);
  }

  /** setup listeners */
  protected void hookListeners() {
    addPaintListener(this);

    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseDown(MouseEvent e) {
        setSelected();
      }
    });

    // menu listener for context menu
    m_menuManager.addMenuListener(new IMenuListener() {
      @Override
      public void menuAboutToShow(IMenuManager manager) {
        m_calendar.showGeneralContextMenu(manager);
      }
    });

    // tab traversal of cells
    this.addTraverseListener(new TraverseListener() {
      @Override
      public void keyTraversed(TraverseEvent e) {
//        System.out.println(toString() + " got event " + e);
        switch (e.detail) {
          case SWT.TRAVERSE_TAB_NEXT:
          case SWT.TRAVERSE_ARROW_NEXT:
            Calendar newDate = Calendar.getInstance();
            newDate.setTime(m_cellDate.getTime());
            newDate.add(Calendar.DATE, 1);
            AbstractCell cell = m_calendar.getCentralPanel().getCellFromDate(newDate.getTime());
            if (cell != null) {
              cell.setSelected();
            }
            e.doit = false;
            break;
          case SWT.TRAVERSE_TAB_PREVIOUS:
          case SWT.TRAVERSE_ARROW_PREVIOUS:
            newDate = Calendar.getInstance();
            newDate.setTime(m_cellDate.getTime());
            newDate.add(Calendar.DATE, -1);
            cell = m_calendar.getCentralPanel().getCellFromDate(newDate.getTime());
            if (cell != null) {
              cell.setSelected();
            }
            e.doit = false;
            break;
        }
      }
    });
    // to make this cell focusable
    addListener(SWT.KeyDown, new Listener() {
      @Override
      public void handleEvent(Event e) {
      }
    });
  }

  public void setSelected(boolean selected) {
    // changed?
    if (m_isSelected == selected) {
      return;
    }

    if (selected) {
      setFocus();
    }

    m_isSelected = selected;
    redraw();
  }

  protected void setSelectedInternal(boolean selected) {
    m_isSelected = selected;
  }

  public boolean getSelected() {
    return m_isSelected;
  }

  public void setDayText(String text) {
    m_dayText = text;
  }

  public void setWeekText(String text) {
    m_weekText = text;
  }

  public Calendar getDate() {
    return m_cellDate;
  }

  protected void setDate(Calendar cellDate) {
    m_cellDate = cellDate;
  }

  protected boolean isFirstColumn() {
    return m_isFirstColumn;
  }

  protected void setFirstColumn(boolean isFirstColumn) {
    m_isFirstColumn = isFirstColumn;
  }

  protected boolean isCurrentPeriod() {
    return m_isCurrentPeriod;
  }

  protected void setCurrentPeriod(boolean isCurrentPeriod) {
    m_isCurrentPeriod = isCurrentPeriod;
  }

  protected void setVisualState() {
    // week name
    String weekName = m_isFirstColumn ? SwtUtility.getNlsText(Display.getCurrent(), "WeekShort") + " " + m_cellDate.get(Calendar.WEEK_OF_YEAR) : "";

    // day name (semi condensed form)
    String day = new SimpleDateFormat("dd.MMM", Locale.getDefault()).format(m_cellDate.getTime());

    // set week and day label
    setDayText(day);
    setWeekText(weekName);

    // gray background for cell not in the curent month
    setBackground(SwtColors.getInstance().getWhite());
    if (!m_isCurrentPeriod && m_calendar.getMarkOutOfMonthDays()) {
      setBackground(SwtColors.getInstance().getLightgray());
    }
  }

  /** add items to this cell */
  protected void addCalendarItems() {
    Collection<CalendarItemContainer> items = getItems();
    if (items != null) {
      for (Object element : getItems()) {
        CalendarItemContainer item = (CalendarItemContainer) element;
        // create new calendar item + set label
        if (this instanceof WeekCell) {
          m_widgetItems.add(new WeekCalendarItem(this, SWT.NONE, item));
        }
        else if (this instanceof MonthCell) {
          m_widgetItems.add(new MonthCalendarItem(this, SWT.NONE, item));
        }
      }
    }
  }

  @Override
  public void setBackground(Color c) {
    super.setBackground(c);

    background = c;
  }

  /** dispose all contained calendar items */
  protected void disposeCalendarItems() {
    if (m_widgetItems != null) {
      for (AbstractCalendarItem item : m_widgetItems) {
        if (item != null && !item.isDisposed()) {
          item.dispose();
        }
      }
      m_widgetItems = new ArrayList<AbstractCalendarItem>();
    }
  }

  @Override
  public void dispose() {

    disposeCalendarItems();
    super.dispose();
  }

  /**
   * @return the absolut time of day in millis regardless of daylight savings
   *         date.getTime() is not correct (+-1 hour) on days where daylight savings is applied
   */
  public static long getTimeOfDayMillis(Date d) {
    Calendar c = Calendar.getInstance();
    c.setTime(d);
    long timeRel = ((c.get(Calendar.HOUR_OF_DAY) * 60L + c.get(Calendar.MINUTE)) * 60L + c.get(Calendar.SECOND)) * 1000L + c.get(Calendar.MILLISECOND);
    c.set(Calendar.HOUR, 0);
    c.set(Calendar.HOUR_OF_DAY, 0);
    c.set(Calendar.MINUTE, 0);
    c.set(Calendar.SECOND, 0);
    c.set(Calendar.MILLISECOND, 0);
    long dayAbs = c.getTimeInMillis();
    return dayAbs + timeRel;
  }

  @Override
  public String toString() {
    DateFormat weekDayFmt = new SimpleDateFormat("EEEEE", Locale.getDefault());
    DateFormat dateFmt = new DateTimeFormatFactory().getDayMonthYear(DateFormat.LONG);
    return "AbstractCell {" + weekDayFmt.format(m_cellDate.getTime()) + " " + dateFmt.format(m_cellDate.getTime()) + "}";
  }

  /* comes from the swt implementation regarding layouting the calendar items */
  private class ItemBoundsOrganizer {
    private CalendarItemContainer m_cc;
    private int m_minLevel;
    private int m_maxLevel = 100000;

    public ItemBoundsOrganizer(CalendarItemContainer cc) {
      m_cc = cc;
    }

    public CalendarItemContainer getCC() {
      return m_cc;
    }

    public int getMinLevel() {
      return m_minLevel;
    }

    public void setMinLevel(int i) {
      m_minLevel = i;
    }

    public void limitMaxLevel(int maximumValue) {
      m_maxLevel = Math.min(m_maxLevel, maximumValue);
    }

    private void saveHorizontalExtents(int horizontalSlotCount) {
      float x0, x1;//0..1
      x0 = 1f * m_minLevel / horizontalSlotCount;
      x1 = 1f * Math.min(m_maxLevel + 1, horizontalSlotCount) / horizontalSlotCount;
      m_cc.setHorizontalExtents(x0, x1);
    }

    @Override
    public String toString() {
      return "BoundsOrganizer[" + m_cc + "]";
    }
  }//end class
}
