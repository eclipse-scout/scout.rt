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
package org.eclipse.scout.rt.client.officeonline.wopi;

import java.io.IOException;

/**
 * Implementors can register a service with this interface in order to provide documents to be handled by the local
 * {@link org.eclipse.scout.rt.ui.html.officeonline.wopi.WopiRequestInterceptor} handled inside the
 * {@link org.eclipse.scout.rt.ui.html.json.servlet.AbstractJsonServlet}
 */
public interface IWopiContentProvider {

  /**
   * @return a {@link FileInfo} with {@link FileInfo#exists()} = true | false if this content provider is responsible
   *         for this fileId
   * @return null if this content provider is not responsible for this kind of fileId
   */
  FileInfo getFileInfo(String fileId);

  byte[] getFileContent(String fileId) throws IOException;

  void setFileContent(String fileId, byte[] content) throws IOException;

}
