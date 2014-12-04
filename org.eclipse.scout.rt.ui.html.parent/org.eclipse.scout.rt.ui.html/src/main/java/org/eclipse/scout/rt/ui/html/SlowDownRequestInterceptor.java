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
package org.eclipse.scout.rt.ui.html;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.service.AbstractService;

@Priority(100)
public class SlowDownRequestInterceptor extends AbstractService implements IServletRequestInterceptor {
  private static final long SLOW_DOWN_DEFAULT = 1000;
  private static final String SLOW_DOWN_PARAM = "slowDown";
  private static final String SESSION_ATTR_ENABLED = SlowDownRequestInterceptor.class.getSimpleName() + ".enabled";
  private static final String SESSION_ATTR_TIME = SlowDownRequestInterceptor.class.getSimpleName() + ".time";
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SlowDownRequestInterceptor.class);

  @Override
  public boolean interceptPost(AbstractScoutAppServlet servlet, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    if (!isEnabledOnSession(req)) {
      return false;
    }

    Long slowDownDuration = getSlowDownDurationFromSession(req);
    if (slowDownDuration == null) {
      slowDownDuration = SLOW_DOWN_DEFAULT;
    }
    try {
      Thread.sleep(slowDownDuration);
    }
    catch (InterruptedException e) {
      LOG.error("", e);
    }

    return false;
  }

  @Override
  public boolean interceptGet(AbstractScoutAppServlet servlet, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    updateState(req);

    return false;
  }

  protected boolean isEnabledOnSession(HttpServletRequest req) {
    HttpSession session = req.getSession(false);
    Boolean active = (Boolean) session.getAttribute(SESSION_ATTR_ENABLED);
    if (active != null) {
      return active.booleanValue();
    }

    return false;
  }

  protected Long getSlowDownDurationFromSession(HttpServletRequest req) {
    HttpSession session = req.getSession(false);
    return (Long) session.getAttribute(SESSION_ATTR_TIME);
  }

  protected void enableSlowDownForSession(HttpServletRequest req, boolean enabled, Long slowDownDuration) {
    HttpSession session = req.getSession(true);
    if (session == null) {
      return;
    }

    if (enabled) {
      session.setAttribute(SESSION_ATTR_ENABLED, enabled);
      session.setAttribute(SESSION_ATTR_TIME, slowDownDuration);

      slowDownDuration = slowDownDuration != null ? slowDownDuration : SLOW_DOWN_DEFAULT;
      LOG.info("Slow down enabled for current session. Slow down duration: " + slowDownDuration);
    }
    else if (session.getAttribute(SESSION_ATTR_ENABLED) != null) {
      session.setAttribute(SESSION_ATTR_ENABLED, null);
      session.setAttribute(SESSION_ATTR_TIME, null);

      LOG.info("Slow down disabled for current session");
    }
  }

  protected void updateState(HttpServletRequest req) {
    Long slowDownDuration = null;
    boolean enabled = false;

    String value = req.getParameter(SLOW_DOWN_PARAM);
    if (value == null || value.trim().isEmpty()) {
      //Once enabled, slowDown must be disabled explicitly with slowDown=false.
      return;
    }

    if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
      enabled = Boolean.parseBoolean(value);
    }
    else {
      slowDownDuration = Long.parseLong(value);
      enabled = true;
    }
    enableSlowDownForSession(req, enabled, slowDownDuration);
  }
}
