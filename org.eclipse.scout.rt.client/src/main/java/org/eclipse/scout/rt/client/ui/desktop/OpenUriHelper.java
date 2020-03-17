/*******************************************************************************
 * Copyright (c) 2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.desktop;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.resource.MimeType;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.ui.UiSystem;
import org.eclipse.scout.rt.shared.ui.UserAgent;
import org.eclipse.scout.rt.shared.ui.UserAgentUtility;

/**
 * This helper decides based on a {@link BinaryResource} which {@link IOpenUriAction} should be used to open it.
 */
@ApplicationScoped
public class OpenUriHelper {

  public IOpenUriAction getOpenUriActionForResource(BinaryResource resource) {
    UserAgent userAgent = UserAgentUtility.getCurrentUserAgent();
    if (userAgent.getUiSystem().equals(UiSystem.IOS) && userAgent.isStandalone()) {
      // Never open files in a new window in iOS home screen mode because they would be opened in safari with a different HTTP session.
      // Because the resource is bound to the HTTP session the download would not be possible
      return OpenUriAction.DOWNLOAD;
    }
    if (resource != null) {
      if (isImage(resource)) {
        return getOpenUriActionImage();
      }
      if (isVideo(resource)) {
        return getOpenUriActionVideo();
      }
      if (isHtmlPage(resource)) {
        return getOpenUriActionHtmlPage();
      }
      if (isZipArchive(resource)) {
        return getOpenUriActionZipArchive();
      }
      if (isPdf(resource)) {
        return getOpenUriActionPdf();
      }
    }
    return getOpenUriActionDefault();
  }

  protected boolean isImage(BinaryResource resource) {
    return MimeType.isOneOf(MimeType.getCommonImageTypes(), resource.getContentType());
  }

  protected IOpenUriAction getOpenUriActionImage() {
    return OpenUriAction.NEW_WINDOW;
  }

  protected boolean isVideo(BinaryResource resource) {
    return MimeType.isOneOf(MimeType.getCommonVideoTypes(), resource.getContentType());
  }

  protected IOpenUriAction getOpenUriActionVideo() {
    return OpenUriAction.NEW_WINDOW;
  }

  protected boolean isHtmlPage(BinaryResource resource) {
    return MimeType.isOneOf(CollectionUtility.arrayList(MimeType.HTM, MimeType.HTML), resource.getContentType());
  }

  protected IOpenUriAction getOpenUriActionHtmlPage() {
    return OpenUriAction.NEW_WINDOW;
  }

  protected boolean isZipArchive(BinaryResource resource) {
    return MimeType.ZIP.equals(MimeType.convertToMimeType(resource.getContentType()));
  }

  protected IOpenUriAction getOpenUriActionZipArchive() {
    return OpenUriAction.DOWNLOAD;
  }

  protected boolean isPdf(BinaryResource resource) {
    return MimeType.PDF.equals(MimeType.convertToMimeType(resource.getContentType()));
  }

  protected IOpenUriAction getOpenUriActionPdf() {
    return OpenUriAction.OPEN;
  }

  protected IOpenUriAction getOpenUriActionDefault() {
    return OpenUriAction.DOWNLOAD;
  }
}
