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

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.window.DefaultToolTip;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class SwtScoutTooltipSupport extends DefaultToolTip {
  private Cell cell;
  private ColumnViewer viewer;

  protected SwtScoutTooltipSupport(ColumnViewer viewer, int style,
      boolean manualActivation) {
    super(viewer.getControl(), style, manualActivation);
    this.viewer = viewer;
  }

  @Override
  protected Object getToolTipArea(Event event) {
    Table table = (Table) event.widget;
    int columns = table.getColumnCount();
    Point point = new Point(event.x, event.y);
    TableItem item = table.getItem(point);

    if (item != null) {
      for (int i = 0; i < columns; i++) {
        if (item.getBounds(i).contains(point)) {
          this.cell = new Cell(item, i);
          return cell;
        }
      }
    }

    return null;
  }

  @Override
  protected Composite createToolTipContentArea(Event event,
      Composite parent) {
    Composite comp = new Composite(parent, SWT.NONE);
    comp.setLayout(new FillLayout());
    Text b = new Text(comp, SWT.MULTI);
    b.setText(((ITableLabelProvider) viewer.getLabelProvider())
        .getColumnText(cell.getData(), cell.index));
    // b.setImage(((ITableLabelProvider) viewer.getLabelProvider())
    // .getColumnImage(cell.getData(), cell.index));

    return comp;
  }

  public static void enableFor(ColumnViewer viewer) {
    new SwtScoutTooltipSupport(viewer, ToolTip.NO_RECREATE, false);
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
