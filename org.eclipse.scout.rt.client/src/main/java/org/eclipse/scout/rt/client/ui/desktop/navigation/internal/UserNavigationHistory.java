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
package org.eclipse.scout.rt.client.ui.desktop.navigation.internal;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.navigation.NavigationHistoryEvent;
import org.eclipse.scout.rt.client.ui.desktop.navigation.NavigationHistoryListener;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithNodes;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.platform.util.CompareUtility;
import org.eclipse.scout.rt.platform.util.EventListenerList;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.shared.services.common.bookmark.AbstractPageState;
import org.eclipse.scout.rt.shared.services.common.bookmark.Bookmark;
import org.eclipse.scout.rt.shared.services.common.bookmark.TablePageState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A limited navigation history for storing the navigation history and navigating in that history.
 *
 * @see org.eclipse.scout.rt.client.ui.desktop.navigation.internal.NavigationHistoryService NavigationHistoryService
 */
public class UserNavigationHistory {
  private static final int MAX_HISTORY_SIZE = 25;
  private static final Logger LOG = LoggerFactory.getLogger(UserNavigationHistory.class);

  private final EventListenerList m_listenerList;
  private final LinkedList<Bookmark> m_bookmarks;
  private int m_index;// 0...MAX_HISTORY_SIZE-1
  private boolean m_addStepEnabled;

  public UserNavigationHistory() {
    m_addStepEnabled = true;
    m_listenerList = new EventListenerList();
    m_bookmarks = new LinkedList<Bookmark>();
  }

  public Bookmark addStep(int level, IPage<?> page) {
    if (!m_addStepEnabled) {
      return null;
    }

    Bookmark bm = null;
    try {
      bm = ClientSessionProvider.currentSession().getDesktop().createBookmark(page);
      if (bm == null) {
        return null;
      }
      decorateBookmark(bm, level, page.getCell().getText(), page.getCell().getIconId());

      return addStep(bm);
    }
    catch (Exception e) {
      return handleAddStepError(e, bm);
    }
  }

  public Bookmark addStep(int level, String name, String iconId) {
    if (!m_addStepEnabled) {
      return null;
    }

    Bookmark bm = null;
    try {
      bm = ClientSessionProvider.currentSession().getDesktop().createBookmark();
      if (bm == null) {
        return null;
      }
      decorateBookmark(bm, level, name, iconId);

      return addStep(bm);
    }
    catch (Exception e) {
      return handleAddStepError(e, bm);
    }
  }

  protected void decorateBookmark(Bookmark bm, int level, String name, String iconId) {
    bm.setTitle(StringUtility.rpad("", " ", level * 2) + name);
    bm.setIconId(iconId);
  }

  /**
   * Logs a warning and returns <code>null</code> for the bookmark that should be used in case of the error.
   *
   * @param t
   *          error to be handled
   * @param bm
   *          corresponding bookmark
   * @return <code>null</code>
   */
  protected Bookmark handleAddStepError(Throwable t, Bookmark bm) {
    String bookmarkTitle = "";
    if (bm != null) {
      bookmarkTitle = bm.getText();
    }
    LOG.warn("Exception occured while adding step to navigation history for bookmark: " + bookmarkTitle, t);
    return null;
  }

  protected void activateBookmark(Bookmark b) {
    IDesktop desktop = ClientSessionProvider.currentSession().getDesktop();
    desktop.activateBookmark(b);
    //scroll to tree selection
    IOutline outline = desktop.getOutline();
    if (outline != null) {
      outline.scrollToSelection();
      //scroll to table selection
      IPage<?> page = outline.getActivePage();
      if (page instanceof IPageWithTable<?>) {
        ITable table = ((IPageWithTable<?>) page).getTable();
        if (table != null) {
          table.scrollToSelection();
        }
      }
      else if (page instanceof IPageWithNodes) {
        ITable table = ((IPageWithNodes) page).getTable();
        if (table != null) {
          table.scrollToSelection();
        }
      }
    }
  }

