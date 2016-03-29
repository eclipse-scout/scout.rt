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

import javax.servlet.http.HttpServletRequest;

import org.eclipse.scout.rt.platform.ApplicationScoped;

@ApplicationScoped
public class ResourceLoaders {

  public IResourceLoader create(HttpServletRequest req, String resourcePath) {
    if (resourcePath.matches("^/icon/.*")) {
      return new IconLoader(req);
    }
    if (resourcePath.matches("^/dynamic/.*")) {
      return new DynamicResourceLoader(req);
    }
    if ((resourcePath.endsWith(".js") || resourcePath.endsWith(".css"))) {
      return new ScriptFileLoader(req);
    }
    if (resourcePath.endsWith(".html")) {
      return new HtmlFileLoader(req);
    }
    if (resourcePath.matches("^/defaultValues$")) {
      return new DefaultValuesLoader(req);
    }
    if (resourcePath.endsWith(".json")) {
      return new JsonFileLoader(req);
    }
    return new BinaryFileLoader(req);
  }
}
