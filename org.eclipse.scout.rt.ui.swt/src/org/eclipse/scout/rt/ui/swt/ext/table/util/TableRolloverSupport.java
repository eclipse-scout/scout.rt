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
package org.eclipse.scout.rt.ui.swt.ext.table.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

public class TableRolloverSupport {

  private final Table m_table;
  private P_MouseHoverListener m_mouseHoverListener = new P_MouseHoverListener();

  public TableRolloverSupport(Table table) {
    m_table = table;
    attachUiListeners();
  }

  public Table getTable() {
    return m_table;
  }

  protected void attachUiListeners() {
    getTable().addMouseMoveListener(m_mouseHoverListener);
    getTable().addMouseTrackListener(m_mouseHoverListener);
    getTable().addDisposeListener(new DisposeListener() {
      @Override
      public void widgetDisposed(DisposeEvent e) {
        detachUiListeners();
      }
    });
  }

  protected void detachUiListeners() {
    getTable().removeMouseMoveListener(m_mouseHoverListener);
    getTable().removeMouseTrackListener(m_mouseHoverListener);
  }

  private class P_MouseHoverListener extends MouseTrackAdapter implements MouseMoveListener {
    private TableItem m_mouseHoverItem;

    // case 1: mouse hover an item
    @Override
    public void mouseMove(MouseEvent e) {
      Table table = getTable();
      TableItem item = table.getItem(new Point(e.x, e.y));
      // another item under the mouse?
      if (item != m_mouseHoverItem && item != null) {
        // old mouse over item valid?
        if (m_mouseHoverItem != null && !m_mouseHoverItem.isDisposed()) {
          // change color and redraw old item
          m_mouseHoverItem.setBackground(null);
          final Rectangle b = m_mouseHoverItem.getBounds();
          redraw(b, table);
        }
        // store, change color and redraw new item
        m_mouseHoverItem = item;
        m_mouseHoverItem.setBackground(getTable().getDisplay().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        Rectangle b = m_mouseHoverItem.getBounds();
        redraw(b, table);
      }
    }

    // case 2: exit from the table
    @Override
    public void mouseExit(MouseEvent e) {
      if (m_mouseHoverItem != null && !m_mouseHoverItem.isDisposed()) {
        m_mouseHoverItem.setBackground(null);
        Rectangle b = m_mouseHoverItem.getBounds();
        redraw(b, getTable());
      }
    }

    // asynchronous redraw
    private void redraw(final Rectangle b, final Table table) {
      getTable().getDisplay().asyncExec(new Runnable() {
        @Override
        public void run() {
          if (table != null && !table.isDisposed()) {
            table.redraw(b.x, b.y, b.width, b.height, false);
          }
        }
      });
    }
  } // end class P_MouseHoverListener

}
