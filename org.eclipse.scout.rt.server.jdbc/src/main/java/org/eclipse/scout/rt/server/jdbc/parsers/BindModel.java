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
package org.eclipse.scout.rt.server.jdbc.parsers;

import java.util.ArrayList;

import org.eclipse.scout.rt.server.jdbc.parsers.token.FunctionInputToken;
import org.eclipse.scout.rt.server.jdbc.parsers.token.IToken;
import org.eclipse.scout.rt.server.jdbc.parsers.token.ValueInputToken;
import org.eclipse.scout.rt.server.jdbc.parsers.token.ValueOutputToken;

public class BindModel {
  private final IToken[] m_allTokens;
  private final IToken[] m_ioTokens;

  public BindModel(IToken[] tokens) {
    m_allTokens = tokens;
    ArrayList<IToken> ioList = new ArrayList<>();
    for (IToken token : tokens) {
      if (token instanceof ValueInputToken) {
        ioList.add(token);
      }
      else if (token instanceof FunctionInputToken) {
        ioList.add(token);
      }
      else if (token instanceof ValueOutputToken) {
        ioList.add(token);
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
    StringBuilder b = new StringBuilder();
    for (IToken m_allToken : m_allTokens) {
      if (m_allToken instanceof ValueInputToken) {
        ValueInputToken valueInputToken = (ValueInputToken) m_allToken;
        if (valueInputToken.getParsedAttribute() != null) {
          b.append(valueInputToken.getParsedAttribute());
          b.append(" ");
        }
        if (valueInputToken.getParsedOp() != null) {
          b.append(valueInputToken.getParsedOp());
          b.append(" ");
        }
      }
      b.append(m_allToken.getReplaceToken());
    }
    return b.toString();
  }

}
