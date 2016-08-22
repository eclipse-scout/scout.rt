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
package org.eclipse.scout.rt.server.jdbc;

import java.sql.Connection;

import org.eclipse.scout.rt.platform.IPlatform.State;
import org.eclipse.scout.rt.platform.IPlatformListener;
import org.eclipse.scout.rt.platform.job.IJobManager;
import org.eclipse.scout.rt.platform.service.IService;
import org.eclipse.scout.rt.server.jdbc.style.ISqlStyle;
import org.eclipse.scout.rt.server.services.common.clustersync.IClusterSynchronizationService;

/**
 * <p>
 * This interface is used for access to a database. It adds functionality additional to that of the underlying JDBC
 * driver.
 * </p>
 * <p>
 * Classes implementing this interface also provide a {@link ISqlStyle} (by {@link #getSqlStyle()}). This interface
 * helps writing database SQL statements.
 * </p>
 * <h2>Simple example</h2>
 *
 * <pre>
 * ISqlService service = BEANS.get(ISqlService.class);
 * String sql = &quot;SELECT PERSON_NR, PERSON_NAME FROM PERSON WHERE COMPANY_NR = :companyNr&quot;;
 * Object[][] queryResult = service.select(sql,
 *     new NVPair(&quot;companyNr&quot;, companyNr));
 * Long firstPersonNr = queryResult[0][0];
 * Long thirdPersonName = queryResult[2][1];
 * </pre>
 *
 * <h2>Bind Variables</h2>
 * <p>
 * The statements passed to be executed can contain bind variables (<code>:variableName</code>). The values to be bound
 * are also passed to the various methods of the class. They are type object, e.g. <code>Object... values</code>.
 * {@link ISqlService} can handle bind bases of the types
 * <ul>
 * <li><code>NVPair(String, Object)</code><br />
 * to directly pass known values. The {@link String} parameter is the name of the bind variable used in the SQL, without
 * prefix <code>:</code></li>
 * <li><code>IFormData</code><br />
 * The properties (bean properties) and fields of the bean are used.</li>
 * <li>a plain java bean<br />
 * The name of the bind variable (e.g. <code>:companyNr</code>) must match the property name (e.g. the bean must
 * implement <code>Long getCompanyNr()</code>)</li>
 * <li>a {@link java.util.Map} with name/value pairs</li>
 * <li>by default the ServerSession's bean properties are used as well</li>
 * </ul>
 * </p>
 * <h3>Database specific tokens</h3> Often used sql functions that may have different names in different sql styles can
 * be written with a leading @ character and are replaced by the correct token.
 * <ul>
 * <li>@sysdate</li>
 * <li>@upper</li>
 * <li>@lower</li>
 * <li>@trim</li>
 * <li>@nvl</li>
 * </ul>
 * <p>
 * <h3>Type Matching</h3>
 * <p>
 * When using this service the database column types are converted to Java types using
 * {@link org.eclipse.scout.rt.platform.util.TypeCastUtility} that can match basically all "normal" java types.
 * </p>
 * </p>
 * <h3>Input Binds</h3>
 * <p>
 * Input binds are treated as follows
 * <ul>
 * <li><code>:name</code> is bound either directly or as a SQL <code>IN</code> list if it is an array or a
 * {@link java.util.Collection}</li>
 * <li><code>:{name}</code> is a batch value. For every value in the array the statement is executed once</li>
 * <li><code>:tableHolder.column</code> is also a batch value. For every row in the tableHolder the statement is
 * executed once. If a statement contains both <code>tableHolder</code> batch values and normal batch values, the batch
 * row count is determined by the number of rows in the table. Missing normal batch values are filled with
 * <code>null</code>.</li>
 * </ul>
 * </p>
 * <p>
 * Valid SQL bind and Java variable combinations, (bind bases):
 * <table>
 * <tr valign="top">
 * <th>Java</th>
 * <th>Sql</th>
 * <th>Executed Statement(s)</th>
 * </tr>
 * <tr valign="top">
 * <td><code>NVPair("var",String s)</code></td>
 * <td><code>PERSON_NR=:var</code></td>
 * <td><code>PERSON_NR=?</code></td>
 * </tr>
 * <tr valign="top">
 * <td><code>NVPair("var",String[] a)</code></td>
 * <td><code>PERSON_NR=:var</code></td>
 * <td><code><code>PERSON_NR IN (a, b, c, ... )</code></td>
 * </tr>
 * <tr valign="top">
 * <td><code>NVPair("var",Collection list)</code></td>
 * <td><code>PERSON_NR=:var</code></td>
 * <td><code>PERSON_NR IN (a, b, c, ...)</code></td>
 * </tr>
 * <tr valign="top">
 * <td><code>NVPair("var",Collection list)</code></td>
 * <td><code>PERSON_NR=:{var}</code></td>
 * <td>for first list element: <code>PERSON_NR=?</code><br />
 * for second list element: <code>PERSON_NR=?</code></td>
 * </tr>
 * </table>
 * </p>
 * <h3>Output Binds</h3>
 * <ul>
 * <li>Output (<code>SELECT INTO</code>) binds are treated as follows
 * <ul>
 * <li><code>:name</code> is filled by the column value. If there are multiple rows it is filled by an array containing
 * all values of the corresponding column.</li>
 * <li><code>:tableHolder.column</code> is a batch <code>INTO</code> bind. For every row in the select, one tableHolder
 * row is created and filled up.</li>
 * </ul>
 * </li>
 * <li>Output (Stored Procedure Out Parameters) binds are treated as follows
 * <ul>
 * <li><code>:[OUT]name</code> is filled by the single out parameter value<br />
 * Note that <code>:{name}</code> is not a legal <code>INTO</code> bind.</li>
 * </ul>
 * </li>
 * </ul>
 * <h3>Pooling modes</h3>
 * <p>
 * This service supports for two modes of connection pooling<br>
 * <ol>
 * <li>Direct (Scout) simple connection pooling when {@link AbstractSqlService#isDirectJdbcConnection()}==true <br>
 * these properties have a prefix of jdbc... and should only be used when there is no j2ee container supporting
 * {@link javax.sql.DataSource}s</li>
 * <li>J2EE connection pooling when {@link AbstractSqlService#isDirectJdbcConnection()}==true<br>
 * these properties have a prefix of jndi...</li>
 * </ol>
 * </p>
 *
 * @see SQL
 * @see ISqlStyle
 */
