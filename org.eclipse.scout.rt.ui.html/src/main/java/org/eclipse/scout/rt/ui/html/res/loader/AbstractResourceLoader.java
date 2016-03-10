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

import org.eclipse.scout.rt.platform.util.Assertions;
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

  protected boolean isMinify() {
    return UiHints.isMinifyHint(m_req);
  }

  protected boolean isCacheEnabled() {
    return UiHints.isCacheHint(m_req);
  }

  protected HttpServletRequest getRequest() {
    return m_req;
  }
}
