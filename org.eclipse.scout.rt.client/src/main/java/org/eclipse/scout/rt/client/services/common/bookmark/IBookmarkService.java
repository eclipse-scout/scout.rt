/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.services.common.bookmark;

import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.shared.services.common.bookmark.Bookmark;
import org.eclipse.scout.rt.shared.services.common.bookmark.BookmarkData;
import org.eclipse.scout.service.IService;

@Priority(-3)
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
  void loadBookmarks() throws ProcessingException;

  /**
   * store the BookmarkModel
   */
  void storeBookmarks() throws ProcessingException;

  /**
   * @return life array of bookmarks
   */
  Bookmark getStartBookmark();

  /**
   * don't saves automatically
   */
  void setStartBookmark() throws ProcessingException;

  /**
   * don't saves automatically
   */
  void deleteStartBookmark() throws ProcessingException;

  /**
   * Convenience method that simply calls {@link IDesktop#activateBookmark(Bookmark, false)}
   */
  void activate(Bookmark bm) throws ProcessingException;

  /**
   * Finds the given bookmark among the user's bookmark and updates it
   * with the currently visible view (analog to setting start-up view).
   */
  void updateBookmark(Bookmark bm) throws ProcessingException;

}
