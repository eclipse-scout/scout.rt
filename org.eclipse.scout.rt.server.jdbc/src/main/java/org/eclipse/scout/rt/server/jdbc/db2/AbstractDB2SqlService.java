/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.jdbc.db2;

import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.server.jdbc.AbstractSqlService;
import org.eclipse.scout.rt.server.jdbc.style.ISqlStyle;

public abstract class AbstractDB2SqlService extends AbstractSqlService {

  @ConfigProperty(ConfigProperty.SQL_STYLE)
  @Order(80)
  @Override
  protected Class<? extends ISqlStyle> getConfiguredSqlStyle() {
    return DB2SqlStyle.class;
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(100)
  @Override
  protected String getConfiguredJdbcDriverName() {
    return "com.ibm.db2.jcc.DB2Driver";
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(110)
  @Override
  protected String getConfiguredJdbcMappingName() {
    return "jdbc:db2//[host][:port]/[database]";
  }

  @Override
  protected String getSequenceNextvalStatement(String sequenceName) {
    return "SELECT " + sequenceName + ".NEXTVAL FROM SYSIBM.SYSDUMMY1 ";
  }
}
