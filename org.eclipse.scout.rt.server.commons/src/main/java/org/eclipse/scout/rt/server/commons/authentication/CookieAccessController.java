/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.commons.authentication;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.AbstractBooleanConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractLongConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractStringConfigProperty;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.security.SecurityUtility;
import org.eclipse.scout.rt.platform.security.SimplePrincipal;
import org.eclipse.scout.rt.platform.util.Base64Utility;
import org.eclipse.scout.rt.shared.SharedConfigProperties.AuthTokenPrivateKeyProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * This access controller can be used for demo, testing and tutorial purposes.
 * <p>
 * It is not recommended for production unless secure cookies are enabled (via `scout.app.sessionCookieConfigSecure` or
 * in the web.xml) and the corporate policy allows for cookie-based auto login.
 * <p>
 * It uses the config.properties {@link NameProperty()}, {@link MaxAgeProperty()}, {@link AuthTokenPrivateKeyProperty}
 * for signing the cookie
 * <p>
 * This access controller should be placed in front of {@link TrivialAccessController}. It needs to know about login,
 * logout and when login succeeded.
 *
 * @since 5.2
 */
public class CookieAccessController implements IAccessController {
  private static final Logger LOG = LoggerFactory.getLogger(CookieAccessController.class);
  private static final String SESSION_ATTRIBUTE_COOKIE_SENT = CookieAccessController.class.getName() + "#cookieSent";

  private boolean m_enabled;
  private String m_cookieName;
  private long m_maxAge;
  private byte[] m_signKey;

  @PostConstruct
  protected void init() {
    m_enabled = CONFIG.getPropertyValue(EnabledProperty.class);
    m_cookieName = CONFIG.getPropertyValue(NameProperty.class);
    m_maxAge = CONFIG.getPropertyValue(MaxAgeProperty.class);
    m_signKey = CONFIG.getPropertyValue(AuthTokenPrivateKeyProperty.class);
    if (m_enabled && m_signKey == null) {
      // don't enforce sign key if not enabled
      throw new PlatformException("Missing config.properties entry used for signing auth data: '{}'", BEANS.get(AuthTokenPrivateKeyProperty.class).getKey());
    }
  }

  /**
   * @return always false
   */
  @Override
  public boolean handle(HttpServletRequest req, HttpServletResponse resp, FilterChain chain) throws IOException, ServletException {
    if (!m_enabled) {
      return false;
    }

    //logout / cookie startup
    switch (getTarget(req)) {
      case "/login":
      case "/logout":
        clearPrincipalOnCookie(resp);
        return false;
    }

    final ServletFilterHelper helper = BEANS.get(ServletFilterHelper.class);
    Principal p = helper.getPrincipalOnSession(req);
    if (p == null) {
      p = loadPrincipalFromCookie(req);
      if (p != null) {
        helper.putPrincipalOnSession(req, p);
        return false;
      }
    }

    if (p != null) {
      storePrincipalToCookie(req, resp, p);
      return false;
    }

    return false;
  }

  @Override
  public void destroy() {
    // no resources to destroy
  }

  protected String getTarget(final HttpServletRequest request) {
    final String pathInfo = request.getPathInfo();
    if (pathInfo != null) {
      return pathInfo;
    }

    final String requestURI = request.getRequestURI();
    return requestURI.substring(requestURI.lastIndexOf('/'));
  }

  /**
   * @param value
   *          to be signed
   * @return signed value in the format <code>base64(signature):value</code>
   */
  protected String signValue(String value) {
    try {
      byte[] sig = SecurityUtility.createMac(m_signKey, value.getBytes(StandardCharsets.UTF_8));
      return Base64Utility.encode(sig) + ":" + value;
    }
    catch (Exception e) {
      throw new PlatformException("Failed signing value '{}'", value, e);
    }
  }

