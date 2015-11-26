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

import java.io.Serializable;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.eclipse.scout.commons.holders.IHolder;
import org.eclipse.scout.rt.server.jdbc.SqlBind;

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
 * <td><code>personId</code>, <code>id</code>, <code>name</code>, <code>age</code>, <code>active</code></td>
 * </tr>
 * <tr>
 * <td>Examples for plain bind values inclusive marker prefix:</td>
 * <td><code>&123</code>, <code>&'John'</code>, <code>&to_date('01.01.2010','dd.mm.yyyy')</code></td>
 * </tr>
 * </table>
 * </p>
 */
public interface ISqlStyle extends Serializable {
  /**
   * Default maximal in-list size
   */
  int MAX_LIST_SIZE = 1000;
  /**
   * Maximal length of a plain text string.
   */
  int MAX_SQL_STRING_LENGTH = 4000;
  /**
   * Can be used to escape plain text that must not be prefixed by <code>:</code><br />
   * E.g.
   *
   * <pre>
   * createNotLike(&quot;P.NAME&quot;, PLAIN_BIND_MARKER_PREFIX + &quot;'%test'&quot;)
   * </pre>
   *
   * to generate <code>P.NAME not like '%test'</code> instead of <code>P.NAME not like :'%test'</code>
   */
  String PLAIN_BIND_MARKER_PREFIX = "&";

  /**
   * ansi: <code>+</code>
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
   *          the value to be bound. If the value is a {@link IHolder} then its content value is used
   * @param nullType
   *          only used in case o is null. If the type is a {@link IHolder} type then its component type is used Ignored
   *          otherwise.
   */
  SqlBind buildBindFor(Object o, Class nullType);

  /**
   * <code>COUNT(p.BUDGET)</code>
   */
  String toAggregationCount(String attribute);

  /**
   * <code>MIN(p.BUDGET)</code>
   */
  String toAggregationMin(String attribute);

  /**
   * <code>MAX(p.BUDGET)</code>
   */
  String toAggregationMax(String attribute);

  /**
   * <code>SUM(p.BUDGET)</code>
   */
  String toAggregationSum(String attribute);

  /**
   * <code>AVG(p.BUDGET)</code>
   */
  String toAggregationAvg(String attribute);

  /**
   * <code>MEDIAN(p.BUDGET)</code>
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
   * test a connection before use<br />
   * Note: this method is called before *every* sql connection pool transaction<br />
   * Note: this method is not called when an rmi connection pool is used
   */
  void testConnection(Connection conn) throws SQLException;

  /**
   * flag signaling whether <code>BLOB</code> or <code>LONG RAW</code> should be used for <code>byte[]</code> binds
   */
  boolean isBlobEnabled();

  /**
   * flag signaling whether <code>CLOB</code> or <code>LONG VARCHAR</code> should be used for <code>char[]</code> and
   * large {@link String} binds
   */
  boolean isClobEnabled();

  /**
   * @return <code>true</code> to handle string as <code>CLOB</code> resp. <code>LONG VARCHAR</code> or
   *         <code>false</code> to handle string as simple {@link String} see {@link #isClobEnabled()}
   */
  boolean isLargeString(String s);

  /*
   * Attribute filters Aggregate SQL code transforming the template code to
   * specific database sql the following methods display an example of an
   * "sql attribute constraint" in the javadoc comment
   */

  /**
   * <code>P.BUDGET between :bindName1 and :bindName2</code><br />
   * (or "<code>&text</code>" for non-binds, see also {@link #PLAIN_BIND_MARKER_PREFIX})
   * <p>
   * for dates use {@link #createDateBetween(String, String, String)} and
   * {@link #createDateTimeBetween(String, String, String)}
   * </p>
   * <p>
   * In an Oracle implementation this could be represented as
   * </p>
   *
   * <pre>
   * attribute BETWEEN :firstBind AND :secondBind
   * </pre>
   */
  String createBetween(String attribute, String bindName1, String bindName2);

  /**
   * <code>P.EVT_CREATED between :bindName1 and :bindName2</code><br />
   * (or "<code>&text</code>" for non-binds, see also {@link #PLAIN_BIND_MARKER_PREFIX})
   * <p>
   * In an Oracle implementation this could be represented as
   * </p>
   * </p>
   *
   * <pre>
   * attribute BETWEEN TRUNC( :firstBind ) AND (TRUNC( :secondBind ) + (86399/86400))
   * </pre>
   */
  String createDateBetween(String attribute, String bindName1, String bindName2);

