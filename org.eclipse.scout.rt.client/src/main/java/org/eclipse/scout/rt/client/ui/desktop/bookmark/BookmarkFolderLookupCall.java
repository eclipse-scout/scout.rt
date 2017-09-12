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
package org.eclipse.scout.rt.client.ui.desktop.bookmark;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.shared.services.common.bookmark.Bookmark;
import org.eclipse.scout.rt.shared.services.common.bookmark.BookmarkFolder;
import org.eclipse.scout.rt.shared.services.common.bookmark.IBookmarkVisitor;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.LocalLookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;

/**
 * hierarchy lookup call for bookmark folder tree
 */
@ClassId("a6df6454-6752-43ea-8624-26c691a1145b")
public class BookmarkFolderLookupCall extends LocalLookupCall<BookmarkFolder> {
  private static final long serialVersionUID = 1L;

  private BookmarkFolder m_rootFolder;

  public BookmarkFolder getRootFolder() {
    return m_rootFolder;
  }

  public void setRootFolder(BookmarkFolder rootFolder) {
    m_rootFolder = rootFolder;
  }

  @Override
  protected List<ILookupRow<BookmarkFolder>> execCreateLookupRows() {
    final List<ILookupRow<BookmarkFolder>> rows = new ArrayList<>();
    if (m_rootFolder != null) {
      m_rootFolder.visit(new IBookmarkVisitor() {
        @Override
        public boolean visitFolder(List<BookmarkFolder> path) {
          if (path.size() >= 2) {
            BookmarkFolder f = path.get(path.size() - 1);
            if (!Bookmark.INBOX_FOLDER_NAME.equals(f.getTitle())) {
              BookmarkFolder parent = null;
              if (path.size() >= 3) {
                parent = path.get(path.size() - 2);
              }
              rows.add(new LookupRow<>(f, f.getTitle()).withParentKey(parent));
            }
          }
          return true;
        }

        @Override
        public boolean visitBookmark(List<BookmarkFolder> path, Bookmark b) {
          return true;
        }
      });
    }
    return rows;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((m_rootFolder == null) ? 0 : m_rootFolder.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    BookmarkFolderLookupCall other = (BookmarkFolderLookupCall) obj;
    if (m_rootFolder == null) {
      if (other.m_rootFolder != null) {
        return false;
      }
    }
    else if (!m_rootFolder.equals(other.m_rootFolder)) {
      return false;
    }
    return true;
  }
}
