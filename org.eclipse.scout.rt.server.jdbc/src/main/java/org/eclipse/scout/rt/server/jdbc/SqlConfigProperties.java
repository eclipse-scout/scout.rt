/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.jdbc;

import org.eclipse.scout.rt.platform.config.AbstractBooleanConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractPositiveIntegerConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractPositiveLongConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractStringConfigProperty;

/**
 * Config properties for org.eclipse.scout.rt.server.jdbc
 */
public final class SqlConfigProperties {

  private SqlConfigProperties() {
  }

  public static class SqlTransactionMemberIdProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "scout.sql.transactionMemberId";
    }

    @Override
    public String description() {
      return "Id of the transaction member on which the connection is available.";
    }
  }

  public static class SqlJndiNameProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "scout.sql.jndi.name";
    }

    @Override
    public String description() {
      return "The name of the object to lookup in the JNDI context. Default is null.";
    }
  }

  public static class SqlJndiInitialContextFactoryProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "scout.sql.jndi.initialContextFactory";
    }

    @Override
    public String description() {
      return "The name of the object to lookup in the JNDI context. Default is null.";
    }
  }

  public static class SqlJndiProviderUrlProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "scout.sql.jndi.providerUrl";
    }

    @Override
    public String description() {
      return "JNDI provider url (e.g. 'ldap://somehost:389'). Default is null.";
    }
  }

  public static class SqlJndiUrlPkgPrefixesProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "scout.sql.jndi.urlPkgPrefixes";
    }

    @Override
    public String description() {
      return "A colon-separated list of package prefixes for the class name of the factory class that will create a URL context factory. Default is null.";
    }
  }

  public static class SqlJdbcMappingNameProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "scout.sql.jdbc.mappingName";
    }

    @Override
    public String description() {
      return "The JDBC mapping name. By default 'jdbc:oracle:thin:@localhost:1521:ORCL' is used.";
    }
  }

  public static class SqlJdbcDriverNameProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "scout.sql.jdbc.driverName";
    }

    @Override
    public String description() {
      return "The driver name to use. By default 'oracle.jdbc.OracleDriver' is used.";
    }
  }

  public static class SqlJdbcPropertiesProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "scout.sql.jdbc.properties";
    }

    @Override
    public String description() {
      return "Semicolon separated list of properties to pass to the JDBC connection. The default value is null. E.g.: key1=val1;key2=val2";
    }
  }

  public static class SqlUsernameProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "scout.sql.username";
    }

    @Override
    public String description() {
      return "The username to connect to the database (JDBC or JNDI)";
    }
  }

  public static class SqlPasswordProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "scout.sql.password";
    }

    @Override
    public String description() {
      return "The password to connect to the database (JDBC or JNDI)";
    }
  }

  public static class SqlDirectJdbcConnectionProperty extends AbstractBooleanConfigProperty {

    @Override
    public String getKey() {
      return "scout.sql.directJdbcConnection";
    }

    @Override
    public String description() {
      return "If true a direct JDBC connection is created. Otherwise, a JNDI connection is used. The default value is true.";
    }
  }

  public static class SqlJdbcPoolConnectionLifetimeProperty extends AbstractPositiveLongConfigProperty {

    @Override
    public String getKey() {
      return "scout.sql.jdbc.pool.connectionIdleTimeout";
    }

    @Override
    public String description() {
      return "Idle connections will be closed after this timeout in milliseconds. The default value is 5 minutes.";
    }
  }

  public static class SqlJdbcPoolConnectionBusyTimeoutProperty extends AbstractPositiveLongConfigProperty {

    @Override
    public String getKey() {
      return "scout.sql.jdbc.pool.connectionBusyTimeout";
    }

    @Override
    public String description() {
      return "Connections will be closed after this timeout in milliseconds even if the connection is still busy. The default value is 6 hours.";
    }
  }

  public static class SqlJdbcStatementCacheSizeProperty extends AbstractPositiveIntegerConfigProperty {

    @Override
    public String getKey() {
      return "scout.sql.jdbc.statementCacheSize";
    }

    @Override
    public String description() {
      return "Maximum number of cached SQL statements. The default value is 25.";
    }
  }

  public static class SqlJdbcPoolSizeProperty extends AbstractPositiveIntegerConfigProperty {

    @Override
    public String getKey() {
      return "scout.sql.jdbc.pool.size";
    }

    @Override
    public String description() {
      return "The maximum number of connections to create. The default pool size is 25.";
    }
  }
}