  /**
   * <code>P.EVT_CREATED between :bindName1 and :bindName2</code><br />
   * (or "<code>&text</code>" for non-binds, see also {@link #PLAIN_BIND_MARKER_PREFIX})
   * <p>
   * In an Oracle implementation this could be represented as
   * </p>
   *
   * <pre>
   * attribute BETWEEN TRUNC( :firstBind , 'MI' ) AND (TRUNC( :secondBind , 'MI') + (59/1440))
   * </pre>
   */
  String createDateTimeBetween(String attribute, String bindName1, String bindName2);

  /**
   * <code>P.NAME like :bindName||'*'</code><br />
   * (or "<code>&text</code>" for non-binds, see also {@link #PLAIN_BIND_MARKER_PREFIX})
   * <p>
   * In an Oracle implementation this could be represented as
   * </p>
   *
   * <pre>
   * UPPER( attribute ) LIKE UPPER( :firstBind || '%')
   * </pre>
   */
  String createStartsWith(String attribute, String bindName);

  /**
   * <code>P.NAME not like :bindName||'*'</code><br />
   * (or "<code>&text</code>" for non-binds, see also {@link #PLAIN_BIND_MARKER_PREFIX})
   * <p>
   * In an Oracle implementation this could be represented as
   * </p>
   *
   * <pre>
   * UPPER( attribute ) NOT LIKE UPPER( :firstBind || '%')
   * </pre>
   */
  String createNotStartsWith(String attribute, String bindName);

  /**
   * <code>P.NAME like '*'||:bindName</code><br />
   * (or "<code>&text</code>" for non-binds, see also {@link #PLAIN_BIND_MARKER_PREFIX})
   * <p>
   * In an Oracle implementation this could be represented as
   * </p>
   *
   * <pre>
   * UPPER( attribute ) LIKE UPPER( '%' || :firstBind )
   * </pre>
   */
  String createEndsWith(String attribute, String bindName);

  /**
   * <code>P.NAME not like '*'||:bindName</code><br />
   * (or "<code>&text</code>" for non-binds, see also {@link #PLAIN_BIND_MARKER_PREFIX})
   * <p>
   * In an Oracle implementation this could be represented as
   * </p>
   *
   * <pre>
   * UPPER( attribute ) NOT LIKE UPPER( '%' || :firstBind )
   * </pre>
   */
  String createNotEndsWith(String attribute, String bindName);

  /**
   * <code>P.NAME like '*'||:bindName||'*'</code><br />
   * (or "<code>&text</code>" for non-binds, see also {@link #PLAIN_BIND_MARKER_PREFIX})
   * <p>
   * In an Oracle implementation this could be represented as
   * </p>
   *
   * <pre>
   * UPPER( attribute ) LIKE UPPER('%'|| :firstBind ||'%')
   * </pre>
   */
  String createContains(String attribute, String bindName);

  /**
   * <p>
   * <code>P.NAME not like '*'||:bindName||'*'</code><br />
   * (or "<code>&text</code>" for non-binds, see also {@link #PLAIN_BIND_MARKER_PREFIX})
   * </p>
   * <p>
   * In an Oracle implementation this could be represented as
   * </p>
   *
   * <pre>
   * UPPER( attribute ) NOT LIKE UPPER('%'|| :firstBind ||'%')
   * </pre>
   */
  String createNotContains(String attribute, String bindName);

  /**
   * <code>P.NAME like :bindName</code><br />
   * (or "<code>&text</code>" for non-binds, see also {@link #PLAIN_BIND_MARKER_PREFIX})
   * <p>
   * In an Oracle implementation this could be represented as
   * </p>
   *
   * <pre>
   * UPPER( attribute ) LIKE UPPER( :firstBind )
   * </pre>
   */
  String createLike(String attribute, String bindName);

  /**
   * <code>P.NAME not like :bindName</code><br />
   * (or "<code>&text</code>" for non-binds, see also {@link #PLAIN_BIND_MARKER_PREFIX})
   * <p>
   * In an Oracle implementation this could be represented as
   * </p>
   *
   * <pre>
   * UPPER( attribute ) NOT LIKE UPPER( :firstBind )
   * </pre>
   */
  String createNotLike(String attribute, String bindName);

  /**
   * <code>P.NAME is null</code>
   * <p>
   * In an Oracle implementation this could be represented as
   * </p>
   *
   * <pre>
   * attribute IS NULL</code>
   * </p>
   */
  String createNull(String attribute);

  /**
   * <code>P.NAME not null</code><br />
   * (or "<code>&text</code>" for non-binds, see also {@link #PLAIN_BIND_MARKER_PREFIX})
   * <p>
   * In an Oracle implementation this could be represented as
   * </p>
   *
   * <pre>
   * attribute IS NOT NULL
   * </pre>
   */
  String createNotNull(String attribute);

