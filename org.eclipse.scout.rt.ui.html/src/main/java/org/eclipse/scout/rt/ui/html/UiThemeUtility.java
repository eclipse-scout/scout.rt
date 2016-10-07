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
package org.eclipse.scout.rt.ui.html;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.util.CompareUtility;
import org.eclipse.scout.rt.server.commons.servlet.CookieUtility;
import org.eclipse.scout.rt.ui.html.UiHtmlConfigProperties.UiThemeProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class UiThemeUtility {

  private static final Logger LOG = LoggerFactory.getLogger(UiThemeUtility.class);
  private static final String THEME_SESSION_ATTRIBUTE = UiThemeUtility.class.getName() + "#theme";

  /**
   * Cookie name used to store the preferred theme of a user (even after user has logged out).
   */
  private static final String THEME_COOKIE_NAME = "scout.ui.theme";

  private static String s_configTheme;
  private static boolean s_configThemeRead;

  private UiThemeUtility() {
    // static access only
  }

  /**
   * Only read configuration for UI theme once.
   */
  public static String getConfiguredTheme() {
    if (s_configThemeRead) {
      return s_configTheme;
    }
    else {
      s_configTheme = CONFIG.getPropertyValue(UiThemeProperty.class);
      s_configThemeRead = true;
      LOG.info("UI theme configured in config.properties: {}", s_configTheme);
      return s_configTheme;
    }
  }

  /**
   * When theme is set to 'default' we return null instead. This is required because we cannot set the theme to 'null'
   * with a request-parameter (because when a request-parameter return null it means the parameter is not set). Thus we
   * send ?theme=default to set the theme to null (which is means Scout loads the default-theme).
   */
  public static String getTheme(HttpServletRequest req) {
    String theme = null;

    // 1st - try to find the theme hint in the session attributes
    String themeFromSession = null;
    HttpSession session = req.getSession(false);
    if (session != null) {
      themeFromSession = (String) session.getAttribute(THEME_SESSION_ATTRIBUTE);
      theme = themeFromSession;
    }

    // 2nd - check if theme is requested by cookie
    if (theme == null) {
      Cookie cookie = CookieUtility.getCookieByName(req, THEME_COOKIE_NAME);
      if (cookie != null) {
        theme = cookie.getValue();
      }
    }

    // 3rd - use theme configured in config.properties or 'default'
    theme = defaultIfNull(theme);

    // store theme in session so we must not check 2 and 3 again for the next requests
    if (session != null && !CompareUtility.equals(theme, themeFromSession)) {
      session.setAttribute(THEME_SESSION_ATTRIBUTE, theme);
    }

    return theme;
  }

  /**
   * When theme is set to 'default', we want to lookup 'colors.css' and not 'colors-default.css'. That's why this method
   * returns null in that case.
   */
  public static String getThemeForLookup(HttpServletRequest req) {
    return getThemeName(getTheme(req));
  }

  /**
   * @return the given theme name or null if the theme name is null or equal to 'default'.
   */
  public static String getThemeName(String theme) {
    if (UiThemeProperty.DEFAULT_THEME.equals(theme)) {
      return null;
    }
    return theme;
  }

  /**
   * @param resp
   *          May be null, since this method can be called during a client-job, when no response is available. In that
   *          case no cookie is written.
   * @param session
   *          HTTP session
   * @param theme
   *          (<code>null</code> will reset the theme to the configured theme)
   */
  public static void storeTheme(HttpServletResponse resp, HttpSession session, String theme) {
    theme = defaultIfNull(theme);
    if (resp != null) {
      CookieUtility.addPersistentCookie(resp, THEME_COOKIE_NAME, theme);
    }
    session.setAttribute(THEME_SESSION_ATTRIBUTE, theme);
  }

  public static String defaultIfNull(String theme) {
    return theme == null ? getConfiguredTheme() : theme;
  }
}
