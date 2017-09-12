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

public class TextToken implements IToken {
  private final String m_parsedToken;

  public TextToken(String parsedToken) {
    m_parsedToken = parsedToken;
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
    return m_parsedToken;
  }

  @Override
  public void setReplaceToken(String s) {
    throw new IllegalArgumentException("Cannot replace content of a TextToken");
  }

  @Override
  public String toString() {
    return "TextToken[" + m_parsedToken + "]";
  }
}