  /**
   * <code>nvl(P.NAME,0) = 0</code><br />
   * special case for '<code>number is null</code>', if <code>0</code> and <code>null</code> are treated equally
   * <p>
   * In an Oracle implementation this could be represented as
   * </p>
   *
   * <pre>
   * NVL(attribute, 0) = 0
   * </pre>
   */
  String createNumberNull(String attribute);

  /**
   * <code>nvl(P.NAME,0)<>0</code><br />
   * special case for '<code>number is null</code>', if <code>0</code> and <code>null</code> are treated equally
   * <p>
   * In an Oracle implementation this could be represented as
   * </p>
   *
   * <pre>
   * NVL( attribute, 0) <> 0
   * </pre>
   */
  String createNumberNotNull(String attribute);

  /**
   * <code>nvl(P.NAME,'0')=='0'</code><br />
   * special case for '<code>text is null</code>', if <code>'0'</code> and <code>null</code> are treated equally
   * <p>
   * In an Oracle implementation this could be represented as
   * </p>
   *
   * <pre>
   * NVL(attribute, '0') = '0'
   * </pre>
   */
  String createTextNull(String attribute);

  /**
   * <code>nvl(P.NAME,'0')<>'0'</code><br />
   * special case for '<code>text is null</code>' when <code>'0'</code> and <code>null</code> are treated equally
   * <p>
   * In an Oracle implementation this could be represented as
   * </p>
   *
   * <pre>
   * NVL( attribute, '0') <> '0'
   * </pre>
   */
  String createTextNotNull(String attribute);

  /**
   * <code>P.NAME in :bindName</code><br />
   * (or "<code>&text</code>" for non-binds, see also {@link #PLAIN_BIND_MARKER_PREFIX} )
   * <p>
   * In an Oracle implementation this could be represented as
   * </p>
   *
   * <pre>
   * attribute IN ( :{firstBind} )
   * </pre>
   */
  String createIn(String attribute, String bindName);

  /**
   * <code>P.NAME not in :bindName</code><br />
   * (or "<code>&text</code>" for non-binds, see also {@link #PLAIN_BIND_MARKER_PREFIX})
   * <p>
   * In an Oracle implementation this could be represented as
   * </p>
   *
   * <pre>
   * NOT( attribute IN ( :{firstBind} ))
   * </pre>
   */
  String createNotIn(String attribute, String bindName);

  /**
   * <code>P.NAME in (o1,o2,...)</code><br />
   * (or "<code>&text</code>" for non-binds, see also {@link #PLAIN_BIND_MARKER_PREFIX})
   */
  String createInList(String attribute, Object array);

  /**
   * <code>P.NAME not in (o1,o2,...)</code><br />
   * (or "<code>&text</code>" for non-binds, see also {@link #PLAIN_BIND_MARKER_PREFIX})
   */
  String createNotInList(String attribute, Object array);

  /**
   * <code>P.NAME in (o1,o2,...)</code><br />
   * (or "<code>&text</code>" for non-binds, see also {@link #PLAIN_BIND_MARKER_PREFIX})
   */
  String createInList(String attribute, boolean plain, Object array);

  /**
   * <code>P.NAME not in (o1,o2,...)</code><br />
   * (or "<code>&text</code>" for non-binds, see also {@link #PLAIN_BIND_MARKER_PREFIX})
   */
  String createNotInList(String attribute, boolean plain, Object array);

  /**
   * Style can handle arrays as bind or as literal replacement.
   *
   * @return true if calls to {@link ISqlStyle.createInList(String attribute, Object array)} or
   *         {@link ISqlStyle.createNotInList(String attribute, Object array)} produce SQL binds.
   */
  boolean isCreatingInListGeneratingBind(Object array);

  /**
   * <code>P.NAME dateIsToday</code>
   * <p>
   * In an Oracle implementation this could be represented as
   * </p>
   *
   * <pre>
   * attribute >= TRUNC(SYSDATE) AND attribute < TRUNC(SYSDATE+1)
   * </pre>
   */
  String createDateIsToday(String attribute);

  /**
   * <code>P.NAME dateIsInLastDays :bindName</code><br />
   * (or "<code>&text</code>" for non-binds, see also {@link #PLAIN_BIND_MARKER_PREFIX})
   * <p>
   * In an Oracle implementation this could be represented as
   * </p>
   *
   * <pre>
   * attribute >= TRUNC(SYSDATE-( :firstBind ))
   * AND
   * attribute < TRUNC(SYSDATE+1)
   * </pre>
   */
  String createDateIsInLastDays(String attribute, String bindName);