  /**
   * @param signedValue
   *          value in the format <code>base64(signature):value</code>
   * @return the verified value extracted from the signedValue
   */
  protected String verifyValue(String signedValue) {
    if (signedValue != null && signedValue.indexOf(':') > -1) {
      String value = signedValue.substring(signedValue.indexOf(':') + 1);
      if (signValue(value).equals(signedValue)) {
        return value;
      }
    }
    return null;
  }

  protected Principal loadPrincipalFromCookie(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies != null && cookies.length > 0) {
      for (Cookie c : cookies) {
        if (m_cookieName.equals(c.getName())) {
          String userId = verifyValue(c.getValue());
          if (userId != null) {
            LOG.info("Load signed cookie '{}' for '{}'", m_cookieName, userId);
            HttpSession session = request.getSession(false);
            if (session != null) {
              session.setAttribute(SESSION_ATTRIBUTE_COOKIE_SENT, Boolean.TRUE);
            }
            return new SimplePrincipal(userId);
          }
        }
      }
    }
    return null;
  }

  protected void storePrincipalToCookie(HttpServletRequest req, HttpServletResponse resp, Principal p) {
    HttpSession session = req.getSession(false);
    if (session == null) {
      return;
    }
    if (Boolean.TRUE.equals(session.getAttribute(SESSION_ATTRIBUTE_COOKIE_SENT))) {
      return;
    }
    session.setAttribute(SESSION_ATTRIBUTE_COOKIE_SENT, Boolean.TRUE);

    String signedValue = signValue(p.getName());
    LOG.info("Store signed cookie '{}' for '{}'", m_cookieName, p.getName());
    Cookie myCookie = new Cookie(m_cookieName, signedValue);
    myCookie.setMaxAge((int) m_maxAge);
    resp.addCookie(myCookie);
  }

  protected void clearPrincipalOnCookie(HttpServletResponse resp) {
    LOG.info("Remove cookie '{}'", m_cookieName);
    Cookie myCookie = new Cookie(m_cookieName, "");
    myCookie.setMaxAge(0);//delete it
    resp.addCookie(myCookie);
  }

  public static class EnabledProperty extends AbstractBooleanConfigProperty {
    @Override
    public String getKey() {
      return "scout.auth.cookieEnabled";
    }

    @Override
    public String description() {
      return String.format("Specifies if the '%s' is enabled.", CookieAccessController.class.getSimpleName());
    }

    @Override
    public Boolean getDefaultValue() {
      return false;
    }
  }

  public static class NameProperty extends AbstractStringConfigProperty {
    @Override
    public String getKey() {
      return "scout.auth.cookieName";
    }

    @Override
    @SuppressWarnings("findbugs:VA_FORMAT_STRING_USES_NEWLINE")
    public String description() {
      return String.format("If the '%s' is enabled, specifies the name for the cookie.\n"
          + "The name must conform to RFC 2109. However, vendors may provide a configuration option that allows cookie names conforming to the original Netscape Cookie Specification to be accepted.\n"
          + "By default 'sso.user.id' is used as cookie name.", CookieAccessController.class.getSimpleName());
    }

    @Override
    public String getDefaultValue() {
      return "sso.user.id";
    }
  }

  public static class MaxAgeProperty extends AbstractLongConfigProperty {
    @Override
    public String getKey() {
      return "scout.auth.cookieMaxAge";
    }

    @Override
    @SuppressWarnings("findbugs:VA_FORMAT_STRING_USES_NEWLINE")
    public String description() {
      return String.format("If the '%s' is enabled, specifies the maximum age in seconds for the cookie.\n"
          + "A positive value indicates that the cookie will expire after that many seconds have passed.\n"
          + "A negative value means that the cookie is not stored persistently and will be deleted when the Web browser exits. A zero value causes the cookie to be deleted.\n"
          + "The default value is 10 hours.", CookieAccessController.class.getSimpleName());
    }

    @Override
    public Long getDefaultValue() {
      //10 hours
      return 36000L;
    }
  }
}
