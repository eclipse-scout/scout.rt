/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.services.common.bookmark;

import org.eclipse.scout.rt.platform.service.IService;
import org.eclipse.scout.rt.shared.services.common.bookmark.Bookmark;
import org.eclipse.scout.rt.shared.services.common.bookmark.BookmarkData;

public interface IBookmarkService extends IService {

  void addBookmarkServiceListener(BookmarkServiceListener listener);

  void removeBookmarkServiceListener(BookmarkServiceListener listener);

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
