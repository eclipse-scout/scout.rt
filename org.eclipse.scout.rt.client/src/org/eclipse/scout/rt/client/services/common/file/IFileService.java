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
package org.eclipse.scout.rt.client.services.common.file;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Locale;

import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.service.IService;

@Priority(-3)
public interface IFileService extends IService {

  /**
   * never returns null. Use {@link File#exists()} to check
   */
  File getLocalFile(String dir, String simpleName) throws ProcessingException;

  /**
   * Sync remote files.<br>
   * After a call to this method it is safe to call {@link #getRemoteFile(String, String, false)}
   */
  void syncRemoteFiles(String folderPath, FilenameFilter filter) throws ProcessingException;

  /**
   * Sync remote files directly into a specified user path.
   */
  void syncRemoteFilesToPath(String rootPath, String folderPath, FilenameFilter filter) throws ProcessingException;

  File getRemoteFile(String dir, String simpleName) throws ProcessingException;

  File getRemoteFile(String dir, String simpleName, Locale locale) throws ProcessingException;

  /**
   * @param checkCache
   *          default true<br>
   *          If {@link #syncRemoteFiles(String, FilenameFilter)} has been
   *          called checkCache may be passed as false.<br>
   *          This is used when a large number of files is processed.
   */
  File getRemoteFile(String dir, String simpleName, Locale locale, boolean checkCache) throws ProcessingException;

  /**
   * @since 21.10.2009
   */
  File getLocalFileLocation(String dir, String name) throws ProcessingException;

  /**
   * @since 21.10.2009
   */
  File getRemoteFileLocation(String dir, String name) throws ProcessingException;

}
