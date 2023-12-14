/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.server.commons.servlet.CookieUtility;
import org.eclipse.scout.rt.ui.html.UiHtmlConfigProperties.UiThemeProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class UiThemeHelper {

  private static final Logger LOG = LoggerFactory.getLogger(UiThemeHelper.class);
  private static final String THEME_SESSION_ATTRIBUTE = UiThemeHelper.class.getName() + "#theme";

  /**
   * Cookie name used to store the preferred theme of a user (even after user has logged out).
   */
  private static final String THEME_COOKIE_NAME = "scout.ui.theme";

  private String m_configTheme;
  private boolean m_configThemeRead;

  /**
   * @return The {@link UiThemeHelper} instance.
   */
  public static UiThemeHelper get() {
    return BEANS.get(UiThemeHelper.class);
  }

  /**
   * @return the value of the config property {@link UiThemeProperty}.
   */
  public String getConfiguredTheme() {
    // Only read configuration for UI theme once.
    if (m_configThemeRead) {
      return m_configTheme;
    }

    m_configTheme = CONFIG.getPropertyValue(UiThemeProperty.class);
    m_configThemeRead = true;
    LOG.info("UI theme configured in config.properties: {}", m_configTheme);
    return m_configTheme;
  }

  /**
   * Returns the theme for the current request. If neither the session or the theme cookie provide a value, the
   * configured default theme is returned. Thus, the return value is never <code>null</code>. This is required because
   * we cannot set the theme to 'null' with a request-parameter (because when a request-parameter return null it means
   * the parameter is not set). Thus we send ?theme=default to set the theme to null (which is means Scout loads the
   * default theme).
   */
  public String getTheme(HttpServletRequest req) {
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
    if (theme == null) {
      theme = getConfiguredTheme();
    }

    theme = validateTheme(theme);

    // store theme in session so we must not check 2 and 3 again for the next requests
    if (session != null && ObjectUtility.notEquals(theme, themeFromSession)) {
      session.setAttribute(THEME_SESSION_ATTRIBUTE, theme);
    }

    return theme;
  }

  /**
   * Callback to validate the extracted theme name before returning to the application.
   *
   * @param themeName
   *          The theme name as extracted. The name may come from the current {@link HttpSession}, the cookie associated
   *          with the current {@link HttpServletRequest} or the application configuration. See
   *          {@link #getTheme(HttpServletRequest)}.
   * @return The validated name. Clients may modify the input value as required. The resulting theme name must exist in
   *         the application.
   */
  protected String validateTheme(String themeName) {
    return themeName;
  }

  /**
   * @param resp
   *          May be null, since this method can be called during a client-job, when no response is available. In that
   *          case no cookie is written.
   * @param session
   *          HTTP session
   * @param theme
   *          <code>null</code> will reset the theme to the configured theme
   */
  public void storeTheme(HttpServletResponse resp, HttpSession session, String theme) {
    theme = ObjectUtility.nvl(theme, getConfiguredTheme());
    if (resp != null) {
      CookieUtility.addPersistentCookie(resp, THEME_COOKIE_NAME, theme);
    }
    session.setAttribute(THEME_SESSION_ATTRIBUTE, theme);
  }

  /**
   * @return <code>true</code> if the given theme is equal to 'default'. Note that this is <b>not</b> the same as
   *         checking for equality to {@link #getConfiguredTheme()}!
   */
  public boolean isDefaultTheme(String theme) {
    return ObjectUtility.equals(theme, UiThemeProperty.DEFAULT_THEME);
  }
}
