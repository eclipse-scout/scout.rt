/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

/**
 * Adapter which provides properties from an owner object to be used in bookmarks.
 */
export interface BookmarkAdapter {
  /**
   * @returns the ID of the owner to be used in bookmarks.
   */
  buildId(): string;
}

/**
 * Objects having a {@link BookmarkAdapter}. Typically, such objects are bookmarkable.
 */
export interface ObjectWithBookmarkAdapter {
  /**
   * @returns the {@link BookmarkAdapter} for this object. Never returns null.
   */
  getBookmarkAdapter(): BookmarkAdapter;
}
