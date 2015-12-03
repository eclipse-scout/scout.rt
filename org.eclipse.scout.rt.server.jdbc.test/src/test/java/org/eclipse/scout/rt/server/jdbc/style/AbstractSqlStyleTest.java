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
package org.eclipse.scout.rt.server.jdbc.style;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

import org.eclipse.scout.rt.server.jdbc.SqlBind;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * JUnit tests for {@link AbstractSqlStyle}
 *
 * @since 3.9.0
 * @author awe, msc, kle
 */
public class AbstractSqlStyleTest {

  /**
   * SQL style for testing.
   */
  AbstractSqlStyle sql = new AbstractSqlStyle() {
    private static final long serialVersionUID = 1L;

    @Override
    public void testConnection(Connection conn) throws SQLException {
    }

    @Override
    public boolean isBlobEnabled() {
      return false;
    }

    @Override
    public boolean isClobEnabled() {
      return false;
    }

    @Override
    public boolean isLargeString(String s) {
      return false;
    }

    @Override
    protected int getMaxListSize() {
      return 0;
    }
  };

  /**
   * Test for {@link AbstractSqlStyle#writeBind} for {@link BigDecimal}
   *
   * @throws SQLException
   */
  @Test
  public void testWriteBind() throws SQLException {
    BigDecimal bd = new BigDecimal("9.123");
    SqlBind bind = new SqlBind(Types.DECIMAL, bd);
    PreparedStatement ps = Mockito.mock(PreparedStatement.class);
    sql.writeBind(ps, 1, bind);
    Mockito.verify(ps).setObject(1, bd, Types.DECIMAL, 3);
  }

  /**
   * Test for {@link AbstractSqlStyle#writeBind} for {@link BigDecimal}
   *
   * @throws SQLException
   */
  @Test
  public void testWriteBindNoScale() throws SQLException {
    BigDecimal bd = new BigDecimal("9");
    SqlBind bind = new SqlBind(Types.NUMERIC, bd);
    PreparedStatement ps = Mockito.mock(PreparedStatement.class);
    sql.writeBind(ps, 1, bind);
    Mockito.verify(ps).setObject(1, bd, Types.NUMERIC, 0);
  }

  /**
   * Test for {@link AbstractSqlStyle#writeBind} for null values with nulltype {@link Clob}
   *
   * @throws SQLException
   */
  @Test
  public void testWriteBindForNullClob() throws SQLException {
    PreparedStatement ps = Mockito.mock(PreparedStatement.class);
    SqlBind bind = new SqlBind(Types.CLOB, null);
    sql.writeBind(ps, 1, bind);
    Mockito.verify(ps).setClob(1, (Clob) null);
  }

  /**
   * Test for {@link AbstractSqlStyle#writeBind} for null values with nulltype {@link Blob}
   *
   * @throws SQLException
   */
  @Test
  public void testWriteBindForNullBlob() throws SQLException {
    PreparedStatement ps = Mockito.mock(PreparedStatement.class);
    SqlBind bind = new SqlBind(Types.BLOB, null);
    sql.writeBind(ps, 1, bind);
    Mockito.verify(ps).setBlob(1, (Blob) null);
  }

  /**
   * Test for {@link AbstractSqlStyle#writeBind} for null values with nulltype {@link Types.LONGVARBINARY}
   *
   * @throws SQLException
   */
  @Test
  public void testWriteBindForLongVarBinary() throws SQLException {
    PreparedStatement ps = Mockito.mock(PreparedStatement.class);
    SqlBind bind = new SqlBind(Types.LONGVARBINARY, null);
    sql.writeBind(ps, 1, bind);
    Mockito.verify(ps).setBytes(1, (byte[]) null);
  }

  /**
   * Test for {@link AbstractSqlStyle#writeBind} for null values with nulltype {@link LONGVARCHAR}
   *
   * @throws SQLException
   */
  @Test
  public void testWriteBindForNullLongVarchar() throws SQLException {
    verifySetNullCalledOnPS(Types.LONGVARCHAR);
  }

  /**
   * Test for {@link AbstractSqlStyle#writeBind} for null values with nulltype {@link LONGVARCHAR}
   *
   * @throws SQLException
   */
  @Test
  public void testWriteBindForNullNullType() throws SQLException {
    verifySetNullCalledOnPS(Types.NULL);
  }

  /**
   * Verifies that {@link PreparedStatement#setNull(int, int)} is called when invoking
   * {@link AbstractSqlStyle#writeBind(PreparedStatement, int, SqlBind)}
   *
   * @param nullType
   *          {@link Types} null type for preparedStatement
   * @throws SQLException
   */
  private void verifySetNullCalledOnPS(int nullType) throws SQLException {
    PreparedStatement ps = Mockito.mock(PreparedStatement.class);
    SqlBind bind = new SqlBind(nullType, null);
    sql.writeBind(ps, 1, bind);
    Mockito.verify(ps).setNull(1, nullType);
  }

  /**
   * Test for {@link AbstractSqlStyle#buildBindFor} for {@link Character} with nulltype {@link Character}.
   */
  @Test
  public void testBuildBindForCharacter() {
    Character c = Character.valueOf('x');
    SqlBind bin = sql.buildBindFor(c, Character.class);
    assertEquals(Types.VARCHAR, bin.getSqlType());
    assertTrue(bin.getValue() instanceof String);
  }

