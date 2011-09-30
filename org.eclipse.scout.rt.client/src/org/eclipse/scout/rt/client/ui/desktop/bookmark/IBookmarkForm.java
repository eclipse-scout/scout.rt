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
package org.eclipse.scout.rt.client.ui.desktop.bookmark;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.shared.services.common.bookmark.Bookmark;
import org.eclipse.scout.rt.shared.services.common.bookmark.BookmarkFolder;

public interface IBookmarkForm extends IForm {

  void setBookmark(Bookmark bookmark);

  Bookmark getBookmark();

  void setBookmarkRootFolder(BookmarkFolder rootFolder);

  BookmarkFolder getBookmarkRootFolder();

  BookmarkFolder getFolder() throws ProcessingException;

  void setFolder(BookmarkFolder folder) throws ProcessingException;

  void startModify() throws ProcessingException;

  void startNew() throws ProcessingException;
}
