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

import org.eclipse.scout.rt.shared.TEXTS;

public class BookmarkData implements Serializable {
  private static final long serialVersionUID = 1L;

  private BookmarkFolder m_globalBookmarks;
  private BookmarkFolder m_userBookmarks;

  public BookmarkData() {
    m_globalBookmarks = new BookmarkFolder();
    m_globalBookmarks.setTitle(TEXTS.get("GlobalBookmarks"));
    m_userBookmarks = new BookmarkFolder();
    m_userBookmarks.setTitle(TEXTS.get("Bookmarks"));
  }

  protected BookmarkData(BookmarkData other) {
    m_globalBookmarks = other.m_globalBookmarks.copy();
    m_userBookmarks = other.m_userBookmarks.copy();
  }

  /**
   * @return the life folder, changes are immediately reflected on the member list
   */
  public BookmarkFolder getGlobalBookmarks() {
    return m_globalBookmarks;
  }

  public void setGlobalBookmarks(BookmarkFolder globalBookmarks) {
    m_globalBookmarks = globalBookmarks;
    if (m_globalBookmarks.getTitle() == null) {
      m_globalBookmarks.setTitle(TEXTS.get("GlobalBookmarks"));
    }
  }

  /**
   * @return the life folder, changes are immediately reflected on the member list
   */
  public BookmarkFolder getUserBookmarks() {
    return m_userBookmarks;
  }

  public void setUserBookmarks(BookmarkFolder userBookmarks) {
    m_userBookmarks = userBookmarks;
    if (m_userBookmarks.getTitle() == null) {
      m_userBookmarks.setTitle(TEXTS.get("Bookmarks"));
    }
  }

  /**
   * Creates a copy of this instance. The copy is basically a deep copy, but resource intensive references like byte
   * arrays containing serialized data as well as immutable objects are shallow copied.
   */
  public BookmarkData copy() {
    return new BookmarkData(this);
  }
}
