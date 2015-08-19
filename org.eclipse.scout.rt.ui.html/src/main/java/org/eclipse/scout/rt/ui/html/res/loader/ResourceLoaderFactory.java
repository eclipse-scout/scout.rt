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

import javax.servlet.http.HttpServletRequest;

import org.eclipse.scout.rt.ui.html.scriptprocessor.ScriptProcessor;

public class ResourceLoaderFactory {

  private ScriptProcessor m_scriptProcessor;

  /**
   * Since creating a new instance of ScriptProcessor is an expensive operation
   * we take care that only one instance exists.
   */
  private synchronized ScriptProcessor getScriptProcessor() {
    if (m_scriptProcessor == null) {
      m_scriptProcessor = new ScriptProcessor();
    }
    return m_scriptProcessor;
  }

  public IResourceLoader createResourceLoader(HttpServletRequest req, String resourcePath) {
    if (resourcePath.matches("^/icon/.*")) {
      return new IconLoader(req);
    }
    if (resourcePath.matches("^/dynamic/.*")) {
      return new DynamicResourceLoader(req);
    }
    if ((resourcePath.endsWith(".js") || resourcePath.endsWith(".css"))) {
      return new ScriptFileLoader(req, getScriptProcessor());
    }
    if (resourcePath.endsWith(".html")) {
      return new HtmlFileLoader(req, getScriptProcessor());
    }
    if (resourcePath.endsWith(".json")) {
      return new JsonFileLoader(req);
    }
    return new BinaryFileLoader(req);
  }

}