  /**
   * <code>P.NAME dateIsInNextDays :bindName</code><br />
   * (or "<code>&text</code>" for non-binds, see also {@link #PLAIN_BIND_MARKER_PREFIX})
   * <p>
   * In an Oracle implementation this could be represented as
   * </p>
   *
   * <pre>
   * attribute &gt;= TRUNC(SYSDATE)
   * AND
   * attribute < TRUNC(SYSDATE+ :firstBind +1)
   * </pre>
   */
  String createDateIsInNextDays(String attribute, String bindName);

  /**
   * <code>P.NAME dateIsInDays :bindName</code><br />
   * (or "<code>&text</code>" for non-binds, see also {@link #PLAIN_BIND_MARKER_PREFIX})
   * <p>
   * In an Oracle implementation this could be represented as
   * </p>
   *
   * <pre>
   * attribute >= TRUNC(SYSDATE+ :firstBind )
   * AND
   * attribute < TRUNC(SYSDATE+ :firstBind +1)
   * </pre>
   */
  String createDateIsInDays(String attribute, String bindName);

  /**
   * <code>P.NAME dateIsInWeeks :bindName</code><br />
   * (or "<code>&text</code>" for non-binds, see also {@link #PLAIN_BIND_MARKER_PREFIX})
   * <p>
   * In an Oracle implementation this could be represented as
   * </p>
   *
   * <pre>
   * attribute >= TRUNC(SYSDATE+ (( :firstBind ) *7) )
   * AND
   * attribute < TRUNC(SYSDATE+(( :firstBind ) *7)+1)
   * </pre>
   */
  String createDateIsInWeeks(String attribute, String bindName);

  /**
   * <code>P.NAME dateIsInLastMonths :bindName</code><br />
   * (or "<code>&text</code>" for non-binds, see also {@link #PLAIN_BIND_MARKER_PREFIX})
   * <p>
   * In an Oracle implementation this could be represented as
   * </p>
   *
   * <pre>
   * attribute >= TRUNC(ADD_MONTHS(SYSDATE,(-1)*( :firstBind )))
   * AND
   * attribute < TRUNC(SYSDATE+1)
   * </pre>
   */
  String createDateIsInLastMonths(String attribute, String bindName);

  /**
   * <code>P.NAME dateIsInNextMonths :bindName</code><br />
   * (or "<code>&text</code>" for non-binds, see also {@link #PLAIN_BIND_MARKER_PREFIX})
   * <p>
   * In an Oracle implementation this could be represented as
   * </p>
   *
   * <pre>
   * attribute >= TRUNC(SYSDATE)
   * AND
   * attribute < TRUNC(ADD_MONTHS(SYSDATE, :firstBind )+1)
   * </pre>
   */
  String createDateIsInNextMonths(String attribute, String bindName);

  /**
   * <code>P.NAME dateIsInMonths :bindName</code><br />
   * (or "<code>&text</code>" for non-binds, see also {@link #PLAIN_BIND_MARKER_PREFIX})
   * <p>
   * In an Oracle implementation this could be represented as
   * </p>
   *
   * <pre>
   * attribute >= TRUNC(ADD_MONTHS(SYSDATE, :firstBind ))
   * AND
   * attribute < TRUNC(ADD_MONTHS(SYSDATE, :firstBind )+1)
   * </pre>
   */
  String createDateIsInMonths(String attribute, String bindName);

  /**
   * <code>P.NAME dateIsInLEDays :bindName</code><br />
   * (or "<code>&text</code>" for non-binds, see also {@link #PLAIN_BIND_MARKER_PREFIX})
   * <p>
   * In an Oracle implementation this could be represented as
   * </p>
   *
   * <pre>
   * attribute < TRUNC(SYSDATE  + :firstBind +1)
   * </pre>
   */
  String createDateIsInLEDays(String attribute, String bindName);

  /**
   * <code>P.NAME dateIsInLEWeeks :bindName</code><br />
   * (or "<code>&text</code>" for non-binds, see also {@link #PLAIN_BIND_MARKER_PREFIX} )
   * <p>
   * In an Oracle implementation this could be represented as
   * </p>
   *
   * <pre>
   * attribute < TRUNC(SYSDATE+(( :firstBind )*7)+1)
   * </pre>
   */
  String createDateIsInLEWeeks(String attribute, String bindName);

  /**
   * <code>P.NAME dateIsInLEMonths :bindName</code><br />
   * (or "<code>&text</code>" for non-binds, see also {@link #PLAIN_BIND_MARKER_PREFIX})
   * <p>
   * In an Oracle implementation this could be represented as
   * </p>
   *
   * <pre>
   * attribute < TRUNC(ADD_MONTHS(SYSDATE, :firstBind )+1)
   * </pre>
   */
  String createDateIsInLEMonths(String attribute, String bindName);

