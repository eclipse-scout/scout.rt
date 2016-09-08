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
      return "org.eclipse.scout.rt.server.services.common.jdbc.AbstractSqlService#transactionMemberId";
    }
  }

  public static class SqlJndiNameProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "org.eclipse.scout.rt.server.services.common.jdbc.AbstractSqlService#jndiName";
    }
  }

  public static class SqlJndiInitialContextFactoryProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "org.eclipse.scout.rt.server.services.common.jdbc.AbstractSqlService#jndiInitialContextFactory";
    }
  }

  public static class SqlJndiProviderUrlProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "org.eclipse.scout.rt.server.services.common.jdbc.AbstractSqlService#jndiProviderUrl";
    }
  }

  public static class SqlJndiUrlPkgPrefixesProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "org.eclipse.scout.rt.server.services.common.jdbc.AbstractSqlService#jndiUrlPkgPrefixes";
    }
  }

  public static class SqlJdbcMappingNameProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "org.eclipse.scout.rt.server.services.common.jdbc.AbstractSqlService#jdbcMappingName";
    }
  }

  public static class SqlJdbcDriverNameProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "org.eclipse.scout.rt.server.services.common.jdbc.AbstractSqlService#jdbcDriverName";
    }
  }

  public static class SqlJdbcPropertiesProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "org.eclipse.scout.rt.server.services.common.jdbc.AbstractSqlService#jdbcProperties";
    }
  }

  public static class SqlUsernameProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "org.eclipse.scout.rt.server.services.common.jdbc.AbstractSqlService#username";
    }
  }

  public static class SqlPasswordProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "org.eclipse.scout.rt.server.services.common.jdbc.AbstractSqlService#password";
    }
  }

  public static class SqlDirectJdbcConnectionProperty extends AbstractBooleanConfigProperty {

    @Override
    public String getKey() {
      return "org.eclipse.scout.rt.server.services.common.jdbc.AbstractSqlService#directJdbcConnection";
    }
  }

  public static class SqlJdbcPoolConnectionLifetimeProperty extends AbstractPositiveLongConfigProperty {

    @Override
    public String getKey() {
      return "org.eclipse.scout.rt.server.services.common.jdbc.AbstractSqlService#jdbcPoolConnectionLifetime";
    }
  }

  public static class SqlJdbcPoolConnectionBusyTimeoutProperty extends AbstractPositiveLongConfigProperty {

    @Override
    public String getKey() {
      return "org.eclipse.scout.rt.server.services.common.jdbc.AbstractSqlService#jdbcPoolConnectionBusyTimeout";
    }
  }

  public static class SqlJdbcStatementCacheSizeProperty extends AbstractPositiveIntegerConfigProperty {

    @Override
    public String getKey() {
      return "org.eclipse.scout.rt.server.services.common.jdbc.AbstractSqlService#jdbcStatementCacheSize";
    }
  }

  public static class SqlJdbcPoolSizeProperty extends AbstractPositiveIntegerConfigProperty {

    @Override
    public String getKey() {
      return "org.eclipse.scout.rt.server.services.common.jdbc.AbstractSqlService#jdbcPoolSize";
    }
  }

  /**
   * Indicates whether to uninstall JDBC driver upon platform shutdown (<code>true</code> by default).
   * <p>
   * This property has no effect if working with JNDI JDBC connections.
   */
  public static class SqlJdbcDriverUnloadProperty extends AbstractBooleanConfigProperty {

    @Override
    public String getKey() {
      return "org.eclipse.scout.rt.server.services.common.jdbc.AbstractSqlService#jdbcDriverUnload";
    }

    @Override
    protected Boolean getDefaultValue() {
      return Boolean.TRUE;
    }
  }
}
