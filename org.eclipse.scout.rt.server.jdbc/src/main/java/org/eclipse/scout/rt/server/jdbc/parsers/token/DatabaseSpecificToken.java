/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.jdbc.parsers.token;

public class DatabaseSpecificToken implements IToken {
  // sql text
  private final String m_parsedToken;
  private String m_replaceToken;
  // bind info
  private final String m_name;

  public DatabaseSpecificToken(String parsedToken, String name) {
    m_parsedToken = parsedToken;
    m_name = name;
  }

  @Override
  public boolean isInput() {
    return false;
  }

  @Override
  public boolean isOutput() {
    return false;
  }

  @Override
  public String getParsedToken() {
    return m_parsedToken;
  }

  @Override
  public String getReplaceToken() {
    if (m_replaceToken != null) {
      return m_replaceToken;
    }
    else {
      return m_parsedToken;
    }
  }

  @Override
  public void setReplaceToken(String s) {
    m_replaceToken = s;
  }

  public String getName() {
    return m_name;
  }
}
