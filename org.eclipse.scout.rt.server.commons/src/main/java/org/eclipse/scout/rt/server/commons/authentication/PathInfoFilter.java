/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.commons.authentication;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import jakarta.servlet.http.HttpServletRequest;

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
    Set<String> patSet = new HashSet<>();
    if (patList != null) {
      for (String s : patList.split("[,\\s]")) {
        s = s.trim();
        if (!s.isEmpty()) {
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
