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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
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
    if (!ACCESS.check(new PublishUserBookmarkPermission())) throw new ProcessingException("Function denied", new SecurityException("Function denied"));
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
    String filename = userId + ".ser";
    RemoteFile spec = new RemoteFile("bookmarks", filename, 0);
    RemoteFile f = SERVICES.getService(IRemoteFileService.class).getRemoteFile(spec);
    if (f.exists()) {
      ObjectInputStream oin = null;
      try {
        byte[] bytes = f.extractData();
        oin = new ObjectInputStream(new ByteArrayInputStream(bytes));
        BookmarkFolder folder = (BookmarkFolder) oin.readObject();
        return folder;
      }
      catch (Throwable t) {
        LOG.error(null, t);
      }
      finally {
        if (oin != null) try {
          oin.close();
        }
        catch (Throwable fatal) {
        }
      }
    }
    return null;
  }

  @Override
  protected BookmarkFolder readGlobalFolder() throws ProcessingException {
    String filename = GLOBAL_FILE_NAME + ".ser";
    RemoteFile spec = new RemoteFile("bookmarks", filename, 0);
    RemoteFile f = SERVICES.getService(IRemoteFileService.class).getRemoteFile(spec);
    if (f.exists()) {
      ObjectInputStream oin = null;
      try {
        byte[] bytes = f.extractData();
        oin = new ObjectInputStream(new ByteArrayInputStream(bytes));
        BookmarkFolder folder = (BookmarkFolder) oin.readObject();
        return folder;
      }
      catch (Throwable t) {
        LOG.error(null, t);
      }
      finally {
        if (oin != null) try {
          oin.close();
        }
        catch (Throwable fatal) {
        }
      }
    }
    return null;
  }

  @Override
  protected void writeUserFolder(BookmarkFolder folder, Object userId) throws ProcessingException {
    ObjectOutputStream oout = null;
    byte[] bytes;
    try {
      if (ACCESS.check(new UpdateUserBookmarkPermission())) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        oout = new ObjectOutputStream(b);
        oout.writeObject(folder);
        oout.close();
        oout = null;
        bytes = b.toByteArray();
        //
        String filename = userId + ".ser";
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
    finally {
      if (oout != null) try {
        oout.close();
      }
      catch (Throwable fatal) {
      }
    }
  }

  @Override
  protected void writeGlobalFolder(BookmarkFolder folder) throws ProcessingException {
    ObjectOutputStream oout = null;
    byte[] bytes;
    try {
      if (ACCESS.check(new UpdateUserBookmarkPermission())) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        oout = new ObjectOutputStream(b);
        oout.writeObject(folder);
        oout.close();
        oout = null;
        bytes = b.toByteArray();
        //
        String filename = GLOBAL_FILE_NAME + ".ser";
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
    finally {
      if (oout != null) try {
        oout.close();
      }
      catch (Throwable fatal) {
      }
    }
  }

}
