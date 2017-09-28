/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.services.common.bookmark;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.Permission;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.serialization.SerializationUtility;
import org.eclipse.scout.rt.server.session.ServerSessionProvider;
import org.eclipse.scout.rt.shared.security.PublishUserBookmarkPermission;
import org.eclipse.scout.rt.shared.security.UpdateUserBookmarkPermission;
import org.eclipse.scout.rt.shared.services.common.bookmark.BookmarkFolder;
import org.eclipse.scout.rt.shared.services.common.file.IRemoteFileService;
import org.eclipse.scout.rt.shared.services.common.file.RemoteFile;
import org.eclipse.scout.rt.shared.services.common.security.ACCESS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileSystemBookmarkStorageService extends AbstractBookmarkStorageService {
  private static final Logger LOG = LoggerFactory.getLogger(FileSystemBookmarkStorageService.class);

  public static final String GLOBAL_FILE_NAME = "all_users";

  /**
   * supports the targetGroup properties
   * <ul>
   * <li>userId Object</li>
   * <li>userIdList Collection of Object</li>
   * </ul>
   */
  @SuppressWarnings("unchecked")
  @Override
  public void publishBookmarkData(BookmarkFolder publishFolder, Map<String, Object> targetGroup) {
    if (!ACCESS.check(new PublishUserBookmarkPermission())) {
      throw new ProcessingException("Function denied", new SecurityException("Function denied"));
    }
    //
    Set<Object> set = new HashSet<>();
    Collection<Object> userIdList = (Collection<Object>) targetGroup.get("userIdList");
    if (userIdList != null) {
      set.addAll(userIdList);
    }
    Object userId = targetGroup.get("userId");
    if (userId != null) {
      set.add(userId);
    }
    for (Object id : set) {
      publishBookmarkDataToUser(publishFolder, id);
    }
  }

  @Override
  protected Object getCurrentUserId() {
    return ServerSessionProvider.currentSession().getUserId();
  }

  @Override
  protected BookmarkFolder readUserFolder(Object userId) {
    return readBookmarkFolder(userId + ".ser");
  }

  @Override
  protected BookmarkFolder readGlobalFolder() {
    return readBookmarkFolder(GLOBAL_FILE_NAME + ".ser");
  }

  @Override
  protected void writeUserFolder(BookmarkFolder folder, Object userId) {
    writeBookmarkFolder(folder, userId + ".ser", new UpdateUserBookmarkPermission());
  }

  @Override
  protected void writeGlobalFolder(BookmarkFolder folder) {
    writeBookmarkFolder(folder, GLOBAL_FILE_NAME + ".ser", new UpdateUserBookmarkPermission());
  }

  /**
   * Reads a bookmark folder.
   *
   * @since 3.8.2
   */
  private BookmarkFolder readBookmarkFolder(String filename) {
    RemoteFile spec = new RemoteFile("bookmarks", filename, 0);
    RemoteFile f = BEANS.get(IRemoteFileService.class).getRemoteFile(spec);
    if (f.exists()) {
      try {
        byte[] bytes = f.extractData();
        return SerializationUtility.createObjectSerializer().deserialize(bytes, BookmarkFolder.class);
      }
      catch (Exception t) {
        LOG.error("Could not deserialize bookmark folder", t);
      }
    }
    return null;
  }

  /**
   * Writes a bookmark folder.
   *
   * @since 3.8.2
   */
  private void writeBookmarkFolder(BookmarkFolder folder, String filename, Permission permission) {
    try {
      if (ACCESS.check(permission)) {
        byte[] bytes = SerializationUtility.createObjectSerializer().serialize(folder);
        //
        RemoteFile spec = new RemoteFile("bookmarks", filename, 0);
        spec.readData(new ByteArrayInputStream(bytes));
        BEANS.get(IRemoteFileService.class).putRemoteFile(spec);
      }
    }
    catch (IOException e) {
      throw new ProcessingException("", e);
    }
  }
}
