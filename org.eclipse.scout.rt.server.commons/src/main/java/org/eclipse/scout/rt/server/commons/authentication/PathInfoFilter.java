/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.commons.authentication;

import java.util.HashSet;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.scout.rt.platform.util.CollectionUtility;

/**
 * Filter used on {@link HttpServletRequest#getPathInfo()}
 *
 * @since 5.0
 */
public class PathInfoFilter {
  private final Pattern m_pattern;

  /**
   * @param simplePattern
   *          pattern list (with wildcard '*') comma, newline or whitespace separated
   */
  public PathInfoFilter(String simplePattern) {
    this(simplePatternListToRegex(simplePattern));
  }

  public PathInfoFilter(Pattern pattern) {
    m_pattern = pattern;
  }

  /**
   * comma separated list of simple patterns with *
   */
  public static Pattern simplePatternListToRegex(String patList) {
    HashSet<String> patSet = new HashSet<String>();
    if (patList != null) {
      for (String s : patList.split("[,\\s]")) {
        s = s.trim();
        if (s.length() > 0) {
          if (!s.startsWith("/")) {
            s = "/" + s;
          }
          s = simplePatternToRegex(s);
          if (s != null) {
            patSet.add(s);
          }
        }
      }
    }
    if (patSet.isEmpty()) {
      return null;
    }
    return Pattern.compile("(" + CollectionUtility.format(patSet, "|") + ")", Pattern.CASE_INSENSITIVE);
  }

  /**
   * pattern with *
   */
  public static String simplePatternToRegex(String s) {
    String r = Pattern.quote(s);//wrapped into \Q...\E
    // * -> \E.*\Q
    r = r.replace("*", "\\E.*\\Q");
    return r;
  }

  public boolean accepts(String pathInfo) {
    return pathInfo != null && m_pattern != null && m_pattern.matcher(pathInfo).matches();
  }

}
