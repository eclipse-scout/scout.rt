/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

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
@Order(5500)
public class UnloadRequestHandler extends AbstractUiServletRequestHandler {
  private static final Logger LOG = LoggerFactory.getLogger(UnloadRequestHandler.class);

  public static final Pattern UNLOAD_PATH_PATTERN = Pattern.compile("^/unload/(.+)$");

  @Override
  public boolean handlePost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
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
