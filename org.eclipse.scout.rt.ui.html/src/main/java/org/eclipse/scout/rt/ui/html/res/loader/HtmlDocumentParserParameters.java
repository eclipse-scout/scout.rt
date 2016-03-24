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

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheKey;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheObject;

public class HtmlDocumentParserParameters {

  private boolean m_minify;
  private boolean m_cacheEnabled;

  /**
   * Cache key of HTML document.
   */
  private HttpCacheKey m_cacheKey;
  private HttpServletRequest m_req;

  public boolean isMinify() {
    return m_minify;
  }

  public boolean isCacheEnabled() {
    return m_cacheEnabled;
  }

  public void setMinify(boolean minify) {
    m_minify = minify;
  }

  public void setCacheEnabled(boolean cacheEnabled) {
    m_cacheEnabled = cacheEnabled;
  }

  public void setRequest(HttpServletRequest request) {
    m_req = request;
  }

  public void setCacheKey(HttpCacheKey cacheKey) {
    m_cacheKey = cacheKey;
  }

  public String getHtmlPath() {
    return m_cacheKey.getResourcePath();
  }

  public HttpCacheObject loadScriptFile(String resourcePath) throws IOException {
    ScriptFileLoader scriptLoader = new ScriptFileLoader(m_req);
    HttpCacheKey cacheKey = scriptLoader.createCacheKey(resourcePath);
    return scriptLoader.loadResource(cacheKey);
  }

}