public interface ISqlService extends IService {

  /**
   * Indicates the order of the SQL service's {@link IPlatformListener} to destroy itself upon entering platform state
   * {@link State#PlatformStopping}. Any listener depending on database access facility must be configured with an order
   * less than {@link #DESTROY_ORDER}.
   *
   * @see IJobManager#DESTROY_ORDER
   * @see IClusterSynchronizationService#DESTROY_ORDER
   */
  long DESTROY_ORDER = 5_800;

  /**
   * Provides {@link ISqlStyle} to write database independent SQL statements.
   *
   * @return the {@link ISqlStyle} for the database used
   */
  ISqlStyle getSqlStyle();

  /**
   * <p>
   * Selects a normal matrix of data.
   * </p>
   * <p>
   * See the interface comment of {@link ISqlService} for description of how to use bind variables
   * </p>
   *
   * @return a two dimensional array, the first index referencing the rows, the second referencing the column. Cannot be
   *         <code>null</code>
   */
  Object[][] select(String s, Object... bindBases);

  /**
   * Select a normal matrix of data and limit amount of returned rows.
   * <p>
   * See the interface comment of {@link ISqlService} for description of how to use bind variables
   * </p>
   *
   * @param maxRowCount
   *          the maximum number of rows returned
   * @return a two dimensional array, the first index referencing the rows, the second referencing the column. Cannot be
   *         <code>null</code>
   */
  Object[][] selectLimited(String s, int maxRowCount, Object... bindBases);

  /**
   * The <code>INTO</code> binds are filled by the corresponding result column.
   * <p>
   * See the interface comment of {@link ISqlService} for description of how to use bind variables
   * </p>
   *
   * @see #select(String, Object...)
   */
  void selectInto(String s, Object... bindBases);

  /**
   * The <code>INTO</code> binds are filled by the corresponding result column.
   * <p>
   * See the interface comment of {@link ISqlService} for description of how to use bind variables
   * </p>
   *
   * @see #selectLimited(String, int, Object...)
   */
  void selectIntoLimited(String s, int maxRowCount, Object... bindBases);

  /**
   * The callback can handle each row individually and therefore stream data
   * <p>
   * See the interface comment of {@link ISqlService} for description of how to use bind variables
   * </p>
   *
   * @param handler
   *          callback to handle individual rows
   * @see #select(String, Object...)
   */
  void selectStreaming(String s, ISelectStreamHandler handler, Object... bindBases);

  /**
   * The callback can handle each row individually and therefore stream data
   * <p>
   * See the interface comment of {@link ISqlService} for description of how to use bind variables
   * </p>
   *
   * @param handler
   *          callback to handle individual rows
   * @see #selectLimited(String, int, Object...)
   */
  void selectStreamingLimited(String s, ISelectStreamHandler handler, int maxRowCount, Object... bindBases);

  /**
   * insert rows
   * <p>
   * See the interface comment of {@link ISqlService} for description of how to use bind variables
   * </p>
   *
   * @return number of inserted rows
   */
  int insert(String s, Object... bindBases);

  /**
   * update rows
   * <p>
   * See the interface comment of {@link ISqlService} for description of how to use bind variables
   * </p>
   *
   * @return number of updated rows
   */
  int update(String s, Object... bindBases);

  /**
   * delete rows
   * <p>
   * See the interface comment of {@link ISqlService} for description of how to use bind variables
   * </p>
   *
   * @return number of deleted rows
   */
  int delete(String s, Object... bindBases);

  /**
   * call a stored procedure
   * <p>
   * See the interface comment of {@link ISqlService} for description of how to use bind variables
   * </p>
   *
   * @return result code
   */
  boolean callStoredProcedure(String s, Object... bindBases);

  /**
   * <p>
   * Normally a <code>commit</code> is not necessary since all SQL is running inside the service request xa transaction.
   * That means all SQL executed in the same service request is guaranteed to run on the same connection.
   * </p>
   * <p>
   * When the service completes work without an exception, a xa commit is done on ALL used service request resources.
   * </p>
   */
  void commit();

  /**
   * <p>
   * Normally not necessary. When the service completes work with an exception, a xa rollback is done on ALL used
   * service request resources
   * </p>
   *
   * @see #commit()
   */
  void rollback();

  /**
   * @return the next value from a given sequence
   */
  Long getSequenceNextval(String sequenceName);

  /**
   * Creates plaintext SQL from the given SQL part by plaining all binds
   *
   * @see ISqlStyle#createPlainText()
   */
  String createPlainText(String s, Object... bindBases);

  String getTransactionMemberId();

  /**
   * @return current connection from {@link org.eclipse.scout.rt.platform.transaction.ITransactionMember} that is
   *         registered inside the {@link org.eclipse.scout.rt.platform.transaction.ITransaction}.
   */
  Connection getConnection();

}
