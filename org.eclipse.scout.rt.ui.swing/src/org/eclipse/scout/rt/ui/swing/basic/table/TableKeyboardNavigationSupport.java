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
package org.eclipse.scout.rt.ui.swing.basic.table;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.SwingUtilities;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.ui.swing.SwingUtility;
import org.eclipse.scout.rt.ui.swing.ext.JTableEx;

public abstract class TableKeyboardNavigationSupport extends AbstractKeyboardNavigationSupport {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(TableKeyboardNavigationSupport.class);

  private final JTableEx m_table;
  private int m_contextColumnIndex = -1;
  private P_MouseListener m_mouseListener;
  private P_KeyListener m_keyListener;

  public TableKeyboardNavigationSupport(JTableEx table) {
    this(table, 500L);
  }

  public TableKeyboardNavigationSupport(JTableEx table, long delay) {
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
    if (m_mouseListener == null) {
      m_mouseListener = new P_MouseListener();
      m_table.addMouseListener(m_mouseListener);
    }
    if (m_keyListener == null) {
      m_keyListener = new P_KeyListener();
      m_table.addKeyListener(m_keyListener);
    }
  }

  private void detachListeners() {
    if (m_mouseListener != null) {
      m_table.removeMouseListener(m_mouseListener);
      m_mouseListener = null;
    }
    if (m_keyListener != null) {
      m_table.removeKeyListener(m_keyListener);
      m_keyListener = null;
    }
  }

  @Override
  void handleSearchPattern(final String regex) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        SwingTableModel tableModel = (SwingTableModel) m_table.getModel();
        int column = m_contextColumnIndex;
        if (column < 0 && m_table.getColumnCount() > 0) {
          column = 0;
        }
        if (column < 0) {
          return;
        }
        if (LOG.isInfoEnabled()) {
          LOG.info("finding regex:" + regex + " in column " + tableModel.getColumnName(column));
        }
        // loop over values and find matching one
        int startIndex = m_table.getSelectionModel().getAnchorSelectionIndex();
        if (startIndex < 0) {
          startIndex = 0;
        }
        else {
          startIndex++;
        }

        int itemCount = m_table.getRowCount();
        for (int i = 0; i < itemCount; i++) {
          int rowIndex = (startIndex + i) % itemCount;
          String value = tableModel.getValueAt(rowIndex, column).toString();
          if (value.toLowerCase().matches(regex)) {
            handleKeyboardNavigation(rowIndex);
            break;
          }
        }
      }
    });
  }

  abstract void handleKeyboardNavigation(int rowIndex);

  private class P_KeyListener extends KeyAdapter {
    @Override
    public void keyTyped(KeyEvent e) {
      if (SwingUtilities.isDescendingFrom(e.getComponent(), m_table)) {
        String keyStrokeText = SwingUtility.getKeyStrokeText(e);
        String keyText = SwingUtility.getKeyText(e);
        if (keyStrokeText != null && keyText.length() == 1) {
          char c = keyText.charAt(0);
          //newline must not be handled
          if (c == ' ' || Character.isLetterOrDigit(c)) {
            addChar(c);
          }
        }
      }
    }
  } // end class P_KeyListener

  private class P_MouseListener extends MouseAdapter {
    @Override
    public void mousePressed(MouseEvent e) {
      SwingTableColumnModel columnModel = (SwingTableColumnModel) m_table.getColumnModel();
      int uiIndex = m_table.columnAtPoint(e.getPoint());
      m_contextColumnIndex = columnModel.getColumn(uiIndex).getModelIndex();
    }
  } // end class  P_TableListener

}
