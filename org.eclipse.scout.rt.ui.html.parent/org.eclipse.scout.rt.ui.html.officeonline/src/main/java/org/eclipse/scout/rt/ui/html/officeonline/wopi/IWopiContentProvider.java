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
package org.eclipse.scout.rt.ui.html.officeonline.wopi;

import java.io.IOException;

import org.eclipse.scout.rt.ui.html.officeonline.CobaltImplementationService;
import org.eclipse.scout.rt.ui.html.officeonline.IOfficeWebAppsService;

/**
 * Implementation of a local {@link AbstractWopiRequestInterceptor} or the remote {@link CobaltImplementationService}
 */
public interface IWopiContentProvider {

  /**
   * @return the url that is visible by the office web apps server
   *         <p>
   *         for example http://localhost:8080/wopi/files
   *         <p>
   *         used by
   *         {@link IOfficeWebAppsService#createIFrameUrl(org.eclipse.scout.rt.ui.html.officeonline.IOfficeWebAppsService.Zone, org.eclipse.scout.rt.ui.html.officeonline.IOfficeWebAppsService.App, org.eclipse.scout.rt.ui.html.officeonline.IOfficeWebAppsService.Action, String, String)}
   */
  String getWopiBaseUrl();

  /**
   * @return a {@link FileInfo} with {@link FileInfo#exists()} = true | false. null if this content provider is not
   *         responsible for this kind of fileId
   */
  FileInfo getFileInfo(String fileId) throws IOException;

  byte[] getFileContent(String fileId) throws IOException;

  void setFileContent(String fileId, byte[] content) throws IOException;
}
