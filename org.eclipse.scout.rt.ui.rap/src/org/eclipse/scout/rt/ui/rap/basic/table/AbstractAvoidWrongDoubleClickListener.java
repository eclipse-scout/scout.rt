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
package org.eclipse.scout.rt.ui.rap.basic.table;

import org.eclipse.scout.commons.holders.IntegerHolder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Widget;

/**
 * Once the RAP <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=358858">BUG 358858</a> is solved and verified
 * this all implementations of this class can directly implement {@link Listener}.
 * 
 * @see BUG https://bugs.eclipse.org/bugs/show_bug.cgi?id=358858
 */
public abstract class AbstractAvoidWrongDoubleClickListener implements Listener {

  private static final long serialVersionUID = 1L;

  private final IntegerHolder mouseUpIndex = new IntegerHolder();
  private final IntegerHolder mouseDoubleClickIndex = new IntegerHolder();

  @Override
  public final void handleEvent(Event event) {
    switch (event.type) {
      case SWT.MouseDoubleClick:
        int dcIndex = getElementIndex(event);
        if (dcIndex > 0) {
          if (mouseUpIndex.getValue() != null && mouseUpIndex.getValue() != dcIndex) {
            // aviod
            break;
          }
          // doit
          mouseDoubleClickIndex.setValue(dcIndex);
        }
        handleEventInternal(event);
        break;
      case SWT.MouseUp:
        // avoid first mouse up after double click event
        if (mouseDoubleClickIndex.getValue() != null) {
          mouseDoubleClickIndex.setValue(null);
          break;
        }
        int index = getElementIndex(event);
        if (index >= 0) {
          mouseUpIndex.setValue(index);
        }
        else {
          mouseUpIndex.setValue(null);
        }
        handleEventInternal(event);
        break;
      default:
        handleEventInternal(event);
        break;
    }
  }

  public abstract void handleEventInternal(Event e);

  protected int getElementIndex(Event event) {
    Point eventPosition = new Point(event.x, event.y);
    Widget widget = event.widget;
    if (widget instanceof List) {
      return getItemIndex(eventPosition, (List) widget);
    }
    else if (widget instanceof Table) {
      return getItemIndex(eventPosition, (Table) widget);
    }
    return -1;
  }

  protected int getItemIndex(Point point, List list) {
    return getItemIndex(point, list.getClientArea(), list.getItemHeight(), list.getTopIndex());

  }

  protected int getItemIndex(Point point, Table table) {
    return getItemIndex(point, table.getClientArea(), table.getItemHeight(), table.getTopIndex());
  }

  private int getItemIndex(Point point, Rectangle itemsArea, int itemHeight, int topIndex) {
    if (itemsArea.contains(point)) {
      int index = (point.y / itemHeight) - 1;
      if (point.y % itemHeight != 0) {
        index++;
      }
      index += topIndex;
      return index;
    }
    return -1;
  }

}
