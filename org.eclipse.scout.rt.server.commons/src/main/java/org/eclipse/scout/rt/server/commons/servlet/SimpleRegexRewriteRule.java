/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.commons.servlet;

public class SimpleRegexRewriteRule implements IRewriteRule {

  private final String m_regex;
  private final String m_replacement;

  public SimpleRegexRewriteRule(String regex, String replacement) {
    m_regex = regex;
    m_replacement = replacement;
  }

  public String getRegex() {
    return m_regex;
  }

  public String getReplacement() {
    return m_replacement;
  }

  @Override
  public String rewrite(String pathInfo) {
    return pathInfo != null ? pathInfo.replaceAll(m_regex, m_replacement) : null;
  }
}
