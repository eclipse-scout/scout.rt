/*******************************************************************************
 * Copyright (c) 2014-2016 BSI Business Systems Integration AG.
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
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.resource.BinaryResources;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheControl;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheKey;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheObject;
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
      return super.createCacheKey(lookupPath);
    }

    // CSS files are different for depending on the current theme (but don't depend on the locale)
    return new HttpCacheKey(lookupPath, Collections.singletonMap(THEME_KEY, m_theme));
  }

  protected String requestWithoutFingerprint(ScriptRequest req) {
    return req.toString(false, true);
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
  protected String translateLess(String pathInfo) {
    if (pathInfo.endsWith(".less")) {
      return pathInfo.substring(0, pathInfo.length() - 5) + ".css";
    }
    return pathInfo;
  }

  public static boolean acceptFile(String file) {
    return ScriptRequest.tryParse(file).isPresent();
  }
}
