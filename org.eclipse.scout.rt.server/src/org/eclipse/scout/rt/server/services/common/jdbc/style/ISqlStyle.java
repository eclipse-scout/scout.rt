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
package org.eclipse.scout.rt.server.services.common.jdbc.style;

import java.io.Serializable;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.eclipse.scout.rt.server.services.common.jdbc.SqlBind;

/**
 * <p>
 * The interface provides database independent access to SQL building blocks.
 * </p>
 * <h2>Example:</h2>
 * <p>
 * Instead of writing in Oracle syntax a condition like
 * 
 * <pre>
 * String condition = &quot;AND NVL(start_date, SYSDATE) &gt; :date&quot;;
 * </pre>
 * 
 * the code
 * 
 * <pre>
 * {@link ISqlStyle} style = service.getSqlStyle();
 * String condition = &quot;AND &quot; + style.nvl(&quot;start_date&quot;, &quot;end_date&quot;) + &quot;(start_date, end_date) &gt; :date&quot;;
 * </pre>
 * 
 * is used. On Oracle it will be translated to <code>NVL(start_date, end_date)</code>, but when running MSSQL you would
 * get <code>ISNULL(start_date, end_date)</code>
 * </p>
 * <h2>Bind Names</h2>
 * <p>
 * All methods that require a bindName accept bind names (without <code>:</code> prefix) <i>or</i> a plain bind value
 * (prepend a {@value #PLAIN_BIND_MARKER_PREFIX}). Plain bind values are created using {@link #toPlainText(Object)}
 * <table>
 * <tr>
 * <td>Examples for bind names:</td>
 * <td> <code>personId</code>, <code>id</code>, <code>name</code>, <code>age</code>, <code>active</code></td>
 * </tr>
 * <tr>
 * <td>Examples for plain bind values inclusive marker prefix:</td>
 * <td><code>&123</code>, <code>&'John'</code>, <code>&to_date('01.01.2010','dd.mm.yyyy')</code></td>
 * </tr>
 * </table>
 * </p>
 */
public interface ISqlStyle extends Serializable {
  String PLAIN_BIND_MARKER_PREFIX = "&";

  /**
   * ansi: +
   */
  String getConcatOp();

  /**
   * ansi: <code>*</code>
   */
  String getLikeWildcard();

  String toPlainText(Object value);

  /**
   * convert <code>*</code> into <code>%</code>
   */
  String toLikePattern(Object value);

  /**
   * bind factory see {@link #isBlobEnabled()}, {@link #isClobEnabled()} and {@link #isLargeString(String)}
   * 
   * @param o
   *          the value to be bound
   * @param nullType
   *          only used in case o is null. Ignored otherwise.
   */
  SqlBind buildBindFor(Object o, Class nullType);

  /**
   * COUNT(p.BUDGET)
   */
  String toAggregationCount(String attribute);

  /**
   * MIN(p.BUDGET)
   */
  String toAggregationMin(String attribute);

  /**
   * MAX(p.BUDGET)
   */
  String toAggregationMax(String attribute);

  /**
   * SUM(p.BUDGET)
   */
  String toAggregationSum(String attribute);

  /**
   * AVG(p.BUDGET)
   */
  String toAggregationAvg(String attribute);

  /**
   * MEDIAN(p.BUDGET)
   */
  String toAggregationMedian(String attribute);

  /**
   * apply bind value to a {@link PreparedStatement}
   */
  void writeBind(PreparedStatement ps, int jdbcBindIndex, SqlBind bind) throws SQLException;

  /**
   * read bind value from a {@link ResultSet}
   */
  Object readBind(ResultSet rs, ResultSetMetaData meta, int type, int jdbcBindIndex) throws SQLException;

  /**
   * apply out parameter to a stored procedure call in a {@link CallableStatement}
   */
  void registerOutput(CallableStatement cs, int index, Class bindType) throws SQLException;

  /**
   * test a connection before use Note: this method is called before *every* sql
   * connection pool transaction Note: this methode is not called when an rmi
   * connection pool is used
   */
  void testConnection(Connection conn) throws SQLException;

