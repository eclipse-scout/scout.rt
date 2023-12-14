/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.res.loader;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.resource.BinaryResources;
import org.eclipse.scout.rt.platform.util.IOUtility;
import org.eclipse.scout.rt.server.commons.servlet.HttpClientInfo;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheKey;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheObject;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpResponseHeaderContributor;
import org.eclipse.scout.rt.shared.SharedConfigProperties.ExternalBaseUrlProperty;
import org.eclipse.scout.rt.shared.ui.IUiEngineType;
import org.eclipse.scout.rt.shared.ui.UiEngineType;
import org.eclipse.scout.rt.shared.ui.UserAgentUtility;
import org.eclipse.scout.rt.shared.ui.webresource.WebResourceDescriptor;
import org.eclipse.scout.rt.shared.ui.webresource.WebResources;

/**
 * This class loads and parses HTML files from WebContent/ folder.
 */
public class HtmlFileLoader extends AbstractResourceLoader {

  private static final String THEME_KEY = "ui.theme";
  private static final String LOCALE_KEY = "ui.locale";
  private static final String MINIFY_KEY = "ui.minify";
  private static final String BROWSER_SUPPORTED_KEY = "ui.browserSupported";

  private final String m_theme;
  private final boolean m_minify;
  private final boolean m_cacheEnabled;

  public HtmlFileLoader(String theme, boolean minify, boolean cacheEnabled) {
    m_theme = theme;
    m_minify = minify;
    m_cacheEnabled = cacheEnabled;
  }

  @Override
  public HttpCacheKey createCacheKey(String pathInfo) {
    Map<String, String> attrs = new HashMap<>();
    // Cache key for HTML files include locale and theme, because "<scout:message>" tags are
    // locale-dependent, while CSS files (and therefore their fingerprint) are theme-dependent.
    Locale locale = NlsLocale.getOrElse(null);
    if (locale != null) {
      attrs.put(LOCALE_KEY, locale.toString());
    }
    attrs.put(THEME_KEY, m_theme);
    attrs.put(MINIFY_KEY, Boolean.toString(m_minify));

    // include if the browser is supported because the injected scripts tags are different for unsupported legacy browsers
    if (!isBrowserSupported()) {
      attrs.put(BROWSER_SUPPORTED_KEY, Boolean.FALSE.toString());
    }
    return new HttpCacheKey(pathInfo, attrs);
  }

  @Override
  public HttpCacheObject loadResource(HttpCacheKey cacheKey) throws IOException {
    String pathInfo = cacheKey.getResourcePath();
    BinaryResource content = loadResource(pathInfo);
    if (content == null) {
      return null;
    }
    // no cache-control, only E-Tag checks to make sure that a session with timeout is correctly
    // forwarded to the login using a GET request BEFORE the first json POST request
    HttpCacheObject httpCacheObject = new HttpCacheObject(cacheKey, content);
    // Suppress automatic "compatibility mode" in IE in intranet zone
    httpCacheObject.addHttpResponseInterceptor(new HttpResponseHeaderContributor("X-UA-Compatible", "IE=edge") {
      private static final long serialVersionUID = 1L;

      @Override
      public void intercept(HttpServletRequest req, HttpServletResponse resp) {
        HttpClientInfo httpClientInfo = HttpClientInfo.get(req);
        if (httpClientInfo.isMshtml()) {
          // Send headers only for IE
          super.intercept(req, resp);
        }
      }
    });
    return httpCacheObject;
  }

  @Override
  public BinaryResource loadResource(String pathInfo) throws IOException {
    URL url = WebResources
        .resolveWebResource(pathInfo, m_minify, m_cacheEnabled)
        .map(WebResourceDescriptor::getUrl)
        .orElse(null);

    if (url == null) {
      return null; // not handled here
    }

    HtmlDocumentParserParameters params = createHtmlDocumentParserParameters(pathInfo);
    HtmlDocumentParser parser = createHtmlDocumentParser(params);
    byte[] parsedDocument = parser.parseDocument(IOUtility.readFromUrl(url));
    return BinaryResources.create()
        .withFilename(pathInfo)
        .withCharset(StandardCharsets.UTF_8)
        .withContent(parsedDocument)
        .withLastModifiedNow()
        .withCachingAllowed(true)
        .build();
  }

  public boolean isBrowserSupported() {
    IUiEngineType uiEngineType = UserAgentUtility.getCurrentUserAgent().getUiEngineType();
    return !UiEngineType.IE.equals(uiEngineType) && !UiEngineType.EDGE.equals(uiEngineType);
  }

  public HtmlDocumentParserParameters createHtmlDocumentParserParameters(String htmlPath) {
    return new HtmlDocumentParserParameters(
        htmlPath,
        m_theme,
        m_minify,
        m_cacheEnabled,
        CONFIG.getPropertyValue(ExternalBaseUrlProperty.class),
        isBrowserSupported());
  }

  /**
   * Override this method to return a specialized impl. of an HTML document parser.
   * <p>
   * The default impl. creates and returns a new instance of {@link HtmlDocumentParser}.
   */
  protected HtmlDocumentParser createHtmlDocumentParser(HtmlDocumentParserParameters params) {
    return new HtmlDocumentParser(params);
  }

}
