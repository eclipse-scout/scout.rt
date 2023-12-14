/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.services.common.bookmark;

import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.PostConstruct;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.bookmark.menu.ActivateBookmarkKeyStroke;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.event.FastListenerList;
import org.eclipse.scout.rt.platform.util.event.IFastListenerList;
import org.eclipse.scout.rt.shared.services.common.bookmark.Bookmark;
import org.eclipse.scout.rt.shared.services.common.bookmark.BookmarkData;
import org.eclipse.scout.rt.shared.services.common.bookmark.BookmarkFolder;
import org.eclipse.scout.rt.shared.services.common.bookmark.IBookmarkStorageService;
import org.eclipse.scout.rt.shared.services.common.bookmark.IBookmarkVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client side service for bookmark support
 * <p>
 * Uses the server side {@link IBookmarkStorageService} for data persistence
 * <p>
 * service state is per {@link IClientSession} instance
 */
public class BookmarkService implements IBookmarkService {
  private static final Logger LOG = LoggerFactory.getLogger(BookmarkService.class);
  private static final String SESSION_DATA_KEY = "bookmarkServiceState";

  private ServiceState getServiceState() {
    IClientSession session = ClientSessionProvider.currentSession();
    if (session == null) {
      throw new IllegalStateException("null client session in current job context");
    }
    ServiceState data = (ServiceState) session.getData(SESSION_DATA_KEY);
    if (data == null) {
      data = new ServiceState();
      data.m_model = new BookmarkData();
      session.setData(SESSION_DATA_KEY, data);
    }
    return data;
  }

  @PostConstruct
  public void addBookmarkServiceListener() {
    addBookmarkServiceListener(this::handleBookmarksChangedInternal);
  }

  protected void handleBookmarksChangedInternal(BookmarkServiceEvent e) {
    switch (e.getType()) {
      case BookmarkServiceEvent.TYPE_CHANGED: {
        //refresh global keystrokes
        final List<Bookmark> list = new ArrayList<>();
        IBookmarkVisitor visitor = new IBookmarkVisitor() {
          @Override
          public boolean visitFolder(List<BookmarkFolder> path) {
            return true;
          }

          @Override
          public boolean visitBookmark(List<BookmarkFolder> path, Bookmark b) {
            if (b.getKeyStroke() != null) {
              list.add(b);
            }
            return true;
          }
        };
        getBookmarkData().getUserBookmarks().visit(visitor);
        getBookmarkData().getGlobalBookmarks().visit(visitor);

        IDesktop desktop = ClientSessionProvider.currentSession().getDesktop();
        if (desktop != null) {
          List<IKeyStroke> newKeyStrokes = new ArrayList<>();
          for (IKeyStroke k : desktop.getKeyStrokes()) {
            if (k instanceof ActivateBookmarkKeyStroke) {
              //remove
            }
            else {
              newKeyStrokes.add(k);
            }
          }
          for (Bookmark b : list) {
            newKeyStrokes.add(new ActivateBookmarkKeyStroke(b));
          }
          desktop.setKeyStrokes(newKeyStrokes);
        }
        break;
      }
    }
  }

  @Override
  public void loadBookmarks() {
    IBookmarkStorageService storageService = BEANS.get(IBookmarkStorageService.class);
    importBookmarks(storageService.getBookmarkData());
  }

  @Override
  public void storeBookmarks() {
    ServiceState state = getServiceState();
    IBookmarkStorageService storageService = BEANS.get(IBookmarkStorageService.class);
    importBookmarks(storageService.storeBookmarkData(state.m_model));
  }

  @Override
  public void setStartBookmark() {
    ServiceState state = getServiceState();
    Bookmark b = ClientSessionProvider.currentSession().getDesktop().createBookmark();
    if (b != null) {
      b.setKind(Bookmark.USER_BOOKMARK);
      state.m_model.getUserBookmarks().setStartupBookmark(b);
    }
  }

  @Override
  public void deleteStartBookmark() {
    ServiceState state = getServiceState();
    state.m_model.getUserBookmarks().setStartupBookmark(null);
  }

  @Override
  public Bookmark getStartBookmark() {
    ServiceState state = getServiceState();
    Bookmark b = state.m_model.getUserBookmarks().getStartupBookmark();
    if (b == null) {
      b = state.m_model.getGlobalBookmarks().getStartupBookmark();
    }
    return b;
  }

  @Override
  public final BookmarkData getBookmarkData() {
    ServiceState state = getServiceState();
    return state.m_model;
  }

  @Override
  public void activate(Bookmark b) {
    if (b != null) {
      try {
        ClientSessionProvider.currentSession().getDesktop().activateBookmark(b);
      }
      catch (Exception t) {
        LOG.error("Could not activate bookmark", t);
      }
    }
  }

  @Override
  public void updateBookmark(Bookmark bm) {

    // Create a new bookmark from the current view:
    Bookmark newBookmark = ClientSessionProvider.currentSession().getDesktop().createBookmark();

    // We want to preserve certain aspects of the old bookmark:
    int cachedKind = bm.getKind();
    String cachedIconId = bm.getIconId();
    String cachedTitle = bm.getTitle();
    String cachedKeyStroke = bm.getKeyStroke();
    double cachedOrder = bm.getOrder();

    // Fill the old bookmark with the data from the new one:
    bm.setSerializedData(newBookmark.getSerializedData());
    // "setSerializedData" overwrites all attributes - restore them from the old bookmark:
    bm.setKind(cachedKind);
    bm.setIconId(cachedIconId);
    bm.setTitle(cachedTitle);
    bm.setKeyStroke(cachedKeyStroke);
    bm.setOrder(cachedOrder);
    // The bookmark's "text" should not be preserved - it is not editable by
    // the user and the only way to tell what the bookmark does.
  }

  @Override
  public IFastListenerList<BookmarkServiceListener> bookmarkServiceListeners() {
    return getServiceState().m_listenerList;
  }

  private void fireBookmarksChanged() {
    BookmarkServiceEvent e = new BookmarkServiceEvent(this, BookmarkServiceEvent.TYPE_CHANGED);
    fireBookmarkSeviceEvent(e);
  }

  private void fireBookmarkSeviceEvent(BookmarkServiceEvent e) {
    bookmarkServiceListeners().list().forEach(listener -> listener.bookmarksChanged(e));
  }

  private void importBookmarks(BookmarkData model) {
    ServiceState state = getServiceState();
    state.m_model.setUserBookmarks(model.getUserBookmarks());
    state.m_model.setGlobalBookmarks(model.getGlobalBookmarks());
    fireBookmarksChanged();
  }

  private static class ServiceState {
    FastListenerList<BookmarkServiceListener> m_listenerList = new FastListenerList<>();
    BookmarkData m_model;
  }
}
