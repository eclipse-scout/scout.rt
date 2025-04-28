/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, BookmarkDo, BookmarkStore, dataObjects, UuidPool, webstorage} from '@eclipse-scout/core';

/**
 * Simple bookmark store that uses the browser's local storage.
 */
export abstract class LocalBookmarkStore extends BookmarkStore {

  protected abstract get _storeId(): string;

  protected _readFromStore(): BookmarkDo[] {
    let raw = webstorage.getItemFromLocalStorage(this._storeId);
    return dataObjects.parse(raw, Array<BookmarkDo>);
  }

  protected _writeToStore(allBookmarks: BookmarkDo[]) {
    if (!allBookmarks) {
      webstorage.removeItemFromLocalStorage(this._storeId);
      return;
    }
    webstorage.setItemToLocalStorage(this._storeId, dataObjects.stringify(allBookmarks));
  }

  override storeBookmark(bookmark: BookmarkDo): JQuery.Promise<void> {
    return $.resolvedPromise().then(() => {
      if (!bookmark) {
        return;
      }

      let allBookmarks = this._readFromStore() || [];
      bookmark.id = bookmark.id || UuidPool.get().take();
      let index = allBookmarks.findIndex(b => b.id === bookmark.id);
      if (index === -1) {
        allBookmarks.push(bookmark);
      } else {
        allBookmarks[index] = bookmark;
      }
      this._writeToStore(allBookmarks);
    });
  }

  override loadBookmark(bookmarkId: string): JQuery.Promise<BookmarkDo> {
    return $.resolvedPromise().then(() => {
      let allBookmarks = this._readFromStore() || [];
      return allBookmarks.find(b => b.id === bookmarkId) || null;
    });
  }

  override loadAllBookmarks(): JQuery.Promise<BookmarkDo[]> {
    return $.resolvedPromise().then(() => {
      return this._readFromStore() || [];
    });
  }

  override storeAllBookmarks(allBookmarks: BookmarkDo[]): JQuery.Promise<void> {
    return $.resolvedPromise().then(() => {
      allBookmarks = arrays.ensure(allBookmarks).filter(bookmark => {
        if (!bookmark) {
          return false;
        }
        bookmark.id = bookmark.id || UuidPool.get().take();
        return true;
      });
      this._writeToStore(allBookmarks);
    });
  }
}
