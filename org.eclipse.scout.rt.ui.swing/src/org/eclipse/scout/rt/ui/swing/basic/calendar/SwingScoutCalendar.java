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
package org.eclipse.scout.rt.ui.swing.basic.calendar;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.calendar.CalendarComponent;
import org.eclipse.scout.rt.client.ui.basic.calendar.ICalendar;
import org.eclipse.scout.rt.ui.swing.SwingPopupWorker;
import org.eclipse.scout.rt.ui.swing.SwingUtility;
import org.eclipse.scout.rt.ui.swing.basic.SwingScoutComposite;
import org.eclipse.scout.rt.ui.swing.ext.MouseClickedBugFix;
import org.eclipse.scout.rt.ui.swing.ext.calendar.CalendarModel;
import org.eclipse.scout.rt.ui.swing.ext.calendar.CalendarViewEvent;
import org.eclipse.scout.rt.ui.swing.ext.calendar.CalendarViewListener;
import org.eclipse.scout.rt.ui.swing.ext.calendar.DateChooser;

public class SwingScoutCalendar extends SwingScoutComposite<ICalendar> {

  private DateChooser m_dateChooser;

  public SwingScoutCalendar() {
  }

  @Override
  protected void initializeSwing() {
    m_dateChooser = new DateChooser(true);
    JPanel container = m_dateChooser.getContainer();
    // focus corrections
    SwingUtility.installDefaultFocusHandling(container);
    setSwingField(container);
    // swing properties
    // listeners
    container.addMouseListener(new P_SwingCalendarMouseListener());
    m_dateChooser.addCalendarViewListener(new P_SwingCalendarListener());
    // F5 key for refresh
    container.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(SwingUtility.createKeystroke("F5"), "refresh");
    container.getActionMap().put("refresh", new P_SwingRefreshAction());
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    // init swing->scout properties
    final Date minDate = m_dateChooser.getViewDateStart();
    final Date maxDate = m_dateChooser.getViewDateEnd();
    // notify Scout
    Runnable t = new Runnable() {
      @Override
      public void run() {
        getScoutObject().getUIFacade().setVisibleRangeFromUI(minDate, maxDate);
      }
    };
    getSwingEnvironment().invokeScoutLater(t, 0);
    // end notify
    setSetupFromScout(getScoutObject().getDisplayMode(), getScoutObject().isDisplayCondensed());
    setCalendarComponentsFromScout(getScoutObject().getComponents());
    setSelectionFromScout(getScoutObject().getSelectedDate(), getScoutObject().getSelectedComponent());
    setWorkHours(getScoutObject().getStartHour(), getScoutObject().getEndHour(), getScoutObject().getUseOverflowCells());
    setShowDisplayModeSelectionPanel(getScoutObject().getShowDisplayModeSelection());
    setMarkNoonHour(getScoutObject().getMarkNoonHour());
    setMarkOutOfMonthDays(getScoutObject().getMarkOutOfMonthDays());
  }

  private void setWorkHours(int startHour, int endHour, boolean useOverflowCells) {
    getDateChooser().setWorkHours(startHour, endHour, useOverflowCells);
  }

  private void setShowDisplayModeSelectionPanel(boolean visible) {
    getDateChooser().setShowDisplayModeSelectionPanel(visible);
  }

  private void setMarkNoonHour(boolean visible) {
    getDateChooser().setMarkNoonHour(visible);
  }

  private void setMarkOutOfMonthDays(boolean visible) {
    getDateChooser().setMarkOutOfMonthDays(visible);
  }

  public DateChooser getDateChooser() {
    return m_dateChooser;
  }

  private void setSetupFromScout(int mode, boolean condensed) {
    getDateChooser().setDisplayMode(mode);
  }

  private void setSelectionFromScout(Date d, CalendarComponent cc) {
    getDateChooser().setDate(d);
  }

  private void setLoadInProgressFromScout(boolean b) {
    if (b) {
      getSwingField().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }
    else {
      getSwingField().setCursor(null);
    }
  }

