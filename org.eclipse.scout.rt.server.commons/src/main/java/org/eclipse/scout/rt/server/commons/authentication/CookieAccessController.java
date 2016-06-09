/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.server.commons.authentication;

import java.io.IOException;
import java.security.Principal;

import javax.annotation.PostConstruct;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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

/**
 * <h3>{@link CookieAccessController}</h3>
 * <p>
 * This access controller can be used for demo, testing and tutorial purposes.
 * <p>
 * It is not recommended for production unless secure cookies are enabled in the web.xml and the corporate policy allows
 * for cookie-based auto login.
 * <p>
 * It uses the config.properties {@link CookieNameProperty()}, {@link MaxAgeProperty()},
 * {@link AuthTokenPrivateKeyProperty} for signing the cookie
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
    if (m_signKey == null) {
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
   * @return signed value in the format <code>base64(signature):value</code>
   */
  protected String signValue(String value) {
    try {
      byte[] sig = SecurityUtility.createMac(m_signKey, value.getBytes("UTF-8"));
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
    if (signedValue != null && signedValue.indexOf(':') > 0) {
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
      return "scout.auth.cookie.enabled";
    }

    @Override
    protected Boolean getDefaultValue() {
      return false;
    }
  }

  public static class NameProperty extends AbstractStringConfigProperty {
    @Override
    public String getKey() {
      return "scout.auth.cookie.name";
    }

    @Override
    protected String getDefaultValue() {
      return "sso.user.id";
    }
  }

  public static class MaxAgeProperty extends AbstractLongConfigProperty {
    @Override
    public String getKey() {
      return "scout.auth.cookie.maxAge";
    }

    @Override
    protected Long getDefaultValue() {
      //10 hours
      return 36000L;
    }
  }

}
