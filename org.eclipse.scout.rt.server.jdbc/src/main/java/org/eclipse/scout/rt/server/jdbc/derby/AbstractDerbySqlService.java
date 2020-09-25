/*******************************************************************************
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.jdbc.derby;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.PlatformExceptionTranslator;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.server.jdbc.AbstractSqlService;
import org.eclipse.scout.rt.server.jdbc.style.ISqlStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cause Derby supports no sequences, we model this behavior with a table with only one column AND entry of type BIGINT
 * (or other numeric type). This table needs to have at the beginning this one entry. The default column name is
 * "LAST_VAL". With the method {@link AbstractDerbySqlService#getConfiguredSequenceColumnName()} one can customize the
 * name of this column. NOTE: With "CREATE SYNONYM DUAL FOR SYSIBM.SYSDUMMY1" one can better reuse Oracle styled SQL.
 */
public abstract class AbstractDerbySqlService extends AbstractSqlService {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractDerbySqlService.class);

  protected String getConfiguredSequenceColumnName() {
    return "LAST_VAL";
  }

  @Override
  protected String getConfiguredJdbcDriverName() {
    return "org.apache.derby.jdbc.EmbeddedDriver";
  }

  @Override
  public Long getSequenceNextval(String sequenceName) {
    //increase
    String update = "UPDATE " + sequenceName + " SET " + getConfiguredSequenceColumnName() + " = " + getConfiguredSequenceColumnName() + " + 1";
    createStatementProcessor(update, null, 0).processModification(getTransaction(), getStatementCache(), null);

    //read
    return super.getSequenceNextval(sequenceName);
  }

  @Override
  protected String getSequenceNextvalStatement(String sequenceName) {
    return "SELECT " + getConfiguredSequenceColumnName() + " FROM " + sequenceName;
  }

  @Override
  @SuppressWarnings("bsiRulesDefinition:htmlInString")
  protected String getConfiguredJdbcMappingName() {
    return "jdbc:derby:<path to db>";
  }

  @Override
  protected Class<? extends ISqlStyle> getConfiguredSqlStyle() {
    return DerbySqlStyle.class;
  }

  protected void runDerbyCommand(String commands) throws SQLException {
    Assertions.assertTrue(StringUtility.hasText(commands), "Commands must be provided.");
    try (Connection connection = DriverManager.getConnection(getJdbcMappingName() + commands)) {
      LOG.debug("Executed derby command '{}' on connection '{}'.", commands, connection);
    }
  }

  public void dropDB() {
    try {
      runDerbyCommand(";drop=true");
    }
    catch (SQLException e) {
      throw BEANS.get(PlatformExceptionTranslator.class).translate(e);
    }
  }

  @Override
  public void destroy() {
    try {
      // Destroy connection pool first
      super.destroy(); // Expects the property scout.sql.jdbc.driverUnload to be set to false, otherwise super.destroy() will throw an exception
    }
    finally {
      shutdownDB();
    }
  }

  protected void shutdownDB() {
    // How to embed derby: https://db.apache.org/derby/papers/DerbyTut/embedded_intro.html
    // Shutdown: https://docs.oracle.com/javadb/10.8.3.0/ref/rrefattrib16471.html
    try {
      runDerbyCommand(";shutdown=true"); // Shutdown derby
    }
    catch (SQLException e) {
      boolean shutDownSuccessful = (e.getErrorCode() == 50000 && "XJ015".equals(e.getSQLState())) // error codes for successful full Derby shutdown
          || (e.getErrorCode() == 45000 && "08006".equals(e.getSQLState())); // error code for successful single Derby database shutdown
      if (shutDownSuccessful) {
        // According to the embedding HowTo above: A clean shutdown throws SQL exceptions, which can be ignored
        LOG.debug("Derby database shutdown completed successfully.", e);
      }
      else {
        LOG.warn("Exception while shutting down derby", e);
      }
    }
  }
}
