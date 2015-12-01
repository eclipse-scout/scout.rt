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
package org.eclipse.scout.rt.server.jdbc;

import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;

import org.eclipse.scout.rt.platform.holders.LongHolder;
import org.eclipse.scout.rt.platform.holders.NVPair;
import org.eclipse.scout.rt.server.TestJdbcServerSession;
import org.eclipse.scout.rt.server.jdbc.fixture.SqlServiceMock;
import org.eclipse.scout.rt.server.jdbc.fixture.VerboseMock;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.server.runner.RunWithServerSession;
import org.eclipse.scout.rt.testing.server.runner.ServerTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test for {@link ISqlService} (using the mock {@link SqlServiceMock}). Use {@link NVPair} to test different
 * configuration of binds.
 */
@RunWith(ServerTestRunner.class)
@RunWithServerSession(TestJdbcServerSession.class)
@RunWithSubject("default")
public class BindValueTest {
  private SqlServiceMock m_sqlService;

  @Before
  public void before() {
    m_sqlService = new SqlServiceMock();
  }

  @Test
  public void testNullBindWithLong() throws Exception {
    m_sqlService.clearProtocol();

    //actual behaviour
    m_sqlService.select("SELECT A FROM T WHERE A = :a", new NVPair("a", null));
    String actual = m_sqlService.getProtocol().toString();

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
    m_sqlService.clearProtocol();

    //actual behaviour
    m_sqlService.select("SELECT A FROM T WHERE A = :a", new NVPair("a", null, Long.class));
    String actual = m_sqlService.getProtocol().toString();

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
    m_sqlService.clearProtocol();

    //actual behaviour
    m_sqlService.select("SELECT A FROM T WHERE A = :a", new NVPair("a", new LongHolder()));
    String actual = m_sqlService.getProtocol().toString();

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
