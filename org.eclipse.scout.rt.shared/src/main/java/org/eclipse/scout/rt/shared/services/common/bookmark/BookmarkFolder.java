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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.platform.util.CompareUtility;

public class BookmarkFolder implements Serializable, Cloneable {
  private static final long serialVersionUID = 1L;

  private long m_id;
  private String m_title;
  private String m_iconId;
  private List<BookmarkFolder> m_folders;
  private List<Bookmark> m_bookmarks;
  private Bookmark m_startupBookmark;

  public BookmarkFolder() {
    m_folders = new ArrayList<BookmarkFolder>();
    m_bookmarks = new ArrayList<Bookmark>();
  }

  public long getId() {
    return m_id;
  }

  public void setId(long id) {
    m_id = id;
  }

  public String getIconId() {
    return m_iconId;
  }

  public void setIconId(String iconid) {
    m_iconId = iconid;
  }

  public String getTitle() {
    return m_title;
  }

  public void setTitle(String s) {
    m_title = s;
  }

  public Bookmark getStartupBookmark() {
    return m_startupBookmark;
  }

  public void setStartupBookmark(Bookmark startupBookmark) {
    m_startupBookmark = startupBookmark;
  }

  /**
   * Convenience method to add bookmarks from a folder (folder tree)
   * <p>
   * Folders are searched by folder name.
   * <p>
   * Missing folders are created.
   * <p>
   * The bookmarks are added by reference (byReference=true) or use {@link Bookmark#clone()} to create a deep copy
   * (byReference=false).
   */
  public void addBookmarks(BookmarkFolder folder, boolean replaceDuplicates, boolean byReference) {
    if (folder == null) {
      return;
    }
    else {
      for (Bookmark b : folder.getBookmarks()) {
        if (!byReference) {
          b = (Bookmark) b.clone();
        }
        if (replaceDuplicates) {
          Bookmark existingBm = getBookmark(b.getTitle());
          if (existingBm != null) {
            getBookmarks().remove(existingBm);
          }
        }
        getBookmarks().add(b);
      }
      for (BookmarkFolder subFolder : folder.getFolders()) {
        BookmarkFolder existingFolder = getFolder(subFolder.getTitle());
        if (existingFolder == null) {
          if (byReference) {
            existingFolder = subFolder;
            getFolders().add(existingFolder);
          }
          else {
            existingFolder = (BookmarkFolder) subFolder.clone();
            getFolders().add(existingFolder);
          }
        }
        else {
          //recursively add
          existingFolder.addBookmarks(subFolder, replaceDuplicates, byReference);
        }
      }
    }
  }

  /**
   * @return the life list of all folders, changes are immediately reflected on the member list
   */
  public List<BookmarkFolder> getFolders() {
    return m_folders;
  }

  /**
   * @return the life list of all bookmarks, changes are immediately reflected on the member list
   */
  public List<Bookmark> getBookmarks() {
    return m_bookmarks;
  }

  /**
   * @return the first bookmark with the specific title
   */
  public Bookmark getBookmark(String title) {
    for (Bookmark b : getBookmarks()) {
      if (CompareUtility.equals(b.getTitle(), title)) {
        return b;
      }
    }
    return null;
  }

  /**
   * @return the first folder with the specific title
   */
  public BookmarkFolder getFolder(String title) {
    for (BookmarkFolder f : getFolders()) {
      if (CompareUtility.equals(f.getTitle(), title)) {
        return f;
      }
    }
    return null;
  }

  @Override
  public Object clone() {
    try {
      BookmarkFolder f = (BookmarkFolder) super.clone();
      //make deep copy of lists
      f.m_folders = new ArrayList<BookmarkFolder>();
      for (BookmarkFolder x : this.m_folders) {
        f.m_folders.add((BookmarkFolder) x.clone());
      }
      f.m_bookmarks = new ArrayList<Bookmark>();
      for (Bookmark x : this.m_bookmarks) {
        f.m_bookmarks.add((Bookmark) x.clone());
      }
      return f;
    }
    catch (CloneNotSupportedException e) {
      throw new UnsupportedOperationException(e);
    }
  }

  /**
   * visit all bookmarks in this folder and all its sub-folders
   */
  public boolean visit(IBookmarkVisitor v) {
    ArrayList<BookmarkFolder> path = new ArrayList<BookmarkFolder>();
    return visitInternal(v, path);
  }

  boolean visitInternal(IBookmarkVisitor v, List<BookmarkFolder> path) {
    boolean result = true;
    try {
      path.add(this);
      //
      result = v.visitFolder(path);
      if (!result) {
        return result;
      }
      for (Bookmark b : new ArrayList<Bookmark>(getBookmarks())) {
        result = v.visitBookmark(path, b);
        if (!result) {
          return result;
        }
      }
      for (BookmarkFolder f : new ArrayList<BookmarkFolder>(getFolders())) {
        result = f.visitInternal(v, path);
        if (!result) {
          return result;
        }
      }
      return result;
    }
    finally {
      path.remove(this);
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((m_iconId == null) ? 0 : m_iconId.hashCode());
    result = prime * result + (int) (m_id ^ (m_id >>> 32));
    result = prime * result + ((m_title == null) ? 0 : m_title.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    BookmarkFolder other = (BookmarkFolder) obj;
    if (m_iconId == null) {
      if (other.m_iconId != null) {
        return false;
      }
    }
    else if (!m_iconId.equals(other.m_iconId)) {
      return false;
    }
    if (m_id != other.m_id) {
      return false;
    }
    if (m_title == null) {
      if (other.m_title != null) {
        return false;
      }
    }
    else if (!m_title.equals(other.m_title)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + " title=" + getTitle() + ", id=" + getId() + "]";
  }
}
