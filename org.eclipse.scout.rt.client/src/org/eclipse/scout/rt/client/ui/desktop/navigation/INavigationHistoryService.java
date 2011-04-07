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
package org.eclipse.scout.rt.client.ui.desktop.navigation;

import java.util.List;

import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.shared.services.common.bookmark.Bookmark;
import org.eclipse.scout.service.IService2;

@Priority(-3)
public interface INavigationHistoryService extends IService2 {
  public static final String NAVIGATION_HISTORY_USER_OBJECT = "navigationHistoryUserObject";

  /**
   * add a navigation savepoint
   */
  Bookmark addStep(int level, String name, String iconId);

  List<Bookmark> getBookmarks();

  int getSize();

  int getIndex();

  /**
   * get the currently active bookmark
   */
  Bookmark getActiveBookmark();

  /**
   * get bookmarks before the current
   */
  List<Bookmark> getBackwardBookmarks();

  boolean hasBackwardBookmarks();

  /**
   * get bookmarks after the current
   */
  List<Bookmark> getForwardBookmarks();

  boolean hasForwardBookmarks();

  /**
   * step foreward in the history by one step and active that bookmark
   */
  void stepForward() throws ProcessingException;

  /**
   * step backward in the history by one step and active that bookmark
   */
  void stepBackward() throws ProcessingException;

  void stepTo(Bookmark bm) throws ProcessingException;

  IMenu[] getMenus();

  void addNavigationHistoryListener(NavigationHistoryListener listener);

  void removeNavigationHistoryListener(NavigationHistoryListener listener);

}
