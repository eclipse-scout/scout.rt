/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.jdbc.mysql;

import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.server.jdbc.AbstractSqlService;
import org.eclipse.scout.rt.server.jdbc.style.ISqlStyle;

public abstract class AbstractMySqlSqlService extends AbstractSqlService {

  @ConfigProperty(ConfigProperty.SQL_STYLE)
  @Order(80)
  @Override
  protected Class<? extends ISqlStyle> getConfiguredSqlStyle() {
    return MySqlSqlStyle.class;
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(100)
  @Override
  protected String getConfiguredJdbcDriverName() {
    return "com.mysql.jdbc.Driver";
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(110)
  @Override
  protected String getConfiguredJdbcMappingName() {
    return "jdbc:mysql://[host][:port]/[database]";
  }
}
