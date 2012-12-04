package org.eclipse.scout.svg.ui.rap.calendarfield;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

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
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.w3c.dom.svg.SVGDocument;

public class RwtScoutCalendarField extends AbstractRwtScoutSvgComposite<ICalendarField<?>> implements IRwtScoutCalendarField {

  private final static int CONTEXT_MENU_POS_INSET = 10;

  private P_InnerCalendarPropertyChangeListener m_innerCalPropertyListener;
  private AbstractCalendarDocumentBuilder[] m_documentBuilders;

  private Menu m_contextMenu;

  public RwtScoutCalendarField() {
    m_documentBuilders = new AbstractCalendarDocumentBuilder[4];
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
  protected void initializeUi(Composite parent) {
    super.initializeUi(parent);

    m_contextMenu = new Menu(getUiField().getShell(), SWT.POP_UP);
    m_contextMenu.addMenuListener(new P_ContextMenuListener());
    getUiField().addDisposeListener(new DisposeListener() {
      private static final long serialVersionUID = 1L;

      @Override
      public void widgetDisposed(DisposeEvent e) {
        if (m_contextMenu != null && !m_contextMenu.isDisposed()) {
          m_contextMenu.dispose();
        }
      }
    });
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
  protected void locationChangedFromUi(LocationEvent event) {
    if (event == null || event.location == null || event.location.length() < 1) return;

    // forward the hyperlink event to the document
    getDocBuilder().hyperlinkActivated(event.location);
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
    builder.setShownDate(selDate);
    builder.setSelection(selDate, selComp);
    builder.setNumContextMenus(getContextMenusFromScout().length);
    builder.setComponents(getScoutObject().getCalendar().getComponents());
    setViewRangeFromUi(builder.getStartDate(), builder.getEndDate());
  }

  private void showPopupMenu() {
    Rectangle calBounds = getAbsoluteCalendarBounds();
    Point contextMenuPos = new Point(calBounds.x + calBounds.width - CONTEXT_MENU_POS_INSET, calBounds.y + calBounds.height - CONTEXT_MENU_POS_INSET);

    getUiField().setMenu(m_contextMenu);
    m_contextMenu.addMenuListener(new MenuAdapter() {
      private static final long serialVersionUID = 1L;

      @Override
      public void menuHidden(MenuEvent e) {
        getUiField().setMenu(null);
        ((Menu) e.getSource()).removeMenuListener(this);
      }
    });
    m_contextMenu.setLocation(contextMenuPos);
    m_contextMenu.setVisible(true);
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
    protected Menu getContextMenu() {
      return m_contextMenu;
    }

    @Override
    protected void setContextMenu(Menu contextMenu) {
      m_contextMenu = contextMenu;
    }

    @Override
    public void menuShown(MenuEvent e) {
      super.menuShown(e);

      final long TIMEOUT = 1200;
      final AtomicReference<IMenu[]> scoutMenusRef = new AtomicReference<IMenu[]>();

      try {
        RwtScoutCalendarField.this.getUiEnvironment().invokeScoutLater(new Runnable() {
          @Override
          public void run() {
            scoutMenusRef.set(getContextMenusFromScout());
          }
        }, TIMEOUT).join(TIMEOUT);
      }
      catch (InterruptedException ex) {
        //nop
      }

      RwtMenuUtility.fillContextMenu(scoutMenusRef.get(), RwtScoutCalendarField.this.getUiEnvironment(), m_contextMenu);
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
