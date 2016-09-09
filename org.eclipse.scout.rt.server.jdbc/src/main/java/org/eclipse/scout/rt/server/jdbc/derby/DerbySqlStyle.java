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
package org.eclipse.scout.rt.server.jdbc.derby;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.eclipse.scout.rt.server.jdbc.style.AbstractSqlStyle;

/**
 * Note: This class is mostly untested.
 */
public class DerbySqlStyle extends AbstractSqlStyle {
  private static final long serialVersionUID = 1L;

  private static final String CAST_SQL_METHOD = "CAST(";

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
  public void testConnection(Connection conn) throws SQLException {
    try (Statement testStatement = conn.createStatement()) {
      testStatement.execute("SELECT 1 FROM SYSIBM.SYSDUMMY1"); //same as VALUES(1)
    }
  }

  //note: fn timestampadd:
  //You should not put a datetime column inside of a timestamp arithmetic function in WHERE clauses because the optimizer will not use any index on the column.
  @Override
  public String createDateIsToday(String attribute) {
    return CAST_SQL_METHOD + attribute + " AS DATE) >= CURRENT_DATE AND " + CAST_SQL_METHOD + attribute + " AS DATE) < " + CAST_SQL_METHOD + "{FN TIMESTAMPADD(SQL_TSI_DAY, 1, CURRENT_TIMESTAMP)} AS DATE)";
  }

  @Override
  public String createDateIsInLastDays(String attribute, String bindName) {
    return CAST_SQL_METHOD + attribute + " AS DATE) >= " + CAST_SQL_METHOD + "{FN TIMESTAMPADD(SQL_TSI_DAY, :" + bindName + ", CURRENT_TIMESTAMP)} AS DATE) AND " + CAST_SQL_METHOD + attribute + " AS DATE) < " + CAST_SQL_METHOD
        + "{FN TIMESTAMPADD(SQL_TSI_DAY, 1, CURRENT_TIMESTAMP)} AS DATE)";
  }

  @Override
  public String createDateIsInNextDays(String attribute, String bindName) {
    return CAST_SQL_METHOD + attribute + " AS DATE) >= CURRENT_DATE AND " + CAST_SQL_METHOD + attribute + " AS DATE) < " + CAST_SQL_METHOD + "{FN TIMESTAMPADD(SQL_TSI_DAY, :" + bindName + " + 1, CURRENT_TIMESTAMP)} AS DATE)";
  }

  @Override
  public String createDateIsInDays(String attribute, String bindName) {
    return CAST_SQL_METHOD + attribute + " AS DATE) >= " + CAST_SQL_METHOD + "{FN TIMESTAMPADD(SQL_TSI_DAY, :" + bindName + ", CURRENT_TIMESTAMP)} AS DATE) AND " + CAST_SQL_METHOD + attribute + " AS DATE) < " + CAST_SQL_METHOD
        + "{FN TIMESTAMPADD(SQL_TSI_DAY, :" + bindName + " + 1, CURRENT_TIMESTAMP)} AS DATE)";
  }

  @Override
  public String createDateIsInWeeks(String attribute, String bindName) {
    return CAST_SQL_METHOD + attribute + " AS DATE) >= " + CAST_SQL_METHOD + "{FN TIMESTAMPADD(SQL_TSI_WEEK, :" + bindName + ", CURRENT_TIMESTAMP)} AS DATE) AND " + CAST_SQL_METHOD + attribute + " AS DATE) < " + CAST_SQL_METHOD
        + "{FN TIMESTAMPADD(SQL_TSI_DAY, 1, {FN TIMESTAMPADD(SQL_TSI_WEEK, :" + bindName + ", CURRENT_TIMESTAMP)})} AS DATE)";
  }

  @Override
  public String createDateIsInLastMonths(String attribute, String bindName) {
    return CAST_SQL_METHOD + attribute + " AS DATE) >= " + CAST_SQL_METHOD + "{FN TIMESTAMPADD(SQL_TSI_MONTH,(-1)*:" + bindName + ", CURRENT_TIMESTAMP)} AS DATE) AND " + CAST_SQL_METHOD + attribute + " AS DATE) < " + CAST_SQL_METHOD
        + "{FN TIMESTAMPADD(SQL_TSI_DAY, 1, CURRENT_TIMESTAMP)} AS DATE)";
  }

