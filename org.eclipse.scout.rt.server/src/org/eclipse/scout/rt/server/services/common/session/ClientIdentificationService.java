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
package org.eclipse.scout.rt.server.services.common.session;

import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.server.commons.cache.IClientIdentificationService;

/**
 *
 */
public class ClientIdentificationService implements IClientIdentificationService {

  private final static String COOKIE_NAME = "scid"; // Scout Client Identifikator

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

        if (cookie.getName().equals(COOKIE_NAME)) {
          return cookie.getValue();
        }
      }
    }
    if (req.getAttribute(COOKIE_NAME) != null) {
      return (String) req.getAttribute(COOKIE_NAME);
    }
    return getNewSessionId(req, res);
  }

  /**
   * generates a new session id, set it as cookie and returns it
   * 
   * @param req
   *          HttpServletRequest
   * @param res
   *          HttpServletResponse
   * @return the new session id
   */
  private String getNewSessionId(HttpServletRequest req, HttpServletResponse res) {
    do {
      String newClientId = UUID.randomUUID().toString();
      Cookie cookie = new Cookie(COOKIE_NAME, newClientId);
      req.setAttribute(COOKIE_NAME, newClientId);
      res.addCookie(cookie);
      return newClientId;
    }
    while (true);
  }

}
