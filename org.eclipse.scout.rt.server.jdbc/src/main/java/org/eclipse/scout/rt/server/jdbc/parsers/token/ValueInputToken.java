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
package org.eclipse.scout.rt.server.jdbc.parsers.token;

/**
 * Prepared statement input bind can be a batch bind can be a plain bind
 */
public class ValueInputToken implements IToken {
  /**
   * sql text
   */
  private final String m_parsedToken;
  /**
   * optional: the op left to the token
   */
  private String m_parsedOp;
  /**
   * optional: the attribute left to the op
   */
  private String m_parsedAttribute;
  private String m_replaceToken;
  // bind info
  private String m_name;
  private boolean m_plainValue;
  private boolean m_plainSql;
  private boolean m_batch;

  public ValueInputToken(String parsedToken, String name, boolean plainValue, boolean plainSql) {
    m_parsedToken = parsedToken;
    m_name = name;
    m_plainValue = plainValue;
    m_plainSql = plainSql;
    if (name.startsWith("{") && name.endsWith("}")) {
      m_name = m_name.substring(1, m_name.length() - 1);
      m_batch = true;
    }
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

  public String getParsedOp() {
    return m_parsedOp;
  }

  public void setParsedOp(String s) {
    m_parsedOp = s;
  }

  public String getParsedAttribute() {
    return m_parsedAttribute;
  }

  public void setParsedAttribute(String s) {
    m_parsedAttribute = s;
  }

  public String getName() {
    return m_name;
  }

  public boolean isPlainValue() {
    return m_plainValue;
  }

  public void setPlainValue(boolean b) {
    m_plainValue = b;
  }

  public boolean isPlainSql() {
    return m_plainSql;
  }

  public void setPlainSql(boolean b) {
    m_plainSql = b;
  }

  public boolean isBatch() {
    return m_batch;
  }

  public void setBatch(boolean b) {
    m_batch = b;
  }

  @Override
  public String toString() {
    StringBuilder b = new StringBuilder();
    b.append(getClass().getSimpleName()).append("[");
    b.append("parsed '");
    if (getParsedAttribute() != null) {
      b.append(getParsedAttribute());
      b.append(" ");
    }
    if (getParsedOp() != null) {
      b.append(getParsedOp());
      b.append(" ");
    }
    b.append(getParsedToken());
    b.append("'");
    b.append(", replaced '");
    if (getParsedAttribute() != null) {
      b.append(getParsedAttribute());
      b.append(" ");
    }
    if (getParsedOp() != null) {
      b.append(getParsedOp());
      b.append(" ");
    }
    b.append(getReplaceToken());
    b.append("'");
    if (isBatch()) {
      b.append(" batch");
    }
    if (isPlainSql()) {
      b.append(" plainSql");
    }
    if (isPlainValue()) {
      b.append(" plainValue");
    }
    b.append("]");
    return b.toString();
  }

}