  @Override
  public String createDateIsInNextMonths(String attribute, String bindName) {
    return CAST_SQL_METHOD + attribute + " AS DATE) >= CURRENT_DATE AND " + CAST_SQL_METHOD + attribute + " AS DATE) < " + CAST_SQL_METHOD + "{FN TIMESTAMPADD(SQL_TSI_DAY, 1, {FN TIMESTAMPADD(SQL_TSI_MONTH, :" + bindName
        + ", CURRENT_TIMESTAMP)})} AS DATE)";
  }

  @Override
  public String createDateIsInMonths(String attribute, String bindName) {
    return CAST_SQL_METHOD + attribute + " AS DATE) >= " + CAST_SQL_METHOD + "{FN TIMESTAMPADD(SQL_TSI_MONTH,:" + bindName + ", CURRENT_TIMESTAMP)} AS DATE) AND " + CAST_SQL_METHOD + attribute + " AS DATE) < " + CAST_SQL_METHOD
        + "{FN TIMESTAMPADD(SQL_TSI_DAY, 1, {FN TIMESTAMPADD(SQL_TSI_MONTH, :" + bindName + ", CURRENT_TIMESTAMP)})} AS DATE)";
  }

  @Override
  public String createDateIsInLEDays(String attribute, String bindName) {
    return CAST_SQL_METHOD + attribute + " AS DATE) < " + CAST_SQL_METHOD + "{FN TIMESTAMPADD(SQL_TSI_DAY,:" + bindName + " + 1, CURRENT_TIMESTAMP)} AS DATE)";
  }

  @Override
  public String createDateIsInLEWeeks(String attribute, String bindName) {
    return CAST_SQL_METHOD + attribute + " AS DATE) < " + CAST_SQL_METHOD + "{FN TIMESTAMPADD(SQL_TSI_DAY, 1, {FN TIMESTAMPADD(SQL_TSI_WEEK, :" + bindName + ", CURRENT_TIMESTAMP)})} AS DATE)";
  }

  @Override
  public String createDateIsInLEMonths(String attribute, String bindName) {
    return CAST_SQL_METHOD + attribute + " AS DATE) < " + CAST_SQL_METHOD + "{FN TIMESTAMPADD(SQL_TSI_DAY, 1, {FN TIMESTAMPADD(SQL_TSI_MONTH, :" + bindName + ", CURRENT_TIMESTAMP)})} AS DATE)";
  }

  @Override
  public String createDateIsInGEDays(String attribute, String bindName) {
    return CAST_SQL_METHOD + attribute + " AS DATE) >= " + CAST_SQL_METHOD + "{FN TIMESTAMPADD(SQL_TSI_DAY,:" + bindName + ", CURRENT_TIMESTAMP)} AS DATE)";
  }

  @Override
  public String createDateIsInGEWeeks(String attribute, String bindName) {
    return CAST_SQL_METHOD + attribute + " AS DATE) >= " + CAST_SQL_METHOD + "{FN TIMESTAMPADD(SQL_TSI_WEEK, :" + bindName + ", CURRENT_TIMESTAMP)} AS DATE)";
  }

  @Override
  public String createDateIsInGEMonths(String attribute, String bindName) {
    return CAST_SQL_METHOD + attribute + " AS DATE) >= " + CAST_SQL_METHOD + "{FN TIMESTAMPADD(SQL_TSI_MONTH, :" + bindName + ", CURRENT_TIMESTAMP)} AS DATE)";
  }

  @Override
  public String createDateIsNotToday(String attribute) {
    return "(" + CAST_SQL_METHOD + attribute + " AS DATE) < CURRENT_DATE OR " + attribute + ">= " + CAST_SQL_METHOD + "{FN TIMESTAMPADD(SQL_TSI_DAY, 1, CURRENT_TIMESTAMP)} AS DATE))";
  }

  //attribute has to be of type timestamp. a cast from date/time to timestamp is not supported by derby, but planned to be implemented(?). (These casts are valid according to SQL standard (1999))
  @Override
  public String createDateTimeIsNow(String attribute) {
    return attribute + " >= {FN TIMESTAMPADD(SQL_TSI_MINUTE, -1, CURRENT_TIMESTAMP)} AND " + attribute + " < {FN TIMESTAMPADD(SQL_TSI_MINUTE, 1, CURRENT_TIMESTAMP)}";
  }

