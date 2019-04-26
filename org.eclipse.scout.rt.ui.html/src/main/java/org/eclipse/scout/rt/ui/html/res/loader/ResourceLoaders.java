/*******************************************************************************
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.res.loader;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.server.commons.servlet.UrlHints;
import org.eclipse.scout.rt.ui.html.UiThemeHelper;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Pattern;

@ApplicationScoped
public class ResourceLoaders {

  private static final Pattern ICON_PAT = Pattern.compile("^/icon/.*");
  private static final Pattern DYNAMIC_RESOURCES_PAT = Pattern.compile("^/" + DynamicResourceInfo.PATH_PREFIX + "/.*");
  private static final Pattern DEFAULT_VALUES_PAT = Pattern.compile("^/defaultValues$");

  public static boolean isNewMode() {
    return Boolean.parseBoolean(System.getProperty("newMode")); // TODO [mvi]: remove
  }

  public IResourceLoader create(HttpServletRequest req, String resourcePath) {
    if (resourcePath == null) {
      return null;
    }

    if (resourcePath.endsWith(".html")) {
      String theme = UiThemeHelper.get().getTheme(req);
      boolean cacheEnabled = UrlHints.isCacheHint(req);
      boolean minify = UrlHints.isMinifyHint(req);
      return new HtmlFileLoader(theme, minify, cacheEnabled);
    }

    boolean newMode = isNewMode();
    if (newMode) {
      boolean minify = UrlHints.isMinifyHint(req);
      boolean cacheEnabled = UrlHints.isCacheHint(req);
      WebResourceLoader loader = new WebResourceLoader(minify, cacheEnabled);
      if (loader.acceptFile(resourcePath)) {
        return loader;
      }
    }
    else {
      // TODO [mvi]: remove old (legacy loader)
      if (ScriptFileLoader.acceptFile(resourcePath)) {
        String theme = UiThemeHelper.get().getTheme(req);
        boolean minify = UrlHints.isMinifyHint(req);
        return new ScriptFileLoader(theme, minify);
      }
    }
    if (ICON_PAT.matcher(resourcePath).matches()) {
      return new IconLoader();
    }
    if (DYNAMIC_RESOURCES_PAT.matcher(resourcePath).matches()) {
      return new DynamicResourceLoader(req);
    }
    if (DEFAULT_VALUES_PAT.matcher(resourcePath).matches()) {
      return new DefaultValuesLoader();
    }
    if (resourcePath.endsWith("/locales.json")) {
      return new LocalesLoader();
    }
    if (resourcePath.endsWith("/texts.json")) {
      return new TextsLoader();
    }
    if (resourcePath.endsWith(".json")) {
      if (JsonModelsLoader.acceptFile(resourcePath)) {
        return new JsonModelsLoader();
      }
      return new JsonFileLoader();
    }

    if (newMode) {
      return null;
    }
    else {
      return new BinaryFileLoader();
    }
  }
}

