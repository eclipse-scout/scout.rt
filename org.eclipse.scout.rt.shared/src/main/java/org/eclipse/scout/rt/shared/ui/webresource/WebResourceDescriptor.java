/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.shared.ui.webresource;

import java.net.URL;
import java.util.Objects;

import org.eclipse.scout.rt.platform.util.Assertions;

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
    return Objects.equals(m_url, that.m_url) &&
        Objects.equals(m_requestPath, that.m_requestPath) &&
        Objects.equals(m_resolvedPath, that.m_resolvedPath);
  }

  @Override
  public int hashCode() {
    return Objects.hash(m_url, m_requestPath, m_resolvedPath);
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
