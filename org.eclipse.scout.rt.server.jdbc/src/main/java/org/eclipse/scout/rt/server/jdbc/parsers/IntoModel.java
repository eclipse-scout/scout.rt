/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
