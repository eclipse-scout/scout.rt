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

import static org.junit.Assert.assertEquals;

import org.eclipse.scout.rt.server.jdbc.parsers.token.IToken;
import org.eclipse.scout.rt.server.jdbc.parsers.token.ValueInputToken;
import org.junit.Test;

/**
 * Tests for {@link BindParser}
 */
public class BindParserTest {

  /**
   * Column starts with "IN"
   */
  @Test
  public void testInListAttribute() {
    String sql = "SELECT INT_COLUMN_ID FROM TABLE1 WHERE INT_COLUMN_ID != :refId";
    BindModel bindModel = new BindParser(sql).parse();
    IToken[] tokens = bindModel.getIOTokens();
    ValueInputToken tok = (ValueInputToken) tokens[0];
    assertEquals("INT_COLUMN_ID", tok.getParsedAttribute());
    assertEquals("!=", tok.getParsedOp());
    assertEquals(":refId", tok.getParsedToken());
    assertEquals("refId", tok.getName());
  }
}
