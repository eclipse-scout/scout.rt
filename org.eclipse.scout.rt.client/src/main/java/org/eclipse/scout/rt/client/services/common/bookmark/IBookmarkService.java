/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.services.common.bookmark;

import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.platform.service.IService;
import org.eclipse.scout.rt.platform.util.event.IFastListenerList;
import org.eclipse.scout.rt.shared.services.common.bookmark.Bookmark;
import org.eclipse.scout.rt.shared.services.common.bookmark.BookmarkData;

public interface IBookmarkService extends IService {

  IFastListenerList<BookmarkServiceListener> bookmarkServiceListeners();

  default void addBookmarkServiceListener(BookmarkServiceListener listener) {
    bookmarkServiceListeners().add(listener);
  }

  default void removeBookmarkServiceListener(BookmarkServiceListener listener) {
    bookmarkServiceListeners().remove(listener);
  }

  /**
   * @return life reference to current model, this reference is always valid and will never change
   */
  BookmarkData getBookmarkData();

  /**
   * (re)load bookmarks from back-end
   */
  void loadBookmarks();

  /**
   * store the BookmarkModel
   */
  void storeBookmarks();

  /**
   * @return life array of bookmarks
   */
  Bookmark getStartBookmark();

  /**
   * don't saves automatically
   */
  void setStartBookmark();

  /**
   * don't saves automatically
   */
  void deleteStartBookmark();

  /**
   * Convenience method that simply calls {@link IDesktop#activateBookmark(Bookmark, false)}
   */
  void activate(Bookmark bm);

  /**
   * Finds the given bookmark among the user's bookmark and updates it with the currently visible view (analog to
   * setting start-up view).
   */
  void updateBookmark(Bookmark bm);
}
