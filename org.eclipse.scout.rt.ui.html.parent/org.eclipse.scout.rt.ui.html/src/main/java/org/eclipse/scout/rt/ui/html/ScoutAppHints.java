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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

/**
 * Manager url param hints for the {@link AbstractScoutAppServlet}
 */
public final class ScoutAppHints {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ScoutAppHints.class);

  private static final String URL_PARAM_ALL_HINTS = "debug";//enables cache, compress, minify
  private static final String URL_PARAM_CACHE_HINT = "cache";
  private static final String URL_PARAM_COMPRESS_HINT = "compress";
  private static final String URL_PARAM_MINIFY_HINT = "minify";

  private static final String SESSION_ATTRIBUTE_CACHE_HINT = ScoutAppHints.class.getName() + "#cache";
  private static final String SESSION_ATTRIBUTE_COMPRESS_HINT = ScoutAppHints.class.getName() + "#compress";
  private static final String SESSION_ATTRIBUTE_MINIFY_HINT = ScoutAppHints.class.getName() + "#minify";

  public static void updateHints(HttpServletRequest req) {
    updateHint(req, URL_PARAM_ALL_HINTS, SESSION_ATTRIBUTE_CACHE_HINT, SESSION_ATTRIBUTE_COMPRESS_HINT, SESSION_ATTRIBUTE_MINIFY_HINT);
    updateHint(req, URL_PARAM_CACHE_HINT, SESSION_ATTRIBUTE_CACHE_HINT);
    updateHint(req, URL_PARAM_COMPRESS_HINT, SESSION_ATTRIBUTE_COMPRESS_HINT);
    updateHint(req, URL_PARAM_MINIFY_HINT, SESSION_ATTRIBUTE_MINIFY_HINT);
  }

  private static void updateHint(HttpServletRequest req, String urlParamNameToReadFrom, String... sessionAttributeNameToStoreTo) {
    String s = req.getParameter(urlParamNameToReadFrom);
    if (s == null) {
      return;
    }
    HttpSession session = req.getSession(false);
    if (session == null) {
      return;
    }
    boolean hint = ("true".equals(s));
    for (String attName : sessionAttributeNameToStoreTo) {
      session.setAttribute(attName, hint);
    }
  }

  public static boolean isCacheHint(HttpServletRequest req) {
    return calculateHint(req, SESSION_ATTRIBUTE_CACHE_HINT, !Platform.inDevelopmentMode());
  }

  public static boolean isCompressHint(HttpServletRequest req) {
    return calculateHint(req, SESSION_ATTRIBUTE_COMPRESS_HINT, !Platform.inDevelopmentMode());
  }

  public static boolean isMinifyHint(HttpServletRequest req) {
    return calculateHint(req, SESSION_ATTRIBUTE_MINIFY_HINT, !Platform.inDevelopmentMode());
  }

  private static boolean calculateHint(HttpServletRequest req, String sessionAttr, boolean defaultValue) {
    HttpSession session = req.getSession(false);
    if (session == null) {
      return defaultValue;
    }
    Boolean hint = (Boolean) session.getAttribute(sessionAttr);
    if (hint != null) {
      return hint.booleanValue();
    }
    return defaultValue;
  }

  private ScoutAppHints() {
  }
}