  @Override
  public String createDateTimeIsInLEMinutes(String attribute, String bindName) {
    return attribute + " < {FN TIMESTAMPADD(SQL_TSI_MINUTE, :" + bindName + "+1, CURRENT_TIMESTAMP)}";
  }

  @Override
  public String createDateTimeIsInLEHours(String attribute, String bindName) {
    return attribute + " < {FN TIMESTAMPADD(SQL_TSI_HOUR, :" + bindName + "+1, CURRENT_TIMESTAMP)}";
  }

  @Override
  public String createDateTimeIsInGEMinutes(String attribute, String bindName) {
    return attribute + " >= {FN TIMESTAMPADD(SQL_TSI_MINUTE, :" + bindName + ", CURRENT_TIMESTAMP)}";
  }

  @Override
  public String createDateTimeIsInGEHours(String attribute, String bindName) {
    return attribute + " >= {FN TIMESTAMPADD(SQL_TSI_HOUR, :" + bindName + ", CURRENT_TIMESTAMP)}";
  }

  @Override
  public String createDateTimeIsNotNow(String attribute) {
    return "(" + attribute + " < CURRENT_TIMESTAMP OR " + attribute + " >= {FN TIMESTAMPADD(SQL_TSI_MINUTE, 1, CURRENT_TIMESTAMP)})";
  }

  //the time methods below are for derby only
  @Override
  public String createTimeIsNow(String attribute) {
    return attribute + ">=(HOUR(CURRENT_TIMESTAMP)*60 + MINUTE(CURRENT_TIMESTAMP))/24.0/60 AND " + attribute + "<(HOUR(CURRENT_TIMESTAMP)*60 + MINUTE(CURRENT_TIMESTAMP)+(1/24.0/60))/24.0/60";
  }

  @Override
  public String createTimeIsNotNow(String attribute) {
    return attribute + "<(HOUR(CURRENT_TIMESTAMP)*60 + MINUTE(CURRENT_TIMESTAMP))/24.0/60 OR " + attribute + ">((HOUR(CURRENT_TIMESTAMP)*60)+MINUTE(CURRENT_TIMESTAMP)+(1/24.0/60))/24.0/60";
  }

  @Override
  public String createTimeIsInMinutes(String attribute, String bindName) {
    return attribute + ">=(HOUR(CURRENT_TIMESTAMP)*60 + MINUTE(CURRENT_TIMESTAMP)+(:" + bindName + "/24.0/60))/24/60 AND " + attribute + "<((HOUR(CURRENT_TIMESTAMP)*60)+MINUTE(CURRENT_TIMESTAMP)+((:" + bindName + "+1)/24.0/60))/24.0/60";
  }

  @Override
  public String createTimeIsInHours(String attribute, String bindName) {
    return attribute + ">=(HOUR(CURRENT_TIMESTAMP)*60 + MINUTE(CURRENT_TIMESTAMP)+(:" + bindName + "/24.0))/24/60 AND " + attribute + "<((HOUR(CURRENT_TIMESTAMP)*60)+MINUTE(CURRENT_TIMESTAMP)+(:" + bindName + "/24.0)+(1/24.0/60))/24.0/60";
  }

  @Override
  public String createTimeIsInLEMinutes(String attribute, String bindName) {
    return attribute + "<(HOUR(CURRENT_TIMESTAMP)*60 + MINUTE(CURRENT_TIMESTAMP)+((:" + bindName + "+1)/24.0/60))/24/60";
  }

  @Override
  public String createTimeIsInLEHours(String attribute, String bindName) {
    return attribute + "<(HOUR(CURRENT_TIMESTAMP)*60 + MINUTE(CURRENT_TIMESTAMP)+(:" + bindName + "/24.0)+(1/24.0/60))/24/60";
  }

  @Override
  public String createTimeIsInGEMinutes(String attribute, String bindName) {
    return attribute + ">=(HOUR(CURRENT_TIMESTAMP)*60 + MINUTE(CURRENT_TIMESTAMP)+(:" + bindName + "/24.0/60))/24.0/60";
  }

  @Override
  public String createTimeIsInGEHours(String attribute, String bindName) {
    return attribute + ">=(HOUR(CURRENT_TIMESTAMP)*60 + MINUTE(CURRENT_TIMESTAMP)+(:" + bindName + "/24.0))/24.0/60";
  }
}
