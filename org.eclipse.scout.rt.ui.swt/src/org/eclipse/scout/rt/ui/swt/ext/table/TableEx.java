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
package org.eclipse.scout.rt.ui.swt.ext.table;

import java.lang.reflect.Method;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class TableEx extends Table {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(TableEx.class);
  private static final int TEXT_MARGIN = 3;
  private P_MouseHoverListener m_mouseHoverListener = new P_MouseHoverListener();
  private boolean m_readOnly;
  private boolean m_multiline;
  private P_MultilineListener m_multilineListener;

  public TableEx(Composite parent, int style) {
    super(parent, style);
  }

  @Override
  protected void checkSubclass() {
  }

  @Override
  public Point computeSize(int hint, int hint2, boolean changed) {
    /*
     * workaround since compute size on the table returns the sum of all columns
     * plus 20px.
     */
    Point size = super.computeSize(hint, hint2, changed);
    TableColumn[] columns = getColumns();
    if (columns != null) {
      int x = 0;
      for (TableColumn col : columns) {
        x += col.getWidth();
      }
      x += columns.length * getGridLineWidth();
      size.x = x;
    }
    return size;
  }

  public void setReadOnly(boolean readOnly) {
    if (m_readOnly != readOnly) {
      m_readOnly = readOnly;
    }
  }

  public boolean isReadOnly() {
    return m_readOnly;
  }

  @Override
  public void setEnabled(boolean enabled) {
    if (enabled) {
      setForeground(null);
    }
    else {
      setForeground(getDisplay().getSystemColor(SWT.COLOR_GRAY));
    }
  }

  public void setMultiLine(boolean multiLine) {
    if (multiLine != m_multiline) {
      Method setHeightMethod;
      try {
        setHeightMethod = Table.class.getDeclaredMethod("setItemHeight", Integer.TYPE);
        setHeightMethod.setAccessible(true);
        setHeightMethod.invoke(this, new Integer(-1));
      }
      catch (Exception e) {
        LOG.warn("could not access setItemHeight method on Table class.", e);
      }
      m_multiline = multiLine;
      if (m_multiline) {
        if (m_multilineListener == null) {
          m_multilineListener = new P_MultilineListener();
        }
        addListener(SWT.MeasureItem, m_multilineListener);
        addListener(SWT.EraseItem, m_multilineListener);
        addListener(SWT.PaintItem, m_multilineListener);
      }
      else {
        if (m_multilineListener != null) {

          // meassure can not be removed due tu caching in the Table class
          removeListener(SWT.MeasureItem, m_multilineListener);
          removeListener(SWT.EraseItem, m_multilineListener);
          removeListener(SWT.PaintItem, m_multilineListener);
        }
      }
      redraw();
    }

  }

  public boolean isMultiLine() {
    return m_multiline;
  }

  private class P_MouseHoverListener extends MouseTrackAdapter implements MouseMoveListener {
    private TableItem m_mouseHoverItem;

    // case 1: mouse hover an item
    @Override
    public void mouseMove(MouseEvent e) {
      TableItem item = getItem(new Point(e.x, e.y));
      // another item under the mouse?
      if (item != m_mouseHoverItem && item != null) {
        // old mouse over item valid?
        if (m_mouseHoverItem != null && !m_mouseHoverItem.isDisposed()) {
          // change color and redraw old item
          m_mouseHoverItem.setBackground(null);
          final Rectangle b = m_mouseHoverItem.getBounds();
          redrawInternal(b);
        }
        // store, change color and redraw new item
        m_mouseHoverItem = item;
        m_mouseHoverItem.setBackground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        Rectangle b = m_mouseHoverItem.getBounds();
        redrawInternal(b);
      }
    }

    // case 2: exit from the table
    @Override
    public void mouseExit(MouseEvent e) {
      if (m_mouseHoverItem != null && !m_mouseHoverItem.isDisposed()) {
        m_mouseHoverItem.setBackground(null);
        Rectangle b = m_mouseHoverItem.getBounds();
        redrawInternal(b);
      }
    }

    // asynchronous redraw
    private void redrawInternal(final Rectangle b) {
      getDisplay().asyncExec(new Runnable() {
        @Override
        public void run() {
          if (!isDisposed()) {
            redraw(b.x, b.y, b.width, b.height, false);
          }
        }
      });
    }
  } // end class P_MouseHoverListener

  private class P_MultilineListener implements Listener {

    @Override
    public void handleEvent(Event event) {
      switch (event.type) {
        case SWT.MeasureItem:
          Point mSize = new Point(0, 0);
          TableItem mitem = (TableItem) event.item;
          Image img = mitem.getImage(event.index);
          if (img != null) {
            Rectangle imgBounds = img.getBounds();
            mSize.x += imgBounds.width + 2;
            mSize.y = Math.max(mSize.y, imgBounds.height + 2 * TEXT_MARGIN);
          }
          String mtext = mitem.getText(event.index);
          Point textSize = m_multiline ? event.gc.textExtent(mtext) : event.gc.stringExtent(mtext);
          mSize.x += textSize.x + 2 * TEXT_MARGIN;
          mSize.y = Math.max(mSize.y, textSize.y + 2 * TEXT_MARGIN);
          event.width = mSize.x;
          event.height = Math.max(event.height, mSize.y);
          break;
        case SWT.PaintItem:
          TableItem pitem = (TableItem) event.item;
          String ptext = pitem.getText(event.index);
          int align = SWT.LEFT;
          TableColumn tc = getColumn(event.index);
          if (tc != null) {
            align = (tc.getStyle() & (SWT.CENTER | SWT.LEFT | SWT.RIGHT));
          }
          /* center column 1 vertically */
          Rectangle itemBounds = pitem.getBounds(event.index);
          int xOffset = itemBounds.x;
          int yOffset = itemBounds.y + TEXT_MARGIN;
          Image pImg = pitem.getImage(event.index);
          if (pImg != null) {
            Rectangle imgBounds = pImg.getBounds();
            int yAdj = Math.max(0, (getItemHeight() - imgBounds.height) / 2);
            event.gc.drawImage(pImg, xOffset, yOffset + yAdj);
            xOffset += 1 + imgBounds.width;
          }
          if (align == SWT.RIGHT) {
            Point extent = event.gc.stringExtent(ptext);
            int dx = Math.max(TEXT_MARGIN, (itemBounds.x + itemBounds.width - xOffset - extent.x - TEXT_MARGIN));
            xOffset += dx;
          }
          else if (align == SWT.CENTER) {
            Point extent = event.gc.stringExtent(ptext);
            int dx = Math.max(TEXT_MARGIN, (itemBounds.x + itemBounds.width - xOffset - extent.x - TEXT_MARGIN) / 2);
            xOffset += dx;
          }
          else {
            xOffset += TEXT_MARGIN;
          }
          Point psize = event.gc.textExtent(ptext);
          int yAdj = Math.max(0, (getItemHeight() - psize.y) / 2);
          event.gc.drawText(ptext, xOffset, yOffset + yAdj, true);
          event.gc.setForeground(event.gc.getDevice().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
          event.gc.drawLine(itemBounds.x, itemBounds.y + itemBounds.height - 1, itemBounds.x + itemBounds.width, itemBounds.y + itemBounds.height - 1);
          break;
        case SWT.EraseItem:
          event.detail &= ~SWT.FOREGROUND;
          break;
      }
    }
  }
}
