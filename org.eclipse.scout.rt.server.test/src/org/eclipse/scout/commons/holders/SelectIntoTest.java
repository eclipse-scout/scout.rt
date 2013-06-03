/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons.holders;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.holders.fixture.SqlServiceMock;
import org.eclipse.scout.rt.server.services.common.jdbc.style.OracleSqlStyle;
import org.junit.Before;
import org.junit.Test;

/**
 * test requiring a server for database stuff
 */
public class SelectIntoTest {

  private SqlServiceMock m_sqlService;

  @Before
  public void setup() throws Exception {
    m_sqlService = new SqlServiceMock();
    m_sqlService.setSqlStyle(new OracleSqlStyle());
  }

  @Test(expected = ProcessingException.class)
  public void testMissingOutputBind() throws Exception {
    m_sqlService.clearProtocol();
    m_sqlService.selectInto("SELECT A FROM T WHERE A = :a INTO :b", new NVPair("a", 1));
  }
}
