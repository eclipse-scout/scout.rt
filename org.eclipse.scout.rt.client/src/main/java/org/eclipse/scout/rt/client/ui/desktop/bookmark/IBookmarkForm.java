/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.desktop.bookmark;

import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.shared.services.common.bookmark.Bookmark;
import org.eclipse.scout.rt.shared.services.common.bookmark.BookmarkFolder;

public interface IBookmarkForm extends IForm {

  /**
   * @param bookmark
   *     the bookmark to be modified
   */
  void setBookmark(Bookmark bookmark);

  /**
   * @return the bookmark for further processing
   */
  Bookmark getBookmark();

  /**
   * Set the top-level folder where the bookmark is located
   *
   * @param rootFolder
   *     top-level folder
   */
  void setBookmarkRootFolder(BookmarkFolder rootFolder);

  /**
   * @return the top-level folder where the bookmark is located
   */
  BookmarkFolder getBookmarkRootFolder();

  /**
   * @return the folder where the bookmark is located
   */
  BookmarkFolder getFolder();

  /**
   * @param folder
   *     the folder where the bookmark is located
   */
  void setFolder(BookmarkFolder folder);

  /**
   * Opens the form for modification of an existing bookmark
   */
  void startModify();

  /**
   * Opens the form for creation of a new bookmark
   */
  void startNew();
}
