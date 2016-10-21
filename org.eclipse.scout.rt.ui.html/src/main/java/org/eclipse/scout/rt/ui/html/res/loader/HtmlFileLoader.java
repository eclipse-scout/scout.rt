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

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.BEANS;
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
import org.eclipse.scout.rt.ui.html.res.IWebContentService;

/**
 * This class loads and parses HTML files from WebContent/ folder.
 */
public class HtmlFileLoader extends AbstractResourceLoader {

  private static final String THEME_KEY = "ui.theme";
  private static final String LOCALE_KEY = "ui.locale";

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
    HashMap<String, String> atts = new HashMap<>();
    Locale locale = NlsLocale.getOrElse(null);
    if (locale != null) {
      atts.put(LOCALE_KEY, locale.toString());
    }
    atts.put(THEME_KEY, m_theme);
    return new HttpCacheKey(pathInfo, atts);
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
    HtmlDocumentParserParameters params = createHtmlDocumentParserParameters(pathInfo);
    URL url = BEANS.get(IWebContentService.class).getWebContentResource(pathInfo);
    if (url == null) {
      // not handled here
      return null;
    }
    byte[] document = IOUtility.readFromUrl(url);
    HtmlDocumentParser parser = createHtmlDocumentParser(params);
    byte[] parsedDocument = parser.parseDocument(document);
    return BinaryResources.create()
        .withFilename(pathInfo)
        .withCharset(StandardCharsets.UTF_8)
        .withContent(parsedDocument)
        .withLastModifiedNow()
        .withCachingAllowed(true)
        .build();
  }

  public HtmlDocumentParserParameters createHtmlDocumentParserParameters(String htmlPath) {
    return new HtmlDocumentParserParameters(
        htmlPath,
        m_theme,
        m_minify,
        m_cacheEnabled,
        CONFIG.getPropertyValue(ExternalBaseUrlProperty.class));
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
