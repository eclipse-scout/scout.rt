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
package org.eclipse.scout.rt.server.jdbc.parsers;

import org.eclipse.scout.rt.server.jdbc.parsers.token.ValueOutputToken;

public class IntoModel {
  String m_filteredStatement;
  private final ValueOutputToken[] m_intoTokens;

  public IntoModel(String filteredStatement, ValueOutputToken[] intoTokens) {
    m_filteredStatement = filteredStatement;
    m_intoTokens = intoTokens;
  }

  public ValueOutputToken[] getOutputTokens() {
    return m_intoTokens;
  }

  public String getFilteredStatement() {
    return m_filteredStatement;
  }
}
