/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.ui.html.res.loader;

import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.server.commons.servlet.UrlHints;
import org.eclipse.scout.rt.ui.html.UiThemeHelper;

@ApplicationScoped
public class ResourceLoaders {

  protected static final Pattern ICON_PATTERN = Pattern.compile("^/icon/(.*)");
  protected static final Pattern DYNAMIC_RESOURCES_PATTERN = Pattern.compile("^/" + DynamicResourceInfo.PATH_PREFIX + "/.*");
  protected static final Pattern DEFAULT_VALUES_PATTERN = Pattern.compile("^/defaultValues$");

  public IResourceLoader create(HttpServletRequest req, String resourcePath) {
    if (resourcePath == null) {
      return null;
    }

    if (ICON_PATTERN.matcher(resourcePath).matches()) {
      return new IconLoader();
    }
    if (DYNAMIC_RESOURCES_PATTERN.matcher(resourcePath).matches()) {
      return new DynamicResourceLoader(req);
    }
    if (DEFAULT_VALUES_PATTERN.matcher(resourcePath).matches()) {
      return new DefaultValuesLoader();
    }
    if (resourcePath.endsWith("/locales.json")) {
      return new LocalesLoader();
    }
    if (resourcePath.endsWith("/texts.json")) {
      return new TextsLoader();
    }

    UiThemeHelper uiThemeHelper = UiThemeHelper.get();
    String theme = uiThemeHelper.getTheme(req);
    boolean minify = UrlHints.isMinifyHint(req);
    boolean cacheEnabled = UrlHints.isCacheHint(req);

    if (resourcePath.endsWith(".html")) {
      return new HtmlFileLoader(theme, minify, cacheEnabled);
    }

    WebResourceLoader loader = new WebResourceLoader(minify, cacheEnabled, uiThemeHelper.isDefaultTheme(theme) ? null : theme);
    if (loader.acceptFile(resourcePath)) {
      return loader;
    }
    return null;
  }
}