  /**
   * flag signalling whether BLOB or LONG RAW should be used for byte[] binds
   */
  boolean isBlobEnabled();

  /**
   * flag signalling whether CLOB or LONG VARCHAR should be used for char[] and
   * large String binds
   */
  boolean isClobEnabled();

  /**
   * @return true to handle string as CLOB resp. LONG VARCHAR or false to handle
   *         string as simple String see {@link #isClobEnabled()}
   */
  boolean isLargeString(String s);

  /*
   * Attribute filters Areate SQL code transforming the template code to
   * specific database sql the folloging methods display an example of an
   * "sql attrinute constraint" in the javadoc comment
   */

  /**
   * P.BUDGET between :bindName1 and :bindName2 (or "&text" for non-binds)
   * for dates use {@link #createDateBetween(String, String, String)} and
   * {@link #createDateTimeBetween(String, String, String)}
   */
  String createBetween(String attribute, String bindName1, String bindName2);

  /**
   * P.EVT_CREATED between :bindName1 and :bindName2 (or "&text" for non-binds)
   * for dates use
   */
  String createDateBetween(String attribute, String bindName1, String bindName2);

  /**
   * P.EVT_CREATED between :bindName1 and :bindName2 (or "&text" for non-binds)
   * for dates use
   */
  String createDateTimeBetween(String attribute, String bindName1, String bindName2);

  /**
   * P.NAME like :bindName||'*' (or "&text" for non-binds)
   */
  String createStartsWith(String attribute, String bindName);

  /**
   * P.NAME not like :bindName||'*' (or "&text" for non-binds)
   */
  String createNotStartsWith(String attribute, String bindName);

  /**
   * P.NAME like '*'||:bindName (or "&text" for non-binds)
   */
  String createEndsWith(String attribute, String bindName);

  /**
   * P.NAME not like '*'||:bindName (or "&text" for non-binds)
   */
  String createNotEndsWith(String attribute, String bindName);

  /**
   * P.NAME like '*'||:bindName||'*' (or "&text" for non-binds)
   */
  String createContains(String attribute, String bindName);

  /**
   * P.NAME not like '*'||:bindName||'*' (or "&text" for non-binds)
   */
  String createNotContains(String attribute, String bindName);

  /**
   * P.NAME like :bindName (or "&text" for non-binds)
   */
  String createLike(String attribute, String bindName);

  /**
   * P.NAME not like :bindName (or "&text" for non-binds)
   */
  String createNotLike(String attribute, String bindName);

  /**
   * P.NAME null (or "&text" for non-binds)
   */
  String createNull(String attribute);

  /**
   * P.NAME not null (or "&text" for non-binds)
   */
  String createNotNull(String attribute);

  /**
   * nvl(P.NAME,0)=0 special case for 'number is null' when 0 and null are
   * threated as the same
   */
  String createNumberNull(String attribute);

  /**
   * nvl(P.NAME,0)<>0 special case for 'number is null' when 0 and null are
   * threated as the same
   */
  String createNumberNotNull(String attribute);

  /**
   * nvl(P.NAME,'0')=='0' special case for 'text is null' when '0' and null are
   * threated as the same
   */
  String createTextNull(String attribute);

  /**
   * nvl(P.NAME,'0')<>'0' special case for 'text is null' when '0' and null are
   * threated as the same
   */
  String createTextNotNull(String attribute);

  /**
   * P.NAME in :bindName (or "&text" for non-binds)
   */
  String createIn(String attribute, String bindName);

  /**
   * P.NAME not in :bindName (or "&text" for non-binds)
   */
  String createNotIn(String attribute, String bindName);

  /**
   * P.NAME in (o1,o2,...) (or "&text" for non-binds)
   */
  String createInList(String attribute, Object array);

  /**
   * P.NAME not in (o1,o2,...) (or "&text" for non-binds)
   */
  String createNotInList(String attribute, Object array);

  /**
   * P.NAME dateIsToday (or "&text" for non-binds)
   */
  String createDateIsToday(String attribute);

