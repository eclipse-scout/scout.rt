/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, BookmarkDo, BookmarkSupport, dataObjects, IBookmarkDo, UuidPool, webstorage} from '../index';

export const bookmarks = {

  // FIXME bsh [js-bookmark] Remove the following debugging/testing methods

  getBookmarkStore(storeId?: string): BookmarkDo[] {
    storeId = storeId || 'scout:bookmarks';
    let raw = webstorage.getItemFromLocalStorage(storeId);
    return dataObjects.parse(raw, Array<BookmarkDo>);
  },

  setBookmarkStore(bookmarkStore: BookmarkDo[], storeId?: string) {
    storeId = storeId || 'scout:bookmarks';
    if (!bookmarkStore) {
      webstorage.removeItemFromLocalStorage(storeId);
      return;
    }
    webstorage.setItemToLocalStorage(storeId, dataObjects.stringify(bookmarkStore));
  },

  storeBookmark(bookmark: BookmarkDo, storeId?: string): JQuery.Promise<void> {
    return $.resolvedPromise().then(() => {
      if (!bookmark) {
        return;
      }

      let bookmarkStore = bookmarks.getBookmarkStore(storeId) || [];
      bookmark.id = bookmark.id || UuidPool.get().take();
      let index = bookmarkStore.findIndex(b => b.id === bookmark.id);
      if (index === -1) {
        bookmarkStore.push(bookmark);
      } else {
        bookmarkStore[index] = bookmark;
      }
      bookmarks.setBookmarkStore(bookmarkStore, storeId);
    });
  },

  // FIXME bsh [js-bookmark] Remove and replace with actual implementation
  loadBookmark(id: string, storeId?: string): JQuery.Promise<BookmarkDo> {
    return $.resolvedPromise().then(() => {
      let bookmarkStore = bookmarks.getBookmarkStore(storeId) || [];
      return bookmarkStore.find(b => b.id === id) || null;
    });
  },

  loadAllBookmarks(storeId?: string): JQuery.Promise<BookmarkDo[]> {
    return $.resolvedPromise().then(() => {
      return bookmarks.getBookmarkStore(storeId) || [];
    });
  },

  storeAllBookmarks(allBookmarks: BookmarkDo[], storeId?: string): JQuery.Promise<void> {
    return $.resolvedPromise().then(() => {
      allBookmarks = arrays.ensure(allBookmarks).filter(bookmark => {
        if (!bookmark) {
          return false;
        }
        bookmark.id = bookmark.id || UuidPool.get().take();
        return true;
      });
      bookmarks.setBookmarkStore(allBookmarks, storeId);
    });
  },

  // --------------------------------------

  /** @deprecated ONLY FOR TESTING PURPOSES - DO NOT USE */
  async create(storeId?: string): Promise<BookmarkDo> {
    let bookmark = await BookmarkSupport.get().createBookmark() as BookmarkDo;
    await bookmarks.storeBookmark(bookmark, storeId);
    return bookmark;
  },

  /** @deprecated ONLY FOR TESTING PURPOSES - DO NOT USE */
  async activate(index?: number, storeId?: string): Promise<BookmarkDo> {
    const bookmarkStore = bookmarks.getBookmarkStore(storeId);
    if (arrays.empty(bookmarkStore)) {
      return null;
    }
    let bookmark = bookmarkStore[index < 0 ? bookmarkStore.length + index : index] || arrays.last(bookmarkStore);
    await BookmarkSupport.get().activateBookmark(bookmark);
    return bookmark;
  },

  /** @deprecated ONLY FOR TESTING PURPOSES - DO NOT USE */
  async all(storeId?: string): Promise<BookmarkDo[]> {
    return bookmarks.loadAllBookmarks(storeId);
  },

  /** @deprecated ONLY FOR TESTING PURPOSES - DO NOT USE */
  delete(index?: number, storeId?: string): IBookmarkDo {
    const bookmarkStore = bookmarks.getBookmarkStore(storeId);
    if (arrays.empty(bookmarkStore)) {
      return null;
    }
    let bookmark = bookmarkStore[index < 0 ? bookmarkStore.length + index : index] || arrays.last(bookmarkStore);
    if (bookmark) {
      arrays.remove(bookmarkStore, bookmark);
      bookmarks.setBookmarkStore(bookmarkStore);
    }
    return bookmark;
  }
} as const;
