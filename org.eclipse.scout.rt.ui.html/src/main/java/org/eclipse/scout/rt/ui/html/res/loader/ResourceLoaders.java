/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.res.loader;

import java.util.regex.Pattern;

import jakarta.servlet.http.HttpServletRequest;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.server.commons.servlet.UrlHints;
import org.eclipse.scout.rt.ui.html.UiThemeHelper;

@ApplicationScoped
public class ResourceLoaders {

  protected static final Pattern ICON_PATTERN = Pattern.compile("^/icon/(.*)");
  protected static final Pattern BINARY_REF_PATTERN = Pattern.compile("^/binref/(.*)");
  protected static final Pattern DYNAMIC_RESOURCES_PATTERN = Pattern.compile("^/" + DynamicResourceInfo.PATH_PREFIX + "/.*");
  protected static final Pattern DEFAULT_VALUES_PATTERN = Pattern.compile("^/defaultValues$");

  public IResourceLoader create(HttpServletRequest req, String resourcePath) {
    if (resourcePath == null) {
      return null;
    }

    if (ICON_PATTERN.matcher(resourcePath).matches()) {
      return new IconLoader();
    }
    if (BINARY_REF_PATTERN.matcher(resourcePath).matches()) {
      return new BinaryRefResourceLoader(req);
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
    if (resourcePath.endsWith('/' + LegacyBrowserScriptLoader.LEGACY_BROWSERS_SCRIPT)) {
      return new LegacyBrowserScriptLoader();
    }
    if (resourcePath.endsWith('/' + ConfigPropertiesLoader.FILE_NAME)) {
      return new ConfigPropertiesLoader();
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