  /**
   * <code>P.NAME dateIsInGEDays :bindName</code><br />
   * (or "<code>&text</code>" for non-binds, see also {@link #PLAIN_BIND_MARKER_PREFIX})
   * <p>
   * In an Oracle implementation this could be represented as
   * </p>
   *
   * <pre>
   * attribute >= TRUNC(SYSDATE  + :firstBind )
   * </pre>
   */
  String createDateIsInGEDays(String attribute, String bindName);

  /**
   * <code>P.NAME dateIsInGEWeeks :bindName</code><br />
   * (or "<code>&text</code>" for non-binds, see also {@link #PLAIN_BIND_MARKER_PREFIX} )
   * <p>
   * In an Oracle implementation this could be represented as
   * </p>
   *
   * <pre>
   * attribute >= TRUNC(SYSDATE+(( :firstBind )*7))
   * </pre>
   */
  String createDateIsInGEWeeks(String attribute, String bindName);

  /**
   * <code>P.NAME dateIsInGEMonths :bindName</code><br />
   * (or "<code>&text</code>" for non-binds, see also {@link #PLAIN_BIND_MARKER_PREFIX})
   * <p>
   * In an Oracle implementation this could be represented as
   * </p>
   *
   * <pre>
   * attribute >=TRUNC(ADD_MONTHS(SYSDATE, :firstBind ))
   * </pre>
   */
  String createDateIsInGEMonths(String attribute, String bindName);

  /**
   * <code>P.NAME dateIsNotToday</code>
   * <p>
   * In an Oracle implementation this could be represented as
   * </p>
   * <code>( attribute <TRUNC(SYSDATE) OR attribute >=TRUNC(SYSDATE+1))
   * </pre>
   * </p>
   */
  String createDateIsNotToday(String attribute);

  /**
   * <code>P.NAME dateTimeIsNow</code>
   * <p>
   * In an Oracle implementation this could be represented as
   * </p>
   *
   * <pre>
   * ( attribute >= TRUNC(SYSDATE, 'MI') AND attribute < (TRUNC(SYSDATE, 'MI')+(1/24/60)))
   * </pre>
   */
  String createDateTimeIsNow(String attribute);

  /**
   * <code>P.NAME dateTimeIsInLEMinutes :bindName</code><br />
   * (or "<code>&text</code>" for non-binds, see also {@link #PLAIN_BIND_MARKER_PREFIX})
   * <p>
   * In an Oracle implementation this could be represented as
   * </p>
   *
   * <pre>
   * attribute < (TRUNC(SYSDATE, 'MI')+(( :firstBind +1)/24/60))
   * </pre>
   */
  String createDateTimeIsInLEMinutes(String attribute, String bindName);

  /**
   * <code>P.NAME dateTimeIsInLEHours :bindName</code><br />
   * (or "<code>&text</code>" for non-binds, see also {@link #PLAIN_BIND_MARKER_PREFIX})
   * <p>
   * In an Oracle implementation this could be represented as
   * </p>
   *
   * <pre>
   * attribute <(TRUNC(SYSDATE, 'MI')+((1/24/60)+( :firstBind /24)))
   * </pre>
   */
  String createDateTimeIsInLEHours(String attribute, String bindName);

  /**
   * <code>P.NAME dateTimeIsInGEMinutes :bindName</code><br />
   * (or "<code>&text</code>" for non-binds, see also {@link #PLAIN_BIND_MARKER_PREFIX})
   * <p>
   * In an Oracle implementation this could be represented as
   * </p>
   *
   * <pre>
   * attribute >=(TRUNC(SYSDATE, 'MI')+( :firstBind /24/60))
   * </pre>
   */
  String createDateTimeIsInGEMinutes(String attribute, String bindName);

  /**
   * <code>P.NAME dateTimeIsInGEHours :bindName</code><br />
   * (or "<code>&text</code>" for non-binds, see also {@link #PLAIN_BIND_MARKER_PREFIX})
   * <p>
   * In an Oracle implementation this could be represented as
   * </p>
   *
   * <pre>
   * attribute >= (TRUNC(SYSDATE, 'MI')+( :firstBind /24))
   * </pre>
   */
  String createDateTimeIsInGEHours(String attribute, String bindName);

  /**
   * <code>P.NAME dateTimeIsNotNow</code>
   * <p>
   * In an Oracle implementation this could be represented as
   * </p>
   *
   * <pre>
   * ( attribute <TRUNC(SYSDATE, 'MI') OR  attribute >=(TRUNC(SYSDATE, 'MI')+(1/24/60)))
   * </pre>
   */
  String createDateTimeIsNotNow(String attribute);