  /**
   * Adds a bookmark to the history as newest element, if it is not null and not the same as the last element.
   * <p>
   * Removes all elements newer than the currently active element, if the next element in the history is not the same as
   * the element to add.
   * <p>
   * </p>
   * Truncates the history to the maximum number of elements.
   * </p>
   *
   * @param bm
   *          bookmark to add
   * @return added bookmark
   */
  public Bookmark addStep(Bookmark bm) {
    if (!m_addStepEnabled) {
      return null;
    }
    if (bm == null) {
      return null;
    }

    // if last position was same as new one, skip it
    if (m_index < m_bookmarks.size()) {
      Bookmark last = m_bookmarks.get(m_index);
      if (isSameBookmark(last, bm)) {
        // replace
        m_bookmarks.set(m_index, bm);
        fireNavigationChanged();
        return bm;
      }
    }
    int nextPos = m_index + 1;
    // check if existing position is already same as new one (keep later
    // objects), otherwise delete later history
    if (nextPos < m_bookmarks.size() && bm.equals(m_bookmarks.get(nextPos))) {
      m_bookmarks.set(nextPos, bm);
      m_index = nextPos;
    }
    else {
      //remove elements after current index
      while (nextPos < m_bookmarks.size()) {
        Bookmark removedBookmark = m_bookmarks.removeLast();
        fireBookmarkRemoved(removedBookmark);
      }
      m_bookmarks.add(bm);
      m_index = m_bookmarks.size() - 1;
      fireBookmarkAdded(bm);
    }
    truncateHistory();
    fireNavigationChanged();
    return bm;
  }

  /**
   * Truncates history, if larger than maximum size
   */
  private void truncateHistory() {
    while (m_bookmarks.size() > MAX_HISTORY_SIZE) {
      Bookmark removedBookmark = m_bookmarks.removeFirst();
      m_index = Math.max(0, m_index - 1);
      fireBookmarkRemoved(removedBookmark);
    }
  }

  private void saveCurrentStep() {
    try {
      // if last position was same as new one, overwrite it
      if (m_index == m_bookmarks.size() - 1) {
        Bookmark last = m_bookmarks.get(m_index);
        Bookmark bm = ClientSessionProvider.currentSession().getDesktop().createBookmark();
        if (bm != null) {
          bm.setTitle(last.getTitle());
          bm.setIconId(last.getIconId());
          // replace
          m_bookmarks.set(m_index, bm);
        }
      }
    }
    catch (Exception e) {
      // nop
    }
  }

