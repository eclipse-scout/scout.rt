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
  private static final int TEXT_MARGIN_Y = 1;
  private static final int TEXT_MARGIN_X = 6;
  private static final int IMAGE_TEXT_PADDING = 1;
  private P_MouseHoverListener m_mouseHoverListener = new P_MouseHoverListener();
  private boolean m_readOnly;
  private boolean m_multiline;

  public TableEx(Composite parent, int style, boolean multiline) {
    super(parent, style);

    m_multiline = multiline;
    if (multiline) {
      Listener multilineListener = new P_MultilineListener();
      addListener(SWT.MeasureItem, multilineListener);
      addListener(SWT.EraseItem, multilineListener);
      addListener(SWT.PaintItem, multilineListener);
    }
  }

  @Override
  protected void checkSubclass() {
  }

  private int m_insideSetTopIndex;

  /**
   * bug fix of swt in win32 when using MeasureListener due to scrollfix in setTopIndex
   * <p>
   * adding loop detection to avoid stack overflow
   * 
   * <pre>
   * ...
   *   at org.eclipse.swt.widgets.Table.setTopIndex(Table.java:5223)
   *   at org.eclipse.swt.widgets.Table.setItemHeight(Table.java:4646)
   *   at org.eclipse.swt.widgets.Table.setItemHeight(Table.java:4700)
   *   at org.eclipse.swt.widgets.Table.sendMeasureItemEvent(Table.java:3761)
   *   at org.eclipse.swt.widgets.Table.hitTestSelection(Table.java:2877)
   *   at org.eclipse.swt.widgets.Table.setTopIndex(Table.java:5223)
   *   at org.eclipse.swt.widgets.Table.setItemHeight(Table.java:4646)
   *   at org.eclipse.swt.widgets.Table.setItemHeight(Table.java:4700)
   *   at org.eclipse.swt.widgets.Table.sendMeasureItemEvent(Table.java:3761)
   *   at org.eclipse.swt.widgets.Table.hitTestSelection(Table.java:2877)
   *   at org.eclipse.swt.widgets.Table.setTopIndex(Table.java:5223)
   * </pre>
   */
  @Override
  public void setTopIndex(int index) {
    if (m_insideSetTopIndex > 0) {
      return;
    }
    try {
      m_insideSetTopIndex++;
      super.setTopIndex(index);
    }
    finally {
      m_insideSetTopIndex--;
    }
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
            mSize.y = Math.max(mSize.y, imgBounds.height + 2 * TEXT_MARGIN_Y);
          }
          String mtext = mitem.getText(event.index);
          Point textSize = m_multiline ? event.gc.textExtent(mtext) : event.gc.stringExtent(mtext);
          mSize.x += textSize.x + 2 * TEXT_MARGIN_X;
          mSize.y = Math.max(mSize.y, textSize.y + 2 * TEXT_MARGIN_Y);
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
          int xImageOffset = itemBounds.x;
          int xTextOffset = xImageOffset + TEXT_MARGIN_X;
          int yOffset = itemBounds.y + TEXT_MARGIN_Y;
          Point textExtent = event.gc.stringExtent(ptext);

          Image pImg = pitem.getImage(event.index);
          Rectangle contentBounds = null;
          if (pImg != null) {
            contentBounds = pImg.getBounds();
            if (textExtent.x > 0) {
              //Add padding between image and text
              contentBounds.width = contentBounds.width + IMAGE_TEXT_PADDING;
              xTextOffset += contentBounds.width;
            }
          }
          else {
            contentBounds = new Rectangle(0, 0, 0, 0);
          }

          contentBounds.width += textExtent.x;
          contentBounds.height += textExtent.y;
          if (align == SWT.RIGHT) {
            int dx = Math.max(TEXT_MARGIN_X, (itemBounds.x + itemBounds.width - xImageOffset - contentBounds.width - TEXT_MARGIN_X));
            xTextOffset += dx;

            //Aligning the image leads to an ugly space when row gets selected...
//            xImageOffset += dx;
          }
          else if (align == SWT.CENTER) {
            int dx = Math.max(TEXT_MARGIN_X, (itemBounds.x + itemBounds.width - xImageOffset - contentBounds.width - TEXT_MARGIN_X) / 2);
            xTextOffset += dx;

            //Aligning the image leads to an ugly space when row gets selected...
//            xImageOffset += dx;
          }

          if (pImg != null) {
            event.gc.drawImage(pImg, xImageOffset, yOffset);
          }
          event.gc.drawText(ptext, xTextOffset, yOffset, true);
          event.gc.setForeground(event.gc.getDevice().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
          break;
        case SWT.EraseItem:
          //Focus rectangle looks ugly in Windows XP if there is a column with an image so we just remove the rectangle
          //In Windows Vista it would look well.
          event.detail &= ~SWT.FOCUSED;

          //Foreground should not be painted because this is done by ourself with the SWT.PaintItem-Event.
          event.detail &= ~SWT.FOREGROUND;
          break;
      }
    }
  }
}
