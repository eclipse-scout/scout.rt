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

import java.util.Map;

import org.eclipse.scout.rt.platform.service.IService;
import org.eclipse.scout.rt.shared.TunnelToServer;
import org.eclipse.scout.rt.shared.security.PublishUserBookmarkPermission;

@TunnelToServer
public interface IBookmarkStorageService extends IService {

  /**
   * insert, update or delete bookmarks of the current user (subject) AND global bookmarks <br>
   * check the property {@link Bookmark#getKind()} for switching
   *
   * @return the new complete set of bookmarks with (eventually) updated ids
   */
  BookmarkData storeBookmarkData(BookmarkData data);

  /**
   * @return all bookmarks of the current user (subject) AND global bookmarks <br>
   *         check the property {@link Bookmark#getKind()} for switching
   */
  BookmarkData getBookmarkData();

  /**
   * copy the bookmarks contained in the folder to the {@value #INBOX_FOLDER_NAME} folder of the users
   * {@link BookmarkData#getUserBookmarks()}, {@link PublishUserBookmarkPermission}
   * <p>
   * Bookmarks are published to the {@link Bookmark#SPOOL_FOLDER_NAME} folder. Once the bookmarks are retrieved by the
   * user, that folder is copied into {@link Bookmark#INBOX_FOLDER_NAME} and the spool folder is cleared.
   *
   * @param publishFolder
   * @param targetGroup
   *          is a map that contains for example userId=scott or something like ou=eclipse.org, department=dev It is up
   *          to the implementation to define and handle the target group parameter
   */
  void publishBookmarkData(BookmarkFolder publishFolder, Map<String, Object> targetGroup);

}