  /**
   * <code>P.NAME timeIsNow</code>
   * <p>
   * In an Oracle implementation this could be represented as
   * </p>
   *
   * <pre>
   * attribute >= ((TO_CHAR(SYSDATE,'HH24')*60) + TO_CHAR(SYSDATE,'MI'))/24/60</code><br />
   * AND
   * attribute < ((TO_CHAR(SYSDATE,'HH24')*60) + TO_CHAR(SYSDATE,'MI')+(1/24/60))/24/60
   * </pre>
   */
  String createTimeIsNow(String attribute);

  /**
   * <code>P.NAME timeIsNow</code>
   * <p>
   * In an Oracle implementation this could be represented as
   * </p>
   *
   * <pre>
   * attribute < ((TO_CHAR(SYSDATE,'HH24')*60) + TO_CHAR(SYSDATE,'MI'))/24/60</code><br />
   * OR
   * attribute > ((TO_CHAR(SYSDATE,'HH24')*60)+TO_CHAR(SYSDATE,'MI')+(1/24/60))/24/60
   * </pre>
   */
  String createTimeIsNotNow(String attribute);

  /**
   * <code>P.NAME timeIsInMinutes :bindName</code><br />
   * (or "<code>&text</code>" for non-binds, see also {@link #PLAIN_BIND_MARKER_PREFIX} )
   * <p>
   * In an Oracle implementation this could be represented as
   * </p>
   *
   * <pre>
   * attribute >=((TO_CHAR(SYSDATE,'HH24')*60) + TO_CHAR(SYSDATE,'MI')+( :firstBind /24/60))/24/60
   * AND
   * attribute <((TO_CHAR(SYSDATE,'HH24')*60)+TO_CHAR(SYSDATE,'MI')+(( :firstBind +1)/24/60))/24/60
   * </pre>
   */
  String createTimeIsInMinutes(String attribute, String bindName);

  /**
   * <code>P.NAME timeIsInHours :bindName</code><br />
   * (or "<code>&text</code>" for non-binds, see also {@link #PLAIN_BIND_MARKER_PREFIX})
   * <p>
   * In an Oracle implementation this could be represented as
   * </p>
   *
   * <pre>
   * attribute >=((TO_CHAR(SYSDATE,'HH24')*60) + TO_CHAR(SYSDATE,'MI')+( :firstBind /24))/24/60
   * AND
   * attribute <((TO_CHAR(SYSDATE,'HH24')*60)+TO_CHAR(SYSDATE,'MI')+( :firstBind /24)+(1/24/60))/24/60
   * </pre>
   */
  String createTimeIsInHours(String attribute, String bindName);

  /**
   * <code>P.NAME timeIsInLEMinutes :bindName</code><br />
   * (or "<code>&text</code>" for non-binds, see also {@link #PLAIN_BIND_MARKER_PREFIX})
   * <p>
   * In an Oracle implementation this could be represented as
   * </p>
   *
   * <pre>
   * attribute <((TO_CHAR(SYSDATE,'HH24')*60) + TO_CHAR(SYSDATE,'MI')+(( :firstBind +1)/24/60))/24/60
   * </pre>
   */
  String createTimeIsInLEMinutes(String attribute, String bindName);

  /**
   * <code>P.NAME timeIsInLEHours :bindName</code><br />
   * (or "<code>&text</code>" for non-binds, see also {@link #PLAIN_BIND_MARKER_PREFIX} )
   * <p>
   * In an Oracle implementation this could be represented as
   * </p>
   *
   * <pre>
   * attribute <((TO_CHAR(SYSDATE,'HH24')*60) + TO_CHAR(SYSDATE,'MI')+( :firstBind /24)+(1/24/60))/24/60
   * </pre>
   */
  String createTimeIsInLEHours(String attribute, String bindName);

  /**
   * <code>P.NAME timeIsInGEMinutes :bindName</code><br />
   * (or "<code>&text</code>" for non-binds, see also {@link #PLAIN_BIND_MARKER_PREFIX})
   * <p>
   * In an Oracle implementation this could be represented as
   * </p>
   *
   * <pre>
   * attribute >=((TO_CHAR(SYSDATE,'HH24')*60) + TO_CHAR(SYSDATE,'MI')+( :firstBind /24/60))/24/60
   * </pre>
   */
  String createTimeIsInGEMinutes(String attribute, String bindName);

