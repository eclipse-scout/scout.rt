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
package org.eclipse.scout.rt.shared.services.common.file;

import java.io.FilenameFilter;
import java.io.OutputStream;

import org.eclipse.scout.rt.platform.service.IService;
import org.eclipse.scout.rt.shared.TunnelToServer;
import org.eclipse.scout.rt.shared.servicetunnel.RemoteServiceAccessDenied;

@TunnelToServer
public interface IRemoteFileService extends IService {

  /**
   * @return RemoteFile when spec.getLastModified() is different than on server, null otherwise <br>
   *         If the realFile.lastModified is <= spec.lastModified then no content is returned assuming that the caller
   *         already has the content. <br>
   *         spec normally doesn't contain any content
   *         <p>
   *         Example with no path: path=null, name="template.dot" <br>
   *         Example with relative path: path="templates/word", name="template.dot" <br>
   *         Example with absolute jndi path: path="/WEB-INF/resources/templates/word", name="template.dot" <br>
   *         Example with absolute filesystem path: path="C:/TEMP/resources/templates/word", name="template.dot" <br>
   */
  RemoteFile getRemoteFile(RemoteFile spec);

  /**
   * @return RemoteFile of specified file block. Use this method to get a large file from server to client. <br>
   *         The flag hasMoreParts indicates if there are more parts to follow.
   */
  RemoteFile getRemoteFilePart(RemoteFile spec, long blockNumber);

  /**
   * Same as {@link #getRemoteFile(RemoteFile)} with the difference that no content is read and returned.
   */
  RemoteFile getRemoteFileHeader(RemoteFile spec);

  @RemoteServiceAccessDenied
  void putRemoteFile(RemoteFile spec);

  /**
   * @return all files specified in foldePath and filter including existingFileInfoOnClient <br>
   *         Note: existing files that have not changed, are returned without content <br>
   *         spec normally doesn't contain any content
   */
  RemoteFile[] getRemoteFiles(String folderPath, FilenameFilter filter, RemoteFile[] existingFileInfoOnClient);

  /**
   * The file is not returned but immediately streamed from the original location to the destination. <br>
   * The destination is not closed after write.
   * <p>
   * Throws an exception if the file does not exist.
   *
   * @see #getRemoteFile(RemoteFile)
   */
  void streamRemoteFile(RemoteFile spec, OutputStream out);

}
