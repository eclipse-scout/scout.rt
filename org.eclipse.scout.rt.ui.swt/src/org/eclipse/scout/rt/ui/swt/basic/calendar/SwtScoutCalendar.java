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
package org.eclipse.scout.rt.ui.swt.basic.calendar;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.scout.commons.OptimisticLock;
import org.eclipse.scout.commons.RunnableWithData;
import org.eclipse.scout.commons.WeakEventListener;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.calendar.CalendarComponent;
import org.eclipse.scout.rt.client.ui.basic.calendar.ICalendar;
import org.eclipse.scout.rt.ui.swt.SwtMenuUtility;
import org.eclipse.scout.rt.ui.swt.action.SwtScoutAction;
import org.eclipse.scout.rt.ui.swt.basic.calendar.widgets.SwtCalendar;
import org.eclipse.scout.rt.ui.swt.form.fields.calendar.SwtScoutCalendarField;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * @author Michael Rudolf, Andreas Hoegger
 */
public class SwtScoutCalendar extends SwtCalendar {

  private ICalendar m_scoutCalendarModel;
  private Lock m_singleObserverLock = new ReentrantLock();
  private SwtScoutCalendarField m_field;
  private OptimisticLock m_propertyChangeLock;

  private P_ScoutCalendarPropertyListener m_activePropertyListener; //used for auto-detach

  public SwtScoutCalendar(Composite parent, int style, SwtScoutCalendarField field) {
    super(parent, style);

    m_propertyChangeLock = new OptimisticLock();

    m_field = field;

    P_SwtCalendarListener cl = new P_SwtCalendarListener();
    addCalendarViewListener(cl);

    // to make this composite focusable
    addListener(SWT.KeyDown, new Listener() {
      @Override
      public void handleEvent(Event e) {
      }
    });
  }

  public void setScoutCalendarModel(ICalendar calendarModel) {
    m_scoutCalendarModel = calendarModel;

    // create a wrapper class for the ScoutCalendarModel
    setModel(new P_SwtCalendarModel(calendarModel.getComponents()));

    //init scout->swt properties
    setDisplayMode(calendarModel.getDisplayMode());
    setCondensedMode(calendarModel.isDisplayCondensed());
    Calendar selectedDate = Calendar.getInstance();
    if (calendarModel.getSelectedDate() != null) {
      selectedDate.setTime(calendarModel.getSelectedDate());
    }
    setViewDate(selectedDate);
    setWorkingHours(calendarModel.getStartHour(), calendarModel.getEndHour(), calendarModel.getUseOverflowCells());
    setShowDisplayModeSelection(calendarModel.getShowDisplayModeSelection());
    setMarkNoonHour(calendarModel.getMarkNoonHour());
    setMarkOutOfMonthDays(calendarModel.getMarkOutOfMonthDays());

    //attach property listener
    m_activePropertyListener = new P_ScoutCalendarPropertyListener();
    m_scoutCalendarModel.addPropertyChangeListener(m_activePropertyListener);
  }

  @Override
  public void showGeneralContextMenu(IMenuManager manager) {
    // pop up with a general menu
    IMenu[] scoutMenus = SwtMenuUtility.collectEmptySpaceMenus(m_scoutCalendarModel, m_field.getEnvironment());
    if (scoutMenus != null) {
      for (IMenu menuItem : scoutMenus) {
        if (menuItem instanceof IMenu) {
          if (menuItem.isSeparator()) {
            manager.add(new Separator());
          }
          else {
            manager.add(new SwtScoutAction(menuItem, m_field.getEnvironment(), Action.AS_PUSH_BUTTON).getSwtAction());
          }
        }
      }
    }
  }

  @Override
  public void showItemContextMenu(IMenuManager manager, Object item) {
    IMenu[] scoutMenus = SwtMenuUtility.collectComponentMenus(m_scoutCalendarModel, m_field.getEnvironment());
    if (scoutMenus != null) {
      for (IMenu menuItem : scoutMenus) {
        if (menuItem instanceof IMenu) {
          if (menuItem.isSeparator()) {
            manager.add(new Separator());
          }
          else {
            manager.add(new SwtScoutAction(menuItem, m_field.getEnvironment(), Action.AS_PUSH_BUTTON).getSwtAction());
          }
        }
      }
    }
  }

