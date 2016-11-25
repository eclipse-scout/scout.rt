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
import org.eclipse.scout.rt.server.commons.servlet.UrlHints;
import org.eclipse.scout.rt.ui.html.UiThemeUtility;

@ApplicationScoped
public class ResourceLoaders {

  public IResourceLoader create(HttpServletRequest req, String resourcePath) {
    if (resourcePath.matches("^/icon/.*")) {
      return new IconLoader();
    }
    if (resourcePath.matches("^/" + DynamicResourceInfo.PATH_PREFIX + "/.*")) {
      return new DynamicResourceLoader(req);
    }
    if (ScriptFileLoader.acceptFile(resourcePath)) {
      String theme = UiThemeUtility.getThemeForLookup(req);
      boolean minify = UrlHints.isMinifyHint(req);
      return new ScriptFileLoader(theme, minify);
    }
    if (resourcePath.endsWith(".html")) {
      String theme = UiThemeUtility.getThemeForLookup(req);
      boolean minify = UrlHints.isMinifyHint(req);
      boolean cacheEnabled = UrlHints.isCacheHint(req);
      return new HtmlFileLoader(theme, minify, cacheEnabled);
    }
    if (resourcePath.matches("^/defaultValues$")) {
      return new DefaultValuesLoader();
    }
    if (resourcePath.endsWith("/locales.json")) {
      return new LocalesLoader();
    }
    if (resourcePath.endsWith("/texts.json")) {
      return new TextsLoader();
    }
    if (JsonModelsLoader.acceptFile(resourcePath)) {
      return new JsonModelsLoader();
    }
    if (resourcePath.endsWith(".json")) {
      return new JsonFileLoader();
    }
    return new BinaryFileLoader();
  }
}
