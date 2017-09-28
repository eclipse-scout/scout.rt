/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.jdbc;

import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.server.commons.healthcheck.AbstractHealthChecker;
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
  protected boolean execCheckHealth() throws Exception {
    ISqlStyle s = SQL.getSqlStyle();
    if (s != null) {
      s.testConnection(SQL.getConnection());
      return true;
    }
    return false;
  }

}
