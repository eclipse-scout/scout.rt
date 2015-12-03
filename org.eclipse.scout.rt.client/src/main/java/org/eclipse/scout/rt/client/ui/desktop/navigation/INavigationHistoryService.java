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
package org.eclipse.scout.rt.client.ui.desktop.navigation;

import java.util.List;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.platform.service.IService;
import org.eclipse.scout.rt.shared.services.common.bookmark.Bookmark;

/**
 * Service interface for storing the navigation history and navigating in that history.
 */
public interface INavigationHistoryService extends IService {
  String SERVICE_DATA_KEY = "navigationHistoryServiceData";

  /**
   * Adds a navigation savepoint (bookmark) to the history
   *
   * @return the added bookmark
   */
  Bookmark addStep(int level, String name, String iconId);

  /**
   * Adds a navigation savepoint (bookmark) to the history for the given page
   *
   * @return the added bookmark
   * @since 3.8.0
   */
  Bookmark addStep(int level, IPage<?> page);

  /**
   * @return the navigation history
   */
  List<Bookmark> getBookmarks();

  /**
   * @return the size of the history
   */
  int getSize();

  /**
   * @return the index in the history of the currently active bookmark
   */
  int getIndex();

  /**
   * @return the currently active bookmark or <code>null</code>, if no bookmark is active
   */
  Bookmark getActiveBookmark();

  /**
   * @return bookmarks before the active bookmark
   */
  List<Bookmark> getBackwardBookmarks();

  /**
   * @return true, if bookmarks exist before the active bookmark
   */
  boolean hasBackwardBookmarks();

  /**
   * @return bookmarks after the active bookmark
   */
  List<Bookmark> getForwardBookmarks();

  /**
   * @return true, if bookmarks exist after the active bookmark
   */
  boolean hasForwardBookmarks();

  /**
   * Steps forward in the history by one step and activates that bookmark
   */
  void stepForward();

  /**
   * Steps backward in the history by one step and activates that bookmark
   */
  void stepBackward();

  /**
   * Steps to the given bookmark and activates it
   *
   * @param bm
   *          the bookmark
   */
  void stepTo(Bookmark bm);

  /**
   * @return the bookmarks in the history as menus.
   */
  List<IMenu> getMenus();

  /**
   * Adds a listener for {@link NavigationHistoryEvent}
   */
  void addNavigationHistoryListener(NavigationHistoryListener listener);

  /**
   * Removes a listener for {@link NavigationHistoryEvent}
   */
  void removeNavigationHistoryListener(NavigationHistoryListener listener);

}