  /**
   * <code>P.NAME timeIsInGEHours :bindName</code><br />
   * (or "<code>&text</code>" for non-binds, see also {@link #PLAIN_BIND_MARKER_PREFIX} )
   * <p>
   * In an Oracle implementation this could be represented as
   * </p>
   *
   * <pre>
   * attribute >=((TO_CHAR(SYSDATE,'HH24')*60) + TO_CHAR(SYSDATE,'MI')+( :firstBind /24))/24/60
   * </pre>
   */
  String createTimeIsInGEHours(String attribute, String bindName);

  /**
   * <code>P.NAME = :bindName</code><br />
   * (or "<code>&text</code>" for non-binds, see also {@link #PLAIN_BIND_MARKER_PREFIX})
   * <p>
   * for dates use {@link #createDateEQ(String, String)} and {@link #createDateTimeEQ(String, String)}
   * </p>
   * <p>
   * In an Oracle implementation this could be represented as
   * </p>
   *
   * <pre>
   * attribute = :firstBind
   * </pre>
   */
  String createEQ(String attribute, String bindName);

  /**
   * <code>P.EVT_CREATED= :bindName</code><br />
   * (or "<code>&text</code>" for non-binds, see also {@link #PLAIN_BIND_MARKER_PREFIX})
   * <p>
   * In an Oracle implementation this could be represented as
   * </p>
   *
   * <pre>
   * attribute = TRUNC( :firstBind )
   * </pre>
   */
  String createDateEQ(String attribute, String bindName);

  /**
   * <code>P.EVT_CREATED = :bindName</code><br />
   * (or "<code>&text</code>" for non-binds, see also {@link #PLAIN_BIND_MARKER_PREFIX})
   * <p>
   * In an Oracle implementation this could be represented as
   * </p>
   *
   * <pre>
   * attribute = TRUNC( :firstBind ,'MI')
   * </pre>
   */
  String createDateTimeEQ(String attribute, String bindName);

  /**
   * <code>P.NAME <> :bindName</code><br />
   * (or "<code>&text</code>" for non-binds, see also {@link #PLAIN_BIND_MARKER_PREFIX})
   * <p>
   * for dates use {@link #createDateNEQ(String, String)} and {@link #createDateTimeNEQ(String, String)}
   * </p>
   * <p>
   * In an Oracle implementation this could be represented as
   * </p>
   *
   * <pre>
   * attribute <> :firstBind
   * </pre>
   */
  String createNEQ(String attribute, String bindName);

  /**
   * <code>P.EVT_CREATED<> :bindName</code><br />
   * (or "<code>&text</code>" for non-binds, see also {@link #PLAIN_BIND_MARKER_PREFIX})
   * <p>
   * In an Oracle implementation this could be represented as
   * </p>
   *
   * <pre>
   * attribute <> TRUNC( :firstBind )
   * </pre>
   */
  String createDateNEQ(String attribute, String bindName);

  /**
   * <code>P.EVT_CREATED <> :bindName</code><br />
   * (or "<code>&text</code>" for non-binds, see also {@link #PLAIN_BIND_MARKER_PREFIX})
   * <p>
   * In an Oracle implementation this could be represented as
   * </p>
   *
   * <pre>
   * attribute <> TRUNC( :firstBind ,'MI')
   * </pre>
   */
  String createDateTimeNEQ(String attribute, String bindName);

  /**
   * <code>P.NAME < :bindName</code><br />
   * (or "<code>&text</code>" for non-binds, see also {@link #PLAIN_BIND_MARKER_PREFIX})
   * <p>
   * for dates use {@link #createDateLT(String, String)} and {@link #createDateTimeLT(String, String)}
   * </p>
   * <p>
   * In an Oracle implementation this could be represented as
   * </p>
   *
   * <pre>
   * attribute < :firstBind
   * </pre>
   */
  String createLT(String attribute, String bindName);

  /**
   * <code>P.EVT_CREATED < :bindName</code><br />
   * (or "<code>&text</code>" for non-binds, see also {@link #PLAIN_BIND_MARKER_PREFIX})
   * <p>
   * In an Oracle implementation this could be represented as
   * </p>
   *
   * <pre>
   * attribute < TRUNC( :firstBind )
   * </pre>
   */
  String createDateLT(String attribute, String bindName);

  /**
   * <code>P.EVT_CREATED < :bindName</code><br />
   * (or "<code>&text</code>" for non-binds, see also {@link #PLAIN_BIND_MARKER_PREFIX})
   * <p>
   * In an Oracle implementation this could be represented as
   * </p>
   *
   * <pre>
   * attribute < TRUNC( :firstBind ,'MI')
   * </pre>
   */
  String createDateTimeLT(String attribute, String bindName);

