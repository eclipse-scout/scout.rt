/*
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.server.commons.servlet.cache;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.DownloadResponseHelper;

/**
 * This interceptor is useful when the user requested the download of a resource ("save as" dialog). Do not use it for
 * inline resources such as images for ImageField.
 *
 * @see DownloadResponseHelper
 */
public class DownloadHttpResponseInterceptor implements IHttpResponseInterceptor {
  private static final long serialVersionUID = 1L;

  private final Map<String, String> m_downloadHeaders;

  public DownloadHttpResponseInterceptor(String filename) {
    m_downloadHeaders = BEANS.get(DownloadResponseHelper.class).getDownloadHeaders(filename);
  }

  @Override
  public void intercept(HttpServletRequest req, HttpServletResponse resp) {
    m_downloadHeaders.forEach((header, value) -> resp.setHeader(header, value));
  }
}
