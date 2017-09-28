/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
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
 * Select into result can be a batch
 */
public class ValueOutputToken implements IToken {
  private final String m_parsedToken;
  private String m_replaceToken;
  private String m_name;
  private boolean m_batch;
  private final boolean m_selectInto;

  public ValueOutputToken(String parsedToken, String name, boolean selectInto) {
    m_parsedToken = parsedToken;
    m_name = name;
    m_selectInto = selectInto;
    if (name.startsWith("{") && name.endsWith("}")) {
      m_name = m_name.substring(1, m_name.length() - 1);
      m_batch = true;
    }
  }

  @Override
  public boolean isInput() {
    return false;
  }

  @Override
  public boolean isOutput() {
    return true;
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

  public boolean isSelectInto() {
    return m_selectInto;
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
    b.append(m_name);
    b.append(" ");
    b.append("parsed '");
    b.append(getParsedToken());
    b.append("'");
    b.append(", replaced '");
    b.append(getReplaceToken());
    b.append("'");
    if (isBatch()) {
      b.append(" batch");
    }
    if (isSelectInto()) {
      b.append(" into");
    }
    b.append("]");
    return b.toString();
  }

}
