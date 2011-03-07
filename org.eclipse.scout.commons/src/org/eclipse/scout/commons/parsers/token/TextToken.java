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
package org.eclipse.scout.commons.parsers.token;

public class TextToken implements IToken {
  private String m_parsedToken;

  public TextToken(String parsedToken) {
    m_parsedToken = parsedToken;
  }

  public boolean isInput() {
    return false;
  }

  public boolean isOutput() {
    return false;
  }

  public String getParsedToken() {
    return m_parsedToken;
  }

  public String getReplaceToken() {
    return m_parsedToken;
  }

  public void setReplaceToken(String s) {
    throw new IllegalArgumentException("Cannot replace content of a TextToken");
  }

  @Override
  public String toString() {
    return "TextToken[" + m_parsedToken + "]";
  }
}
