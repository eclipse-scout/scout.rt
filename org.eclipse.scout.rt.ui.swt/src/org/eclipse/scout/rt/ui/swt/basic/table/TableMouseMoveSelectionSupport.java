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

import org.eclipse.scout.rt.ui.swt.form.fields.ISwtScoutDndSupport;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

/**
 * Gives the possibility to select table rows by pressing the left mouse button and moving over the rows to select.
 * <p>
 * Does nothing if the given table does not allow the selection of multiple rows.
 * 
 * @since 3.8.2
 */
public class TableMouseMoveSelectionSupport {
  private Table m_table;
  private boolean m_mouseDown = false;
  private int m_selectionStartIndex;

  private ISwtScoutDndSupport m_dndSupport;
  private TableItem m_itemToSelect;
  private TableItem[] m_existingSelection;

  public TableMouseMoveSelectionSupport(Table table, ISwtScoutDndSupport dndSupport) {
    m_table = table;
    m_dndSupport = dndSupport;

    if ((m_table.getStyle() & SWT.MULTI) != 0) {
      m_table.addMouseListener(new P_MouseListener());
      m_table.addMouseMoveListener(new P_MouseMoveListener());
      m_table.addMouseTrackListener(new P_MouseTrackListener());
      m_table.addSelectionListener(new P_SelectionListener());
    }
  }

  public Table getTable() {
    return m_table;
  }

  private void stopSelecting() {
    m_mouseDown = false;
    selectingStopped();
  }

  /**
   * May be overridden to add custom handling after completing the selection.
   */
  protected void selectingStopped() {

  }

  /**
   * Can be used by dnd support to check whether the items under the mouse cursor are selected and therefore can be
   * dragged. If the items are not already selected, moving the mouse with a pressed left mouse button should create a
   * selection instead of dragging the element. Therefore false will be returned in that case.
   */
  public boolean acceptDrag() {
    if (m_itemToSelect == null) {
      return true;
    }

    if (m_existingSelection != null) {
      for (TableItem selectedItem : m_existingSelection) {
        if (selectedItem == m_itemToSelect) {
          return true;
        }
      }
    }

    return false;
  }

  private class P_MouseListener extends MouseAdapter {

    @Override
    public void mouseDown(MouseEvent event) {
      if (event.button != 1) {
        return;
      }

      m_mouseDown = true;
      m_selectionStartIndex = -1;
      TableItem item = getTable().getItem(new Point(event.x, event.y));
      if (item != null) {
        m_selectionStartIndex = getTable().indexOf(item);
      }
    }

    @Override
    public void mouseUp(MouseEvent e) {
      stopSelecting();
    }

  }

  private class P_MouseMoveListener implements MouseMoveListener {

    @Override
    public void mouseMove(MouseEvent event) {
      if (m_dndSupport != null && m_dndSupport.isDraggingEnabled()) {
        //Save current selection and the item at mouse pos.
        //At the moment of dragging the selection has already been changed so it's not possible to check if the user clicked on an already selected item or not.
        m_itemToSelect = getTable().getItem(new Point(event.x, event.y));

        //Saving the elements in P_SelectionListener is not sufficient because the selection event seems not to be fired properly after selecting multiple items with the mouse
        //So it's necessary to save the selection on mouse move too
        m_existingSelection = getTable().getSelection();
      }

      if (!m_mouseDown || m_selectionStartIndex == -1) {
        return;
      }
      m_itemToSelect = getTable().getItem(new Point(event.x, event.y));
      if (m_itemToSelect != null) {
        int currentIndex = getTable().indexOf(m_itemToSelect);
        if ((event.stateMask & SWT.MOD1) == 0) {
          //If ctrl is not pressed select from mouse down index to current index
          if (m_selectionStartIndex <= currentIndex) {
            getTable().setSelection(m_selectionStartIndex, currentIndex);
          }
          else {
            getTable().setSelection(currentIndex, m_selectionStartIndex);
          }
        }
        else {
          // If ctrl is pressed separately add current indices to the selection to keep the original selection
          if (m_selectionStartIndex <= currentIndex) {
            for (int row = m_selectionStartIndex; row <= currentIndex; row++) {
              getTable().select(row);
            }
          }
          else {
            for (int row = currentIndex; row < m_selectionStartIndex; row++) {
              getTable().select(row);
            }
          }

        }
      }
    }
  }

  private class P_MouseTrackListener extends MouseTrackAdapter {

    @Override
    public void mouseExit(MouseEvent e) {
      stopSelecting();
    }

  }

  private class P_SelectionListener extends SelectionAdapter {

    @Override
    public void widgetSelected(SelectionEvent e) {
      if (m_dndSupport != null && m_dndSupport.isDraggingEnabled()) {
        m_existingSelection = getTable().getSelection();
      }
    }

  }
}