  /**
   * P.NAME dateIsInLastDays :bindName (or "&text" for non-binds)
   */
  String createDateIsInLastDays(String attribute, String bindName);

  /**
   * P.NAME dateIsInNextDays :bindName (or "&text" for non-binds)
   */
  String createDateIsInNextDays(String attribute, String bindName);

  /**
   * P.NAME dateIsInDays :bindName (or "&text" for non-binds)
   */
  String createDateIsInDays(String attribute, String bindName);

  /**
   * P.NAME dateIsInWeeks :bindName (or "&text" for non-binds)
   */
  String createDateIsInWeeks(String attribute, String bindName);

  /**
   * P.NAME dateIsInLastMonths :bindName (or "&text" for non-binds)
   */
  String createDateIsInLastMonths(String attribute, String bindName);

  /**
   * P.NAME dateIsInNextMonths :bindName (or "&text" for non-binds)
   */
  String createDateIsInNextMonths(String attribute, String bindName);

  /**
   * P.NAME dateIsInMonths :bindName (or "&text" for non-binds)
   */
  String createDateIsInMonths(String attribute, String bindName);

  /**
   * P.NAME dateIsInLEDays :bindName (or "&text" for non-binds)
   */
  String createDateIsInLEDays(String attribute, String bindName);

  /**
   * P.NAME dateIsInLEWeeks :bindName (or "&text" for non-binds)
   */
  String createDateIsInLEWeeks(String attribute, String bindName);

  /**
   * P.NAME dateIsInLEMonths :bindName (or "&text" for non-binds)
   */
  String createDateIsInLEMonths(String attribute, String bindName);

  /**
   * P.NAME dateIsInGEDays :bindName (or "&text" for non-binds)
   */
  String createDateIsInGEDays(String attribute, String bindName);

  /**
   * P.NAME dateIsInGEWeeks :bindName (or "&text" for non-binds)
   */
  String createDateIsInGEWeeks(String attribute, String bindName);

  /**
   * P.NAME dateIsInGEMonths :bindName (or "&text" for non-binds)
   */
  String createDateIsInGEMonths(String attribute, String bindName);

  /**
   * P.NAME dateIsNotToday (or "&text" for non-binds)
   */
  String createDateIsNotToday(String attribute);

  /**
   * P.NAME dateTimeIsNow (or "&text" for non-binds)
   */
  String createDateTimeIsNow(String attribute);

  /**
   * P.NAME dateTimeIsInLEMinutes :bindName (or "&text" for non-binds)
   */
  String createDateTimeIsInLEMinutes(String attribute, String bindName);

  /**
   * P.NAME dateTimeIsInLEHours :bindName (or "&text" for non-binds)
   */
  String createDateTimeIsInLEHours(String attribute, String bindName);

  /**
   * P.NAME dateTimeIsInGEMinutes :bindName (or "&text" for non-binds)
   */
  String createDateTimeIsInGEMinutes(String attribute, String bindName);

  /**
   * P.NAME dateTimeIsInGEHours :bindName (or "&text" for non-binds)
   */
  String createDateTimeIsInGEHours(String attribute, String bindName);

  /**
   * P.NAME dateTimeIsNotNow (or "&text" for non-binds)
   */
  String createDateTimeIsNotNow(String attribute);

  /**
   * P.NAME timeIsNow (or "&text" for non-binds)
   */
  String createTimeIsNow(String attribute);

  /**
   * P.NAME timeIsNow (or "&text" for non-binds)
   */
  String createTimeIsNotNow(String attribute);

  /**
   * P.NAME timeIsInMinutes :bindName (or "&text" for non-binds)
   */
  String createTimeIsInMinutes(String attribute, String bindName);

  /**
   * P.NAME timeIsInHours :bindName (or "&text" for non-binds)
   */
  String createTimeIsInHours(String attribute, String bindName);

  /**
   * P.NAME timeIsInLEMinutes :bindName (or "&text" for non-binds)
   */
  String createTimeIsInLEMinutes(String attribute, String bindName);

  /**
   * P.NAME timeIsInLEHours :bindName (or "&text" for non-binds)
   */
  String createTimeIsInLEHours(String attribute, String bindName);

