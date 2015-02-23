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
package org.eclipse.scout.rt.ui.swt.ext.table.internal;

import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.ui.swt.Activator;
import org.eclipse.scout.rt.ui.swt.SwtIcons;
import org.eclipse.scout.rt.ui.swt.basic.table.ISwtScoutTable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

/**
 *
 */
public class EditableTableMarkerSupport {

  private Table m_table;
  private Listener m_markerPaintListener;
  private Image m_editableMarkerImage;

  public EditableTableMarkerSupport(Table table) {
    m_table = table;
    m_editableMarkerImage = Activator.getIcon(SwtIcons.CellEditable);
    m_markerPaintListener = new P_EditableMarkerPaintLiastener();
    m_table.addListener(SWT.PaintItem, m_markerPaintListener);
    m_table.addDisposeListener(new DisposeListener() {
      @Override
      public void widgetDisposed(DisposeEvent e) {
        m_table.removeListener(SWT.PaintItem, m_markerPaintListener);
      }
    });
  }

  private boolean isEditableIconNeeded(Event event, TableItem item) {
    IColumn<?> col = ((IColumn<?>) item.getParent().getColumn(event.index).getData(ISwtScoutTable.KEY_SCOUT_COLUMN));
    if (col != null && col.isEditable() && !col.getDataType().isAssignableFrom(Boolean.class)) {
      return true;
    }
    return false;
  }

  private class P_EditableMarkerPaintLiastener implements Listener {
    @Override
    public void handleEvent(Event event) {
      TableItem item = (TableItem) event.item;
      switch (event.type) {
        case SWT.PaintItem:
          if (isEditableIconNeeded(event, item)) {
            IColumn<?> col = ((IColumn<?>) item.getParent().getColumn(event.index).getData(ISwtScoutTable.KEY_SCOUT_COLUMN));
            ICell cell = ((ITableRow) item.getData()).getCell(col);
            Image markerIcon = m_editableMarkerImage;
            if (markerIcon != null && cell.isEditable()) {
              event.gc.drawImage(markerIcon, event.x, event.y);
            }
          }
          break;
      }
    }
  }

}
