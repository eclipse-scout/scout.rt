/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {App, BookmarkDo, objects, scout, Session} from '../index';

/**
 * Global storage service for {@link BookmarkDo bookmarks}.
 *
 * An implementation can be provided by registering an object factory for this type.
 */
export class BookmarkStore {

  protected static _INSTANCES: Map<Session, BookmarkStore> = new Map();

  /**
   * Returns an instance of {@link BookmarkStore} for the given {@link Session}. If no instance is associated
   * with the session yet, a new instance is created.
   *
   * @param session Optional session object. If this is omitted, the first session of the app is used.
   * If the app does not have any active sessions (e.g. during unit testing), this argument is mandatory.
   */
  static get(session?: Session): BookmarkStore {
    session = session || App.get().sessions[0];
    scout.assertParameter('session', session);
    return objects.getOrSetIfAbsent(BookmarkStore._INSTANCES, session, session => scout.create(BookmarkStore));
  }

  // --------------------------------------

  /**
   * Saves a bookmark to this bookmark store. The previous version (if any) is overwritten. The stored
   * bookmark is returned afterward (may have been altered while being persisted, e.g. assigned an ID).
   */
  storeBookmark(bookmark: BookmarkDo): JQuery.Promise<BookmarkDo> {
    throw new Error('Not implemented');
  }

  /**
   * Returns the bookmark for the given ID, or `null` if the bookmark could not be found.
   */
  loadBookmark(bookmarkId: string): JQuery.Promise<BookmarkDo> {
    throw new Error('Not implemented');
  }

  /**
   * Returns a sorted list of all bookmarks in this store.
   */
  loadAllBookmarks(): JQuery.Promise<BookmarkDo[]> {
    throw new Error('Not implemented');
  }

  /**
   * Replaces all bookmarks in this store with the given list.
   */
  storeAllBookmarks(allBookmarks: BookmarkDo[]): JQuery.Promise<void> {
    throw new Error('Not implemented');
  }
}