  /**
   * Test for {@link AbstractSqlStyle#buildBindFor} for null values with nulltype {@link Character}.
   */
  @Test
  public void testBuildBindForNullCharacter() {
    SqlBind bin = sql.buildBindFor(null, Character.class);
    assertEquals(Types.VARCHAR, bin.getSqlType());
    assertNull(bin.getValue());
  }

  /**
   * Test for {@link AbstractSqlStyle#buildBindFor} for {@link BigDecimal} with nulltype {@link BigDecimal}.
   */
  @Test
  public void testBuildBindForBigDecimal() {
    final int testValue = 100;
    final BigDecimal b = BigDecimal.valueOf(testValue);
    SqlBind bin = sql.buildBindFor(b, BigDecimal.class);
    assertEquals(Types.NUMERIC, bin.getSqlType());
    assertTrue(bin.getValue() instanceof BigDecimal);
  }

  /**
   * Test for {@link AbstractSqlStyle#buildBindFor} for null values with nulltype {@link BigDecimal}.
   */
  @Test
  public void testBuildBindForNullBigDecimal() {
    SqlBind bin = sql.buildBindFor(null, BigDecimal.class);
    assertEquals(Types.NUMERIC, bin.getSqlType());
    assertNull(bin.getValue());
  }

  @Test
  public void testNoDecimalConversion() throws Exception {
    ResultSet rs = Mockito.mock(ResultSet.class);
    ResultSetMetaData meta = Mockito.mock(ResultSetMetaData.class);
    int jdbcBindIndex = 0;
    SqlStyleDecimalConversionNone sqlDecimalConversion = new SqlStyleDecimalConversionNone();

    Mockito.when(rs.getBigDecimal(jdbcBindIndex)).thenReturn(BigDecimal.valueOf(123.456789d));
    Mockito.when(rs.getBigDecimal(jdbcBindIndex + 1)).thenReturn(BigDecimal.valueOf(987654L));
    Mockito.when(rs.wasNull()).thenReturn(false);

    Object o1 = sqlDecimalConversion.readBind(rs, meta, Types.DECIMAL, jdbcBindIndex);
    Object o2 = sqlDecimalConversion.readBind(rs, meta, Types.DECIMAL, jdbcBindIndex + 1);
    assertTrue(o1 instanceof BigDecimal);
    assertTrue(o2 instanceof BigDecimal);

    o1 = sqlDecimalConversion.readBind(rs, meta, Types.NUMERIC, jdbcBindIndex);
    o2 = sqlDecimalConversion.readBind(rs, meta, Types.NUMERIC, jdbcBindIndex + 1);
    assertTrue(o1 instanceof BigDecimal);
    assertTrue(o2 instanceof BigDecimal);
  }

  @Test
  public void testLegacyDecimalConversion() throws Exception {
    ResultSet rs = Mockito.mock(ResultSet.class);
    ResultSetMetaData meta = Mockito.mock(ResultSetMetaData.class);
    final int jdbcBindIndex = 0;
    SqlStyleDecimalConversionLegacy sqlDecimalConversion = new SqlStyleDecimalConversionLegacy();

    Mockito.when(rs.getBigDecimal(jdbcBindIndex)).thenReturn(BigDecimal.valueOf(123.456789d));
    Mockito.when(rs.getBigDecimal(jdbcBindIndex + 1)).thenReturn(BigDecimal.valueOf(987654L));
    Mockito.when(rs.wasNull()).thenReturn(false);

    Object o1 = sqlDecimalConversion.readBind(rs, meta, Types.DECIMAL, jdbcBindIndex);
    Object o2 = sqlDecimalConversion.readBind(rs, meta, Types.DECIMAL, jdbcBindIndex + 1);
    assertTrue(o1 instanceof Double);
    assertTrue(o2 instanceof Long);

    o1 = sqlDecimalConversion.readBind(rs, meta, Types.NUMERIC, jdbcBindIndex);
    o2 = sqlDecimalConversion.readBind(rs, meta, Types.NUMERIC, jdbcBindIndex + 1);
    assertTrue(o1 instanceof Double);
    assertTrue(o2 instanceof Long);
  }

  /**
   * SQL style decimal conversion with no conversion strategy
   */
  private static class SqlStyleDecimalConversionNone extends AbstractSqlStyle {
    private static final long serialVersionUID = 1L;

    @Override
    protected DecimalConversion getConfiguredDecimalConversionStrategy() {
      return DecimalConversion.NONE;
    }

    @Override
    public void testConnection(Connection conn) throws SQLException {
    }

    @Override
    public boolean isBlobEnabled() {
      return false;
    }

    @Override
    public boolean isClobEnabled() {
      return false;
    }

    @Override
    public boolean isLargeString(String s) {
      return false;
    }

    @Override
    protected int getMaxListSize() {
      return 0;
    }
  }

  /**
   * SQL style decimal conversion with legacy strategy
   */
  private static class SqlStyleDecimalConversionLegacy extends AbstractSqlStyle {
    private static final long serialVersionUID = 1L;

    @Override
    protected DecimalConversion getConfiguredDecimalConversionStrategy() {
      return DecimalConversion.LEGACY;
    }

    @Override
    public void testConnection(Connection conn) throws SQLException {
    }

    @Override
    public boolean isBlobEnabled() {
      return false;
    }

    @Override
    public boolean isClobEnabled() {
      return false;
    }

    @Override
    public boolean isLargeString(String s) {
      return false;
    }

    @Override
    protected int getMaxListSize() {
      return 0;
    }
  }
}
