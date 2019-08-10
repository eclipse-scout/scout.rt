/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.ui.html.res.loader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.resource.BinaryResources;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheControl;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheKey;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheObject;
import org.eclipse.scout.rt.ui.html.UiHtmlConfigProperties.ScriptfileBuildProperty;
import org.eclipse.scout.rt.ui.html.res.DevelopmentScriptfileCache;
import org.eclipse.scout.rt.ui.html.res.IWebContentService;
import org.eclipse.scout.rt.ui.html.script.ScriptFileBuilder;
import org.eclipse.scout.rt.ui.html.script.ScriptOutput;
import org.eclipse.scout.rt.ui.html.script.ScriptRequest;
import org.eclipse.scout.rt.ui.html.script.ScriptSource.FileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class loads and parses CSS and JS files from WebContent/ folder.
 */
public class ScriptFileLoader extends AbstractResourceLoader {

  private static final Logger LOG = LoggerFactory.getLogger(ScriptFileLoader.class);

  public static final String THEME_KEY = "ui.theme";
  public static final String MINIFYED_KEY = "ui.minify";

  private final String m_theme;
  private final boolean m_minify;

  public ScriptFileLoader(String theme, boolean minify) {
    m_theme = theme;
    m_minify = minify;
  }

  @Override
  public HttpCacheKey createCacheKey(String resourcePath) {
    // remove the fingerprint from the cache key so that requests with randomly generated fingerprints result in the same cached item (there is only one anyway).
    // this prevents cache (memory) pollution and high load on the server due to script re-generation.
    String lookupPath = ScriptRequest.tryParse(resourcePath)
        .map(this::requestWithoutFingerprint)
        .orElse(resourcePath);

    if (FileType.JS == FileType.resolveFromFilename(lookupPath)) {
      // JavaScript files are always the same, no matter what the theme or the locale is
      Map<String, String> attributes = new HashMap<>(2);
      attributes.put(MINIFYED_KEY, Boolean.toString(m_minify));
      return new HttpCacheKey(lookupPath, Collections.unmodifiableMap(attributes));
    }

    // CSS files are different for depending on the current theme (but don't depend on the locale)
    Map<String, String> attributes = new HashMap<>(2);
    attributes.put(THEME_KEY, m_theme);
    attributes.put(MINIFYED_KEY, Boolean.toString(m_minify));
    return new HttpCacheKey(lookupPath, Collections.unmodifiableMap(attributes));
  }

  protected String requestWithoutFingerprint(ScriptRequest req) {
    return req.lookupPath();
  }

  @Override
  public boolean validateResource(String requestedExternalPath, HttpCacheObject cachedObject) {
    boolean valid = super.validateResource(requestedExternalPath, cachedObject);
    if (!valid) {
      return false;
    }

    return ScriptRequest.tryParse(requestedExternalPath)
        .map(sr -> validateResource(sr, cachedObject.getResource()))
        .orElse(Boolean.FALSE);
  }

  /**
   * validates if the given {@link BinaryResource} is a valid response for the {@link ScriptRequest} given.
   *
   * @param request
   *          is never {@code null}.
   * @param responseCandidate
   *          is never {@code null}.
   */
  protected boolean validateResource(ScriptRequest request, BinaryResource responseCandidate) {
    String requestedFingerprint = request.fingerprint();
    if (!StringUtility.hasText(requestedFingerprint)) {
      // no specific fingerprint was requested (e.g. in dev mode)
      return true;
    }

    String responseFingerprint = responseCandidate.getFingerprintAsHexString();
    if (requestedFingerprint.equals(responseFingerprint)) {
      return true;
    }

    LOG.debug("Resource '{}' having fingerprint '{}' could not be found. A resource was found but would have a different fingerprint '{}'.", request, requestedFingerprint, responseFingerprint);
    return false;
  }

  @Override
  public HttpCacheObject loadResource(HttpCacheKey cacheKey) throws IOException {
    // development resource cache to ensure css files are only build when changes in less files.
    if (Platform.get().inDevelopmentMode() && !CONFIG.getPropertyValue(ScriptfileBuildProperty.class)) {
      HttpCacheObject resouceFromDevCache = BEANS.get(DevelopmentScriptfileCache.class).get(cacheKey);
      if (resouceFromDevCache != null) {
        return resouceFromDevCache;
      }
    }
    return super.loadResource(cacheKey);
  }

  @Override
  public BinaryResource loadResource(String pathInfo) throws IOException {
    ScriptFileBuilder builder = new ScriptFileBuilder(BEANS.get(IWebContentService.class), m_theme, m_minify);
    ScriptOutput out = builder.buildScript(pathInfo);
    if (out == null) {
      return null;
    }

    return BinaryResources.create()
        .withFilename(translateLess(out.getPathInfo()))
        .withCharset(StandardCharsets.UTF_8)
        .withContent(out.getContent())
        .withLastModified(out.getLastModified())
        .withCachingAllowed(true)
        .withCacheMaxAge(HttpCacheControl.MAX_AGE_ONE_YEAR)
        .build();
  }

  /**
   * When the client requests a .css file, we translate the request to a .less file internally.
   */
  public static String translateLess(String pathInfo) {
    if (pathInfo.endsWith(".less")) {
      return pathInfo.substring(0, pathInfo.length() - 5) + ".css";
    }
    return pathInfo;
  }

  public static boolean acceptFile(String file) {
    return ScriptRequest.tryParse(file).isPresent();
  }
}
