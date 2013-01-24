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
package org.eclipse.scout.rt.ui.rap.basic.table;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.swt.widgets.Item;

public class RwtScoutTooltipSupport /*XXX rap extends DefaultToolTip */{
  private Cell m_uiCell;
  private ColumnViewer m_uiViewer;

  protected RwtScoutTooltipSupport(ColumnViewer uiViewer, int style, boolean manualActivation) {
    //XXX rap     super(m_uiViewer.getControl(), style, manualActivation);
    this.m_uiViewer = uiViewer;
  }

  /*
  //XXX rap
  @Override
  protected Object getToolTipArea(Event event) {
    Table table = (Table) event.widget;
    int columns = table.getColumnCount();
    Point point = new Point(event.x, event.y);
    TableItem item = table.getItem(point);

    if (item != null) {
      for (int i = 0; i < columns; i++) {
        if (item.getBounds(i).contains(point)) {
          this.m_uiCell = new Cell(item, i);
          return m_uiCell;
        }
      }
    }

    return null;
  }
  */
/*
 * //XXX rap
  @Override
  protected Composite createToolTipContentArea(Event event,
      Composite parent) {
    Composite comp = new Composite(parent, SWT.NONE);
    comp.setLayout(new FillLayout());
    Text b = new Text(comp, SWT.MULTI);
    b.setText(((ITableLabelProvider) m_uiViewer.getLabelProvider())
        .getColumnText(m_uiCell.getData(), m_uiCell.index));
    // b.setImage(((ITableLabelProvider) m_uiViewer.getLabelProvider())
    // .getColumnImage(m_uiCell.getData(), m_uiCell.index));

    return comp;
  }

*/
  public static void enableFor(ColumnViewer viewer) {
    //XXX rap new RwtScoutTooltipSupport(viewer, ToolTip.NO_RECREATE, false);
  }

  private static class Cell {
    private Item item;
    private int index;

    public Cell(Item item, int index) {
      this.item = item;
      this.index = index;
    }

    public Object getData() {
      return item.getData();
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + index;
      result = prime * result + ((item == null) ? 0 : item.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (getClass() != obj.getClass()) return false;
      final Cell other = (Cell) obj;
      if (index != other.index) return false;
      if (item == null) {
        if (other.item != null) return false;
      }
      else if (!item.equals(other.item)) return false;
      return true;
    }
  }

}
