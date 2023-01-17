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

/**
 * Prepared statement input bind can be a batch bind can be a plain bind
 */
public class FunctionInputToken implements IToken {
  // sql text
  private final String m_parsedToken;
  private String m_replaceToken;
  private boolean m_plainValue;
  private boolean m_plainSql;
  private boolean m_plainToken;
  // bind info
  private final String m_name;
  private final String[] m_args;

  public FunctionInputToken(String parsedToken, String name, String[] args, boolean plainValue, boolean plainSql) {
    m_parsedToken = parsedToken;
    m_name = name;
    m_args = args;
    m_plainValue = plainValue;
    m_plainSql = plainSql;
  }

  @Override
  public boolean isInput() {
    return true;
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

  public String[] getArgs() {
    return m_args;
  }

  public boolean isPlainValue() {
    return m_plainValue;
  }

  public void setPlainValue(boolean b) {
    m_plainValue = b;
  }

  public boolean isPlainToken() {
    return m_plainToken;
  }

  public void setPlainToken(boolean b) {
    m_plainToken = b;
  }

  public boolean isPlainSql() {
    return m_plainSql;
  }

  public void setPlainSql(boolean b) {
    m_plainSql = b;
  }
}
