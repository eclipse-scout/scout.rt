/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.ext.table.util;

import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

/**
 * <h3>TableCellRolloverSupport</h3> ...
 * 
 * @since 3.7.0 June 2011
 */
public class TableCellRolloverSupport {
  private TableItem m_mouseHoverItem;
  private int m_cursorIndex = -1;
  private int[] m_colPositions;
  private Color m_mouseHoverBackground;
  private P_RowMouseTrackListener m_trackListener = new P_RowMouseTrackListener();
  private final Table m_table;
  private final TableViewer m_viewer;

  public TableCellRolloverSupport(TableViewer viewer) {
    m_viewer = viewer;
    m_table = m_viewer.getTable();
    m_mouseHoverBackground = new Color(m_table.getDisplay(), 179, 195, 255);
    attachListeners();
  }

  protected void attachListeners() {
    //XXX rap    getTable().addMouseMoveListener(m_trackListener);
    //XXX rap getTable().addMouseTrackListener(m_trackListener);
    getTable().addDisposeListener(new DisposeListener() {
      private static final long serialVersionUID = 1L;

      @Override
      public void widgetDisposed(DisposeEvent e) {
        dettachListeners();
        if (m_mouseHoverBackground != null && !m_mouseHoverBackground.isDisposed() && m_mouseHoverBackground.getDevice() != null) {
          m_mouseHoverBackground.dispose();
          m_mouseHoverBackground = null;
        }
      }
    });
  }

  protected void dettachListeners() {
    //XXX rap getTable().removeMouseMoveListener(m_trackListener);
    //XXX rap getTable().removeMouseTrackListener(m_trackListener);
  }

  public Table getTable() {
    return m_table;
  }

  public TableViewer getViewer() {
    return m_viewer;
  }

  private void saveColumnPositions() {
    m_colPositions = new int[getTable().getColumnCount()];
    TableItem item = getTable().getItem(0);
    for (int i = 0; i < getTable().getColumnCount(); i++) {
      m_colPositions[i] = item.getBounds(i).x + item.getBounds(i).width;
    }
  }

  private int getColumnIndex(int x) {
    int i = 0;
    while (i < m_colPositions.length && x > m_colPositions[i]) {
      i++;
    }
    return i;
  }

  private void asyncRedraw(final int x, final int y, final int width, final int height) {
    getTable().getDisplay().asyncExec(new Runnable() {
      @Override
      public void run() {
        if (getTable() != null && !getTable().isDisposed()) {
          getTable().redraw(x, y, width, height, true);
        }
      }
    });
  }

  private class P_RowMouseTrackListener extends MouseTrackAdapter implements MouseMoveListener {//XXX RAP SLE: no listener for MouseTrackAdapter in RAP, has to be made different. JScript-Injectjion?

    private static final long serialVersionUID = 1L;

    @Override
    public void mouseMove(org.eclipse.swt.events.MouseEvent e) {
      Point p = new Point(e.x, e.y);
      int index = getColumnIndex(e.x);
      if (getTable().getItem(p) != m_mouseHoverItem || m_cursorIndex != index) {
        TableItem item = getTable().getItem(p);
        if (item != null) {
          if (m_mouseHoverItem != null && !m_mouseHoverItem.isDisposed() && m_cursorIndex > -1) {
            m_mouseHoverItem.setBackground(m_cursorIndex, ((ITableColorProvider) m_viewer.getLabelProvider()).getBackground(m_mouseHoverItem
                .getData(), m_cursorIndex));
            m_mouseHoverItem.setForeground(m_cursorIndex, ((ITableColorProvider) m_viewer.getLabelProvider()).getForeground(m_mouseHoverItem
                .getData(), m_cursorIndex));
            Rectangle bounds = m_mouseHoverItem.getBounds(m_cursorIndex);
            asyncRedraw(bounds.x, bounds.y, bounds.width, bounds.height);
          }
          m_mouseHoverItem = item;
          m_cursorIndex = index;
          m_mouseHoverItem.setBackground(m_cursorIndex, m_mouseHoverBackground);
          m_mouseHoverItem.setForeground(m_cursorIndex, Display.getCurrent().getSystemColor(
              SWT.COLOR_LIST_FOREGROUND));
          Rectangle bounds = m_mouseHoverItem.getBounds(m_cursorIndex);
          asyncRedraw(bounds.x, bounds.y, bounds.width, bounds.height);
        }
      }
    }

    @Override
    public void mouseExit(org.eclipse.swt.events.MouseEvent e) {
      if (m_mouseHoverItem != null && !m_mouseHoverItem.isDisposed() && m_cursorIndex > -1) {
        m_mouseHoverItem.setBackground(m_cursorIndex, ((ITableColorProvider) getViewer().getLabelProvider()).getBackground(m_mouseHoverItem
            .getData(), m_cursorIndex));
        m_mouseHoverItem.setForeground(m_cursorIndex, ((ITableColorProvider) getViewer().getLabelProvider()).getForeground(m_mouseHoverItem
            .getData(), m_cursorIndex));
        Rectangle bounds = m_mouseHoverItem.getBounds(m_cursorIndex);
        m_mouseHoverItem = null;
        m_cursorIndex = -1;
        asyncRedraw(bounds.x, bounds.y, bounds.width, bounds.height);
      }
    }

    @Override
    public void mouseEnter(org.eclipse.swt.events.MouseEvent e) {
      if (m_colPositions == null) {
        saveColumnPositions();
      }
    }
  } // end class P_RowMouseTrackListener

}
