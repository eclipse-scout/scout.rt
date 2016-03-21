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
import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.resource.BinaryResources;
import org.eclipse.scout.rt.platform.util.IOUtility;
import org.eclipse.scout.rt.ui.html.UiThemeUtility;
import org.eclipse.scout.rt.ui.html.cache.HttpCacheKey;
import org.eclipse.scout.rt.ui.html.cache.HttpCacheObject;
import org.eclipse.scout.rt.ui.html.cache.HttpResponseHeaderContributor;
import org.eclipse.scout.rt.ui.html.res.BrowserInfo;
import org.eclipse.scout.rt.ui.html.res.IWebContentService;
import org.eclipse.scout.rt.ui.html.scriptprocessor.ScriptProcessor;

/**
 * This class loads and parses HTML files from WebContent/ folder.
 */
public class HtmlFileLoader extends AbstractResourceLoader {
  private static final String THEME_KEY = "ui.theme";
  private static final String LOCALE_KEY = "ui.locale";

  private ScriptProcessor m_scriptProcessor;

  public HtmlFileLoader(HttpServletRequest req, ScriptProcessor scriptProcessor) {
    super(req);
    m_scriptProcessor = scriptProcessor;
  }

  @Override
  public HttpCacheKey createCacheKey(String pathInfo) {
    HashMap<String, String> atts = new HashMap<>();
    Locale locale = NlsLocale.getOrElse(null);
    if (locale != null) {
      atts.put(LOCALE_KEY, locale.toString());
    }
    atts.put(THEME_KEY, UiThemeUtility.getThemeForLookup(getRequest()));
    return new HttpCacheKey(pathInfo, atts);
  }

  @Override
  public HttpCacheObject loadResource(HttpCacheKey cacheKey) throws IOException {
    String pathInfo = cacheKey.getResourcePath();
    URL url = BEANS.get(IWebContentService.class).getWebContentResource(pathInfo);
    if (url == null) {
      // not handled here
      return null;
    }
    byte[] document = IOUtility.readFromUrl(url);
    HtmlDocumentParserParameters params = createHtmlDocumentParserParameters(cacheKey);
    HtmlDocumentParser parser = createHtmlDocumentParser(params);
    byte[] parsedDocument = parser.parseDocument(document);
    BinaryResource content = BinaryResources.create()
        .withFilename(pathInfo)
        .withCharset(StandardCharsets.UTF_8)
        .withContent(parsedDocument)
        .withLastModifiedNow()
        .withCachingAllowed(true)
        .build();

    // no cache-control, only E-Tag checks to make sure that a session with timeout is correctly
    // forwarded to the login using a GET request BEFORE the first json POST request
    HttpCacheObject httpCacheObject = new HttpCacheObject(cacheKey, content);
    // Suppress automatic "compatibility mode" in IE in intranet zone
    httpCacheObject.addHttpResponseInterceptor(new HttpResponseHeaderContributor("X-UA-Compatible", "IE=edge") {
      private static final long serialVersionUID = 1L;

      @Override
      public void intercept(HttpServletRequest req, HttpServletResponse resp) {
        BrowserInfo browserInfo = BrowserInfo.createFrom(req);
        if (browserInfo.isMshtml()) {
          // Send headers only for IE
          super.intercept(req, resp);
        }
      }
    });
    return httpCacheObject;
  }

  protected HtmlDocumentParserParameters createHtmlDocumentParserParameters(HttpCacheKey cacheKey) {
    HtmlDocumentParserParameters params = new HtmlDocumentParserParameters();
    params.setMinify(isMinify());
    params.setCacheEnabled(isCacheEnabled());
    params.setRequest(getRequest());
    params.setCacheKey(cacheKey);
    params.setScriptProcessor(m_scriptProcessor);
    return params;
  }

  protected HtmlDocumentParser createHtmlDocumentParser(HtmlDocumentParserParameters params) {
    return new HtmlDocumentParser(params);
  }
}
