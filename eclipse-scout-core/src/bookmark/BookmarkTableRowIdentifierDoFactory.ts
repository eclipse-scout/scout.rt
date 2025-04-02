/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  BookmarkTableRowIdentifierBooleanComponentDo, BookmarkTableRowIdentifierDateComponentDo, BookmarkTableRowIdentifierDo, BookmarkTableRowIdentifierLongComponentDo, BookmarkTableRowIdentifierObjectComponentDo,
  BookmarkTableRowIdentifierStringComponentDo, IBookmarkTableRowIdentifierComponentDo, objects, PageWithTable, scout, TableRow
} from '../index';

/**
 * Basic factory to create data objects that uniquely identify a row in a table page via the primary keys.
 *
 * Since data types are much more rudimentary in JavaScript, this factory only creates a subset of all possible row
 * identifier components. For example, all numeric values are returned as {@link BookmarkTableRowIdentifierLongComponentDo}
 * because there is only one number data type and this factory cannot decide whether a value should be a `Long` or an
 * `Integer`. Likewise, IDs are always returned as {@link BookmarkTableRowIdentifierStringComponentDo}, because they
 * are usually represented by strings without any additional metadata. If such identifiers are persisted, they cannot
 * be found again automatically (e.g. for automatic value migration).
 */
export class BookmarkTableRowIdentifierDoFactory {

  createTableRowIdentifier(tablePage: PageWithTable, row: TableRow, allowObjectFallback = false): BookmarkTableRowIdentifierDo {
    let keys = row.getKeyValues();
    let keyComponents = keys.map(key => this.createTableRowIdentifierComponent(tablePage, key, allowObjectFallback));
    return scout.create(BookmarkTableRowIdentifierDo, {keyComponents});
  }

  createTableRowIdentifierComponent(tablePage: PageWithTable, key: any, allowObjectFallback = false): IBookmarkTableRowIdentifierComponentDo {
    if (objects.isNullOrUndefined(key)) {
      return null;
    }

    if (key instanceof Date) {
      return scout.create(BookmarkTableRowIdentifierDateComponentDo, {key});
    }
    if (typeof key === 'number') {
      return scout.create(BookmarkTableRowIdentifierLongComponentDo, {key});
    }
    if (typeof key === 'string') {
      return scout.create(BookmarkTableRowIdentifierStringComponentDo, {key});
    }
    if (typeof key === 'boolean') {
      return scout.create(BookmarkTableRowIdentifierBooleanComponentDo, {key});
    }

    if (allowObjectFallback) {
      return scout.create(BookmarkTableRowIdentifierObjectComponentDo, {key});
    }
    throw new Error(`Unable to create table row identifier for key value "${key}"`);
  }
}
