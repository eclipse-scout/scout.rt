/*******************************************************************************
 * Copyright (c) 2012,2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.svg.ui.rap.calendarfield;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.calendar.CalendarComponent;
import org.eclipse.scout.rt.client.ui.basic.calendar.ICalendar;
import org.eclipse.scout.rt.client.ui.form.fields.calendarfield.ICalendarField;
import org.eclipse.scout.rt.ui.rap.RwtMenuUtility;
import org.eclipse.scout.rt.ui.rap.ext.MenuAdapterEx;
import org.eclipse.scout.rt.ui.svg.calendar.builder.AbstractCalendarDocumentBuilder;
import org.eclipse.scout.rt.ui.svg.calendar.builder.listener.ICalendarDocumentListener;
import org.eclipse.scout.svg.ui.rap.AbstractRwtScoutSvgComposite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Menu;
import org.w3c.dom.svg.SVGDocument;

public class RwtScoutCalendarField extends AbstractRwtScoutSvgComposite<ICalendarField<?>> implements IRwtScoutCalendarField {

  private static final int CALENDAR_DISPLAY_MODE_COUNT = 4;
  private static final int CONTEXT_MENU_POS_INSET = 10;

  private P_InnerCalendarPropertyChangeListener m_innerCalPropertyListener;
  private AbstractCalendarDocumentBuilder[] m_documentBuilders;

  public RwtScoutCalendarField() {
    m_documentBuilders = new AbstractCalendarDocumentBuilder[CALENDAR_DISPLAY_MODE_COUNT];
  }

  @Override
  protected SVGDocument getSvgDocument() {
    return getDocBuilder().getSVGDocument();
  }

  @Override
  protected void updateSvgDocument() {
    // inform the doc builder about the current size of the calendar (used for text scaling)
    Rectangle r = getAbsoluteCalendarBounds();
    getDocBuilder().setSize(r.width, r.height);

    // continue refresh
    super.updateSvgDocument();
  }

  @Override
  protected void detachScout() {
    if (m_innerCalPropertyListener != null && getScoutObject() != null && getScoutObject().getCalendar() != null) {
      getScoutObject().getCalendar().removePropertyChangeListener(m_innerCalPropertyListener);
      m_innerCalPropertyListener = null;
    }

    // release svg documents
    for (int i = 0; i < m_documentBuilders.length; i++) {
      if (m_documentBuilders[i] != null) {
        m_documentBuilders[i].dispose();
      }
      m_documentBuilders[i] = null;
    }
    super.detachScout();
  }

  @Override
  protected void attachScout() {
    super.attachScout();

    if (getScoutObject() != null && getScoutObject().getCalendar() != null) {
      if (m_innerCalPropertyListener == null) {
        m_innerCalPropertyListener = new P_InnerCalendarPropertyChangeListener();
        getScoutObject().getCalendar().addPropertyChangeListener(m_innerCalPropertyListener);
      }

      initDocBuilder(getDocBuilder());
      updateSvgDocument();
    }
  }

  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
    if (ICalendar.PROP_VIEW_RANGE.equals(name)) {
      updateSvgDocument();
    }
    else if (ICalendar.PROP_SELECTED_DATE.equals(name)) {
      getDocBuilder().setNumContextMenus(getContextMenusFromScout().length);
      updateSvgDocument();
    }
    else if (ICalendar.PROP_SELECTED_COMPONENT.equals(name)) {
      getDocBuilder().setNumContextMenus(getContextMenusFromScout().length);
      updateSvgDocument();
    }
    else if (ICalendar.PROP_COMPONENTS.equals(name)) {
      getDocBuilder().setComponents(getScoutObject().getCalendar().getComponents());
      updateSvgDocument();
    }
    else if (ICalendar.PROP_DISPLAY_MODE.equals(name)) {
      initDocBuilder(getDocBuilder());
      updateSvgDocument();
    }
  }

  @Override
  protected void hyperlinkActivatedFromUi(String url) {
    if (url == null || url.length() < 1) {
      return;
    }

    // forward the hyperlink event to the document
    getDocBuilder().hyperlinkActivated(url);
  }

  private AbstractCalendarDocumentBuilder getDocBuilder() {
    int mode = getScoutObject().getCalendar().getDisplayMode();
    int index = mode - 1;
    AbstractCalendarDocumentBuilder ret = m_documentBuilders[index];
    if (ret == null) {
      ret = AbstractCalendarDocumentBuilder.createInstance(mode);
      initDocBuilder(ret);
      ret.addCalendarDocumentListener(new P_CalendarDocumentListener());
      m_documentBuilders[index] = ret; // save instance
    }
    return ret;
  }

  private void initDocBuilder(AbstractCalendarDocumentBuilder builder) {
    Date selDate = getScoutObject().getCalendar().getSelectedDate();
    CalendarComponent selComp = getScoutObject().getCalendar().getSelectedComponent();

    builder.setWorkHours(getScoutObject().getCalendar().getStartHour(), getScoutObject().getCalendar().getEndHour(), getScoutObject().getCalendar().getUseOverflowCells());
    builder.setShowDisplayModeSelectionPanel(getScoutObject().getCalendar().getShowDisplayModeSelection());
    builder.setMarkNoonHour(getScoutObject().getCalendar().getMarkNoonHour());
    builder.setMarkOutOfMonthDays(getScoutObject().getCalendar().getMarkOutOfMonthDays());
    builder.reconfigureLayout();
    builder.setShownDate(selDate);
    builder.setSelection(selDate, selComp);
    builder.setNumContextMenus(getContextMenusFromScout().length);
    builder.setComponents(getScoutObject().getCalendar().getComponents());
    setViewRangeFromUi(builder.getStartDate(), builder.getEndDate());
  }

  private void showPopupMenu() {
    Rectangle calBounds = getAbsoluteCalendarBounds();
    Point contextMenuPos = new Point(calBounds.x + calBounds.width - CONTEXT_MENU_POS_INSET, calBounds.y + calBounds.height - CONTEXT_MENU_POS_INSET);

    createAndShowMenu(contextMenuPos);
  }

  private Menu createMenu() {
    if (getUiField().getMenu() != null) {
      getUiField().getMenu().dispose();
      getUiField().setMenu(null);
    }

    Menu contextMenu = new Menu(getUiField().getShell(), SWT.POP_UP);
    contextMenu.addMenuListener(new P_ContextMenuListener());
    getUiField().setMenu(contextMenu);

    return contextMenu;
  }

  private void createAndShowMenu(Point location) {
    Menu menu = createMenu();
    showMenu(menu, location);
  }

  private void showMenu(Menu menu, Point location) {
    menu.setLocation(location);
    menu.setVisible(true);
  }

  private Rectangle getAbsoluteCalendarBounds() {
    Rectangle ret = new Rectangle(0, 0, 0, 0);
    Rectangle browserBounds = getAbsoluteBrowserBounds();

    float calRatio = AbstractCalendarDocumentBuilder.ORIG_CALENDAR_WIDTH / AbstractCalendarDocumentBuilder.ORIG_CALENDAR_HEIGHT;
    if (calRatio < browserBounds.width / (double) browserBounds.height) {
      // browser is wider than the calendar -> height limits the size of the calendar
      ret.width = (int) Math.round(browserBounds.height * calRatio);
      ret.height = browserBounds.height;
    }
    else {
      ret.width = browserBounds.width;
      ret.height = (int) Math.round(browserBounds.width / calRatio);
    }

    ret.x = browserBounds.x + (int) Math.round((browserBounds.width - ret.width) / 2.0);
    ret.y = browserBounds.y + (int) Math.round((browserBounds.height - ret.height) / 2.0);
    return ret;
  }

  private void setViewRangeFromUi(final Date start, final Date end) {
    if (getScoutObject() != null && getScoutObject().getCalendar() != null) {
      getUiEnvironment().invokeScoutLater(new Runnable() {
        @Override
        public void run() {
          getScoutObject().getCalendar().getUIFacade().setVisibleRangeFromUI(start, end);
        }
      }, 0);
    }
  }

  private void setDisplayModeFromUi(final int mode) {
    getUiEnvironment().invokeScoutLater(new Runnable() {
      @Override
      public void run() {
        getScoutObject().getCalendar().setDisplayMode(mode);
      }
    }, 0);
  }

  private void setSelectionFromUi(final Date d, final CalendarComponent c) {
    if (getScoutObject() != null && getScoutObject().getCalendar() != null) {
      getUiEnvironment().invokeScoutLater(new Runnable() {
        @Override
        public void run() {
          getScoutObject().getCalendar().getUIFacade().setSelectionFromUI(d, c);
        }
      }, 0);
    }
  }

  private IMenu[] getContextMenusFromScout() {
    return RwtMenuUtility.collectMenus(getScoutObject().getCalendar(), true, true, getUiEnvironment());
  }

  private final class P_InnerCalendarPropertyChangeListener implements PropertyChangeListener {
    @Override
    public void propertyChange(final PropertyChangeEvent e) {
      debugHandlePropertyChanged(e);
      if (isIgnoredScoutEvent(PropertyChangeEvent.class, e.getPropertyName())) {
        return;
      }
      if (isHandleScoutPropertyChange(e.getPropertyName(), e.getNewValue()) &&
          getUiEnvironment().getDisplay() != null &&
          !getUiEnvironment().getDisplay().isDisposed()) {
        Runnable t = new Runnable() {
          @Override
          public void run() {
            if (!isUiDisposed()) {
              try {
                getUpdateUiFromScoutLock().acquire();
                handleScoutPropertyChange(e.getPropertyName(), e.getNewValue());
              }
              finally {
                getUpdateUiFromScoutLock().release();
              }
            }
          }
        };
        getUiEnvironment().invokeUiLater(t);
      }
    }
  } // end class P_InnerCalendarPropertyChangeListener

  private final class P_ContextMenuListener extends MenuAdapterEx {
    private static final long serialVersionUID = 1L;

    public P_ContextMenuListener() {
      super(getUiField(), getUiContainer());
    }

    @Override
    public void menuShown(MenuEvent e) {
      super.menuShown(e);

      Menu menu = ((Menu) e.getSource());
      RwtMenuUtility.fillContextMenu(getContextMenusFromScout(), RwtScoutCalendarField.this.getUiEnvironment(), menu);
    }
  } // end class P_ContextMenuListener

  private final class P_CalendarDocumentListener implements ICalendarDocumentListener {
    @Override
    public void visibleRangeChanged(Date start, Date end) {
      setViewRangeFromUi(start, end);
    }

    @Override
    public void displayModeMenuActivated(int displayMode) {
      setDisplayModeFromUi(displayMode);
    }

    @Override
    public void popupMenuActivated() {
      showPopupMenu();
    }

    @Override
    public void selectionChanged(Date selectedDate, CalendarComponent selectedComponent) {
      setSelectionFromUi(selectedDate, selectedComponent);
    }
  } // end class P_CalendarDocumentListener
}
