/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.deeplink;

import java.util.regex.Matcher;

public class FooBarDeepLinkHandler extends AbstractDeepLinkHandler {

  public static final String HANDLER_NAME = "foobar";

  private String m_lastMatch;

  protected FooBarDeepLinkHandler() {
    super(defaultPattern(HANDLER_NAME, "[0-9]+"));
  }

  @Override
  public String getName() {
    return HANDLER_NAME;
  }

  @Override
  protected void handleImpl(Matcher matcher) throws DeepLinkException {
    m_lastMatch = matcher.group(1);
    if ("321".equals(m_lastMatch)) {
      throw new DeepLinkException();
    }
  }

  public String getLastMatch() {
    return m_lastMatch;
  }
}
