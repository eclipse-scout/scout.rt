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
package org.eclipse.scout.rt.server.services.common.bookmark;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.Permission;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.serialization.SerializationUtility;
import org.eclipse.scout.rt.server.ServerJob;
import org.eclipse.scout.rt.shared.security.PublishUserBookmarkPermission;
import org.eclipse.scout.rt.shared.security.UpdateUserBookmarkPermission;
import org.eclipse.scout.rt.shared.services.common.bookmark.BookmarkFolder;
import org.eclipse.scout.rt.shared.services.common.bookmark.IBookmarkStorageService;
import org.eclipse.scout.rt.shared.services.common.file.IRemoteFileService;
import org.eclipse.scout.rt.shared.services.common.file.RemoteFile;
import org.eclipse.scout.rt.shared.services.common.security.ACCESS;
import org.eclipse.scout.service.SERVICES;

@Priority(-1)
public class FileSystemBookmarkStorageService extends AbstractBookmarkStorageService implements IBookmarkStorageService {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(FileSystemBookmarkStorageService.class);

  public static final String GLOBAL_FILE_NAME = "all_users";

  public FileSystemBookmarkStorageService() {
  }

  /**
   * supports the targetGroup properties
   * <ul>
   * <li>userId Object</li>
   * <li>userIdList Collection of Object</li>
   * </ul>
   */
  @SuppressWarnings("unchecked")
  @Override
  public void publishBookmarkData(BookmarkFolder publishFolder, Map<String, Object> targetGroup) throws ProcessingException {
    if (!ACCESS.check(new PublishUserBookmarkPermission())) {
      throw new ProcessingException("Function denied", new SecurityException("Function denied"));
    }
    //
    HashSet<Object> set = new HashSet<Object>();
    Collection<Object> userIdList = (Collection<Object>) targetGroup.get("userIdList");
    if (userIdList != null) {
      for (Object userId : userIdList) {
        set.add(userId);
      }
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
    return ServerJob.getCurrentSession().getUserId();
  }

  @Override
  protected BookmarkFolder readUserFolder(Object userId) throws ProcessingException {
    return readBookmarkFolder(userId + ".ser");
  }

  @Override
  protected BookmarkFolder readGlobalFolder() throws ProcessingException {
    return readBookmarkFolder(GLOBAL_FILE_NAME + ".ser");
  }

  @Override
  protected void writeUserFolder(BookmarkFolder folder, Object userId) throws ProcessingException {
    writeBookmarkFolder(folder, userId + ".ser", new UpdateUserBookmarkPermission());
  }

  @Override
  protected void writeGlobalFolder(BookmarkFolder folder) throws ProcessingException {
    writeBookmarkFolder(folder, GLOBAL_FILE_NAME + ".ser", new UpdateUserBookmarkPermission());
  }

  /**
   * Reads a bookmark folder.
   * 
   * @since 3.8.2
   */
  private BookmarkFolder readBookmarkFolder(String filename) throws ProcessingException {
    RemoteFile spec = new RemoteFile("bookmarks", filename, 0);
    RemoteFile f = SERVICES.getService(IRemoteFileService.class).getRemoteFile(spec);
    if (f.exists()) {
      try {
        byte[] bytes = f.extractData();
        return SerializationUtility.createObjectSerializer().deserialize(bytes, BookmarkFolder.class);
      }
      catch (Throwable t) {
        LOG.error(null, t);
      }
    }
    return null;
  }

  /**
   * Writes a bookmark folder.
   * 
   * @since 3.8.2
   */
  private void writeBookmarkFolder(BookmarkFolder folder, String filename, Permission permission) throws ProcessingException {
    try {
      if (ACCESS.check(permission)) {
        byte[] bytes = SerializationUtility.createObjectSerializer().serialize(folder);
        //
        RemoteFile spec = new RemoteFile("bookmarks", filename, 0);
        spec.readData(new ByteArrayInputStream(bytes));
        SERVICES.getService(IRemoteFileService.class).putRemoteFile(spec);
      }
    }
    catch (ProcessingException e) {
      throw e;
    }
    catch (IOException e) {
      throw new ProcessingException("", e);
    }
  }
}