  @Override
  public void setDisplayMode(final int newMode) {
    super.setDisplayMode(newMode);
    if (m_singleObserverLock.tryLock()) {
      try {
        //notify scout model
        JobEx job = m_field.getEnvironment().invokeScoutLater(new Runnable() {
          @Override
          public void run() {
            if (m_scoutCalendarModel != null) {
              m_scoutCalendarModel.setDisplayMode(newMode);
            }
          }
        }, 0);
        try {
          job.join(2345);
        }
        catch (InterruptedException e) {
          //nop
        }
        //end notify
      }
      finally {
        m_singleObserverLock.unlock();
      }
    }
  }

  @Override
  public void setCondensedMode(final boolean condensed) {
    super.setCondensedMode(condensed);
    if (m_singleObserverLock.tryLock()) {
      try {
        //notify scout model
        Runnable r = new Runnable() {
          @Override
          public void run() {
            if (m_scoutCalendarModel != null) {
              m_scoutCalendarModel.setDisplayCondensed(condensed);
            }
          }
        };
        JobEx job = m_field.getEnvironment().invokeScoutLater(r, 0);
        try {
          job.join(2345);
        }
        catch (InterruptedException e) {
          //nop
        }
        //end notify
      }
      finally {
        m_singleObserverLock.unlock();
      }
    }
  }

  @Override
  public void setSelectedDateFromUI(final Calendar c) {
    super.setSelectedDateFromUI(c);
    if (m_scoutCalendarModel != null) {
      //notify Scout
      Runnable r = new Runnable() {
        @Override
        public void run() {
          m_scoutCalendarModel.setSelectedDate(c.getTime());
        }
      };
      m_field.getEnvironment().invokeScoutLater(r, 0);
      //end notify
    }
  }

  private void setSelectedDateFromScout(final Calendar c) {
    super.setSelectedDate(c);
  }

  @Override
  protected void shiftViewDate(int type, int amount, boolean fireNotification) {
    super.shiftViewDate(type, amount, fireNotification);
    if (m_scoutCalendarModel != null) {
      //notify Scout
      Runnable r = new Runnable() {
        @Override
        public void run() {
          m_scoutCalendarModel.setViewRange(getViewDateStart().getTime(), getViewDateEnd().getTime());
        }
      };
      JobEx job = m_field.getEnvironment().invokeScoutLater(r, 0);
      try {
        job.join(2345);
      }
      catch (InterruptedException e) {
        //nop
      }
      //end notify
    }
  }

  protected void handleScoutPropertyChange(String name, final Object newValue) {

    try {
      if (m_propertyChangeLock.acquire()) {
        if (name.equals(ICalendar.PROP_DISPLAY_MODE)) {
          int i = ((Number) newValue).intValue();
          setDisplayMode(i);
          fireViewDateChanged();
          refreshLayout();
        }
        else if (name.equals(ICalendar.PROP_DISPLAY_CONDENSED)) {
          boolean b = ((Boolean) newValue).booleanValue();
          setCondensedMode(b);
          refreshLayout();
        }
        else if (name.equals(ICalendar.PROP_SELECTED_DATE)) {
          final Calendar cal = Calendar.getInstance();
          cal.setTime((Date) newValue);
          setSelectedDateFromScout(cal);
          setViewDate(cal);
          fireViewDateChanged();
          refreshLayout();
        }
        else if (name.equals(ICalendar.PROP_VIEW_RANGE)) {
          fireViewDateChanged();
          refreshLayout();
        }
        else if (name.equals(ICalendar.PROP_COMPONENTS)) {
          setCalendarComponentsFromScout((CalendarComponent[]) newValue);
        }
        else if (name.equals(ICalendar.PROP_SELECTED_COMPONENT)) {
          // handled by calendarListener below
        }
        else if (name.equals(ICalendar.PROP_LOAD_IN_PROGRESS)) {
          setLoadInProgressFromScout((Boolean) newValue);
        }
        else {
          LOG.debug("Following property not handled: " + name + " -> " + (newValue == null ? "none" : newValue.toString()));
        }
      }
    }
    finally {
      m_propertyChangeLock.release();
    }
  }

  private void setLoadInProgressFromScout(boolean b) {
    if (b) {
      setCursor(new Cursor(SwtColors.getStandardDisplay(), SWT.CURSOR_WAIT));
    }
    else {
      setCursor(null);
    }
  }

  public SwtCalendar getSwtCalendar() {
    return this;
  }

