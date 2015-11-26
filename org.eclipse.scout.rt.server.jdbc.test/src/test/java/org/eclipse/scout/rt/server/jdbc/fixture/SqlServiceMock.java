/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.jdbc.fixture;

import java.sql.Connection;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.rt.server.jdbc.AbstractSqlService;
import org.eclipse.scout.rt.server.jdbc.SQL;
import org.eclipse.scout.rt.server.jdbc.internal.exec.PreparedStatementCache;
import org.eclipse.scout.rt.server.jdbc.parsers.sql.SqlFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlServiceMock extends AbstractSqlService {
  private static Logger logger = LoggerFactory.getLogger(SqlServiceMock.class);

  private final StringBuffer m_protocol;
  private Object[][] m_resultData;

  public SqlServiceMock() {
    m_protocol = new StringBuffer();
  }

  public StringBuffer getProtocol() {
    return m_protocol;
  }

  public void clearProtocol() {
    m_protocol.setLength(0);
  }

  /**
   * set the data that should be returned by any query that is executed in the following
   */
  public void setResultData(Object[][] resultData) {
    m_resultData = resultData;
  }

  public Object[][] getResultData() {
    return m_resultData;
  }

  private void createPlainTextLog(String s, Object... bindBases) {
    if (logger.isInfoEnabled()) {
      String plainTextSql = SQL.createPlainText(s, bindBases);
      if (StringUtility.hasText(plainTextSql)) {
        StringBuffer callLoc = new StringBuffer();
        try {
          StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
          if (stackTrace.length > 4) {
            callLoc.append(" ").append(stackTrace[4].getClassName());
            callLoc.append(".").append(stackTrace[4].getMethodName());
            callLoc.append("(").append(stackTrace[4].getFileName());
            callLoc.append(":").append(stackTrace[4].getLineNumber());
            callLoc.append(")");
          }
        }
        catch (Throwable t) {
        }
        String w = SqlFormatter.wellform(plainTextSql);
        logger.info("SQL Log:\n" + w);
        logger.info(callLoc.toString());
      }
    }
  }

  @Override
  public Object[][] select(String s, Object... bindBases) {
    return createStatementProcessor(s, bindBases, 0).processSelect(getTransaction(), new PreparedStatementCache(1), null);
  }

  @Override
  public void selectInto(String s, Object... bindBases) {
    createPlainTextLog(s, bindBases);
    createStatementProcessor(s, bindBases, 0).processSelectInto(getTransaction(), new PreparedStatementCache(1), null);
  }

  @Override
  public int update(String s, Object... bindBases) {
    createPlainTextLog(s, bindBases);
    return createStatementProcessor(s, bindBases, 0).processModification(getTransaction(), new PreparedStatementCache(1), null);
  }

  @Override
  protected Connection getTransaction() {
    return new ConnectionMock(m_protocol, getResultData()).getConnection();
  }
}
