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

import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.navigation.NavigationHistoryEvent;
import org.eclipse.scout.rt.client.ui.desktop.navigation.NavigationHistoryListener;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithNodes;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.shared.AbstractIcons;
import org.eclipse.scout.rt.shared.services.common.bookmark.AbstractPageState;
import org.eclipse.scout.rt.shared.services.common.bookmark.Bookmark;
import org.eclipse.scout.rt.shared.services.common.bookmark.TablePageState;

/**
 *
 */
public class UserNavigationHistory {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(UserNavigationHistory.class);

  private final EventListenerList m_listenerList;
  private final LinkedList<Bookmark> m_bookmarks;
  private int m_index;// 0...inf
  private boolean m_addStepEnabled;

  public UserNavigationHistory() {
    m_addStepEnabled = true;
    m_listenerList = new EventListenerList();
    m_bookmarks = new LinkedList<Bookmark>();
  }

  public Bookmark addStep(int level, IPage page) {
    if (!m_addStepEnabled) {
      return null;
    }

    Bookmark bm = null;
    try {
      bm = ClientSyncJob.getCurrentSession().getDesktop().createBookmark(page);
      if (bm == null) {
        return null;
      }
      decorateBookmark(bm, level, page.getCell().getText(), page.getCell().getIconId());

      return addStep(bm);
    }
    catch (Throwable t) {
      return handleAddStepError(t, bm);
    }
  }

  public Bookmark addStep(int level, String name, String iconId) {
    if (!m_addStepEnabled) {
      return null;
    }

    Bookmark bm = null;
    try {
      bm = ClientSyncJob.getCurrentSession().getDesktop().createBookmark();
      if (bm == null) {
        return null;
      }
      decorateBookmark(bm, level, name, iconId);

      return addStep(bm);
    }
    catch (Throwable t) {
      return handleAddStepError(t, bm);
    }
  }

  protected void decorateBookmark(Bookmark bm, int level, String name, String iconId) {
    bm.setTitle(StringUtility.rpad("", " ", level * 2) + name);
    bm.setIconId(iconId);
  }

  protected Bookmark handleAddStepError(Throwable t, Bookmark bm) {
    String bookmarkTitle = "";
    if (bm != null) {
      bookmarkTitle = bm.getText();
    }
    LOG.warn("Exception occured while adding step to navigation history for bookmark: " + bookmarkTitle, t);
    return null;
  }

  protected void activateBookmark(Bookmark b) throws ProcessingException {
    IDesktop desktop = ClientSyncJob.getCurrentSession().getDesktop();
    desktop.activateBookmark(b, false);
    //scroll to tree selection
    IOutline outline = desktop.getOutline();
    if (outline != null) {
      outline.scrollToSelection();
      //scroll to table selection
      IPage page = outline.getActivePage();
      if (page instanceof IPageWithTable<?>) {
        ITable table = ((IPageWithTable<?>) page).getTable();
        if (table != null) {
          table.scrollToSelection();
        }
      }
      else if (page instanceof IPageWithNodes) {
        ITable table = ((IPageWithNodes) page).getInternalTable();
        if (table != null) {
          table.scrollToSelection();
        }
      }
    }
  }

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
      while (nextPos < m_bookmarks.size()) {
        Bookmark removedBookmark = m_bookmarks.removeLast();
        m_index = m_index - 1;
        fireBookmarkRemoved(removedBookmark);
      }
      m_bookmarks.add(bm);
      m_index = m_bookmarks.size() - 1;
      fireBookmarkAdded(bm);
    }
    // size check, if list larger than 25 entries, truncate it
    while (m_bookmarks.size() > 25) {
      Bookmark removedBookmark = m_bookmarks.removeFirst();
      m_index = Math.max(0, m_index - 1);
      fireBookmarkRemoved(removedBookmark);
    }
    fireNavigationChanged();
    return bm;
  }

  private void saveCurrentStep() {
    try {
      // if last position was same as new one, overwrite it
      if (m_index == m_bookmarks.size() - 1) {
        Bookmark last = m_bookmarks.get(m_index);
        Bookmark bm = ClientSyncJob.getCurrentSession().getDesktop().createBookmark();
        if (bm != null) {
          bm.setTitle(last.getTitle());
          bm.setIconId(last.getIconId());
          // replace
          m_bookmarks.set(m_index, bm);
        }
      }
    }
    catch (Throwable t) {
      // nop
    }
  }

  private boolean isSameBookmark(Bookmark oldbm, Bookmark newbm) {
    if (CompareUtility.equals(oldbm.getTitle(), newbm.getTitle())) {
      TablePageState oldLastNode = null;
      TablePageState newLastNode = null;
      List<AbstractPageState> list = oldbm.getPath();
      if (list != null && list.size() > 0) {
        AbstractPageState s = list.get(list.size() - 1);
        if (s instanceof TablePageState) {
          oldLastNode = (TablePageState) s;
        }
      }
      list = newbm.getPath();
      if (list != null && list.size() > 0) {
        AbstractPageState s = list.get(list.size() - 1);
        if (s instanceof TablePageState) {
          newLastNode = (TablePageState) s;
        }
      }
      if (oldLastNode != null && newLastNode != null) {
        if (CompareUtility.equals(oldLastNode.getSearchFormState(), newLastNode.getSearchFormState())) {
          return true;
        }
        else {
          return false;
        }
      }
      else {
        return true;
      }
    }
    else {
      return false;
    }
  }

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

  public void stepForward() throws ProcessingException {
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

  public void stepBackward() throws ProcessingException {
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

  public void stepTo(Bookmark b) throws ProcessingException {
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

  public IMenu[] getMenus() {
    List<Bookmark> bookmarks = getBookmarks();
    Bookmark current = getActiveBookmark();
    // children
    ArrayList<IMenu> newList = new ArrayList<IMenu>();
    for (Bookmark b : bookmarks) {
      ActivateNavigationHistoryMenu m = new ActivateNavigationHistoryMenu(b);
      if (b == current) {
        m.setIconId(AbstractIcons.NavigationCurrent);
        m.setEnabled(false);
      }
      newList.add(m);
    }
    return newList.toArray(new IMenu[newList.size()]);
  }

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
