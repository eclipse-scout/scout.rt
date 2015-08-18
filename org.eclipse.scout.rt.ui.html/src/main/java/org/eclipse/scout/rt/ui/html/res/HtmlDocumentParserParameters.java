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
package org.eclipse.scout.rt.ui.html.res;

import java.io.IOException;

import org.eclipse.scout.rt.ui.html.cache.HttpCacheObject;

/**
 *
 */
public class HtmlDocumentParserParameters {

  private String m_resourcePath;

  private boolean m_minify;

  private boolean m_cacheEnabled;

  private IScriptFileLoader m_scriptFileLoader;

  public String getResourcePath() {
    return m_resourcePath;
  }

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

  public void setScriptFileLoader(IScriptFileLoader scriptFileLoader) {
    m_scriptFileLoader = scriptFileLoader;
  }

  public HttpCacheObject loadScriptFile(String scriptPath) throws IOException {
    return m_scriptFileLoader.loadScriptFile(scriptPath);
  }

  static interface IScriptFileLoader {

    HttpCacheObject loadScriptFile(String scriptPath) throws IOException;

  }

}
