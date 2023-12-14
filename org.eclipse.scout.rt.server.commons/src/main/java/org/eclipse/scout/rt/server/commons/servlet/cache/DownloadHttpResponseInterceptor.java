/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.commons.servlet.cache;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
    m_downloadHeaders.forEach(resp::setHeader);
  }
}
