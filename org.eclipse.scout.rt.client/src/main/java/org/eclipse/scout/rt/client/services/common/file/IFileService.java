/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.services.common.file;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Locale;

import org.eclipse.scout.rt.platform.service.IService;

public interface IFileService extends IService {

  /**
   * never returns null. Use {@link File#exists()} to check
   */
  File getLocalFile(String dir, String simpleName);

  /**
   * Sync remote files.<br>
   * After a call to this method it is safe to call {@link #getRemoteFile(String, String, false)}
   */
  void syncRemoteFiles(String folderPath, FilenameFilter filter);

  /**
   * Sync remote files directly into a specified user path.
   */
  void syncRemoteFilesToPath(String rootPath, String folderPath, FilenameFilter filter);

  File getRemoteFile(String dir, String simpleName);

  File getRemoteFile(String dir, String simpleName, Locale locale);

  /**
   * @param checkCache
   *          default true<br>
   *          If {@link #syncRemoteFiles(String, FilenameFilter)} has been called checkCache may be passed as false.<br>
   *          This is used when a large number of files is processed.
   */
  File getRemoteFile(String dir, String simpleName, Locale locale, boolean checkCache);

  /**
   * @since 21.10.2009
   */
  File getLocalFileLocation(String dir, String name);

  /**
   * @since 21.10.2009
   */
  File getRemoteFileLocation(String dir, String name);

}
