/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.res.loader;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.FileUtility;
import org.eclipse.scout.rt.ui.html.UiHints;
import org.eclipse.scout.rt.ui.html.cache.HttpCacheKey;

public abstract class AbstractResourceLoader implements IResourceLoader {

  private final HttpServletRequest m_req;

  public AbstractResourceLoader(HttpServletRequest req) {
    Assertions.assertNotNull(req, "Argument 'req' must not be null");
    m_req = req;
  }

  @Override
  public HttpCacheKey createCacheKey(String resourcePath, Locale locale) {
    return new HttpCacheKey(resourcePath);
  }

  protected String detectContentType(String path) {
    if (path == null) {
      return null;
    }
    int lastSlash = path.lastIndexOf('/');
    String fileName = lastSlash >= 0 ? path.substring(lastSlash + 1) : path;
    // Prefer mime type mapping from container
    String contentType = m_req.getServletContext().getMimeType(fileName);
    if (contentType != null) {
      return contentType;
    }
    int lastDot = path.lastIndexOf('.');
    String fileExtension = lastDot >= 0 ? path.substring(lastDot + 1) : path;
    return FileUtility.getContentTypeForExtension(fileExtension);
  }

  protected boolean isMinify() {
    return UiHints.isMinifyHint(m_req);
  }

  protected boolean isCacheEnabled() {
    return UiHints.isCacheHint(m_req);
  }

  protected HttpSession getSession() {
    return m_req.getSession();
  }

  protected HttpServletRequest getRequest() {
    return m_req;
  }

}
