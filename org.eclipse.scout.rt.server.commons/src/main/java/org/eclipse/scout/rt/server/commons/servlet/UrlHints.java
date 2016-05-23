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
package org.eclipse.scout.rt.server.commons.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.eclipse.scout.rt.platform.IPlatform;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.server.commons.ServerCommonsConfigProperties;
import org.eclipse.scout.rt.server.commons.ServerCommonsConfigProperties.UrlHintsEnabledProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper to manage url param hints for servlets
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
 * <p>
 * <b>Security considerations</b>
 * <p>
 * cache, compress, minify have the default value <code>not({@link IPlatform#inDevelopmentMode()})</code>. The above URL
 * parameters can only be changed using URL parameters if the config property
 * {@link ServerCommonsConfigProperties.UrlHintsEnabledProperty} is set to true.
 */
public final class UrlHints {
  private static final Logger LOG = LoggerFactory.getLogger(UrlHints.class);

  private static final boolean UPDATE_ENABLED = CONFIG.getPropertyValue(UrlHintsEnabledProperty.class);

  /**
   * Enables/disables cache, compress, minify. Also decides if scoutClass attribute is added to the DOM for form-fields.
   */
  private static final String URL_PARAM_DEBUG = "debug";
  /**
   * Enables/disables caching.
   */
  private static final String URL_PARAM_CACHE_HINT = "cache";
  /**
   * Enables/disables gzip compression of js and css.
   */
  private static final String URL_PARAM_COMPRESS_HINT = "compress";
  /**
   * Enables/disables minify of js and css.
   */
  private static final String URL_PARAM_MINIFY_HINT = "minify";
  /**
   * Enables/disables js inspector.
   */
  private static final String URL_PARAM_INSPECTOR_HINT = "inspector";

  private static final String SESSION_ATTRIBUTE_CACHE_HINT = UrlHints.class.getName() + "#cache";
  private static final String SESSION_ATTRIBUTE_COMPRESS_HINT = UrlHints.class.getName() + "#compress";
  private static final String SESSION_ATTRIBUTE_MINIFY_HINT = UrlHints.class.getName() + "#minify";
  private static final String SESSION_ATTRIBUTE_INSPECTOR_HINT = UrlHints.class.getName() + "#inspector";

  private UrlHints() {
    // static access only
  }

  public static void updateHints(HttpServletRequest req) {
    if (!UPDATE_ENABLED) {
      return;
    }
    Boolean debug = getRequestParameterBoolean(req, URL_PARAM_DEBUG);
    if (debug != null) {
      updateHint(req, !debug.booleanValue(),
          SESSION_ATTRIBUTE_CACHE_HINT,
          UrlHints.SESSION_ATTRIBUTE_COMPRESS_HINT,
          SESSION_ATTRIBUTE_MINIFY_HINT);
      updateHint(req, debug.booleanValue(), SESSION_ATTRIBUTE_INSPECTOR_HINT);
    }

    updateHint(req, getRequestParameterBoolean(req, URL_PARAM_INSPECTOR_HINT), SESSION_ATTRIBUTE_INSPECTOR_HINT);
    updateHint(req, getRequestParameterBoolean(req, URL_PARAM_CACHE_HINT), SESSION_ATTRIBUTE_CACHE_HINT);
    updateHint(req, getRequestParameterBoolean(req, UrlHints.URL_PARAM_COMPRESS_HINT), UrlHints.SESSION_ATTRIBUTE_COMPRESS_HINT);
    updateHint(req, getRequestParameterBoolean(req, URL_PARAM_MINIFY_HINT), SESSION_ATTRIBUTE_MINIFY_HINT);
  }

  private static void updateHint(HttpServletRequest req, Object value, String... sessionAttributeNameToStoreTo) {
    if (value == null) {
      return;
    }
    HttpSession session = req.getSession(false);
    if (session == null) {
      return;
    }
    for (String attName : sessionAttributeNameToStoreTo) {
      LOG.info("Set UrlHint: {}={}", attName, value);
      session.setAttribute(attName, value);
    }
  }

  private static Boolean getRequestParameterBoolean(HttpServletRequest req, String name) {
    String s = req.getParameter(name);
    return s != null ? ("true".equals(s)) : null;
  }

  private static boolean calculateHint(HttpServletRequest req, String sessionAttr, boolean defaultValue) {
    if (req != null) {
      HttpSession session = req.getSession(false);
      if (session != null) {
        Boolean hint = (Boolean) session.getAttribute(sessionAttr);
        if (hint != null) {
          return hint.booleanValue();
        }
      }
    }
    return defaultValue;
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

  public static boolean isCompressHint(HttpServletRequest req) {
    return calculateHint(req, SESSION_ATTRIBUTE_COMPRESS_HINT, !Platform.get().inDevelopmentMode());
  }

}
