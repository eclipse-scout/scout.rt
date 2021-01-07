/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
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
    try (Connection connection = DriverManager.getConnection(commands)) {
      LOG.debug("Executed derby command '{}' on connection '{}'.", commands, connection);
    }
  }

  public void dropDB() {
    try {
      runDerbyCommand(getJdbcMappingName() + ";drop=true");
    }
    catch (SQLException e) {
      throw BEANS.get(PlatformExceptionTranslator.class).translate(e);
    }
  }

  @Override
  public void destroy() {
    try {
      super.destroy(); // Destroy connection pool first
    }
    finally {
      shutdownDB();
    }
  }

  @Override
  protected void deregisterDriver() {
    // nop
    // do not deregister the derby driver in destroy() because the driver is still used in shutdownDB() for the shutdown command.
    // it will be de-registered by the shutdown command
  }

  protected void shutdownDB() {
    // How to embed derby: https://db.apache.org/derby/papers/DerbyTut/embedded_intro.html
    // Shutdown: https://db.apache.org/derby/docs/10.9/devguide/tdevdvlp40464.html
    // completely shutdown the database AND the engine.
    // Shutting down only this DB instance would not shutdown the engine and leaks threads and memory. Therefore use full shutdown command.
    String derbyShutdownCommand = "jdbc:derby:;shutdown=true";
    try {
      runDerbyCommand(derbyShutdownCommand); // Shutdown derby
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
