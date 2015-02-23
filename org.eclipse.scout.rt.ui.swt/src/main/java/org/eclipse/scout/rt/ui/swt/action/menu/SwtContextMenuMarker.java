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
package org.eclipse.scout.rt.ui.swt.action.menu;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.rt.ui.swt.Activator;
import org.eclipse.scout.rt.ui.swt.SwtIcons;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Scrollable;

/**
 *
 */
public class SwtContextMenuMarker implements ISwtContextMenuMarker {

  private final Image m_dropDownIcon;
  private final Control m_owner;
  private boolean m_visible;
  private Rectangle m_dropDownBounds = null;
  private Point m_mouseDownPosition = null;
  private List<SelectionListener> m_selectionListeners;
  private int m_position;

  /**
   * @param owner
   * @param position
   *          one of {@link SWT#RIGHT} or {@link SWT#LEFT}
   */
  public SwtContextMenuMarker(Control owner, int position) {
    m_owner = owner;
    m_position = position;
    m_selectionListeners = new ArrayList<SelectionListener>();
    m_dropDownIcon = Activator.getIcon(SwtIcons.DropDownFieldArrowDownDisabled);
    owner.addPaintListener(new P_ContextMenuPaintListener());
    owner.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseDown(MouseEvent e) {

        if (e.button == 1 && isPositionInDropDownBounds(e.x, e.y)) {
          m_mouseDownPosition = new Point(e.x, e.y);
        }
      }

      @Override
      public void mouseUp(MouseEvent e) {
        if (e.button == 1 && m_mouseDownPosition != null && isPositionInDropDownBounds(e.x, e.y)) {
          handleMarkerSelected(e);
        }
        m_mouseDownPosition = null;
      }
    });
    owner.addMouseMoveListener(new MouseMoveListener() {
      private Cursor m_backupCursor = null;
      private Cursor m_arrowCursor = getOwner().getDisplay().getSystemCursor(SWT.CURSOR_ARROW);

      @Override
      public void mouseMove(MouseEvent e) {
        if (isPositionInDropDownBounds(e.x, e.y)) {
          if (m_backupCursor == null) {
            m_backupCursor = getOwner().getCursor();
            getOwner().setCursor(m_arrowCursor);
          }
        }
        else if (m_backupCursor != null) {
          getOwner().setCursor(m_backupCursor);
          m_backupCursor = null;
        }
      }
    });
  }

  private boolean isPositionInDropDownBounds(int x, int y) {
    if (m_dropDownBounds != null) {
      if (x > m_dropDownBounds.x - 3 && y > m_dropDownBounds.y - 3 &&
          x < m_dropDownBounds.x + m_dropDownBounds.width + 3 && y < m_dropDownBounds.y + m_dropDownBounds.height + 6) {
        return true;
      }
    }
    return false;
  }

  public Control getOwner() {
    return m_owner;
  }

  public int getPosition() {
    return m_position;
  }

  @Override
  public void setMarkerVisible(boolean visible) {
    m_visible = visible;
    m_owner.redraw();
  }

  @Override
  public boolean isMarkerVisible() {
    return m_visible;
  }

  @Override
  public void addSelectionListener(SelectionListener listener) {
    synchronized (m_selectionListeners) {
      m_selectionListeners.add(listener);
    }
  }

  @Override
  public boolean removeSelectionListener(SelectionListener listener) {
    synchronized (m_selectionListeners) {
      return m_selectionListeners.remove(listener);
    }
  }

  private void handleMarkerSelected(MouseEvent e) {
    List<SelectionListener> listeners = null;
    synchronized (m_selectionListeners) {
      listeners = CollectionUtility.arrayList(m_selectionListeners);
    }
    Event event = new Event();
    event.button = e.button;
    event.count = e.count;
    event.data = e.data;
    event.doit = true;
    event.display = e.display;
    event.stateMask = e.stateMask;
    event.time = e.time;
    event.widget = e.widget;
    event.x = e.x;
    event.y = e.y;
    SelectionEvent selectionEvent = new SelectionEvent(event);
    for (SelectionListener l : listeners) {
      l.widgetSelected(selectionEvent);
      if (!selectionEvent.doit) {
        break;
      }
    }
  }

  public static int getMarkerAlignment(int scoutObjectAlignment) {

    if (scoutObjectAlignment > 0) {
      return SWT.LEFT;
    }
    return SWT.RIGHT;
  }

  private class P_ContextMenuPaintListener implements PaintListener {

    @Override
    public void paintControl(PaintEvent e) {
      // clear old icon (the only way how this seems to work)
      Rectangle clipping = e.gc.getClipping();
      if (m_dropDownBounds != null && m_dropDownBounds.intersects(clipping)) {
        m_dropDownBounds = null;
        getOwner().redraw();
        return;
      }
      if (getPosition() == SWT.LEFT) {
        paintTopLeft(e);
      }
      else {
        paintTopRight(e);
      }
    }

    private void paintTopLeft(PaintEvent e) {
      if (isMarkerVisible()) {
        Point iconSize = new Point(m_dropDownIcon.getImageData().width, m_dropDownIcon.getImageData().height);
        Rectangle clientArea = getOwner().getBounds();
        if (getOwner() instanceof Scrollable) {
          clientArea = ((Scrollable) getOwner()).getClientArea();
        }
        Rectangle iconBounds = new Rectangle(clientArea.x, clientArea.y, iconSize.x, iconSize.y);
        Rectangle clipping = e.gc.getClipping();
        if (clipping.equals(iconBounds)) {
          e.gc.drawImage(m_dropDownIcon, iconBounds.x, iconBounds.y);
          m_dropDownBounds = iconBounds;
        }
        else {
          // redraw new icon
          getOwner().redraw(iconBounds.x, iconBounds.y, iconBounds.width, iconBounds.height, false);
        }
      }
    }

    private void paintTopRight(PaintEvent e) {
      if (isMarkerVisible()) {
        Point iconSize = new Point(m_dropDownIcon.getImageData().width, m_dropDownIcon.getImageData().height);
        Rectangle clientArea = getOwner().getBounds();
        if (getOwner() instanceof Scrollable) {
          clientArea = ((Scrollable) getOwner()).getClientArea();
        }
        Rectangle iconBounds = new Rectangle(clientArea.x + clientArea.width - iconSize.x, clientArea.y, iconSize.x, iconSize.y);
        Rectangle clipping = e.gc.getClipping();
        if (clipping.equals(iconBounds)) {
          e.gc.drawImage(m_dropDownIcon, iconBounds.x, iconBounds.y);
          m_dropDownBounds = iconBounds;
        }
        else {
          // redraw new icon
          getOwner().redraw(iconBounds.x, iconBounds.y, iconBounds.width, iconBounds.height, false);
        }
      }
    }
  }
}
