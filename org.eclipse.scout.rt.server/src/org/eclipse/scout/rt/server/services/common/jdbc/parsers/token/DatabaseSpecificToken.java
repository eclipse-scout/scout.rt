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
package org.eclipse.scout.rt.server.services.common.jdbc.parsers.token;


public class DatabaseSpecificToken implements IToken {
  // sql text
  private String m_parsedToken;
  private String m_replaceToken;
  // bind info
  private String m_name;

  public DatabaseSpecificToken(String parsedToken, String name) {
    m_parsedToken = parsedToken;
    m_name = name;
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
    if (m_replaceToken != null) {
      return m_replaceToken;
    }
    else {
      return m_parsedToken;
    }
  }

  public void setReplaceToken(String s) {
    m_replaceToken = s;
  }

  public String getName() {
    return m_name;
  }

}
