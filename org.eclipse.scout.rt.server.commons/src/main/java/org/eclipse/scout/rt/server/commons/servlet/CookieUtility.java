/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.commons.servlet;

import java.util.concurrent.TimeUnit;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;

/**
 * @since 6.1
 */
public final class CookieUtility {

  private CookieUtility() {
    // static access only
  }

  /**
   * @return the {@link Cookie} associated with the given <code>cookieName</code> in the given <code>req</code>, or
   *         <code>null</code> if no such cookie exists.
   * @throws AssertionException
   *           if <code>req</code> is <code>null</code>
   */
  public static Cookie getCookieByName(HttpServletRequest req, String cookieName) {
    Assertions.assertNotNull(req, "Missing HTTP servlet request");
    Cookie[] cookies = req.getCookies();
    if (cookies == null || cookieName == null) {
      return null;
    }
    for (Cookie cookie : cookies) {
      if (cookieName.equals(cookie.getName())) {
        return cookie;
      }
    }
    return null;
  }

  /**
   * Adds a persistent cookie with a default <code>maxAge</code> of 30 days.
   */
  public static void addPersistentCookie(HttpServletResponse resp, String cookieName, String value) {
    addPersistentCookie(resp, cookieName, value, (int) TimeUnit.DAYS.toSeconds(30));
  }

  /**
   * Adds a persistent cookie with the given name and value, valid for maxAge seconds.
   */
  public static void addPersistentCookie(HttpServletResponse resp, String cookieName, String value, int maxAgeInSeconds) {
    Cookie cookie = new Cookie(cookieName, value);
    cookie.setMaxAge(maxAgeInSeconds);
    resp.addCookie(cookie);
  }

  /**
   * Same as {@link #addPersistentCookie(HttpServletResponse, String, String, int)}, but accepts a <code>long</code> as
   * <code>maxAgeInseconds</code>. Note that this value will be <b>cast to <code>int</code></b> without additional
   * checks!
   * <p>
   * This is a convenience method.
   */
  public static void addPersistentCookie(HttpServletResponse resp, String cookieName, String value, long maxAgeInSeconds) {
    addPersistentCookie(resp, cookieName, value, (int) maxAgeInSeconds);
  }

  /**
   * Adds a session (non-persistent) cookie to the given {@link HttpServletResponse}. The value <code>maxAge</code> is
   * set to <code>-1</code>.
   *
   * @see Cookie#setMaxAge(int)
   */
  public static void addSessionCookie(HttpServletResponse resp, String cookieName, String value) {
    Cookie cookie = new Cookie(cookieName, value);
    cookie.setMaxAge(-1); // "do not store" = session cookie
    resp.addCookie(cookie);
  }

  /**
   * Deletes, i.e. invalidates a cookie by setting the <code>maxAge</code> to <code>0</code>.
   *
   * @see Cookie#setMaxAge(int)
   */
  public static void deleteCookie(HttpServletResponse resp, String cookieName) {
    Cookie cookie = new Cookie(cookieName, "");
    cookie.setMaxAge(0); // delete it
    resp.addCookie(cookie);
  }
}
