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
package org.eclipse.scout.rt.ui.html;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.server.commons.servlet.filter.gzip.GzipServletFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper to manage url param hints for the {@link UiServlet}.
 * <p>
 * The following hints are supported:
 * <ul>
 * <li><b><code>?cache=(true|false)</code></b>: Enable/disable HTTP caching of resources.
 * <li><b><code>?compress=(true|false)</code></b>: Enable/disable GZIP compression (if client supports it).
 * <li><b><code>?minify=(true|false)</code></b>: Enable/disable "minification" of JS/CSS files.
 * <li><b><code>?debug=(true|false)</code></b>: Enable/disable all of the above flags.
 * <li><b><code>?inspector=(true|false)</code></b>: Enable/disable inspector attributes in DOM ("modelClass", "classId")
 * </ul>
 * All values are <code>true</code> by default, unless the application is run in development mode.
 */
public final class UiHints {
  private static final Logger LOG = LoggerFactory.getLogger(UiHints.class);

  /**
   * Enables/disables cache, compress, minify. Also decides if scoutClass attribute is added to the DOM for form-fields.
   */
  private static final String URL_PARAM_DEBUG = "debug";
  private static final String URL_PARAM_CACHE_HINT = "cache";
  private static final String URL_PARAM_MINIFY_HINT = "minify";
  private static final String URL_PARAM_INSPECTOR_HINT = "inspector";

  private static final String SESSION_ATTRIBUTE_CACHE_HINT = UiHints.class.getName() + "#cache";
  private static final String SESSION_ATTRIBUTE_MINIFY_HINT = UiHints.class.getName() + "#minify";
  private static final String SESSION_ATTRIBUTE_INSPECTOR_HINT = UiHints.class.getName() + "#inspector";

  private UiHints() {
    // static access only
  }

  public static void updateHints(HttpServletRequest req) {
    Boolean debug = getRequestParameterBoolean(req, URL_PARAM_DEBUG);
    if (debug != null) {
      updateHint(req, !debug.booleanValue(),
          SESSION_ATTRIBUTE_CACHE_HINT,
          GzipServletFilter.SESSION_ATTRIBUTE_COMPRESS_HINT,
          SESSION_ATTRIBUTE_MINIFY_HINT);
      updateHint(req, debug.booleanValue(), SESSION_ATTRIBUTE_INSPECTOR_HINT);
    }

    updateHint(req, getRequestParameterBoolean(req, URL_PARAM_INSPECTOR_HINT), SESSION_ATTRIBUTE_INSPECTOR_HINT);
    updateHint(req, getRequestParameterBoolean(req, URL_PARAM_CACHE_HINT), SESSION_ATTRIBUTE_CACHE_HINT);
    updateHint(req, getRequestParameterBoolean(req, GzipServletFilter.URL_PARAM_COMPRESS_HINT), GzipServletFilter.SESSION_ATTRIBUTE_COMPRESS_HINT);
    updateHint(req, getRequestParameterBoolean(req, URL_PARAM_MINIFY_HINT), SESSION_ATTRIBUTE_MINIFY_HINT);
  }

  private static void updateHint(HttpServletRequest req, Object value, String... sessionAttributeNameToStoreTo) {
    if (value == null) {
      return;
    }
    HttpSession session = req.getSession();
    for (String attName : sessionAttributeNameToStoreTo) {
      LOG.info("Set UiHint: {}={}", attName, value);
      session.setAttribute(attName, value);
    }
  }

  private static Boolean getRequestParameterBoolean(HttpServletRequest req, String name) {
    String s = req.getParameter(name);
    return s != null ? ("true".equals(s)) : null;
  }

  public static boolean isInspectorHint(HttpServletRequest req) {
    return calculateHint(req, SESSION_ATTRIBUTE_INSPECTOR_HINT, Platform.get().inDevelopmentMode());
  }

  public static boolean isCacheHint(HttpServletRequest req) {
    return calculateHint(req, SESSION_ATTRIBUTE_CACHE_HINT, !Platform.get().inDevelopmentMode());
  }

  public static boolean isMinifyHint(HttpServletRequest req) {
    return calculateHint(req, SESSION_ATTRIBUTE_MINIFY_HINT, !Platform.get().inDevelopmentMode());
  }

  private static boolean calculateHint(HttpServletRequest req, String sessionAttr, boolean defaultValue) {
    HttpSession session = req.getSession(false);
    if (session != null) {
      Boolean hint = (Boolean) session.getAttribute(sessionAttr);
      if (hint != null) {
        return hint.booleanValue();
      }
    }
    return defaultValue;
  }

}
