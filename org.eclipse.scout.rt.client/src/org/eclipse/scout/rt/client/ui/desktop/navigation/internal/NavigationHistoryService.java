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

import java.util.List;

import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientJob;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.desktop.navigation.INavigationHistoryService;
import org.eclipse.scout.rt.client.ui.desktop.navigation.NavigationHistoryListener;
import org.eclipse.scout.rt.shared.services.common.bookmark.Bookmark;
import org.eclipse.scout.service.AbstractService;

@Priority(-1)
public class NavigationHistoryService extends AbstractService implements INavigationHistoryService {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(NavigationHistoryService.class);

  @Override
  public Bookmark addStep(int level, String name, String iconId) {
    return getUserNavigationHistory().addStep(level, name, iconId);
  }

  public Bookmark getActiveBookmark() {
    return getUserNavigationHistory().getActiveBookmark();
  }

  public List<Bookmark> getBookmarks() {
    return getUserNavigationHistory().getBookmarks();
  }

  public List<Bookmark> getBackwardBookmarks() {
    return getUserNavigationHistory().getBackwardBookmarks();
  }

  public boolean hasBackwardBookmarks() {
    return getUserNavigationHistory().hasBackwardBookmarks();
  }

  public List<Bookmark> getForwardBookmarks() {
    return getUserNavigationHistory().getForwardBookmarks();
  }

  public boolean hasForwardBookmarks() {
    return getUserNavigationHistory().hasForwardBookmarks();
  }

  public void stepForward() throws ProcessingException {
    getUserNavigationHistory().stepForward();
  }

  public void stepBackward() throws ProcessingException {
    getUserNavigationHistory().stepBackward();

  }

  public void stepTo(Bookmark b) throws ProcessingException {
    getUserNavigationHistory().stepTo(b);
  }

  public IMenu[] getMenus() {
    return getUserNavigationHistory().getMenus();
  }

  public int getSize() {
    return getUserNavigationHistory().getSize();
  }

  public int getIndex() {
    return getUserNavigationHistory().getIndex();
  }

  public void addNavigationHistoryListener(NavigationHistoryListener listener) {
    getUserNavigationHistory().addNavigationHistoryListener(listener);
  }

  public void removeNavigationHistoryListener(NavigationHistoryListener listener) {
    getUserNavigationHistory().removeNavigationHistoryListener(listener);
  }

  private UserNavigationHistory getUserNavigationHistory() {
    IClientSession session = ClientJob.getCurrentSession();
    if (session == null) {
      throw new IllegalStateException("null client session in current job context");
    }
    UserNavigationHistory data = (UserNavigationHistory) session.getData(SERVICE_DATA_KEY);
    if (data == null) {
      data = new UserNavigationHistory();
      session.setData(SERVICE_DATA_KEY, data);
    }
    return data;
  }
}
