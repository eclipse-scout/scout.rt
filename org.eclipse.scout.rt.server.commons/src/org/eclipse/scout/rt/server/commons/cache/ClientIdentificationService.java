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
 */
public class ClientIdentificationService implements IClientIdentificationService {

  private static final String SCOUT_CLIENT_ID = "scid";

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
    Cookie[] cookies = req.getCookies();
    if (cookies != null) {
      for (int i = 0; i < cookies.length; i++) {
        Cookie cookie = cookies[i];
        if (cookie.getName().equals(SCOUT_CLIENT_ID)) {
          return cookie.getValue();
        }
      }
    }
    if (req.getAttribute(SCOUT_CLIENT_ID) != null) {
      return (String) req.getAttribute(SCOUT_CLIENT_ID);
    }
    return createAndSetId(req, res);
  }

  /**
   * generates a new id, set it as cookie and returns it
   * 
   * @param req
   *          HttpServletRequest
   * @param res
   *          HttpServletResponse
   * @return the new session id
   */
  private String createAndSetId(HttpServletRequest req, HttpServletResponse res) {
    String newClientId = UUID.randomUUID().toString();
    Cookie cookie = new Cookie(SCOUT_CLIENT_ID, newClientId);
    req.setAttribute(SCOUT_CLIENT_ID, newClientId);
    res.addCookie(cookie);
    return newClientId;
  }

}
