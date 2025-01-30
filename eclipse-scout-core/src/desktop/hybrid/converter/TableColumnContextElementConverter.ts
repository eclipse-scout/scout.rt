/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Column, HybridActionContextElementConverter, HybridActionContextElementConverters, ModelAdapter, objects, scout, strings, TableAdapter} from '../../../index';

export class TableColumnContextElementConverter extends HybridActionContextElementConverter<TableAdapter, string, Column> {

  static JSON_ELEMENT_PREFIX = 'col:';

  override _acceptAdapter(adapter: ModelAdapter): adapter is TableAdapter {
    return adapter instanceof TableAdapter;
  }

  override _acceptJsonElement(jsonElement: any): jsonElement is string {
    return objects.isString(jsonElement) && strings.startsWith(jsonElement, TableColumnContextElementConverter.JSON_ELEMENT_PREFIX);
  }

  override _acceptModelElement(element: any): element is Column {
    return element instanceof Column;
  }

  override _jsonToElement(adapter: TableAdapter, jsonElement: string): Column {
    let table = adapter.widget;
    let columnId = strings.removePrefix(jsonElement, TableColumnContextElementConverter.JSON_ELEMENT_PREFIX);
    return scout.assertValue(table.columnById(columnId), `Unknown column with id "${columnId}" in table ${adapter.id}`);
  }

  override _elementToJson(adapter: TableAdapter, element: Column): string {
    let columnId = element.id;
    return strings.addPrefix(columnId, TableColumnContextElementConverter.JSON_ELEMENT_PREFIX);
  }
}

HybridActionContextElementConverters.get().register(TableColumnContextElementConverter);
