/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.ui.html.json.table;

import java.util.Collection;
import java.util.EventObject;

import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;

@SuppressWarnings({"serial", "squid:S2057"})
public class JsonTableEvent extends EventObject {

  public static final int TYPE_ROWS_INSERTED = 100;
  public static final int TYPE_ROWS_DELETED = 200;

  private final int m_type;
  private Collection<? extends ITableRow> m_rows;

  public JsonTableEvent(JsonTable<?> source, int type, Collection<? extends ITableRow> rows) {
    super(source);
    m_type = type;
    m_rows = rows;
  }

  @Override
  public JsonTable<? extends ITable> getSource() {
    return (JsonTable<?>) super.getSource();
  }

  /**
   * @return a live collection of every inserted row.
   */
  public Collection<? extends ITableRow> getRows() {
    return m_rows;
  }

  public int getType() {
    return m_type;
  }
}
