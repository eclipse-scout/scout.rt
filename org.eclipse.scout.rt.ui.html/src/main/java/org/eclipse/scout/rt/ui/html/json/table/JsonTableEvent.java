/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
