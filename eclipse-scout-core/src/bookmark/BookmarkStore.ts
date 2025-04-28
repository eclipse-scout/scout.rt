/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {BookmarkDo} from '@eclipse-scout/core';

export abstract class BookmarkStore {

  abstract storeBookmark(bookmark: BookmarkDo): JQuery.Promise<void>;

  abstract loadBookmark(bookmarkId: string): JQuery.Promise<BookmarkDo>;

  abstract loadAllBookmarks(): JQuery.Promise<BookmarkDo[]>;

  abstract storeAllBookmarks(allBookmarks: BookmarkDo[]): JQuery.Promise<void>;
}
