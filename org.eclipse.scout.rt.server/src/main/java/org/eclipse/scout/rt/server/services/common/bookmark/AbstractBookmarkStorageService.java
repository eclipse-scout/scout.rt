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

import java.util.Map;

import org.eclipse.scout.rt.shared.security.ReadGlobalBookmarkPermission;
import org.eclipse.scout.rt.shared.security.UpdateGlobalBookmarkPermission;
import org.eclipse.scout.rt.shared.security.UpdateUserBookmarkPermission;
import org.eclipse.scout.rt.shared.services.common.bookmark.Bookmark;
import org.eclipse.scout.rt.shared.services.common.bookmark.BookmarkData;
import org.eclipse.scout.rt.shared.services.common.bookmark.BookmarkFolder;
import org.eclipse.scout.rt.shared.services.common.bookmark.IBookmarkStorageService;
import org.eclipse.scout.rt.shared.services.common.security.ACCESS;

public abstract class AbstractBookmarkStorageService implements IBookmarkStorageService {

  @Override
  public BookmarkData getBookmarkData() {
    Object userId = getCurrentUserId();
    BookmarkData newData = readBookmarks(userId);
    if (processSpoolToInbox(newData.getUserBookmarks())) {
      writeUserFolder(newData.getUserBookmarks(), userId);
    }
    return newData;
  }

  @Override
  public BookmarkData storeBookmarkData(BookmarkData newData) {
    Object userId = getCurrentUserId();
    BookmarkFolder existingUserFolder = readUserFolder(userId);
    if (existingUserFolder != null) {
      BookmarkFolder existingSpoolFolder = existingUserFolder.getFolder(Bookmark.SPOOL_FOLDER_NAME);
      if (existingSpoolFolder != null) {
        newData.getUserBookmarks().getFolders().add(existingSpoolFolder);
        processSpoolToInbox(newData.getUserBookmarks());
      }
    }
    writeBookmarks(newData, userId);
    return readBookmarks(userId);
  }

  /**
   * @return true if there was some data to process
   */
  private boolean processSpoolToInbox(BookmarkFolder folder) {
    if (folder != null) {
      BookmarkFolder spoolFolder = folder.getFolder(Bookmark.SPOOL_FOLDER_NAME);
      if (spoolFolder != null) {
        BookmarkFolder inboxFolder = folder.getFolder(Bookmark.INBOX_FOLDER_NAME);
        if (inboxFolder == null) {
          inboxFolder = new BookmarkFolder();
          inboxFolder.setTitle(Bookmark.INBOX_FOLDER_NAME);
          folder.getFolders().add(0, inboxFolder);
        }
        inboxFolder.addBookmarks(spoolFolder, true, false);
        folder.getFolders().remove(spoolFolder);
        return true;
      }
    }
    return false;
  }

  @Override
  public void publishBookmarkData(BookmarkFolder publishFolder, Map<String, Object> targetGroup) {
    //default is empty
  }

  /**
   * default is <code>return ServerJob.getCurrentSession().getUserId();</code>
   */
  protected abstract Object getCurrentUserId();

  /**
   * add bookmarks to the publish folder of a user
   */
  protected void publishBookmarkDataToUser(BookmarkFolder publishFolder, Object userId) {
    if (userId != null) {
      BookmarkFolder userFolder = readUserFolder(userId);
      if (userFolder == null) {
        userFolder = new BookmarkFolder();
      }
      BookmarkFolder userSpool = userFolder.getFolder(Bookmark.SPOOL_FOLDER_NAME);
      if (userSpool == null) {
        userSpool = new BookmarkFolder();
        userSpool.setTitle(Bookmark.SPOOL_FOLDER_NAME);
        userFolder.getFolders().add(0, userSpool);
      }
      userSpool.addBookmarks(publishFolder, true, false);
      writeUserFolder(userFolder, userId);
    }
  }

  protected BookmarkData readBookmarks(Object userId) {
    BookmarkData model = new BookmarkData();
    //user
    BookmarkFolder folder = readUserFolder(userId);
    if (folder != null) {
      model.setUserBookmarks(folder);
    }
    //global
    if (ACCESS.check(new ReadGlobalBookmarkPermission())) {
      folder = readGlobalFolder();
      if (folder != null) {
        model.setGlobalBookmarks(folder);
      }
    }
    return model;
  }

  protected void writeBookmarks(BookmarkData model, Object userId) {
    //user
    if (ACCESS.check(new UpdateUserBookmarkPermission())) {
      writeUserFolder(model.getUserBookmarks(), userId);
    }
    //global
    if (ACCESS.check(new UpdateGlobalBookmarkPermission())) {
      writeGlobalFolder(model.getGlobalBookmarks());
    }
  }

  /**
   * read resource containing user folder
   */
  protected abstract BookmarkFolder readUserFolder(Object userId);

  /**
   * read resource containing global folder
   */
  protected abstract BookmarkFolder readGlobalFolder();

  /**
   * write resource containing user folder
   */
  protected abstract void writeUserFolder(BookmarkFolder folder, Object userId);

  /**
   * read resource containing global folder
   */
  protected abstract void writeGlobalFolder(BookmarkFolder folder);
}
