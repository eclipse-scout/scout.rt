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
package org.eclipse.scout.rt.shared.services.common.bookmark;

import java.util.List;

/**
 * Visitor to visit all bookmarks recursively inside a folder.
 */
public interface IBookmarkVisitor {
  /**
   * @return true to continue visiting, false to cancel
   */
  boolean visitFolder(List<BookmarkFolder> path);

  /**
   * @return true to continue visiting, false to cancel
   */
  boolean visitBookmark(List<BookmarkFolder> path, Bookmark b);
}
