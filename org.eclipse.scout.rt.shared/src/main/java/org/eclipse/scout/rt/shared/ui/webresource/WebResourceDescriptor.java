/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.ui.webresource;

import java.net.URL;
import java.util.Objects;

import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.UriUtility;

public class WebResourceDescriptor {

  private final URL m_url;
  private final String m_requestPath;
  private final String m_resolvedPath;

  public WebResourceDescriptor(URL url, String requestPath, String resolvedPath) {
    m_url = Assertions.assertNotNull(url);
    m_requestPath = Assertions.assertNotNull(requestPath);
    m_resolvedPath = Assertions.assertNotNull(resolvedPath);
  }

  public URL getUrl() {
    return m_url;
  }

  public String getRequestPath() {
    return m_requestPath;
  }

  public String getResolvedPath() {
    return m_resolvedPath;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    WebResourceDescriptor that = (WebResourceDescriptor) o;
    return UriUtility.equals(m_url, that.m_url) &&
        Objects.equals(m_requestPath, that.m_requestPath) &&
        Objects.equals(m_resolvedPath, that.m_resolvedPath);
  }

  @Override
  public int hashCode() {
    return Objects.hash(UriUtility.hashCode(m_url), m_requestPath, m_resolvedPath);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + '[' +
        "url=" + m_url +
        ", requestPath='" + m_requestPath + '\'' +
        ", resolvedPath='" + m_resolvedPath + '\'' +
        ']';
  }
}
