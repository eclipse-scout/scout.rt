/*
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.server.jdbc.oracle;

import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.server.jdbc.AbstractSqlService;
import org.eclipse.scout.rt.server.jdbc.style.ISqlStyle;

public abstract class AbstractOracleSqlService extends AbstractSqlService {

  @ConfigProperty(ConfigProperty.SQL_STYLE)
  @Order(80)
  @Override
  protected Class<? extends ISqlStyle> getConfiguredSqlStyle() {
    return OracleSqlStyle.class;
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(100)
  @Override
  protected String getConfiguredJdbcDriverName() {
    return "oracle.jdbc.OracleDriver";
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(110)
  @Override
  protected String getConfiguredJdbcMappingName() {
    return "jdbc:oracle:thin:@[host][:port]/[database]";
  }
}
