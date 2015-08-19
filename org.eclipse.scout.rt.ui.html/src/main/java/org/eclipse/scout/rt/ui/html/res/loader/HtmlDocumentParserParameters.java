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
package org.eclipse.scout.rt.ui.html.res.loader;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.scout.rt.ui.html.cache.HttpCacheKey;
import org.eclipse.scout.rt.ui.html.cache.HttpCacheObject;
import org.eclipse.scout.rt.ui.html.scriptprocessor.ScriptProcessor;

class HtmlDocumentParserParameters {

  private boolean m_minify;

  private boolean m_cacheEnabled;

  private ScriptProcessor m_scriptProcessor;

  /**
   * Cache key from HTML document.
   */
  private HttpCacheKey m_cacheKey;

  private HttpServletRequest m_req;

  boolean isMinify() {
    return m_minify;
  }

  boolean isCacheEnabled() {
    return m_cacheEnabled;
  }

  void setMinify(boolean minify) {
    m_minify = minify;
  }

  void setCacheEnabled(boolean cacheEnabled) {
    m_cacheEnabled = cacheEnabled;
  }

  void setScriptProcessor(ScriptProcessor scriptProcessor) {
    m_scriptProcessor = scriptProcessor;
  }

  void setRequest(HttpServletRequest request) {
    m_req = request;
  }

  void setCacheKey(HttpCacheKey cacheKey) {
    m_cacheKey = cacheKey;
  }

  String getHtmlPath() {
    return m_cacheKey.getResourcePath();
  }

  HttpCacheObject loadScriptFile(String resourcePath) throws IOException {
    ScriptFileLoader scriptFileLoader = new ScriptFileLoader(m_req, m_scriptProcessor);
    HttpCacheKey cacheKey = scriptFileLoader.createCacheKey(resourcePath, m_cacheKey.getLocale());
    return scriptFileLoader.loadResource(cacheKey);
  }

}
