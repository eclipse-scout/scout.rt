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
package org.eclipse.scout.rt.ui.json;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.TableEvent;

public class TableEventFilter extends AbstractEventFilter<TableEvent> {

  private ITable m_source;

  public TableEventFilter(ITable source) {
    m_source = source;
  }

  @Override
  public TableEvent filterIgnorableModelEvent(TableEvent event) {
    for (TableEvent eventToIgnore : getIgnorableModelEvents()) {
      if (eventToIgnore.getType() == event.getType()) {
        List<ITableRow> rows = new ArrayList<>(event.getRows());
        rows.removeAll(eventToIgnore.getRows());
        if (rows.size() == 0) {
          //Event should be ignored if no nodes remain or if the event contained no nodes at all
          return null;
        }

        TableEvent newEvent = new TableEvent(m_source, event.getType(), rows);
        return newEvent;
      }
    }
    return event;
  }
}
