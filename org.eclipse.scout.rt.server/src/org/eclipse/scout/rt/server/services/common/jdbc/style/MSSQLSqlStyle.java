/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.services.common.jdbc.style;

import java.sql.Connection;
import java.sql.SQLException;

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
    return 1000;
  }

  public boolean isLargeString(String s) {
    return (s.length() > 4000);
  }

  public boolean isBlobEnabled() {
    return true;
  }

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

  public void testConnection(Connection conn) throws SQLException {
    /*
     * Statement testStatement=null;
     * try{ testStatement=conn.createStatement();
     * testStatement.execute("SELECT 1 FROM DUAL");
     * //TODO what is the name of a dummy table or typical check statement to run as a test?
     * }
     * finally{
     * if(testStatement!=null) try{testStatement.close();}catch(Throwable t){}
     * }
     */
  }

}
