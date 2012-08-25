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
package org.eclipse.scout.rt.ui.swt.basic.table;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.ui.swt.ext.table.TableEx;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableItem;

/**
 * <h3>TableKeyBoardNavigationSupport</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 13.04.2010
 */
public abstract class TableKeyboardNavigationSupport extends AbstractKeyboardNavigationSupport {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(TableKeyboardNavigationSupport.class);

  private final TableEx m_table;
  private Listener m_tableListener;
  private int m_contextColumnIndex;
  private static final int EVENT_TYPE_KEY = SWT.KeyDown;

  private DisposeListener m_disposeListener = new DisposeListener() {
    @Override
    public void widgetDisposed(DisposeEvent e) {
      detachListeners();
    }
  };

  public TableKeyboardNavigationSupport(TableEx table) {
    this(table, 500L);
  }

  public TableKeyboardNavigationSupport(TableEx table, long delay) {
    super(delay);
    m_table = table;
    attachListeners();
  }

  public void dispose() {
    detachListeners();
  }

  public void resetContextColumnIndex() {
    m_contextColumnIndex = -1;
  }

  private void attachListeners() {
    if (m_tableListener == null) {

      m_tableListener = new P_TableListener();
      m_table.addListener(SWT.MouseDown, m_tableListener);
      m_table.addListener(EVENT_TYPE_KEY, m_tableListener);
    }
    m_table.addDisposeListener(m_disposeListener);
  }

  private void detachListeners() {
    if (m_tableListener != null) {
      m_table.removeListener(SWT.MouseDown, m_tableListener);
      m_table.removeListener(EVENT_TYPE_KEY, m_tableListener);
      m_tableListener = null;
    }
    m_table.removeDisposeListener(m_disposeListener);
  }

  @Override
  void handleSearchPattern(final String regex) {
    m_table.getDisplay().asyncExec(new Runnable() {
      @Override
      public void run() {
        int column = m_contextColumnIndex;
        if (column < 0) {
          for (int i = 0; i < m_table.getColumnCount(); i++) {
            if (m_table.getSortColumn() == m_table.getColumn(i)) {
              column = i;
              break;
            }
          }
        }
        if (column < 0 && m_table.getColumnCount() > 0) {
          column = 0;
        }
        if (column < 0) {
          return;
        }
        if (LOG.isInfoEnabled()) {
          LOG.info("finding regex:" + regex + " in column " + m_table.getColumn(column).getText());
        }
        // loop over values and find matching one
        int startIndex = 0;
        if (m_table.getSelectionIndex() < 0) {
          startIndex = 0;
        }
        else {
          startIndex = m_table.getSelectionIndex() + 1;
        }
        int itemCount = m_table.getItemCount();
        for (int i = 0; i < itemCount; i++) {
          TableItem tableItem = m_table.getItem((startIndex + i) % itemCount);
          String itemText = tableItem.getText(column);
          if (itemText != null && itemText.toLowerCase().matches(regex)) {
            handleKeyboardNavigation(tableItem);
            return;
          }
        }
      }
    });
  }

  abstract void handleKeyboardNavigation(TableItem tableItem);

  private class P_TableListener implements Listener {
    @Override
    public void handleEvent(Event event) {
      if (event.doit) {
        switch (event.type) {
          case EVENT_TYPE_KEY:
            if ((event.stateMask == 0 || event.stateMask == SWT.SHIFT) && Character.isLetter((char) event.keyCode) && (event.keyCode != SWT.KEYPAD_CR)) {
              addChar(Character.toLowerCase((char) event.keyCode));
            }
            break;
          case SWT.MouseDown:
            Point p = new Point(event.x, event.y);
            TableItem item = m_table.getItem(p);
            if (item != null) {
              for (int i = 0; i < m_table.getColumnCount(); i++) {
                Rectangle rect = item.getBounds(i);
                if (rect.contains(p)) {
                  m_contextColumnIndex = i;
                  break;
                }
              }
            }
            break;
          default:
            break;
        }
      }

    }
  } // end class P_TableListener

}
