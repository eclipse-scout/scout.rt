/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json.table;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.TableEvent;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.ui.html.json.AbstractEventFilter;

public class TableEventFilter extends AbstractEventFilter<TableEvent, TableEventFilterCondition> {

  private JsonTable<? extends ITable> m_jsonTable;

  public TableEventFilter(JsonTable<? extends ITable> jsonTable) {
    m_jsonTable = jsonTable;
  }

  @Override
  public TableEvent filter(TableEvent event) {
    for (TableEventFilterCondition condition : getConditions()) {
      if (condition.getType() == event.getType()) {
        if (condition.checkRows()) {
          List<ITableRow> rows = new ArrayList<>(event.getRows());
          rows.removeAll(condition.getRows());
          if (rows.size() == 0) {
            //Event should be ignored if no nodes remain or if the event contained no nodes at all
            return null;
          }

          TableEvent newEvent = new TableEvent((ITable) m_jsonTable.getModel(), event.getType(), rows);
          return newEvent;
        }
        else if (condition.checkColumns()) {
          //Columns are not delivered by the event itself (at least not with COLUMN_ORDER_CHANGED) -> grab from table
          if (CollectionUtility.equalsCollection(m_jsonTable.getColumnsInViewOrder(), condition.getColumns())) {
            return null;
          }
          //Don't ignore if columns are different
          return event;
        }

        return null;
      }
      else if (condition.checkUserFilter()) {
        if (condition.getUserFilter().equals(event.getUserFilter())) {
          return null;
        }
        //Don't ignore if filters are different
        return event;
      }

      return null;
    }
    return event;
  }
}
