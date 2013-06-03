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

import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;

import org.eclipse.scout.commons.holders.fixture.SqlServiceMock;
import org.eclipse.scout.commons.holders.fixture.VerboseMock;
import org.eclipse.scout.rt.server.services.common.jdbc.style.OracleSqlStyle;
import org.junit.Test;

/**
 * Test for bind (see {@link NVPair}), requiring a server for database stuff
 */
public class BindValueTest {
  protected SqlServiceMock sqlService = new SqlServiceMock();

  public BindValueTest() {
    sqlService = new SqlServiceMock();
    sqlService.setSqlStyle(new OracleSqlStyle());
  }

  @Test
  public void testNullBindWithLong() throws Exception {
    sqlService.clearProtocol();

    //actual behaviour
    sqlService.select("SELECT A FROM T WHERE A = :a", new NVPair("a", null));
    String actual = sqlService.getProtocol().toString();

    //expected behaviour
    VerboseMock m = new VerboseMock(new StringBuffer());
    m.log(Connection.class, "prepareStatement", "SELECT A FROM T WHERE A = ?");
    m.log(PreparedStatement.class, "setNull", 1, Types.NULL);
    m.log(PreparedStatement.class, "executeQuery");
    m.log(ResultSet.class, "getFetchSize");
    m.log(ResultSet.class, "next");
    m.log(ResultSet.class, "close");
    String expected = m.getProtocol().toString();

    //check
    assertEquals(expected, actual);
  }

  @Test
  public void testNullBindWithLongAndNullType() throws Exception {
    sqlService.clearProtocol();

    //actual behaviour
    sqlService.select("SELECT A FROM T WHERE A = :a", new NVPair("a", null, Long.class));
    String actual = sqlService.getProtocol().toString();

    //expected behaviour
    VerboseMock m = new VerboseMock(new StringBuffer());
    m.log(Connection.class, "prepareStatement", "SELECT A FROM T WHERE A = ?");
    m.log(PreparedStatement.class, "setObject", 1, null, Types.BIGINT);
    m.log(PreparedStatement.class, "executeQuery");
    m.log(ResultSet.class, "getFetchSize");
    m.log(ResultSet.class, "next");
    m.log(ResultSet.class, "close");
    String expected = m.getProtocol().toString();

    //check
    assertEquals(expected, actual);
  }

  @Test
  public void testNullBindWithLongHolder() throws Exception {
    sqlService.clearProtocol();

    //actual behaviour
    sqlService.select("SELECT A FROM T WHERE A = :a", new NVPair("a", new LongHolder()));
    String actual = sqlService.getProtocol().toString();

    //expected behaviour
    VerboseMock m = new VerboseMock(new StringBuffer());
    m.log(Connection.class, "prepareStatement", "SELECT A FROM T WHERE A = ?");
    m.log(PreparedStatement.class, "setObject", 1, null, Types.BIGINT);
    m.log(PreparedStatement.class, "executeQuery");
    m.log(ResultSet.class, "getFetchSize");
    m.log(ResultSet.class, "next");
    m.log(ResultSet.class, "close");
    String expected = m.getProtocol().toString();

    //check
    assertEquals(expected, actual);
  }
}
