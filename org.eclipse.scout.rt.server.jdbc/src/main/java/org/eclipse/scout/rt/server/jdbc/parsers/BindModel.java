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
package org.eclipse.scout.rt.server.jdbc.parsers;

import java.util.ArrayList;

import org.eclipse.scout.rt.server.jdbc.parsers.token.FunctionInputToken;
import org.eclipse.scout.rt.server.jdbc.parsers.token.IToken;
import org.eclipse.scout.rt.server.jdbc.parsers.token.ValueInputToken;
import org.eclipse.scout.rt.server.jdbc.parsers.token.ValueOutputToken;

public class BindModel {
  private IToken[] m_allTokens;
  private IToken[] m_ioTokens;

  public BindModel(IToken[] tokens) {
    m_allTokens = tokens;
    ArrayList<IToken> ioList = new ArrayList<IToken>();
    for (int i = 0; i < tokens.length; i++) {
      if (tokens[i] instanceof ValueInputToken) {
        ioList.add(tokens[i]);
      }
      else if (tokens[i] instanceof FunctionInputToken) {
        ioList.add(tokens[i]);
      }
      else if (tokens[i] instanceof ValueOutputToken) {
        ioList.add(tokens[i]);
      }
    }
    m_ioTokens = ioList.toArray(new IToken[ioList.size()]);
  }

  public IToken[] getAllTokens() {
    return m_allTokens;
  }

  public IToken[] getIOTokens() {
    return m_ioTokens;
  }

  public String getFilteredStatement() {
    StringBuffer b = new StringBuffer();
    for (int i = 0; i < m_allTokens.length; i++) {
      if (m_allTokens[i] instanceof ValueInputToken) {
        ValueInputToken valueInputToken = (ValueInputToken) m_allTokens[i];
        if (valueInputToken.getParsedAttribute() != null) {
          b.append(valueInputToken.getParsedAttribute());
          b.append(" ");
        }
        if (valueInputToken.getParsedOp() != null) {
          b.append(valueInputToken.getParsedOp());
          b.append(" ");
        }
      }
      b.append(m_allTokens[i].getReplaceToken());
    }
    return b.toString();
  }

}