  /**
   * P.NAME timeIsInGEMinutes :bindName (or "&text" for non-binds)
   */
  String createTimeIsInGEMinutes(String attribute, String bindName);

  /**
   * P.NAME timeIsInGEHours :bindName (or "&text" for non-binds)
   */
  String createTimeIsInGEHours(String attribute, String bindName);

  /**
   * P.NAME = :bindName (or "&text" for non-binds)
   * for dates use {@link #createDateEQ(String, String)} and {@link #createDateTimeEQ(String, String)}
   */
  String createEQ(String attribute, String bindName);

  /**
   * P.EVT_CREATED= :bindName (or "&text" for non-binds)
   */
  String createDateEQ(String attribute, String bindName);

  /**
   * P.EVT_CREATED = :bindName (or "&text" for non-binds)
   */
  String createDateTimeEQ(String attribute, String bindName);

  /**
   * P.NAME <> :bindName (or "&text" for non-binds)
   * for dates use {@link #createDateNEQ(String, String)} and {@link #createDateTimeNEQ(String, String)}
   */
  String createNEQ(String attribute, String bindName);

  /**
   * P.EVT_CREATED<> :bindName (or "&text" for non-binds)
   */
  String createDateNEQ(String attribute, String bindName);

  /**
   * P.EVT_CREATED <> :bindName (or "&text" for non-binds)
   */
  String createDateTimeNEQ(String attribute, String bindName);

  /**
   * P.NAME < :bindName (or "&text" for non-binds)
   * for dates use {@link #createDateLT(String, String)} and {@link #createDateTimeLT(String, String)}
   */
  String createLT(String attribute, String bindName);

  /**
   * P.EVT_CREATED < :bindName (or "&text" for non-binds)
   */
  String createDateLT(String attribute, String bindName);

  /**
   * P.EVT_CREATED < :bindName (or "&text" for non-binds)
   */
  String createDateTimeLT(String attribute, String bindName);

  /**
   * P.NAME <= :bindName (or "&text" for non-binds)
   * for dates use {@link #createDateLE(String, String)} and {@link #createDateTimeLE(String, String)}
   */
  String createLE(String attribute, String bindName);

  /**
   * P.EVT_CREATED <= :bindName (or "&text" for non-binds)
   */
  String createDateLE(String attribute, String bindName);

  /**
   * P.EVT_CREATED <= :bindName (or "&text" for non-binds)
   */
  String createDateTimeLE(String attribute, String bindName);

  /**
   * P.NAME > :bindName (or "&text" for non-binds)
   * for dates use {@link #createDateGT(String, String)} and {@link #createDateTimeGT(String, String)}
   */
  String createGT(String attribute, String bindName);

  /**
   * P.EVT_CREATED > :bindName (or "&text" for non-binds)
   */
  String createDateGT(String attribute, String bindName);

  /**
   * P.EVT_CREATED > :bindName (or "&text" for non-binds)
   */
  String createDateTimeGT(String attribute, String bindName);

  /**
   * P.NAME >= :bindName (or "&text" for non-binds)
   * for dates use {@link #createDateGE(String, String)} and {@link #createDateTimeGE(String, String)}
   */
  String createGE(String attribute, String bindName);

  /**
   * P.EVT_CREATED >= :bindName (or "&text" for non-binds)
   */
  String createDateGE(String attribute, String bindName);

  /**
   * P.EVT_CREATED >= :bindName (or "&text" for non-binds)
   */
  String createDateTimeGE(String attribute, String bindName);

  /**
   * expression for the current date using the database specific keyword, SYSDATE on oracle
   */
  String getSysdateToken();

  /**
   * expression for upper case text using the database specific keyword, UPPER on oracle
   */
  String getUpperToken();

  /**
   * expression for lower case text using the database specific keyword, LOWER on oracle
   */
  String getLowerToken();

  /**
   * expression for trimmed text using the database specific keyword, TRIM on oracle
   */
  String getTrimToken();

  /**
   * expression for null-value/default-value using the database specific keyword, NVL on oracle
   */
  String getNvlToken();

}
