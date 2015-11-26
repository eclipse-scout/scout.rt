/*******************************************************************************
 * Copyright (c) 2010,2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.jdbc.mssql;

import java.sql.Connection;
import java.sql.SQLException;

import org.eclipse.scout.rt.server.jdbc.style.AbstractSqlStyle;

/**
 * @since Build 206
 */
public class MSSQLSqlStyle extends AbstractSqlStyle {
  private static final long serialVersionUID = 1L;

  @Override
  public String getConcatOp() {
    return "+";
  }

  @Override
  public String getLikeWildcard() {
    return "%";
  }

  @Override
  protected int getMaxListSize() {
    return MAX_LIST_SIZE;
  }

  @Override
  public boolean isLargeString(String s) {
    return (s.length() > MAX_SQL_STRING_LENGTH);
  }

  @Override
  public boolean isBlobEnabled() {
    return true;
  }

  @Override
  public boolean isClobEnabled() {
    return true;
  }

  @Override
  public String createDateTimeIsNow(String attribute) {
    return "TRUNC(" + attribute + ", 'MI')=TRUNC(SYSDATE, 'MI')";
  }

  @Override
  public String createDateTimeIsNotNow(String attribute) {
    return "TRUNC(" + attribute + ", 'MI')!=TRUNC(SYSDATE, 'MI')";
  }

  @Override
  public void testConnection(Connection conn) throws SQLException {
    //TODO what is the name of a dummy table or typical check statement to run as a test?
  }
}
