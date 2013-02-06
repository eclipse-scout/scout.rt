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
package org.eclipse.scout.rt.ui.rap.ext.table;

import org.eclipse.rwt.internal.textsize.TextSizeUtil;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

@SuppressWarnings("restriction")
public class TableEx extends Table {
  private static final long serialVersionUID = 1L;
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(TableEx.class);

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

  public Point getPreferredContentSize(int maxRowCount) {
    Point max = new Point(0, 0);
    for (int r = 0, nr = getItemCount(); r < nr && r < maxRowCount; r++) {
      int w = 0;
      int h = 0;
      TableItem item = getItem(r);
      for (int c = 0, nc = getColumnCount(); c < nc; c++) {
        Rectangle d = item.getBounds(c);
        String text = item.getText();
        if (!StringUtility.hasText(text)) {
          if (item.getData() instanceof ITableRow) {
            text = ((ITableRow) item.getData()).getCell(c).getText();
          }
        }
        if (StringUtility.hasText(text)) {
          int textWidth = TextSizeUtil.stringExtent(item.getFont(), text).x;
          d.width = textWidth;
        }
        w += d.width;
        h = Math.max(h, d.height);
      }
      h = Math.max(h, item.getBounds().height);
      //Add some points in heigt to ensure we do not have a scrollbar (depending to the count of items)
      if (nr > 3) {
        max.y += h + 1;
      }
      else if (nr > 1) {
        max.y += h + 2;
      }
      else {
        max.y += h + 4;
      }
      max.x = Math.max(max.x, w);
    }
    return max;
  }

  @Override
  public void setEnabled(boolean enabled) {
    //XXX set a style instead of a fg
    if (enabled) {
      setForeground(null);
    }
    else {
      setForeground(getDisplay().getSystemColor(SWT.COLOR_GRAY));
    }
  }

}
