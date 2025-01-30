/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {HybridActionContextElementConverter, HybridActionContextElementConverters, ModelAdapter, objects, scout, strings, TableAdapter, TableRow} from '../../../index';

export class TableRowContextElementConverter extends HybridActionContextElementConverter<TableAdapter, string, TableRow> {

  static JSON_ELEMENT_PREFIX = 'row:';

  override _acceptAdapter(adapter: ModelAdapter): adapter is TableAdapter {
    return adapter instanceof TableAdapter;
  }

  override _acceptJsonElement(jsonElement: any): jsonElement is string {
    return objects.isString(jsonElement) && strings.startsWith(jsonElement, TableRowContextElementConverter.JSON_ELEMENT_PREFIX);
  }

  override _acceptModelElement(element: any): element is TableRow {
    return element instanceof TableRow;
  }

  override _jsonToElement(adapter: TableAdapter, jsonElement: string): TableRow {
    let table = adapter.widget;
    let rowId = strings.removePrefix(jsonElement, TableRowContextElementConverter.JSON_ELEMENT_PREFIX);
    return scout.assertValue(table.rowById(rowId), `Unknown row with id "${rowId}" in table ${adapter.id}`);
  }

  override _elementToJson(adapter: TableAdapter, element: TableRow): string {
    let rowId = element.id;
    return strings.addPrefix(rowId, TableRowContextElementConverter.JSON_ELEMENT_PREFIX);
  }
}

HybridActionContextElementConverters.get().register(TableRowContextElementConverter);