  public void setCalendarComponentsFromScout(CalendarComponent[] c) {
    getSwtCalendar().setModel(new P_SwtCalendarModel(c));
  }

  /**
   * Private classes
   */

  protected class P_ScoutCalendarPropertyListener implements PropertyChangeListener, WeakEventListener {
    @Override
    public void propertyChange(PropertyChangeEvent e) {
      RunnableWithData r = new RunnableWithData() {
        @Override
        public void run() {
          PropertyChangeEvent e2 = (PropertyChangeEvent) getData("event");
          handleScoutPropertyChange(e2.getPropertyName(), e2.getNewValue());
        }
      };
      r.setData("event", e);
      m_field.getEnvironment().invokeSwtLater(r);

    }
  }//end class

  private class P_SwtCalendarModel implements CalendarModel {
    private CalendarComponent[] m_components;
    private HashMap<Date, Collection<CalendarComponent>> m_dayMap;

    public P_SwtCalendarModel(CalendarComponent[] components) {
      m_components = components;
      //build map of all items per day
      m_dayMap = new HashMap<Date, Collection<CalendarComponent>>();
      for (CalendarComponent comp : m_components) {
        for (Date day : comp.getCoveredDays()) {
          Collection<CalendarComponent> list = m_dayMap.get(day);
          if (list == null) {
            list = new ArrayList<CalendarComponent>();
            m_dayMap.put(day, list);
          }
          list.add(comp);
        }
      }
    }

    @Override
    public Collection<CalendarComponent> getItemsAt(Date dateTruncatedToDay) {
      return m_dayMap.get(dateTruncatedToDay);
    }

    @Override
    public String getTooltip(Object item, Date d) {
      CalendarComponent comp = (CalendarComponent) item;
      return comp.getTooltip(d);
    }

    @Override
    public String getLabel(Object item, Date d) {
      CalendarComponent comp = (CalendarComponent) item;
      return comp.getLabel(d);
    }

    @Override
    public Date getFromDate(Object item) {
      CalendarComponent comp = (CalendarComponent) item;
      return comp.getFromDate();
    }

    @Override
    public Date getToDate(Object item) {
      CalendarComponent comp = (CalendarComponent) item;
      return comp.getToDate();
    }

    @Override
    public Color getColor(Object item) {
      CalendarComponent comp = (CalendarComponent) item;
      return SwtColors.getInstance().getColor(comp.getCell().getBackgroundColor());
    }

    @Override
    public boolean isFullDay(Object item) {
      CalendarComponent comp = (CalendarComponent) item;
      return comp.isFullDay();
    }

    @Override
    public boolean isDraggable(Object item) {
      CalendarComponent comp = (CalendarComponent) item;
      return comp.isDraggable();
    }

    @Override
    public void moveItem(Object item, Date newDate) {
      CalendarComponent comp = (CalendarComponent) item;
      m_field.getScoutObject().getCalendar().getUIFacade().fireComponentMovedFromUI(comp, newDate);
    }
  }//end class

  private class P_SwtCalendarListener implements ICalendarViewListener {
    @Override
    public void viewChanged(final CalendarViewEvent e) {
      switch (e.getType()) {
        case CalendarViewEvent.TYPE_SELECTION_CHANGED: {
          try {
            if (m_propertyChangeLock.acquire()) {
              final Date d = getSelectedDate().getTime();
              final CalendarComponent cc = (CalendarComponent) getSelectedItem();
              //notify Scout
              RunnableWithData r = new RunnableWithData() {
                @Override
                public void run() {
                  if (cc != null) {
                    m_field.getScoutObject().getCalendar().getUIFacade().setSelectionFromUI(d, cc);
                  }
                  //refreshLayout();
                }
              };
              m_field.getEnvironment().invokeScoutLater(r, 2345);
              //end notify
            }
          }
          finally {
            m_propertyChangeLock.release();
          }
          refreshLayout();
          break;
        }
        case CalendarViewEvent.TYPE_VISIBLE_RANGE_CHANGED: {
          //notify Scout
          RunnableWithData t = new RunnableWithData() {
            @Override
            public void run() {
              m_field.getScoutObject().getCalendar().getUIFacade().setVisibleRangeFromUI(e.getViewDateStart(), e.getViewDateEnd());
              //refreshLayout();
            }
          };
          m_field.getEnvironment().invokeScoutLater(t, 2345);
          //end notify

          break;
        }
      }
    }
  }//end class
}
