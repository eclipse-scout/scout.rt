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
package org.eclipse.scout.rt.client.services.common.bookmark.internal;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientJob;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.services.common.bookmark.BookmarkServiceEvent;
import org.eclipse.scout.rt.client.services.common.bookmark.BookmarkServiceListener;
import org.eclipse.scout.rt.client.services.common.bookmark.IBookmarkService;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.bookmark.menu.ActivateBookmarkKeyStroke;
import org.eclipse.scout.rt.shared.services.common.bookmark.Bookmark;
import org.eclipse.scout.rt.shared.services.common.bookmark.BookmarkData;
import org.eclipse.scout.rt.shared.services.common.bookmark.BookmarkFolder;
import org.eclipse.scout.rt.shared.services.common.bookmark.IBookmarkStorageService;
import org.eclipse.scout.rt.shared.services.common.bookmark.IBookmarkVisitor;
import org.eclipse.scout.service.AbstractService;
import org.eclipse.scout.service.SERVICES;

/**
 * Client side service for bookmark support
 * <p>
 * Uses the server side {@link org.eclipse.scout.rt.client.ui.desktop.bookmark.IBookmarkStorageService} for data
 * persistence
 * <p>
 * service state is per {@link IClientSession} instance
 */
@Priority(-3)
public class BookmarkService extends AbstractService implements IBookmarkService {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(BookmarkService.class);
  private static final String SESSION_DATA_KEY = "bookmarkServiceState";

  public BookmarkService() {
  }

  private ServiceState getServiceState() {
    IClientSession session = ClientJob.getCurrentSession();
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

  @SuppressWarnings("deprecation")
  @Override
  public void initializeService() {
    super.initializeService();
    addBookmarkServiceListener(new BookmarkServiceListener() {
      @Override
      public void bookmarksChanged(BookmarkServiceEvent e) {
        handleBookmarksChangedInternal(e);
      }
    });
  }

  protected void handleBookmarksChangedInternal(BookmarkServiceEvent e) {
    switch (e.getType()) {
      case BookmarkServiceEvent.TYPE_CHANGED: {
        //refresh global keystrokes
        final ArrayList<Bookmark> list = new ArrayList<Bookmark>();
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

        IDesktop desktop = ClientSyncJob.getCurrentSession().getDesktop();
        if (desktop != null) {
          ArrayList<IKeyStroke> newKeyStrokes = new ArrayList<IKeyStroke>();
          for (IKeyStroke k : desktop.getKeyStrokes()) {
            if (k instanceof ActivateBookmarkKeyStroke) {
              //remove
            }
            else {
              newKeyStrokes.add(k);
            }
          }
          for (Bookmark b : list) {
            ActivateBookmarkKeyStroke k = new ActivateBookmarkKeyStroke(b);
            k.prepareAction();
            newKeyStrokes.add(k);
          }
          desktop.setKeyStrokes(newKeyStrokes.toArray(new IKeyStroke[newKeyStrokes.size()]));
        }
        break;
      }
    }
  }

  @Override
  public void loadBookmarks() throws ProcessingException {
    IBookmarkStorageService storageService = SERVICES.getService(IBookmarkStorageService.class);
    importBookmarks(storageService.getBookmarkData());
  }

  @Override
  public void storeBookmarks() throws ProcessingException {
    ServiceState state = getServiceState();
    IBookmarkStorageService storageService = SERVICES.getService(IBookmarkStorageService.class);
    importBookmarks(storageService.storeBookmarkData(state.m_model));
  }

  @Override
  public void setStartBookmark() throws ProcessingException {
    ServiceState state = getServiceState();
    Bookmark b = ClientSyncJob.getCurrentSession().getDesktop().createBookmark();
    b.setKind(Bookmark.USER_BOOKMARK);
    state.m_model.getUserBookmarks().setStartupBookmark(b);
  }

  @Override
  public void deleteStartBookmark() throws ProcessingException {
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
  public void activate(Bookmark b) throws ProcessingException {
    if (b != null) {
      try {
        ClientSyncJob.getCurrentSession().getDesktop().activateBookmark(b, false);
      }
      catch (Throwable t) {
        LOG.error(null, t);
      }
    }
  }

  @Override
  public void updateBookmark(Bookmark bm) throws ProcessingException {

    // Create a new bookmark from the current view:
    Bookmark newBookmark = ClientSyncJob.getCurrentSession().getDesktop().createBookmark();

    // We want to preserve certain aspects of the old bookmark:
    int cachedKind = bm.getKind();
    String cachedIconId = bm.getIconId();
    String cachedTitle = bm.getTitle();
    String cachedKeyStroke = bm.getKeyStroke();

    // Fill the old bookmark with the data from the new one:
    bm.setSerializedData(newBookmark.getSerializedData());
    // "setSerializedData" overwrites all attributes - restore them from the old bookmark:
    bm.setKind(cachedKind);
    bm.setIconId(cachedIconId);
    bm.setTitle(cachedTitle);
    bm.setKeyStroke(cachedKeyStroke);
    // The bookmark's "text" should not be preserved - it is not editable by
    // the user and the only way to tell what the bookmark does.
  }

  @Override
  public void addBookmarkServiceListener(BookmarkServiceListener listener) {
    ServiceState state = getServiceState();
    state.m_listenerList.add(BookmarkServiceListener.class, listener);
  }

  @Override
  public void removeBookmarkServiceListener(BookmarkServiceListener listener) {
    ServiceState state = getServiceState();
    state.m_listenerList.remove(BookmarkServiceListener.class, listener);
  }

  private void fireBookmarksChanged() {
    BookmarkServiceEvent e = new BookmarkServiceEvent(this, BookmarkServiceEvent.TYPE_CHANGED);
    fireBookmarkSeviceEvent(e);
  }

  private void fireBookmarkSeviceEvent(BookmarkServiceEvent e) {
    ServiceState state = getServiceState();
    EventListener[] a = state.m_listenerList.getListeners(BookmarkServiceListener.class);
    if (a != null) {
      for (int i = 0; i < a.length; i++) {
        ((BookmarkServiceListener) a[i]).bookmarksChanged(e);
      }
    }
  }

  private void importBookmarks(BookmarkData model) throws ProcessingException {
    ServiceState state = getServiceState();
    state.m_model.setUserBookmarks(model.getUserBookmarks());
    state.m_model.setGlobalBookmarks(model.getGlobalBookmarks());
    fireBookmarksChanged();
  }

  private static class ServiceState {
    EventListenerList m_listenerList = new EventListenerList();
    BookmarkData m_model;
  }
}
