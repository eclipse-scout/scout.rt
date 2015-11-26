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

import org.eclipse.scout.rt.client.ui.dnd.JavaTransferObject;
import org.eclipse.scout.rt.shared.services.common.bookmark.Bookmark;
import org.eclipse.scout.rt.shared.services.common.bookmark.BookmarkFolder;

/**
 * A bookmark node in {@link AbstractBookmarkTreeField}. Could be used to handle a drop request and read the node data
 * from a {@link JavaTransferObject}
 */
public interface IBookmarkNode {

  /**
   * @return the bookmark for further processing
   */
  Bookmark getBookmark();

  /**
   * @return the folder where the bookmark was located for further processing
   */
  BookmarkFolder getParentFolder();

}
