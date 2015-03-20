/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.commons.cache;

import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Uses a cookie to identify clients.
 * 
 * @since 4.0.0
 */
public class ClientIdentificationService implements IClientIdentificationService {

  protected static final String SCOUT_CLIENT_ID_KEY = "scid";

  /**
   * returns the session id of the HTTP-Request. If no session id is set a new id will be generated and set.
   * 
   * @param req
   *          HttpServletRequest
   * @param res
   *          HttpServletResponse
   * @return the session id
   */
  @Override
  public String getClientId(HttpServletRequest req, HttpServletResponse res) {
    String existingId = findExistingId(req);
    if (existingId != null) {
      return existingId;
    }

    String newClientId = createNewId();
    setClientId(req, res, newClientId);
    return newClientId;
  }

  /**
   * Try to find existing id as a cookie or request attribute (on first request)
   */
  private String findExistingId(HttpServletRequest req) {
    Cookie[] cookies = req.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if (SCOUT_CLIENT_ID_KEY.equals(cookie.getName())) {
          return cookie.getValue();
        }
      }
    }
    Object clientIdRequestAttribute = req.getAttribute(SCOUT_CLIENT_ID_KEY);
    if (clientIdRequestAttribute instanceof String) {
      return (String) clientIdRequestAttribute;
    }
    return null;
  }

  /**
   * sets id as cookie to response and as attribute to the request for the first request
   * 
   * @param req
   *          HttpServletRequest
   * @param res
   *          HttpServletResponse
   */
  private void setClientId(HttpServletRequest req, HttpServletResponse res, String newClientId) {
    req.setAttribute(SCOUT_CLIENT_ID_KEY, newClientId);
    res.addCookie(new Cookie(SCOUT_CLIENT_ID_KEY, newClientId));
  }

  protected String createNewId() {
    return UUID.randomUUID().toString();
  }

}
