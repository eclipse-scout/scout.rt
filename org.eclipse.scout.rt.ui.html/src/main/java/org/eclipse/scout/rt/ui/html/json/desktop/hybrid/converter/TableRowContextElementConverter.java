/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.desktop.hybrid.converter;

import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.ui.html.json.table.JsonTable;

public class TableRowContextElementConverter extends AbstractHybridActionContextElementConverter<JsonTable<?>, String, ITableRow> {

  protected static final String JSON_ELEMENT_PREFIX = "row:";

  @Override
  protected boolean acceptJsonElementImpl(String jsonElement) {
    return StringUtility.startsWith(jsonElement, JSON_ELEMENT_PREFIX);
  }

  @Override
  protected ITableRow jsonToElement(JsonTable<?> adapter, String jsonElement) {
    String rowId = StringUtility.removePrefix(jsonElement, JSON_ELEMENT_PREFIX);
    return adapter.getTableRow(rowId);
  }

  @Override
  protected String elementToJson(JsonTable<?> adapter, ITableRow element) {
    adapter.processBufferedEvents();
    String rowId = adapter.getTableRowId(element);
    return StringUtility.addPrefix(rowId, JSON_ELEMENT_PREFIX);
  }
}
