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
package org.eclipse.scout.rt.server.jdbc.style;

import java.io.ByteArrayInputStream;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import org.eclipse.scout.rt.platform.holders.IHolder;
import org.eclipse.scout.rt.platform.util.IOUtility;
import org.eclipse.scout.rt.platform.util.TriState;
import org.eclipse.scout.rt.platform.util.TypeCastUtility;
import org.eclipse.scout.rt.server.jdbc.SqlBind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractSqlStyle implements ISqlStyle {
  private static final long serialVersionUID = 1L;
  private static final Logger LOG = LoggerFactory.getLogger(AbstractSqlStyle.class);

  @Override
  public String getConcatOp() {
    return "||";
  }

  @Override
  public String getLikeWildcard() {
    return "%";
  }

  @Override
  public String toLikePattern(Object value) {
    String s = (value != null ? value.toString() : "");
    s = s.replace("*", getLikeWildcard());
    return s;
  }

  @Override
  public String toPlainText(Object value) {
    if (value instanceof IHolder) {
      value = ((IHolder) value).getValue();
    }
    //
    if (value == null) {
      return "null";
    }
    else if (value instanceof Boolean) {
      Boolean b = (Boolean) value;
      return b ? "1" : "0";
    }
    else if (value instanceof TriState) {
      TriState t = (TriState) value;
      return "" + t.toString();
    }
    else if (value instanceof String) {
      String s = (String) value;
      if (s.length() > MAX_SQL_STRING_LENGTH) {
        s = s.substring(0, MAX_SQL_STRING_LENGTH);
        LOG.warn("toPlainText of a String with more than " + MAX_SQL_STRING_LENGTH + " characters failed; truncated to '" + s + "'");
        return "'" + s.replaceAll("'", "''") + "'";
      }
      return "'" + s.replaceAll("'", "''") + "'";
    }
    else if (value instanceof char[]) {
      if (((char[]) value).length > MAX_SQL_STRING_LENGTH) {
        String s = new String((char[]) value, 0, MAX_SQL_STRING_LENGTH);
        LOG.warn("toPlainText of a CLOB with more than " + MAX_SQL_STRING_LENGTH + " characters failed; truncated to '" + s + "'");
        return "'" + s.replaceAll("'", "''") + "'";
      }
      String s = new String((char[]) value);
      return "'" + s.replaceAll("'", "''") + "'";
    }
    else if (value instanceof byte[]) {
      LOG.warn("toPlainText of a BLOB failed; using NULL");
      return "NULL";
    }
    else if (value instanceof Date) {
      Date d = (Date) value;
      SimpleDateFormat fmt = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
      return "to_date('" + fmt.format(d) + "','dd.mm.yyyy hh24:mi:ss')";
    }
    else if (value instanceof Collection || value.getClass().isArray()) {
      Object array;
      if (value instanceof Collection) {
        array = ((Collection) value).toArray();
      }
      else {
        array = value;
      }
      int n = Array.getLength(array);
      StringBuffer buf = new StringBuffer();
      buf.append("(");
      if (n > 0) {
        for (int i = 0; i < n; i++) {
          if (i > 0) {
            buf.append(",");
          }
          buf.append(toPlainText(Array.get(array, i)));
        }
      }
      else {
        buf.append("-1");
      }
      buf.append(")");
      return buf.toString();
    }
    else {
      return value.toString();
    }
  }

  @Override
  public String toAggregationAvg(String attribute) {
    return "AVG(" + attribute + ")";
  }

  @Override
  public String toAggregationCount(String attribute) {
    return "COUNT(" + attribute + ")";
  }

  @Override
  public String toAggregationMax(String attribute) {
    return "MAX(" + attribute + ")";
  }

  @Override
  public String toAggregationMedian(String attribute) {
    return "MEDIAN(" + attribute + ")";
  }

  @Override
  public String toAggregationMin(String attribute) {
    return "MIN(" + attribute + ")";
  }

  @Override
  public String toAggregationSum(String attribute) {
    return "SUM(" + attribute + ")";
  }

  @Override
  public SqlBind buildBindFor(Object o, Class nullType) {
    if (o instanceof IHolder) {
      IHolder h = (IHolder) o;
      o = h.getValue();
      nullType = h.getHolderType();
    }
    //
    Class c;
    if (o != null) {
      c = o.getClass();
    }
    else {
      if (nullType != null && IHolder.class.isAssignableFrom(nullType)) {
        try {
          nullType = TypeCastUtility.getGenericsParameterClass(nullType, IHolder.class);
        }
        catch (RuntimeException t) {
          nullType = null;
        }
      }
      c = nullType;
    }
    //
    if (o == null && c == null) {
      return new SqlBind(Types.NULL, o);
    }
    //
    if (Timestamp.class.isAssignableFrom(c)) {
      return new SqlBind(Types.TIMESTAMP, o);
    }
    else if (java.sql.Date.class.isAssignableFrom(c)) {
      return new SqlBind(Types.DATE, o);
    }
    else if (Calendar.class.isAssignableFrom(c)) {
      if (o == null) {
        return new SqlBind(Types.TIMESTAMP, o);
      }
      else {
        return new SqlBind(Types.TIMESTAMP, new Timestamp(((Calendar) o).getTimeInMillis()));
      }
    }
    else if (java.util.Date.class.isAssignableFrom(c)) {
      if (o == null) {
        return new SqlBind(Types.TIMESTAMP, o);
      }
      else {
        return new SqlBind(Types.TIMESTAMP, new Timestamp(((Date) o).getTime()));
      }
    }
    else if (Double.class.isAssignableFrom(c)) {
      return new SqlBind(Types.DOUBLE, o);
    }
    else if (Float.class.isAssignableFrom(c)) {
      return new SqlBind(Types.FLOAT, o);
    }
    else if (Integer.class.isAssignableFrom(c)) {
      return new SqlBind(Types.INTEGER, o);
    }
    else if (Long.class.isAssignableFrom(c)) {
      return new SqlBind(Types.BIGINT, o);
    }
    else if (BigInteger.class.isAssignableFrom(c)) {
      return new SqlBind(Types.BIGINT, o);
    }
    else if (BigDecimal.class.isAssignableFrom(c)) {
      return new SqlBind(Types.NUMERIC, o);
    }
    else if (Character.class.isAssignableFrom(c)) {
      if (o == null) {
        return new SqlBind(Types.VARCHAR, o);
      }
      else {
        return new SqlBind(Types.VARCHAR, o.toString());
      }
    }
    else if (String.class.isAssignableFrom(c) || char[].class == c) {
      if (o == null) {
        return new SqlBind(Types.VARCHAR, o);
      }
      else {
        String s;
        boolean large;
        if (String.class.isAssignableFrom(c)) {
          s = (String) o;
          large = isLargeString(s);
        }
        else {
          // char[] binds are always treated as lobs
          s = new String((char[]) o);
          large = true;
        }
        if (large) {
          if (isClobEnabled()) {
            return new SqlBind(Types.CLOB, s);
          }
          else {
            return new SqlBind(Types.LONGVARCHAR, s);
          }
        }
        else {
          return new SqlBind(Types.VARCHAR, s);
        }
      }
    }
    else if (byte[].class == c) {
      if (o == null) {
        if (isBlobEnabled()) {
          return new SqlBind(Types.BLOB, o);
        }
        else {
          return new SqlBind(Types.LONGVARBINARY, o);
        }
      }
      else {
        if (isBlobEnabled()) {
          return new SqlBind(Types.BLOB, o);
        }
        else {
          return new SqlBind(Types.LONGVARBINARY, o);
        }
      }
    }
    else if (Boolean.class.isAssignableFrom(c)) {
      if (o == null) {
        return new SqlBind(Types.INTEGER, null);
      }
      else {
        return new SqlBind(Types.INTEGER, ((Boolean) o).booleanValue() ? 1 : 0);
      }
    }
    else if (TriState.class.isAssignableFrom(c)) {
      if (o == null) {
        return new SqlBind(Types.INTEGER, null);
      }
      else {
        return new SqlBind(Types.INTEGER, ((TriState) o).getIntegerValue());
      }
    }
    else if (Blob.class.isAssignableFrom(c)) {
      return new SqlBind(Types.BLOB, o);
    }
    else if (Clob.class.isAssignableFrom(c)) {
      return new SqlBind(Types.CLOB, o);
    }
    else if (c != null && c.isArray()) {
      return new SqlBind(Types.ARRAY, o);
    }
    else {
      if (o == null) {
        return new SqlBind(Types.VARCHAR, o);
      }
      else {
        throw new IllegalArgumentException("no SqlBind mapping for " + c);
      }
    }
  }

  @Override
  public void writeBind(PreparedStatement ps, int jdbcBindIndex, SqlBind bind) throws SQLException {
    switch (bind.getSqlType()) {
      case Types.NULL: {
        try {
          ps.setNull(jdbcBindIndex, Types.NULL);

        }
        catch (SQLException e) {
          ps.setNull(jdbcBindIndex, Types.VARCHAR);
        }
        break;
      }
      case Types.CLOB: {
        if (bind.getValue() instanceof Clob || bind.getValue() == null) {
          ps.setClob(jdbcBindIndex, (Clob) bind.getValue());
        }
        else {
          String s = (String) bind.getValue();
          ps.setCharacterStream(jdbcBindIndex, new StringReader(s), s.length());
        }
        break;
      }
      case Types.BLOB: {
        if (bind.getValue() instanceof Blob || bind.getValue() == null) {
          ps.setBlob(jdbcBindIndex, (Blob) bind.getValue());
        }
        else {
          byte[] data = (byte[]) bind.getValue();
          ps.setBinaryStream(jdbcBindIndex, new ByteArrayInputStream(data), data.length);
        }
        break;
      }
      case Types.LONGVARCHAR: {
        String s = (String) bind.getValue();
        if (s != null) {
          ps.setCharacterStream(jdbcBindIndex, new StringReader(s), s.length());
        }
        else {
          ps.setNull(jdbcBindIndex, Types.LONGVARCHAR);
        }
        break;
      }
      case Types.LONGVARBINARY: {
        byte[] data = (byte[]) bind.getValue();
        try {
          // try using setBytes()
          ps.setBytes(jdbcBindIndex, data);
        }
        catch (SQLException e1) {
          try {
            // try using byte stream
            ps.setBinaryStream(jdbcBindIndex, new ByteArrayInputStream(data), data.length);
          }
          catch (SQLException e2) {
            // send original exception
            if (e1 instanceof SQLException) {
              throw (SQLException) e1;
            }
            else {
              throw new SQLException("failed setting bytes on jdbcBindIndex " + jdbcBindIndex);
            }
          }
        }
        break;
      }
      case Types.DECIMAL:
      case Types.NUMERIC: {
        if (bind.getValue() instanceof BigDecimal) {
          int scale = ((BigDecimal) bind.getValue()).scale();
          ps.setObject(jdbcBindIndex, bind.getValue(), bind.getSqlType(), scale);
        }
        else {
          writeDefaultBind(ps, jdbcBindIndex, bind);
        }
        break;
      }
      default: {
        writeDefaultBind(ps, jdbcBindIndex, bind);
      }
    }
  }

  private void writeDefaultBind(PreparedStatement ps, int jdbcBindIndex, SqlBind bind) throws SQLException {
    ps.setObject(jdbcBindIndex, bind.getValue(), bind.getSqlType());
  }

  @Override
  public Object readBind(ResultSet rs, ResultSetMetaData meta, int type, int jdbcBindIndex) throws SQLException {
    Object o = null;
    switch (type) {
      // General Number
      case Types.DECIMAL:
      case Types.NUMERIC: {
        o = getConfiguredDecimalConversionStrategy().convertDecimalType(rs.getBigDecimal(jdbcBindIndex));
        break;
      }
        // Long
      case Types.BIT:
      case Types.BIGINT:
      case Types.SMALLINT:
      case Types.INTEGER:
      case Types.TINYINT: {
        o = new Long(rs.getLong(jdbcBindIndex));
        break;
      }
        // Double
      case Types.DOUBLE:
      case Types.FLOAT:
      case Types.REAL: {
        o = new Double(rs.getDouble(jdbcBindIndex));
        break;
      }
        // String
      case Types.VARCHAR:
      case Types.CHAR: {
        o = rs.getString(jdbcBindIndex);
        break;
      }
        // Date
      case Types.DATE: {
        // Build 154: changed from getDate to getTimestamp()
        // o=result.getDate(i+1);
        o = rs.getTimestamp(jdbcBindIndex);
        break;
      }
      case Types.TIME: {
        o = rs.getTime(jdbcBindIndex);
        break;
      }
      case Types.TIMESTAMP: {
        o = rs.getTimestamp(jdbcBindIndex);
        break;
      }
        // Raw
      case Types.LONGVARCHAR: {
        try {
          o = rs.getString(jdbcBindIndex);
        }
        catch (SQLException e) {
          throw e;
        }
        break;
      }
      case Types.LONGVARBINARY:
      case Types.VARBINARY:
      case Types.BINARY: {
        o = rs.getBytes(jdbcBindIndex);
        break;
      }
      case Types.CLOB: {
        Clob c = rs.getClob(jdbcBindIndex);
        if (c == null) {
          o = null;
        }
        else {
          try {
            int len = (int) c.length();
            if (len > 0) {
              // fast read
              char[] ch = new char[len];
              Reader r = c.getCharacterStream();
              int processed = 0;
              while (processed < len) {
                processed += r.read(ch, processed, len - processed);
              }
              o = new String(ch);
            }
            else {
              // dynamic read
              o = IOUtility.getContent(c.getCharacterStream());
            }
          }
          catch (SQLException e) {
            throw e;
          }
          catch (Exception e) {
            SQLException sqe = new SQLException("read CLOB on column0=" + jdbcBindIndex);
            sqe.initCause(e);
            throw sqe;
          }
        }
        break;
      }
      case Types.BLOB: {
        Blob b = rs.getBlob(jdbcBindIndex);
        if (b == null) {
          o = null;
        }
        else {
          o = b.getBytes(1, (int) b.length());
        }
        break;
      }
      default: {
        o = rs.getObject(jdbcBindIndex);
      }
    }
    // set to null if necessary
    if (rs.wasNull()) {
      o = null;
    }
    return o;
  }

  @Override
  public void registerOutput(CallableStatement cs, int index, Class c) throws SQLException {
    if (c == null) {
      throw new SQLException("registering output index " + index + " with type null");
    }
    int jdbcType = getJdbcType(c);
    cs.registerOutParameter(index, jdbcType);
  }

  protected int getJdbcType(Class c) {
    int jdbcType;
    if (Timestamp.class.isAssignableFrom(c)) {
      jdbcType = Types.TIMESTAMP;
    }
    else if (java.sql.Date.class.isAssignableFrom(c)) {
      jdbcType = Types.DATE;
    }
    else if (Calendar.class.isAssignableFrom(c)) {
      jdbcType = Types.TIMESTAMP;
    }
    else if (java.util.Date.class.isAssignableFrom(c)) {
      jdbcType = Types.TIMESTAMP;
    }
    else if (Double.class.isAssignableFrom(c)) {
      jdbcType = Types.DOUBLE;
    }
    else if (Float.class.isAssignableFrom(c)) {
      jdbcType = Types.FLOAT;
    }
    else if (Integer.class.isAssignableFrom(c)) {
      jdbcType = Types.INTEGER;
    }
    else if (Long.class.isAssignableFrom(c)) {
      jdbcType = Types.BIGINT;
    }
    else if (BigInteger.class.isAssignableFrom(c)) {
      jdbcType = Types.BIGINT;
    }
    else if (BigDecimal.class.isAssignableFrom(c)) {
      jdbcType = Types.NUMERIC;
    }
    else if (String.class.isAssignableFrom(c) || char[].class == c) {
      jdbcType = Types.VARCHAR;
    }
    else if (byte[].class == c) {
      if (isBlobEnabled()) {
        jdbcType = Types.BLOB;
      }
      else {
        jdbcType = Types.LONGVARBINARY;
      }
    }
    else if (Boolean.class.isAssignableFrom(c)) {
      jdbcType = Types.INTEGER;
    }
    else if (TriState.class.isAssignableFrom(c)) {
      jdbcType = Types.INTEGER;
    }
    else if (Blob.class.isAssignableFrom(c)) {
      jdbcType = Types.BLOB;
    }
    else if (Clob.class.isAssignableFrom(c)) {
      jdbcType = Types.CLOB;
    }
    else if (c.isArray()) {
      jdbcType = Types.ARRAY;
    }
    else {
      jdbcType = Types.NUMERIC;
    }
    return jdbcType;
  }

  @Override
  public String createBetween(String attribute, String bindName1, String bindName2) {
    return attribute + " BETWEEN " + adaptBindName(bindName1) + " AND " + adaptBindName(bindName2);
  }

  @Override
  public String createDateBetween(String attribute, String bindName1, String bindName2) {
    return attribute + " BETWEEN TRUNC(" + adaptBindNameTimeDateOp(bindName1) + ") AND (TRUNC(" + adaptBindNameTimeDateOp(bindName2) + ")+(86399/86400)) ";
  }

  @Override
  public String createDateTimeBetween(String attribute, String bindName1, String bindName2) {
    return attribute + " BETWEEN TRUNC(" + adaptBindNameTimeDateOp(bindName1) + ",'MI') AND (TRUNC(" + adaptBindNameTimeDateOp(bindName2) + ",'MI')+(59/86400)) ";
  }

  @Override
  public String createStartsWith(String attribute, String bindName) {
    return "upper(" + attribute + ") like upper(" + adaptBindName(bindName) + "||'%')";
  }

  @Override
  public String createNotStartsWith(String attribute, String bindName) {
    return "upper(" + attribute + ") not like upper(" + adaptBindName(bindName) + "||'%')";
  }

  @Override
  public String createEndsWith(String attribute, String bindName) {
    return "upper(" + attribute + ") like upper('%'||" + adaptBindName(bindName) + ")";
  }

  @Override
  public String createNotEndsWith(String attribute, String bindName) {
    return "upper(" + attribute + ") not like upper('%'||" + adaptBindName(bindName) + ")";
  }

  @Override
  public String createContains(String attribute, String bindName) {
    return "upper(" + attribute + ") like upper('%'||" + adaptBindName(bindName) + "||'%')";
  }

  @Override
  public String createNotContains(String attribute, String bindName) {
    return "upper(" + attribute + ") not like upper('%'||" + adaptBindName(bindName) + "||'%')";
  }

  @Override
  public String createLike(String attribute, String bindName) {
    return "upper(" + attribute + ") like upper(" + adaptBindName(bindName) + ")";
  }

  @Override
  public String createNotLike(String attribute, String bindName) {
    return "upper(" + attribute + ") not like upper(" + adaptBindName(bindName) + ")";
  }

  @Override
  public String createNull(String attribute) {
    return attribute + " is null";
  }

  @Override
  public String createNotNull(String attribute) {
    return attribute + " is not null";
  }

  @Override
  public String createNumberNull(String attribute) {
    return "nvl(" + attribute + ",0)=0";
  }

  @Override
  public String createNumberNotNull(String attribute) {
    return "nvl(" + attribute + ",0)<>0";
  }

  @Override
  public String createTextNull(String attribute) {
    return "nvl(" + attribute + ",'0')='0'";
  }

  @Override
  public String createTextNotNull(String attribute) {
    return "nvl(" + attribute + ",'0')<>'0'";
  }

  @Override
  public String createIn(String attribute, String bindName) {
    return attribute + "=" + adaptBindName(bindName);
  }

  @Override
  public String createNotIn(String attribute, String bindName) {
    return "NOT(" + attribute + "=" + adaptBindName(bindName) + ")";
  }

  @Override
  public String createDateIsToday(String attribute) {
    return attribute + ">=TRUNC(SYSDATE) AND " + attribute + "<TRUNC(SYSDATE+1)";
  }

  @Override
  public String createDateIsInLastDays(String attribute, String bindName) {
    return attribute + ">=TRUNC(SYSDATE-(" + adaptBindNameTimeDateOp(bindName) + ")) AND " + attribute + "<TRUNC(SYSDATE+1)";
  }

  @Override
  public String createDateIsInNextDays(String attribute, String bindName) {
    return attribute + ">=TRUNC(SYSDATE) AND " + attribute + "<TRUNC(SYSDATE+" + adaptBindNameTimeDateOp(bindName) + "+1)";
  }

  @Override
  public String createDateIsInDays(String attribute, String bindName) {
    return attribute + ">=TRUNC(SYSDATE+" + adaptBindNameTimeDateOp(bindName) + ") AND " + attribute + "<TRUNC(SYSDATE+" + adaptBindNameTimeDateOp(bindName) + "+1)";
  }

  @Override
  public String createDateIsInWeeks(String attribute, String bindName) {
    return attribute + ">=TRUNC(SYSDATE+((" + adaptBindNameTimeDateOp(bindName) + ")*7)) AND " + attribute + "<TRUNC(SYSDATE+((" + adaptBindNameTimeDateOp(bindName) + ")*7)+1)";
  }

  @Override
  public String createDateIsInLastMonths(String attribute, String bindName) {
    return attribute + ">=TRUNC(ADD_MONTHS(SYSDATE,(-1)*(" + adaptBindNameTimeDateOp(bindName) + "))) AND " + attribute + "<TRUNC(SYSDATE+1)";
  }

  @Override
  public String createDateIsInNextMonths(String attribute, String bindName) {
    return attribute + ">=TRUNC(SYSDATE) AND " + attribute + "<TRUNC(ADD_MONTHS(SYSDATE," + adaptBindNameTimeDateOp(bindName) + ")+1)";
  }

  @Override
  public String createDateIsInMonths(String attribute, String bindName) {
    return attribute + ">=TRUNC(ADD_MONTHS(SYSDATE," + adaptBindNameTimeDateOp(bindName) + ")) AND " + attribute + "<TRUNC(ADD_MONTHS(SYSDATE," + adaptBindNameTimeDateOp(bindName) + ")+1)";
  }

  @Override
  public String createDateIsInLEDays(String attribute, String bindName) {
    return attribute + "<TRUNC(SYSDATE+" + adaptBindNameTimeDateOp(bindName) + "+1)";
  }

  @Override
  public String createDateIsInLEWeeks(String attribute, String bindName) {
    return attribute + "<TRUNC(SYSDATE+((" + adaptBindNameTimeDateOp(bindName) + ")*7)+1)";
  }

  @Override
  public String createDateIsInLEMonths(String attribute, String bindName) {
    return attribute + "<TRUNC(ADD_MONTHS(SYSDATE," + adaptBindNameTimeDateOp(bindName) + ")+1)";
  }

  @Override
  public String createDateIsInGEDays(String attribute, String bindName) {
    return attribute + ">=TRUNC(SYSDATE+" + adaptBindNameTimeDateOp(bindName) + ")";
  }

  @Override
  public String createDateIsInGEWeeks(String attribute, String bindName) {
    return attribute + ">=TRUNC(SYSDATE+((" + adaptBindNameTimeDateOp(bindName) + ")*7))";
  }

  @Override
  public String createDateIsInGEMonths(String attribute, String bindName) {
    return attribute + ">=TRUNC(ADD_MONTHS(SYSDATE," + adaptBindNameTimeDateOp(bindName) + "))";
  }

  @Override
  public String createDateIsNotToday(String attribute) {
    return "(" + attribute + "<TRUNC(SYSDATE) OR " + attribute + ">=TRUNC(SYSDATE+1))";
  }

  @Override
  public String createDateTimeIsNow(String attribute) {
    return "(" + attribute + ">=TRUNC(SYSDATE, 'MI') AND " + attribute + "<(TRUNC(SYSDATE, 'MI')+(1/24/60)))";
  }

  @Override
  public String createDateTimeIsInLEMinutes(String attribute, String bindName) {
    return attribute + "<(TRUNC(SYSDATE, 'MI')+((" + adaptBindNameTimeDateOp(bindName) + "+1)/24/60))";
  }

  @Override
  public String createDateTimeIsInLEHours(String attribute, String bindName) {
    return attribute + "<(TRUNC(SYSDATE, 'MI')+((1/24/60)+(" + adaptBindNameTimeDateOp(bindName) + "/24)))";
  }

  @Override
  public String createDateTimeIsInGEMinutes(String attribute, String bindName) {
    return attribute + ">=(TRUNC(SYSDATE, 'MI')+(" + adaptBindNameTimeDateOp(bindName) + "/24/60))";
  }

  @Override
  public String createDateTimeIsInGEHours(String attribute, String bindName) {
    return attribute + ">=(TRUNC(SYSDATE, 'MI')+(" + adaptBindNameTimeDateOp(bindName) + "/24))";
  }

  @Override
  public String createDateTimeIsNotNow(String attribute) {
    return "(" + attribute + "<TRUNC(SYSDATE, 'MI') OR " + attribute + ">=(TRUNC(SYSDATE, 'MI')+(1/24/60)))";
  }

  @Override
  public String createTimeIsNow(String attribute) {
    return attribute + ">=((TO_CHAR(SYSDATE,'HH24')*60) + TO_CHAR(SYSDATE,'MI'))/24/60 AND " + attribute + "<((TO_CHAR(SYSDATE,'HH24')*60)+TO_CHAR(SYSDATE,'MI')+(1/24/60))/24/60";
  }

  @Override
  public String createTimeIsNotNow(String attribute) {
    return attribute + "<((TO_CHAR(SYSDATE,'HH24')*60) + TO_CHAR(SYSDATE,'MI'))/24/60 OR " + attribute + ">((TO_CHAR(SYSDATE,'HH24')*60)+TO_CHAR(SYSDATE,'MI')+(1/24/60))/24/60";
  }

  @Override
  public String createTimeIsInMinutes(String attribute, String bindName) {
    return attribute + ">=((TO_CHAR(SYSDATE,'HH24')*60) + TO_CHAR(SYSDATE,'MI')+(" + adaptBindNameTimeDateOp(bindName) + "/24/60))/24/60 AND " + attribute + "<((TO_CHAR(SYSDATE,'HH24')*60)+TO_CHAR(SYSDATE,'MI')+(("
        + adaptBindNameTimeDateOp(bindName) + "+1)/24/60))/24/60";
  }

  @Override
  public String createTimeIsInHours(String attribute, String bindName) {
    return attribute + ">=((TO_CHAR(SYSDATE,'HH24')*60) + TO_CHAR(SYSDATE,'MI')+(" + adaptBindNameTimeDateOp(bindName) + "/24))/24/60 AND " + attribute + "<((TO_CHAR(SYSDATE,'HH24')*60)+TO_CHAR(SYSDATE,'MI')+("
        + adaptBindNameTimeDateOp(bindName) + "/24)+(1/24/60))/24/60";
  }

  @Override
  public String createTimeIsInLEMinutes(String attribute, String bindName) {
    return attribute + "<((TO_CHAR(SYSDATE,'HH24')*60) + TO_CHAR(SYSDATE,'MI')+((" + adaptBindNameTimeDateOp(bindName) + "+1)/24/60))/24/60";
  }

  @Override
  public String createTimeIsInLEHours(String attribute, String bindName) {
    return attribute + "<((TO_CHAR(SYSDATE,'HH24')*60) + TO_CHAR(SYSDATE,'MI')+(" + adaptBindNameTimeDateOp(bindName) + "/24)+(1/24/60))/24/60";
  }

  @Override
  public String createTimeIsInGEMinutes(String attribute, String bindName) {
    return attribute + ">=((TO_CHAR(SYSDATE,'HH24')*60) + TO_CHAR(SYSDATE,'MI')+(" + adaptBindNameTimeDateOp(bindName) + "/24/60))/24/60";
  }

  @Override
  public String createTimeIsInGEHours(String attribute, String bindName) {
    return attribute + ">=((TO_CHAR(SYSDATE,'HH24')*60) + TO_CHAR(SYSDATE,'MI')+(" + adaptBindNameTimeDateOp(bindName) + "/24))/24/60";
  }

  @Override
  public String createEQ(String attribute, String bindName) {
    return attribute + "=" + adaptBindName(bindName);
  }

  @Override
  public String createDateEQ(String attribute, String bindName) {
    return attribute + "=TRUNC(" + adaptBindName(bindName) + ")";
  }

  @Override
  public String createDateTimeEQ(String attribute, String bindName) {
    return attribute + "=TRUNC(" + adaptBindNameTimeDateOp(bindName) + ",'MI')";
  }

  @Override
  public String createGE(String attribute, String bindName) {
    return attribute + ">=" + adaptBindName(bindName);
  }

  @Override
  public String createDateGE(String attribute, String bindName) {
    return attribute + ">=TRUNC(" + adaptBindName(bindName) + ")";
  }

  @Override
  public String createDateTimeGE(String attribute, String bindName) {
    return attribute + ">=TRUNC(" + adaptBindNameTimeDateOp(bindName) + ",'MI')";
  }

  @Override
  public String createGT(String attribute, String bindName) {
    return attribute + ">" + adaptBindName(bindName);
  }

  @Override
  public String createDateGT(String attribute, String bindName) {
    return attribute + ">TRUNC(" + adaptBindName(bindName) + ")";
  }

  @Override
  public String createDateTimeGT(String attribute, String bindName) {
    return attribute + ">TRUNC(" + adaptBindNameTimeDateOp(bindName) + ",'MI')";
  }

  @Override
  public String createLE(String attribute, String bindName) {
    return attribute + "<=" + adaptBindName(bindName);
  }

  @Override
  public String createDateLE(String attribute, String bindName) {
    return attribute + "<=(TRUNC(" + adaptBindName(bindName) + ")+(86399/86400))";
  }

  @Override
  public String createDateTimeLE(String attribute, String bindName) {
    return attribute + "<=(TRUNC(" + adaptBindNameTimeDateOp(bindName) + ",'MI')+(59/86400))";
  }

  @Override
  public String createLT(String attribute, String bindName) {
    return attribute + "<" + adaptBindName(bindName);
  }

  @Override
  public String createDateLT(String attribute, String bindName) {
    return attribute + "<TRUNC(" + adaptBindName(bindName) + ")";
  }

  @Override
  public String createDateTimeLT(String attribute, String bindName) {
    return attribute + "<TRUNC(" + adaptBindNameTimeDateOp(bindName) + ",'MI')";
  }

  @Override
  public String createNEQ(String attribute, String bindName) {
    return attribute + "<>" + adaptBindName(bindName);
  }

  @Override
  public String createDateNEQ(String attribute, String bindName) {
    return attribute + "<>TRUNC(" + adaptBindName(bindName) + ")";
  }

  @Override
  public String createDateTimeNEQ(String attribute, String bindName) {
    return attribute + "<>TRUNC(" + adaptBindNameTimeDateOp(bindName) + ",'MI')";
  }

  protected abstract int getMaxListSize();

  @Override
  public String createInList(String attribute, Object array) {
    return createInList(attribute, false, array);
  }

  @Override
  public String createInList(String attribute, boolean plain, Object array) {
    Object[] values = toArray(array);
    if (values.length == 0) {
      return createNull(attribute);
    }
    int max = getMaxListSize();
    StringBuffer buf = new StringBuffer();
    buf.append("(");// bracket 1
    for (int i = 0; i < values.length; i = i + max) {
      if (i > 0) {
        buf.append(" OR ");
      }
      buf.append("(");// bracket 2
      buf.append(attribute);
      buf.append(" IN ");
      buf.append("(");// bracket 3
      for (int k = i; k < values.length && k < i + max; k++) {
        if (k > i) {
          buf.append(",");
        }
        buf.append(toPlainText(values[k]));
      }
      buf.append(")");// bracket 3
      buf.append(")");// bracket 2
    }
    buf.append(")");// bracket 1
    return buf.toString();
  }

  @Override
  public String createNotInList(String attribute, Object array) {
    return createNotInList(attribute, false, array);
  }

  @Override
  public String createNotInList(String attribute, boolean plain, Object array) {
    Object[] values = toArray(array);
    if (values.length == 0) {
      return createNotNull(attribute);
    }
    int max = getMaxListSize();
    StringBuffer buf = new StringBuffer();
    buf.append("(");// bracket 1
    for (int i = 0; i < values.length; i = i + max) {
      if (i > 0) {
        buf.append(" AND ");
      }
      buf.append("(");// bracket 2
      buf.append(attribute);
      buf.append(" NOT IN ");
      buf.append("(");// bracket 3
      for (int k = i; k < values.length && k < i + max; k++) {
        if (k > i) {
          buf.append(",");
        }
        buf.append(toPlainText(values[k]));
      }
      buf.append(")");// bracket 3
      buf.append(")");// bracket 2
    }
    buf.append(")");// bracket 1
    return buf.toString();
  }

  @Override
  public boolean isCreatingInListGeneratingBind(Object array) {
    return false;
  }

  @Override
  public String getSysdateToken() {
    return "SYSDATE";
  }

  @Override
  public String getLowerToken() {
    return "LOWER";
  }

  @Override
  public String getUpperToken() {
    return "UPPER";
  }

  @Override
  public String getTrimToken() {
    return "TRIM";
  }

  @Override
  public String getNvlToken() {
    return "NVL";
  }

  protected Object[] toArray(Object array) {
    if (array == null) {
      return new Object[0];
    }
    if (!array.getClass().isArray()) {
      return new Object[]{array};
    }
    int len = Array.getLength(array);
    Object[] a = new Object[len];
    for (int i = 0; i < a.length; i++) {
      a[i] = Array.get(array, i);
    }
    return a;
  }

  protected String adaptBindName(String bindName) {
    if (bindName == null) {
      return bindName;
    }
    if (bindName.startsWith(PLAIN_BIND_MARKER_PREFIX)) {
      return bindName.substring(1);
    }
    return ":" + bindName;
  }

  protected String adaptBindNameTimeDateOp(String bindName) {
    return adaptBindName(bindName);
  }

  @Override
  public void commit() {
  }

  @Override
  public void rollback() {
  }

  /**
   * Gets the strategy to convert a decimal / numeric DB type into a data type in Java.
   * <code>DecimalConversion.NONE</code> means that nothing is converted. <code>DecimalConversion.LEGACY</code> means
   * that the old implementation (prior to Scout 3.10.0-M2) will be used: Numeric / decimal types without a scale are
   * converted to <code>java.lang.Long</code>, with a scale they are converted to <code>java.lang.Double</code>. By
   * default <code>DecimalConversion.NONE</code> will be returned.
   *
   * @since 3.10.0-M2
   * @return strategy for the decimal conversion
   */
  protected DecimalConversion getConfiguredDecimalConversionStrategy() {
    return DecimalConversion.NONE;
  }

  /**
   * @since 3.10.0-M2
   */
  public enum DecimalConversion {
    NONE {
      @Override
      public Object convertDecimalType(BigDecimal bd) {
        return bd;
      }
    },
    LEGACY {
      @Override
      public Object convertDecimalType(BigDecimal bd) {
        if (bd == null) {
          return null;
        }
        if (bd.scale() == 0) {
          return bd.longValue();
        }
        return bd.doubleValue();
      }
    };

    public abstract Object convertDecimalType(BigDecimal bd);
  }

}
