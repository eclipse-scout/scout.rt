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
package org.eclipse.scout.rt.server.commons.servlet;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.util.BooleanUtility;
import org.eclipse.scout.rt.platform.util.LazyValue;
import org.eclipse.scout.rt.server.commons.ServerCommonsConfigProperties.UrlHintsEnabledProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper to manage set and retrieve URL hints for a servlet request. Hints can be set by passing certain URL
 * parameters. Once set, the hints are stored in a session cookie (so they will be available at all subsequent requests,
 * e.g. when loading CSS or JS resources).
 * <p>
 * <b>Security considerations</b>
 * <p>
 * The URL hints can only be changed using URL parameters if the config property {@link UrlHintsEnabledProperty} is set
 * to true. Otherwise, the default values are used.
 *
 * @see UrlHints
 */
@ApplicationScoped
public class UrlHintsHelper {

  private static final Logger LOG = LoggerFactory.getLogger(UrlHintsHelper.class);

  /**
   * <code>false</code> if URL params should <b>not</b> be evaluated to change hints (e.g. on production systems for
   * security reasons).
   */
  private static final LazyValue<Boolean> UPDATE_ENABLED = new LazyValue<>(() -> CONFIG.getPropertyValue(UrlHintsEnabledProperty.class));

  private static final LazyValue<UrlHints> DEFAULT_HINTS = new LazyValue<>(() -> {
    UrlHints urlHints = BEANS.get(UrlHints.class);
    urlHints.setReadOnly();
    return urlHints;
  });

  private static final String URL_HINTS_COOKIE_NAME = "scout.urlHints";
  private static final String CACHED_URL_HINTS_ATTRIBUTE_NAME = "scout.cachedUrlHints";

  public UrlHints getDefaultUrlHints() {
    return DEFAULT_HINTS.get();
  }

  public boolean isUpdateEnabled() {
    return BooleanUtility.nvl(UPDATE_ENABLED.get());
  }

  public UrlHints fromCookieString(String cookieString) {
    UrlHints result = BEANS.get(UrlHints.class);
    result.setFromCookieString(cookieString);
    result.setChanged(false); // <-- mark as unchanged
    return result;
  }

  public UrlHints fromCookie(Cookie cookie) {
    return fromCookieString(cookie == null ? null : cookie.getValue());
  }

  public void updateHints(HttpServletRequest req, HttpServletResponse resp) {
    if (req == null || !isUpdateEnabled()) {
      return;
    }
    UrlHints urlHints = getUrlHints(req);
    urlHints.setFromUrlParams(req);

    // If hints are equal to the defaults, remove the cookie if it exists (it is unnecessary to send it along with each request)
    if (urlHints.equals(BEANS.get(UrlHintsHelper.class).getDefaultUrlHints())) {
      Cookie cookie = CookieUtility.getCookieByName(req, URL_HINTS_COOKIE_NAME);
      if (cookie != null) {
        LOG.info("Deleting existing URL hints cookie (reset to default values)");
        CookieUtility.deleteCookie(resp, URL_HINTS_COOKIE_NAME);
      }
    }
    // Otherwise, update the cookie, but only if they are different than before. If they are
    // unchanged, the cookie apparently already has the correct value.
    else if (urlHints.isChanged()) {
      LOG.info("Setting URL hints cookie to {} [{}]", urlHints.toCookieString(), urlHints.toHumanReadableString());
      CookieUtility.addSessionCookie(resp, URL_HINTS_COOKIE_NAME, urlHints.toCookieString());
    }
  }

  protected UrlHints getUrlHints(HttpServletRequest req) {
    if (req == null || !isUpdateEnabled()) {
      return getDefaultUrlHints(); // assume defaults
    }

    UrlHints urlHints = (UrlHints) req.getAttribute(CACHED_URL_HINTS_ATTRIBUTE_NAME);
    if (urlHints != null) {
      // Only calculate hints once per request
      return urlHints;
    }

    // Construct from cookie
    Cookie cookie = CookieUtility.getCookieByName(req, URL_HINTS_COOKIE_NAME);
    urlHints = fromCookie(cookie);

    // Cache for more calls to this method within the same servlet request
    req.setAttribute(CACHED_URL_HINTS_ATTRIBUTE_NAME, urlHints);
    return urlHints;
  }

  public boolean isInspectorHint(HttpServletRequest req) {
    return getUrlHints(req).isInspector();
  }

  public boolean isCacheHint(HttpServletRequest req) {
    return getUrlHints(req).isCache();
  }

  public boolean isMinifyHint(HttpServletRequest req) {
    return getUrlHints(req).isMinify();
  }

  public boolean isCompressHint(HttpServletRequest req) {
    return getUrlHints(req).isCompress();
  }
}