  /**
   * <code>P.NAME <= :bindName</code><br />
   * (or "<code>&text</code>" for non-binds, see also {@link #PLAIN_BIND_MARKER_PREFIX})
   * <p>
   * for dates use {@link #createDateLE(String, String)} and {@link #createDateTimeLE(String, String)}
   * </p>
   * <p>
   * In an Oracle implementation this could be represented as
   * </p>
   *
   * <pre>
   * attribute <= :firstBind
   * </pre>
   */
  String createLE(String attribute, String bindName);

  /**
   * <code>P.EVT_CREATED <= :bindName</code><br />
   * (or "<code>&text</code>" for non-binds, see also {@link #PLAIN_BIND_MARKER_PREFIX})
   * <p>
   * In an Oracle implementation this could be represented as
   * </p>
   *
   * <pre>
   * attribute <= (TRUNC( :firstBind )+(86399/86400))
   * </pre>
   */
  String createDateLE(String attribute, String bindName);

  /**
   * <code>P.EVT_CREATED <= :bindName</code><br />
   * (or "<code>&text</code>" for non-binds, see also {@link #PLAIN_BIND_MARKER_PREFIX})
   * <p>
   * In an Oracle implementation this could be represented as
   * </p>
   *
   * <pre>
   * attribute <= (TRUNC( :firstBind ,'MI')+(59/1440))
   * </pre>
   */
  String createDateTimeLE(String attribute, String bindName);

  /**
   * <code>P.NAME > :bindName</code><br />
   * (or "<code>&text</code>" for non-binds, see also {@link #PLAIN_BIND_MARKER_PREFIX})
   * <p>
   * for dates use {@link #createDateGT(String, String)} and {@link #createDateTimeGT(String, String)}
   * </p>
   * <p>
   * In an Oracle implementation this could be represented as
   * </p>
   *
   * <pre>
   * attribute > :firstBind
   * </pre>
   */
  String createGT(String attribute, String bindName);

  /**
   * <code>P.EVT_CREATED > :bindName</code><br />
   * (or "<code>&text</code>" for non-binds, see also {@link #PLAIN_BIND_MARKER_PREFIX})
   * <p>
   * In an Oracle implementation this could be represented as
   * </p>
   *
   * <pre>
   * attribute > TRUNC( :firstBind )
   * </pre>
   */
  String createDateGT(String attribute, String bindName);

  /**
   * <code>P.EVT_CREATED > :bindName</code><br />
   * (or "<code>&text</code>" for non-binds, see also {@link #PLAIN_BIND_MARKER_PREFIX})
   * <p>
   * In an Oracle implementation this could be represented as
   * </p>
   *
   * <pre>
   * attribute > TRUNC( :firstBind ,'MI')
   * </pre>
   */
  String createDateTimeGT(String attribute, String bindName);

  /**
   * <code>P.NAME >= :bindName</code><br />
   * (or "<code>&text</code>" for non-binds, see also {@link #PLAIN_BIND_MARKER_PREFIX})
   * <p>
   * for dates use {@link #createDateGE(String, String)} and {@link #createDateTimeGE(String, String)}
   * </p>
   * <p>
   * In an Oracle implementation this could be represented as
   * </p>
   *
   * <pre>
   * attribute >= :firstBind
   * </pre>
   */
  String createGE(String attribute, String bindName);

  /**
   * <code>P.EVT_CREATED >= :bindName</code><br />
   * (or "<code>&text</code>" for non-binds, see also {@link #PLAIN_BIND_MARKER_PREFIX})
   * <p>
   * In an Oracle implementation this could be represented as
   * </p>
   *
   * <pre>
   * attribute >= TRUNC( :firstBind )
   * </pre>
   */
  String createDateGE(String attribute, String bindName);

  /**
   * <code>P.EVT_CREATED >= :bindName</code><br />
   * (or "<code>&text</code>" for non-binds, see also {@link #PLAIN_BIND_MARKER_PREFIX})
   * <p>
   * In an Oracle implementation this could be represented as
   * </p>
   *
   * <pre>
   * attribute >= TRUNC( :firstBind ,'MI')
   * </pre>
   */
  String createDateTimeGE(String attribute, String bindName);

  /**
   * expression for the current date using the database specific keyword, <code>SYSDATE</code> on oracle
   */
  String getSysdateToken();

  /**
   * expression for upper case text using the database specific keyword, <code>UPPER</code> on oracle
   */
  String getUpperToken();

  /**
   * expression for lower case text using the database specific keyword, <code>LOWER</code> on oracle
   */
  String getLowerToken();

  /**
   * expression for trimmed text using the database specific keyword, <code>TRIM</code> on oracle
   */
  String getTrimToken();

  /**
   * expression for null-value/default-value using the database specific keyword, <code>NVL</code> on oracle
   */
  String getNvlToken();

  void commit();

  void rollback();
}
