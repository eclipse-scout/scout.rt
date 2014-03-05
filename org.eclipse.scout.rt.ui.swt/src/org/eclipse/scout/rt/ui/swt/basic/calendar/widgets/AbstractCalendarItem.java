package org.eclipse.scout.rt.ui.swt.basic.calendar.widgets;

import org.eclipse.scout.rt.ui.swt.basic.calendar.CalendarItemContainer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

/**
 * Base class of a week or month calendar item.
 * 
 * @author Michael Rudolf, Andreas Hoegger
 */
public abstract class AbstractCalendarItem extends Composite implements PaintListener, MouseTrackListener {

  /** model calendar item */
  private CalendarItemContainer m_item;

  /** reference to parent cell */
  private AbstractCell m_cell;

  private Menu m_contextMenu;

  public AbstractCalendarItem(AbstractCell parent, int style, CalendarItemContainer item) {
    super(parent, style);

    // stores the (model) calendar item container associated with this graphical item
    m_item = item;

    // stores a ref to the parent cell/composite
    m_cell = parent;

    createControls();
    setLayout();
    setupMenu();
    hookListeners();
  }

  protected CalendarItemContainer getItem() {
    return m_item;
  }

  protected void setItem(CalendarItemContainer item) {
    m_item = item;
  }

  protected AbstractCell getCell() {
    return m_cell;
  }

  protected void setCell(AbstractCell cell) {
    m_cell = cell;
  }

  protected void createControls() {

  }

  protected void setLayout() {

  }

  private void setupMenu() {
    // context menu
    m_contextMenu = new Menu(getShell(), SWT.POP_UP);
    setMenu(m_contextMenu);
  }

  protected void hookListeners() {
    addPaintListener(this);

    // menu listener for context menu
    m_contextMenu.addMenuListener(new P_ContextMenuListener());

    // intercept a mouse click
    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseDown(MouseEvent e) {
        m_cell.getCalendar().setSelectedDateFromUI(m_cell.getDate());
        m_cell.getCalendar().setSelectedItem(m_item.getItem());
      }
    });

    // for tooltip
    addMouseTrackListener(this);
  }

  // -- PaintListener

  /** needs to be overriden */
  @Override
  public void paintControl(PaintEvent e) {
  }

  // -- MouseTrackListener

  @Override
  public void mouseHover(MouseEvent e) {
    setToolTipText(m_cell.getCalendar().getModel().getTooltip(m_item.getItem(), m_cell.getDate().getTime()));
  }

  @Override
  public void mouseEnter(MouseEvent e) {
  }

  @Override
  public void mouseExit(MouseEvent e) {
    setToolTipText("");
  }

  @Override
  public String toString() {
    // text
    String s = m_cell.getCalendar().getModel().getLabel(m_item.getItem(), m_cell.getDate().getTime());

    if (this instanceof WeekCalendarItem) {
      return "WeekCalendarItem {" + s + "}";
    }
    else {
      return "MonthCalendarItem {" + s + "}";
    }

  }

  private class P_ContextMenuListener extends MenuAdapter {
    @Override
    public void menuShown(MenuEvent e) {
      // clear all previous
      // Windows BUG: fires menu hide before the selection on the menu item is
      // propagated.
      if (m_contextMenu != null) {
        for (MenuItem item : m_contextMenu.getItems()) {
          disposeMenuItem(item);
        }
      }
      m_cell.getCalendar().showItemContextMenu(m_contextMenu, m_item.getItem());

    }

    private void disposeMenuItem(MenuItem item) {
      Menu menu = item.getMenu();
      if (menu != null) {
        for (MenuItem childItem : menu.getItems()) {
          disposeMenuItem(childItem);
        }
        menu.dispose();
      }
      item.dispose();
    }

  } // end class P_ContextMenuListener
}
