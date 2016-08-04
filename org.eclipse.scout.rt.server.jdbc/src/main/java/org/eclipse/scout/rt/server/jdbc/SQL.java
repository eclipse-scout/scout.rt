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
import java.sql.SQLException;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.server.jdbc.style.ISqlStyle;

/**
 * <p>
 * This is a convenience class to use {@link ISqlService}.
 * </p>
 * <p>
 * It is just wrapper for a service class implementing {@link ISqlService} and contains almost no logic on its own.
 * Other references would be {@link ISqlStyle} to generate SQL code independent of the used database.
 * </p>
 * <h4>Usage</h4>
 * <p>
 * Example:
 *
 * <pre>
 * Object[][] queryResult = SQL.select(&quot;SELECT PERSON_NR, PERSON_NAME FROM PERSON&quot;);
 * Long firstPersonNr = queryResult[0][0];
 * Long thirdPersonName = queryResult[2][1];
 * </pre>
 *
 * Without this class the necessary code would look like this
 *
 * <pre>
 * ISqlService service = BEANS.get(ISqlService.class);
 * Object[][] queryResult = service.select(&quot;SELECT PERSON_NR, PERSON_NAME FROM PERSON&quot;);
 * Long firstPersonNr = queryResult[0][0];
 * Long thirdPersonName = queryResult[2][1];
 * </pre>
 * </p>
 *
 * @see ISqlService
 */
public final class SQL {
  public static Class<? extends ISqlService> usedServiceType = ISqlService.class;

  private SQL() {
  }

  /**
   * <p>
   * Access to the driver name
   * </p>
   * <p>
   * Note: {@link ProcessingException} and {@link SQLException} are caught without logging or rethrowing them again.
   * </p>
   *
   * @return the name of the JDBC driver used
   */
  public static String getDriverName() {
    String driverName = null;
    try {
      @SuppressWarnings("resource")
      Connection conn = getConnection();
      driverName = conn.getMetaData().getDriverName();
    }
    catch (SQLException | RuntimeException e) { // NOSONAR
      // nop
    }
    return driverName;
  }

  public static Connection getConnection() {
    ISqlService service = BEANS.get(usedServiceType);
    return service.getConnection();
  }

  public static ISqlStyle getSqlStyle() {
    ISqlService service = BEANS.get(usedServiceType);
    return service.getSqlStyle();
  }

  /**
   * @see ISqlService#select(String, Object...)
   */
  public static Object[][] select(String s, Object... bindBases) {
    ISqlService service = BEANS.get(usedServiceType);
    return service.select(s, bindBases);
  }

  /**
   * @see ISqlService#selectLimited(String, int, Object...)
   */
  public static Object[][] selectLimited(String s, int maxRowCount, Object... bindBases) {
    ISqlService service = BEANS.get(usedServiceType);
    return service.selectLimited(s, maxRowCount, bindBases);
  }

  /**
   * @see ISqlService#selectInto(String, Object...)
   */
  public static void selectInto(String s, Object... bindBases) {
    ISqlService service = BEANS.get(usedServiceType);
    service.selectInto(s, bindBases);
  }

  /**
   * @see ISqlService#selectIntoLimited(String, int, Object...)
   */
  public static void selectIntoLimited(String s, int maxRowCount, Object... bindBases) {
    ISqlService service = BEANS.get(usedServiceType);
    service.selectIntoLimited(s, maxRowCount, bindBases);
  }

  /**
   * @see ISqlService#insert(String, Object...)
   */
  public static int insert(String s, Object... bindBases) {
    ISqlService service = BEANS.get(usedServiceType);
    return service.insert(s, bindBases);
  }

  /**
   * @see ISqlService#update(String, Object...)
   */
  public static int update(String s, Object... bindBases) {
    ISqlService service = BEANS.get(usedServiceType);
    return service.update(s, bindBases);
  }

  /**
   * @see ISqlService#delete(String, Object...)
   */
  public static int delete(String s, Object... bindBases) {
    ISqlService service = BEANS.get(usedServiceType);
    return service.delete(s, bindBases);
  }

  /**
   * @see ISqlService#callStoredProcedure(String, Object...)
   */
  public static boolean callStoredProcedure(String s, Object... bindBases) {
    ISqlService service = BEANS.get(usedServiceType);
    return service.callStoredProcedure(s, bindBases);
  }

  /**
   * @see ISqlService#createPlainText(String, Object...)
   */
  public static String createPlainText(String s, Object... bindBases) {
    ISqlService service = BEANS.get(usedServiceType);
    return service.createPlainText(s, bindBases);
  }

  /**
   * @see ISqlService#commit()
   */
  public static void commit() {
    ISqlService service = BEANS.get(usedServiceType);
    service.commit();
  }

  /**
   * @see ISqlService#rollback()
   */
  public static void rollback() {
    ISqlService service = BEANS.get(usedServiceType);
    service.rollback();
  }

  /**
   * @see ISqlService#getSequenceNextval(String)
   */
  public static Long getSequenceNextval(String sequenceName) {
    ISqlService service = BEANS.get(usedServiceType);
    return service.getSequenceNextval(sequenceName);
  }

}
