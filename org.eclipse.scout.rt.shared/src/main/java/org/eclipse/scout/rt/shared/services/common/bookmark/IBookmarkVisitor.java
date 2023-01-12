/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
