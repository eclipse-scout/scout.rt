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

export class BookmarkTableRowIdentifierDoFactory {

  createTableRowIdentifier(tablePage: PageWithTable, row: TableRow, allowObjectFallback = false): BookmarkTableRowIdentifierDo {
    let keys = row.getKeyValues();
    let keyComponents = keys.map(key => tablePage.createTableRowIdentifierComponent(key, allowObjectFallback));
    return scout.create(BookmarkTableRowIdentifierDo, {keyComponents});
  }

  createTableRowIdentifierComponent(tablePage: PageWithTable, key: any, allowObjectFallback = false): IBookmarkTableRowIdentifierComponentDo {
    if (objects.isNullOrUndefined(key)) {
      return null;
    }

    // FIXME bsh [js-bookmark] How to check for IEntityKey?
    // FIXME bsh [js-bookmark] How to check for TypedId?
    // FIXME bsh [js-bookmark] How to check for IId?
    if (key instanceof Date) {
      return scout.create(BookmarkTableRowIdentifierDateComponentDo, {key});
    }
    // FIXME bsh [js-bookmark] Integer vs. Long?
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