  protected void setCalendarComponentsFromScout(CalendarComponent[] c) {
    getDateChooser().setModel(new P_SwingCalendarModel(c));
  }

  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
    if (name.equals(ICalendar.PROP_SELECTED_DATE) || name.equals(ICalendar.PROP_SELECTED_COMPONENT)) {
      setSelectionFromScout(getScoutObject().getSelectedDate(), getScoutObject().getSelectedComponent());
    }
    else if (name.equals(ICalendar.PROP_COMPONENTS)) {
      setCalendarComponentsFromScout((CalendarComponent[]) newValue);
    }
    else if (name.equals(ICalendar.PROP_LOAD_IN_PROGRESS)) {
      setLoadInProgressFromScout((Boolean) newValue);
    }
    else if (name.equals(ICalendar.PROP_DISPLAY_MODE) || name.equals(ICalendar.PROP_DISPLAY_CONDENSED)) {
      setSetupFromScout(getScoutObject().getDisplayMode(), getScoutObject().isDisplayCondensed());
    }
  }

  private void handleSwingCalendarPopup(final MouseEvent e) {
    if (getUpdateSwingFromScoutLock().isAcquired()) {
      return;
    }
    //
    final CalendarComponent item = (CalendarComponent) getDateChooser().getCalendarItemFor(e);
    if (item != null) {
      // popup on item
      // notify Scout
      Runnable t = new Runnable() {
        @Override
        public void run() {
          IMenu[] scoutMenus = getScoutObject().getUIFacade().fireComponentPopupFromUI();
          // call swing menu
          new SwingPopupWorker(getSwingEnvironment(), e.getComponent(), e.getPoint(), scoutMenus).enqueue();
        }
      };
      getSwingEnvironment().invokeScoutLater(t, 5678);
      // end notify
    }
    else {
      // popup with general menu
      // notify Scout
      Runnable t = new Runnable() {
        @Override
        public void run() {
          IMenu[] scoutMenus = getScoutObject().getUIFacade().fireNewPopupFromUI();
          // call swing menu
          new SwingPopupWorker(getSwingEnvironment(), e.getComponent(), e.getPoint(), scoutMenus).enqueue();
        }
      };
      getSwingEnvironment().invokeScoutLater(t, 5678);
      // end notify
    }
  }

  private void handleSwingCalendarItemAction(MouseEvent e) {
    if (getUpdateSwingFromScoutLock().isAcquired()) {
      return;
    }
    //
    // action on item
    // notify Scout
    Runnable t = new Runnable() {
      @Override
      public void run() {
        getScoutObject().getUIFacade().fireComponentActionFromUI();
      }
    };
    getSwingEnvironment().invokeScoutLater(t, 0);
    // end notify
  }

  /**
   * Private classes
   */

  private class P_SwingCalendarModel implements CalendarModel {
    private CalendarComponent[] m_components;
    private HashMap<Date, Collection<Object>> m_dayMap;

    public P_SwingCalendarModel(CalendarComponent[] components) {
      m_components = components;
      // build map of all items per day
      m_dayMap = new HashMap<Date, Collection<Object>>();
      for (CalendarComponent comp : m_components) {
        for (Date day : comp.getCoveredDays()) {
          Collection<Object> list = m_dayMap.get(day);
          if (list == null) {
            list = new ArrayList<Object>();
            m_dayMap.put(day, list);
          }
          list.add(comp);
        }
      }
    }

    @Override
    public Collection<Object> getItemsAt(Date dateTruncatedToDay) {
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
      return SwingUtility.createColor(comp.getCell().getBackgroundColor());
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
    public void moveItem(final Object item, final Date newDate) {
      // notify Scout
      Runnable t = new Runnable() {
        @Override
        public void run() {
          CalendarComponent comp = (CalendarComponent) item;
          getScoutObject().getUIFacade().fireComponentMovedFromUI(comp, newDate);
        }
      };
      getSwingEnvironment().invokeScoutLater(t, 0);
      // end notify
    }
  }// end class

  private class P_SwingRefreshAction extends AbstractAction {
    private static final long serialVersionUID = 1L;

    @Override
    public void actionPerformed(ActionEvent e) {
      if (getUpdateSwingFromScoutLock().isAcquired()) {
        return;
      }
      //
      // notify Scout
      Runnable t = new Runnable() {
        @Override
        public void run() {
          getScoutObject().getUIFacade().fireReloadFromUI();
        }
      };
      getSwingEnvironment().invokeScoutLater(t, 0);
      // end notify
    }
  }// end class

  private class P_SwingCalendarMouseListener extends MouseAdapter {
    MouseClickedBugFix fix;

    @Override
    public void mousePressed(MouseEvent e) {
      fix = new MouseClickedBugFix(e);
      // Mac Popup
      if (e.isPopupTrigger()) {
        handleSwingCalendarPopup(e);
      }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
      if (e.isPopupTrigger()) {
        handleSwingCalendarPopup(e);
      }
      if (fix != null) {
        fix.mouseReleased(this, e);
      }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
      if (fix.mouseClicked()) {
        return;
      }
      if (!e.isPopupTrigger() && e.getClickCount() >= 2) {
        handleSwingCalendarItemAction(e);
      }
    }
  }// end class

  private class P_SwingCalendarListener implements CalendarViewListener {
    @Override
    public void viewChanged(CalendarViewEvent e) {
      switch (e.getType()) {
        case CalendarViewEvent.TYPE_SELECTION_CHANGED: {
          if (getUpdateSwingFromScoutLock().isAcquired()) {
            return;
          }
          //
          final Date d = getDateChooser().getDate();
          final CalendarComponent cc = (CalendarComponent) getDateChooser().getSelectedItem();
          // notify Scout
          Runnable t = new Runnable() {
            @Override
            public void run() {
              getScoutObject().getUIFacade().setSelectionFromUI(d, cc);
            }
          };

          getSwingEnvironment().invokeScoutLater(t, 0);
          // end notify
          break;
        }
        case CalendarViewEvent.TYPE_SETUP_CHANGED: {
          if (getUpdateSwingFromScoutLock().isAcquired()) {
            return;
          }
          //
          final int m = getDateChooser().getDisplayMode();
          final Date minDate = getDateChooser().getViewDateStart();
          final Date maxDate = getDateChooser().getViewDateEnd();
          // notify Scout
          Runnable t = new Runnable() {
            @Override
            public void run() {
              getScoutObject().setDisplayMode(m);
              getScoutObject().getUIFacade().setVisibleRangeFromUI(minDate, maxDate);
            }
          };

          getSwingEnvironment().invokeScoutLater(t, 0);
          // end notify
          break;
        }
        case CalendarViewEvent.TYPE_VISIBLE_RANGE_CHANGED: {
          final Date minDate = getDateChooser().getViewDateStart();
          final Date maxDate = getDateChooser().getViewDateEnd();
          // notify Scout
          Runnable t = new Runnable() {
            @Override
            public void run() {
              getScoutObject().getUIFacade().setVisibleRangeFromUI(minDate, maxDate);
            }
          };

          getSwingEnvironment().invokeScoutLater(t, 0);
          // end notify
          break;
        }
      }
    }
  }// end class
}
