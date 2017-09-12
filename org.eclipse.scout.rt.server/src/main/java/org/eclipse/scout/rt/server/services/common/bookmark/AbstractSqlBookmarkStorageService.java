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
package org.eclipse.scout.rt.server.services.common.bookmark;

import java.io.IOException;

import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.serialization.SerializationUtility;
import org.eclipse.scout.rt.shared.services.common.bookmark.BookmarkFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This service stores the bookmark data's 3 root folders as individial data records
 */
public abstract class AbstractSqlBookmarkStorageService extends AbstractBookmarkStorageService {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractSqlBookmarkStorageService.class);

  /**
   * @return sql data with the following columns
   *
   *         <pre>
   * ID    NUMBER required
   * DATA  BINARY BookmarkFolder object required
   *         </pre>
   */
  @ConfigOperation
  @Order(10)
  protected Object[][] execSelectUserBookmarkFolder(Object userId) {
    return new Object[0][];
  }

  /**
   * @return sql data with the following columns
   *
   *         <pre>
   * ID    NUMBER required
   * DATA  BINARY BookmarkFolder object required
   *         </pre>
   */
  @ConfigOperation
  @Order(11)
  protected Object[][] execSelectGlobalBookmarkFolder() {
    return new Object[0][];
  }

  /**
   * <p>
   * The sql statement is created based on the 'folderData' and 'id' property of the model<br>
   *
   * <pre>
   * if (folderData != null) {
   *   if (id == 0) {
   *     //INSERT
   *   }
   *   else {
   *     //UPDATE
   *   }
   * }
   * else {
   *   //DELETE
   * }
   * </pre>
   *
   * SQL Examples:
   *
   * <pre>
   * SQL.insert(
   *     &quot;INSERT INTO BOOKMARKS(ID,BINARY) VALUES( SEQ.NEXTVAL, :kind, :folderData )&quot;,
   *     new NVPair(&quot;folderData&quot;, folderData));
   *
   * SQL.update(
   *     &quot;UPDATE BOOKMARKS SET BINARY=folderData WHERE ID=:id&quot;,
   *     new NVPair(&quot;id&quot;, id),
   *     new NVPair(&quot;folderData&quot;, folderData));
   *
   * SQL.delete(
   *     &quot;DELETE FROM BOOKMARKS WHERE ID=:id&quot;,
   *     new NVPair(&quot;id&quot;, id));
   * </pre>
   */
  @ConfigOperation
  @Order(20)
  protected void execStoreUserBookmarkFolder(Object userId, long id, byte[] folderData) {
  }

  /**
   * <p>
   * The sql statement is created based on the 'folderData' and 'id' property of the model<br>
   *
   * <pre>
   * if (folderData != null) {
   *   if (id == 0) {
   *     //INSERT
   *   }
   *   else {
   *     //UPDATE
   *   }
   * }
   * else {
   *   //DELETE
   * }
   * </pre>
   *
   * SQL Examples:
   *
   * <pre>
   * SQL.insert(
   *     &quot;INSERT INTO BOOKMARKS(ID,BINARY) VALUES( SEQ.NEXTVAL, :kind, :folderData )&quot;,
   *     new NVPair(&quot;folderData&quot;, folderData));
   *
   * SQL.update(
   *     &quot;UPDATE BOOKMARKS SET BINARY=folderData WHERE ID=:id&quot;,
   *     new NVPair(&quot;id&quot;, id),
   *     new NVPair(&quot;folderData&quot;, folderData));
   *
   * SQL.delete(
   *     &quot;DELETE FROM BOOKMARKS WHERE ID=:id&quot;,
   *     new NVPair(&quot;id&quot;, id));
   * </pre>
   */
  @ConfigOperation
  @Order(21)
  protected void execStoreGlobalBookmarkFolder(long id, byte[] folderData) {
  }

  /**
   * @param id
   * @param binary
   *          data
   * @return the {@link BookmarkFolder} created by the binary data
   */
  @ConfigOperation
  @Order(30)
  protected BookmarkFolder execResolveBookmarkFolder(long id, Object binaryData) {
    try {
      byte[] bytesResolved = (byte[]) binaryData;
      return SerializationUtility.createObjectSerializer().deserialize(bytesResolved, BookmarkFolder.class);
    }
    catch (Exception e) {
      throw new ProcessingException("loading object" + id, e);
    }
  }

  @Override
  protected BookmarkFolder readUserFolder(Object userId) {
    Object[][] data = execSelectUserBookmarkFolder(userId);
    for (Object[] row : data) {
      long id = ((Number) row[0]).longValue();
      try {
        BookmarkFolder folder = execResolveBookmarkFolder(id, row[1]);
        if (folder != null) {
          folder.setId(id);
          return folder;
        }
      }
      catch (RuntimeException p) {
        LOG.warn("invalid bookmark folder binary data for id={}: {}", id, p);
      }
    }
    return null;
  }

  @Override
  protected BookmarkFolder readGlobalFolder() {
    Object[][] data = execSelectGlobalBookmarkFolder();
    for (Object[] row : data) {
      long id = ((Number) row[0]).longValue();
      try {
        BookmarkFolder folder = execResolveBookmarkFolder(id, row[1]);
        if (folder != null) {
          folder.setId(id);
          return folder;
        }
      }
      catch (RuntimeException p) {
        LOG.warn("invalid bookmark folder binary data for id={}: {}", id, p);
      }
    }
    return null;
  }

  @Override
  protected void writeUserFolder(BookmarkFolder folder, Object userId) {
    try {
      byte[] data = SerializationUtility.createObjectSerializer().serialize(folder);
      execStoreUserBookmarkFolder(userId, folder.getId(), data);
    }
    catch (IOException e) {
      throw new ProcessingException("storing user bookmarks", e);
    }
  }

  @Override
  protected void writeGlobalFolder(BookmarkFolder folder) {
    try {
      byte[] data = SerializationUtility.createObjectSerializer().serialize(folder);
      execStoreGlobalBookmarkFolder(folder.getId(), data);
    }
    catch (IOException e) {
      throw new ProcessingException("storing global bookmarks", e);
    }
  }
}
