/*
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
    return pathInfo.replaceAll(m_regex, m_replacement);
  }
}
