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
package org.eclipse.scout.rt.ui.html.json;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Session;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.ui.html.AbstractUiServletRequestHandler;
import org.eclipse.scout.rt.ui.html.HttpSessionHelper;
import org.eclipse.scout.rt.ui.html.ISessionStore;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles "/unload/<uiSessionId>" requests from browsers that support the Beacon API.
 *
 * @see Session.js
 * @see {@link https://developer.mozilla.org/en-US/docs/Web/API/Navigator/sendBeacon}
 * @since 6.1
 */
@Order(5040)
public class UnloadRequestHandler extends AbstractUiServletRequestHandler {
  private static final Logger LOG = LoggerFactory.getLogger(UnloadRequestHandler.class);

  public static final Pattern UNLOAD_PATH_PATTERN = Pattern.compile("^/unload/(.+)$");

  @Override
  public boolean handlePost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    final String pathInfo = req.getPathInfo();

    final Matcher matcher = UNLOAD_PATH_PATTERN.matcher(pathInfo);
    if (!matcher.matches()) {
      return false;
    }

    final String uiSessionId = matcher.group(1);
    handleUnloadRequest(req, resp, uiSessionId);
    return true;
  }

  protected void handleUnloadRequest(HttpServletRequest req, HttpServletResponse resp, String uiSessionId) {
    LOG.info("Unloading UI session with ID {} (requested by UI)", uiSessionId);

    final HttpSession httpSession = req.getSession();
    final ISessionStore sessionStore = BEANS.get(HttpSessionHelper.class).getSessionStore(httpSession);
    IUiSession uiSession = sessionStore.getUiSession(uiSessionId);

    if (uiSession != null) {
      final ReentrantLock uiSessionLock = uiSession.uiSessionLock();
      uiSessionLock.lock();
      try {
        uiSession.dispose();
      }
      finally {
        uiSessionLock.unlock();
      }
    }
  }
}
