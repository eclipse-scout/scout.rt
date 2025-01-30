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

import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.ui.html.json.table.JsonTable;

public class TableColumnContextElementConverter extends AbstractHybridActionContextElementConverter<JsonTable<?>, String, IColumn<?>> {

  protected static final String JSON_ELEMENT_PREFIX = "col:";

  @Override
  protected boolean acceptJsonElementImpl(String jsonElement) {
    return StringUtility.startsWith(jsonElement, JSON_ELEMENT_PREFIX);
  }

  @Override
  protected IColumn<?> jsonToElement(JsonTable<?> adapter, String jsonElement) {
    String columnId = StringUtility.removePrefix(jsonElement, JSON_ELEMENT_PREFIX);
    return adapter.getColumn(columnId);
  }

  @Override
  protected String elementToJson(JsonTable<?> adapter, IColumn<?> element) {
    adapter.processBufferedEvents();
    String columnId = adapter.getColumnId(element);
    return StringUtility.addPrefix(columnId, JSON_ELEMENT_PREFIX);
  }
}