  /**
   * @param oldbm
   *          the old bookmark to compare
   * @param newbm
   *          the new bookmark to compare
   * @return true, if the bookmarks oldbm and newbm have the same title and the same parent search form states and
   *         parent labels.
   */
  private boolean isSameBookmark(Bookmark oldbm, Bookmark newbm) {
    List<AbstractPageState> oldPath = oldbm.getPath();
    List<AbstractPageState> newPath = newbm.getPath();
    if (CompareUtility.equals(oldbm.getTitle(), newbm.getTitle()) && CompareUtility.equals(oldPath.size(), newPath.size())) {
      ListIterator<AbstractPageState> oldIt = oldPath.listIterator(oldPath.size());
      ListIterator<AbstractPageState> newIt = newPath.listIterator(newPath.size());
      while (oldIt.hasPrevious()) {
        AbstractPageState oldState = oldIt.previous();
        TablePageState oldNode = null;
        TablePageState newNode = null;
        if (oldState instanceof TablePageState) {
          oldNode = (TablePageState) oldState;
        }
        AbstractPageState newState = newIt.previous();
        if (newState instanceof TablePageState) {
          newNode = (TablePageState) newState;
        }

        if (oldNode != null && newNode != null && CompareUtility.notEquals(oldNode.getSearchFormState(), newNode.getSearchFormState())) {
          return false;
        }
        else if (oldState != null && oldState.getLabel() != null && !oldState.getLabel().equals(newState.getLabel())) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  /**
   * @return the currently active bookmark or <code>null</code>, if no bookmark is active
   */
  public Bookmark getActiveBookmark() {
    if (m_index < m_bookmarks.size()) {
      return m_bookmarks.get(m_index);
    }
    else {
      return null;
    }
  }

  public List<Bookmark> getBookmarks() {
    return new ArrayList<Bookmark>(m_bookmarks);
  }

  public List<Bookmark> getBackwardBookmarks() {
    ArrayList<Bookmark> actions = new ArrayList<Bookmark>();
    int startIndex = 0;
    int endIndex = getIndex() - 1;
    if (startIndex <= endIndex) {
      for (int i = startIndex; i <= endIndex; i++) {
        Bookmark b = m_bookmarks.get(i);
        actions.add(b);
      }
    }
    return actions;
  }

  public boolean hasBackwardBookmarks() {
    int startIndex = 0;
    int endIndex = getIndex() - 1;
    return (startIndex <= endIndex);
  }

  public List<Bookmark> getForwardBookmarks() {
    ArrayList<Bookmark> actions = new ArrayList<Bookmark>();
    int startIndex = getIndex() + 1;
    int endIndex = m_bookmarks.size() - 1;
    if (startIndex >= 0 && endIndex >= startIndex) {
      for (int i = startIndex; i <= endIndex; i++) {
        Bookmark b = m_bookmarks.get(i);
        actions.add(b);
      }
    }
    return actions;
  }

  public boolean hasForwardBookmarks() {
    int startIndex = getIndex() + 1;
    int endIndex = m_bookmarks.size() - 1;
    return (startIndex >= 0 && endIndex >= startIndex);
  }

  /**
   * Steps forward in the history by one step and activates that bookmark.
   * <p>
   * If stepping forward is not possible anymore, because no more bookmarks are available in the history, the active
   * bookmark stays the same.
   * </p>
   */
  public void stepForward() {
    int nextPos = m_index + 1;
    if (nextPos >= 0 && nextPos < m_bookmarks.size()) {
      saveCurrentStep();
      //
      m_index = nextPos;
      Bookmark b = m_bookmarks.get(m_index);
      try {
        m_addStepEnabled = false;
        //
        activateBookmark(b);
      }
      finally {
        m_addStepEnabled = true;
      }
      fireNavigationChanged();
    }
  }

  /**
   * Steps backward in the history by one step and activates that bookmark.
   * <p>
   * If stepping backward is not possible anymore, because no more bookmarks are available in the history, the active
   * bookmark stays the same.
   * </p>
   */
  public void stepBackward() {
    int nextPos = m_index - 1;
    if (nextPos >= 0 && nextPos < m_bookmarks.size()) {
      saveCurrentStep();
      //
      m_index = nextPos;
      fireNavigationChanged();
      Bookmark b = m_bookmarks.get(m_index);
      try {
        m_addStepEnabled = false;
        //
        activateBookmark(b);
      }
      finally {
        m_addStepEnabled = true;
      }
    }
  }

  public void stepTo(Bookmark b) {
    for (int i = 0; i < m_bookmarks.size(); i++) {
      if (m_bookmarks.get(i) == b) {
        saveCurrentStep();
        //
        m_index = i;
        fireNavigationChanged();
        try {
          m_addStepEnabled = false;
          //
          activateBookmark(b);
        }
        finally {
          m_addStepEnabled = true;
        }
        break;
      }
    }
  }

  public List<IMenu> getMenus() {
    List<Bookmark> bookmarks = getBookmarks();
    Bookmark current = getActiveBookmark();
    // children
    List<IMenu> newList = new ArrayList<IMenu>(bookmarks.size());
    for (Bookmark b : bookmarks) {
      try {
        ActivateNavigationHistoryMenu m = new ActivateNavigationHistoryMenu(b);
        m.initAction();
        if (b == current) {
          m.setEnabled(false);
        }
        newList.add(m);
      }
      catch (RuntimeException e) {
        LOG.error("could not initialize menu for bookmark '" + b + "'.", e);
      }
    }
    return newList;
  }

  /**
   * @return the current number of bookmarks in the history.
   */
  public int getSize() {
    return m_bookmarks.size();
  }

  public int getIndex() {
    return m_index;
  }

  public void addNavigationHistoryListener(NavigationHistoryListener listener) {
    m_listenerList.add(NavigationHistoryListener.class, listener);
  }

  public void removeNavigationHistoryListener(NavigationHistoryListener listener) {
    m_listenerList.remove(NavigationHistoryListener.class, listener);
  }

  private void fireBookmarkAdded(Bookmark bookmark) {
    NavigationHistoryEvent e = new NavigationHistoryEvent(this, NavigationHistoryEvent.TYPE_BOOKMARK_ADDED, bookmark);
    fireNavigationHistoryEvent(e);
  }

  private void fireBookmarkRemoved(Bookmark bookmark) {
    NavigationHistoryEvent e = new NavigationHistoryEvent(this, NavigationHistoryEvent.TYPE_BOOKMARK_REMOVDED, bookmark);
    fireNavigationHistoryEvent(e);
  }

  private void fireNavigationChanged() {
    NavigationHistoryEvent e = new NavigationHistoryEvent(this, NavigationHistoryEvent.TYPE_CHANGED);
    fireNavigationHistoryEvent(e);
  }

  private void fireNavigationHistoryEvent(NavigationHistoryEvent e) {
    EventListener[] a = m_listenerList.getListeners(NavigationHistoryListener.class);
    if (a != null) {
      for (int i = 0; i < a.length; i++) {
        ((NavigationHistoryListener) a[i]).navigationChanged(e);
      }
    }
  }

}
