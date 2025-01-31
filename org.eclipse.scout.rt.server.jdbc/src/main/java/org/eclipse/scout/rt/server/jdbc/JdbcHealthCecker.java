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

import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.server.commons.healthcheck.AbstractHealthChecker;
import org.eclipse.scout.rt.server.commons.healthcheck.HealthCheckCategoryId;
import org.eclipse.scout.rt.server.jdbc.style.ISqlStyle;

/**
 * Basic JDBC Health Check using {@link ISqlStyle#testConnection(java.sql.Connection)} to verify database connectivity.
 *
 * @since 6.1
 */
public class JdbcHealthCecker extends AbstractHealthChecker {

  @Override
  protected long getConfiguredTimeoutMillis() {
    return TimeUnit.MINUTES.toMillis(1);
  }

  @Override
  protected boolean execCheckHealth(HealthCheckCategoryId category) throws Exception {
    ISqlStyle s = SQL.getSqlStyle();
    if (s != null) {
      s.testConnection(SQL.getConnection());
      return true;
    }
    return false;
  }
}
