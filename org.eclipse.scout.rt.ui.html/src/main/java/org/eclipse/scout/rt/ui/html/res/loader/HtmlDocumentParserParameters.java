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

import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheKey;

public class HtmlDocumentParserParameters {

  private final boolean m_minify;
  private final boolean m_cacheEnabled;
  private final String m_basePath;
  private final String m_theme;

  /**
   * Cache key of HTML document.
   */
  private final HttpCacheKey m_cacheKey;

  public HtmlDocumentParserParameters(HttpCacheKey cacheKey, String theme, boolean minify, boolean cacheEnabled, String basePath) {
    m_minify = minify;
    m_cacheEnabled = cacheEnabled;
    m_basePath = basePath;
    m_theme = theme;
    m_cacheKey = cacheKey;
  }

  public boolean isMinify() {
    return m_minify;
  }

  public boolean isCacheEnabled() {
    return m_cacheEnabled;
  }

  public String getHtmlPath() {
    return m_cacheKey.getResourcePath();
  }

  public String getBasePath() {
    return m_basePath;
  }

  public String getTheme() {
    return m_theme;
  }

}
