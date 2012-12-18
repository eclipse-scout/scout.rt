/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.window.desktop.navigation;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.service.BrowserNavigation;
import org.eclipse.rap.rwt.client.service.BrowserNavigationEvent;
import org.eclipse.rap.rwt.client.service.BrowserNavigationListener;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.ui.desktop.navigation.INavigationHistoryService;
import org.eclipse.scout.rt.client.ui.desktop.navigation.NavigationHistoryEvent;
import org.eclipse.scout.rt.client.ui.desktop.navigation.NavigationHistoryListener;
import org.eclipse.scout.rt.shared.services.common.bookmark.AbstractPageState;
import org.eclipse.scout.rt.shared.services.common.bookmark.Bookmark;
import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.scout.service.SERVICES;

////TODO RAP 2.0 Migration
public class RwtScoutNavigationSupport {

  private final IRwtEnvironment m_uiEnvironment;
  private BrowserNavigation m_uiHistory;
  private INavigationHistoryService m_historyService;
  private P_NavigationHistoryListener m_scoutListener;
  private BrowserNavigationListener m_uiListener = new BrowserNavigationListener() {
    private static final long serialVersionUID = 1L;

    @Override
    public void navigated(BrowserNavigationEvent event) {
      handleNavigationFromUi(event.getState());
    }
  };

  public RwtScoutNavigationSupport(IRwtEnvironment uiEnvironment) {
    m_uiEnvironment = uiEnvironment;
  }

  public void install() {
    if (m_uiHistory == null) {
      m_uiHistory = RWT.getClient().getService(BrowserNavigation.class);
      m_uiHistory.addBrowserNavigationListener(m_uiListener);
    }
    new ClientSyncJob("", getUiEnvironment().getClientSession()) {
      @Override
      protected void runVoid(IProgressMonitor monitor) throws Throwable {
        m_historyService = SERVICES.getService(INavigationHistoryService.class);
        if (m_scoutListener == null) {
          m_scoutListener = new P_NavigationHistoryListener();
          m_historyService.addNavigationHistoryListener(m_scoutListener);
        }
      }
    }.schedule();
  }

  public void uninstall() {
    if (m_historyService != null && m_scoutListener != null) {
      m_historyService.removeNavigationHistoryListener(m_scoutListener);
    }
    if (m_uiHistory != null) {
      m_uiHistory.addBrowserNavigationListener(m_uiListener);
    }
  }

  protected void handleNavigationFromUi(final String entryId) {
    Runnable t = new Runnable() {
      @Override
      public void run() {
        try {
          for (Bookmark b : m_historyService.getBookmarks()) {
            if (getId(b).equals(entryId)) {
              m_historyService.stepTo(b);
              break;
            }
          }
        }
        catch (ProcessingException e) {
          //nop
        }
      }
    };
    getUiEnvironment().invokeScoutLater(t, 0);

  }

  private IRwtEnvironment getUiEnvironment() {
    return m_uiEnvironment;
  }

  protected void handleBookmarkAddedFromScout(Bookmark bookmark) {
    String id = getId(bookmark);
    StringBuilder textBuilder = new StringBuilder(getUiEnvironment().getClientSession().getDesktop().getTitle() + " - ");
    textBuilder.append(cleanNl(bookmark.getText()));
    m_uiHistory.pushState(id, textBuilder.toString());
  }

  private String cleanNl(String s) {
    s = s.replaceAll("(\r\n)|(\n\r)|(\n)|(\r)", " -");
    return s;
  }

  private String cleanBrowserSpecialChars(String s) {
    s = s.replaceAll("\\s*\\-\\s*", "-");
    s = s.replaceAll("\\s+", "-");
    s = s.replaceAll(",", "");
    return s;
  }

  private String getId(Bookmark b) {
    StringBuilder key = new StringBuilder();
    if (!StringUtility.isNullOrEmpty(b.getOutlineClassName())) {
      key.append(b.getOutlineClassName());
    }
    List<AbstractPageState> path = b.getPath();
    if (!path.isEmpty()) {
      for (int i = 0; i < path.size(); i++) {
        if (!StringUtility.isNullOrEmpty(path.get(i).getLabel())) {
          key.append("-" + path.get(i).getLabel());
        }
      }
    }
    return cleanBrowserSpecialChars(cleanNl(key.toString()));
  }

  private class P_NavigationHistoryListener implements NavigationHistoryListener {
    @Override
    public void navigationChanged(NavigationHistoryEvent e) {
      if (e.getType() == NavigationHistoryEvent.TYPE_BOOKMARK_ADDED) {
        final Bookmark bookmark = e.getBookmark();
        Runnable r = new Runnable() {
          @Override
          public void run() {
            handleBookmarkAddedFromScout(bookmark);
          }
        };
        getUiEnvironment().invokeUiLater(r);
      }
    }
  }
}
