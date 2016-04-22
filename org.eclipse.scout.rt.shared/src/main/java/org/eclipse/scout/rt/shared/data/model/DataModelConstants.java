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
package org.eclipse.scout.rt.shared.data.model;

public interface DataModelConstants {
  /**
   * internal type for operators that inherit type of attribute (default)
   */
  int TYPE_INHERITED = -1;

  /**
   * type for operators that need no value, such as IS NULL or IS NOT NULL
   */
  int TYPE_NONE = 0;

  int TYPE_CODE_LIST = 1;
  int TYPE_CODE_TREE = 2;
  int TYPE_NUMBER_LIST = 3;
  int TYPE_NUMBER_TREE = 4;
  int TYPE_DATE = 5;
  int TYPE_TIME = 6;
  int TYPE_DATE_TIME = 7;
  int TYPE_INTEGER = 8;
  int TYPE_LONG = 9;
  /**
   * @deprecated will be removed with scout 7: use {@link DataModelConstants#TYPE_BIG_DECIMAL} instead
   */
  @Deprecated
  int TYPE_DOUBLE = 10;
  int TYPE_BIG_DECIMAL = 10;
  int TYPE_PLAIN_INTEGER = 11;
  int TYPE_PLAIN_LONG = 12;
  /**
   * @deprecated will be removed with scout 7: use {@link DataModelConstants#TYPE_PLAIN_BIG_DECIMAL} instead
   */
  @Deprecated
  int TYPE_PLAIN_DOUBLE = 13;
  int TYPE_PLAIN_BIG_DECIMAL = 13;
  int TYPE_PERCENT = 14;
  int TYPE_STRING = 15;
  int TYPE_SMART = 16;
  /**
   * Attribute used to create a count(Entity) on the enclosing entity.
   */
  int TYPE_AGGREGATE_COUNT = 17;
  /**
   * Attribute used for full text searches
   */
  int TYPE_FULL_TEXT = 18;
  /**
   * Attribute used for rich text searches
   */
  int TYPE_RICH_TEXT = 19;

  /**
   * marker operator that does nothing, no "attribute" "op" "value" pattern is used but simply "attribute"
   */
  int OPERATOR_NONE = 0;
  /**
   * <p>
   * Is a string contained in another string?
   * </p>
   * <p>
   * When using the SqlService, this corresponds to
   * {@link org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle#createContains(String, String)}
   * </p>
   */
  int OPERATOR_CONTAINS = 1;
  /**
   * <p>
   * Is a date between now and a number of days from today on?
   * </p>
   * <p>
   * When using the SqlService, this corresponds to
   * {@link org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle#createDateIsInDays(String, String)}
   * </p>
   */
  int OPERATOR_DATE_IS_IN_DAYS = 2;
  /**
   * <p>
   * Is a date more or equal days in the future as a value given?
   * </p>
   * <p>
   * When using the SqlService, this corresponds to
   * {@link org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle#createDateIsInGEDays(String, String)}
   * </p>
   */
  int OPERATOR_DATE_IS_IN_GE_DAYS = 3;
  /**
   * <p>
   * Is a date more or equal months in the future as a value given?
   * </p>
   * <p>
   * When using the SqlService, this corresponds to
   * {@link org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle#createDateIsInGEMonths(String, String)}
   * </p>
   */
  int OPERATOR_DATE_IS_IN_GE_MONTHS = 4;
  /**
   * <p>
   * Is a date less or equal days in the future as the value given?
   * </p>
   * <p>
   * When using the SqlService, this corresponds to
   * {@link org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle#createDateIsInLEDays(String, String)}
   * </p>
   */
  int OPERATOR_DATE_IS_IN_LE_DAYS = 5;
  /**
   * <p>
   * Is a date less or equal months in the future as a value given?
   * </p>
   * <p>
   * When using the SqlService, this corresponds to
   * {@link org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle#createDateIsInLEMonths(String, String)}
   * </p>
   */
  int OPERATOR_DATE_IS_IN_LE_MONTHS = 6;
  /**
   * <p>
   * Is a date in the past no more days in the past than a value given?
   * </p>
   * <p>
   * When using the SqlService, this corresponds to
   * {@link org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle#createDateIsInLastDays(String, String)}
   * </p>
   */
  int OPERATOR_DATE_IS_IN_LAST_DAYS = 7;
  /**
   * <p>
   * Is a date in the past no more months in the past than a value given?
   * </p>
   * <p>
   * When using the SqlService, this corresponds to
   * {@link org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle#createDateIsInLastMonths(String, String)}
   * </p>
   */
  int OPERATOR_DATE_IS_IN_LAST_MONTHS = 8;
  /**
   * <p>
   * Is a date exactly the number of months in the future as specified by value.
   * </p>
   * <p>
   * When using the SqlService, this corresponds to
   * {@link org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle#createDateIsInMonths(String, String)}
   * </p>
   */
  int OPERATOR_DATE_IS_IN_MONTHS = 9;
  /**
   * <p>
   * Is a date in the number of next days as specified by a value?
   * </p>
   * <p>
   * When using the SqlService, this corresponds to
   * {@link org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle#createDateIsInNextDays(String, String)}
   * </p>
   */
  int OPERATOR_DATE_IS_IN_NEXT_DAYS = 10;
  /**
   * <p>
   * Is a date in the number of next months as specified by a value?
   * </p>
   * <p>
   * When using the SqlService, this corresponds to
   * {@link org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle#createDateIsInNextMonths(String, String)}
   * </p>
   */
  int OPERATOR_DATE_IS_IN_NEXT_MONTHS = 11;
  /**
   * <p>
   * Is a date not today?
   * </p>
   * <p>
   * When using the SqlService, this corresponds to
   * {@link org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle#createDateIsNotToday(String)}
   * </p>
   */
  int OPERATOR_DATE_IS_NOT_TODAY = 12;
  /**
   * <p>
   * Is a date today?
   * </p>
   * <p>
   * When using the SqlService, this corresponds to
   * {@link org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle#createDateIsToday(String)}
   * </p>
   */
  int OPERATOR_DATE_IS_TODAY = 13;
  /**
   * <p>
   * Is a date with time more or equal hours in the future as specified by a value?
   * </p>
   * <p>
   * When using the SqlService, this corresponds to
   * {@link org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle#createDateTimeIsInGEHours(String, String)}
   * </p>
   */
  int OPERATOR_DATE_TIME_IS_IN_GE_HOURS = 14;
  /**
   * <p>
   * Is a date with time more or equal minutes in the future as specified by a value?
   * </p>
   * <p>
   * When using the SqlService, this corresponds to
   * {@link org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle#createDateTimeIsInGEMinutes(String, String)}
   * </p>
   */
  int OPERATOR_DATE_TIME_IS_IN_GE_MINUTES = 15;
  /**
   * <p>
   * Is a date with time less or equal hours in the future as specified by a value?
   * </p>
   * <p>
   * When using the SqlService, this corresponds to
   * {@link org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle#createDateTimeIsInLEHours(String, String)}
   * </p>
   */
  int OPERATOR_DATE_TIME_IS_IN_LE_HOURS = 16;
  /**
   * <p>
   * Is a date with time less or equal minutes in the future as specified by a value?
   * </p>
   * <p>
   * When using the SqlService, this corresponds to
   * {@link org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle#createDateTimeIsInLEMinutes(String, String)}
   * </p>
   */
  int OPERATOR_DATE_TIME_IS_IN_LE_MINUTES = 17;
  /**
   * <p>
   * Is a date with time not now?
   * </p>
   * <p>
   * When using the SqlService, this corresponds to
   * {@link org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle#createDateTimeIsNotNow(String)}
   * </p>
   */
  int OPERATOR_DATE_TIME_IS_NOT_NOW = 18;
  /**
   * <p>
   * Is a date with time now?
   * </p>
   * <p>
   * When using the SqlService, this corresponds to
   * {@link org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle#createDateTimeIsNow(String)}
   * </p>
   */
  int OPERATOR_DATE_TIME_IS_NOW = 19;
  /**
   * <p>
   * Is a value equal to another value?
   * </p>
   * <p>
   * When using the SqlService, this corresponds to
   * {@link org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle#createEQ(String, String)}
   * </p>
   */
  int OPERATOR_EQ = 20;
  /**
   * <p>
   * Is a date equal to another date?
   * </p>
   * <p>
   * When using the SqlService, this corresponds to
   * {@link org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle#createDateEQ(String, String)}
   * </p>
   */
  int OPERATOR_DATE_EQ = 49;
  /**
   * <p>
   * Is a date with time equal to another date with time?
   * </p>
   * <p>
   * When using the SqlService, this corresponds to
   * {@link org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle#createDateTimeEQ(String, String)}
   * </p>
   */
  int OPERATOR_DATE_TIME_EQ = 50;
  /**
   * <p>
   * Does the end of a string match another string?
   * </p>
   * <p>
   * When using the SqlService, this corresponds to
   * {@link org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle#createEndsWith(String, String)}
   * </p>
   */
  int OPERATOR_ENDS_WITH = 21;
  /**
   * <p>
   * Is a value greater or equal to another value?
   * </p>
   * <p>
   * When using the SqlService, this corresponds to
   * {@link org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle#createGE(String, String)}
   * </p>
   */
  int OPERATOR_GE = 22;
  /**
   * <p>
   * Is a date greater or equal to another date?
   * </p>
   * <p>
   * When using the SqlService, this corresponds to
   * {@link org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle#createDateGE(String, String)}
   * </p>
   */
  int OPERATOR_DATE_GE = 51;
  /**
   * <p>
   * Is a date with time greater or equal to another date with time?
   * </p>
   * <p>
   * When using the SqlService, this corresponds to
   * {@link org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle#createDateTimeGE(String, String)}
   * </p>
   */
  int OPERATOR_DATE_TIME_GE = 52;
  /**
   * <p>
   * Is a value greater than another value?
   * </p>
   * <p>
   * When using the SqlService, this corresponds to
   * {@link org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle#createGT(String, String)}
   * </p>
   */
  int OPERATOR_GT = 23;
  /**
   * <p>
   * Is a date greater than another date?
   * </p>
   * <p>
   * When using the SqlService, this corresponds to
   * {@link org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle#createDateGT(String, String)}
   * </p>
   */
  int OPERATOR_DATE_GT = 53;
  /**
   * <p>
   * Is a date with time greater than another date with time?
   * </p>
   * <p>
   * When using the SqlService, this corresponds to
   * {@link org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle#createDateTimeGT(String, String)}
   * </p>
   */
  int OPERATOR_DATE_TIME_GT = 54;
  /**
   * <p>
   * Is a value contained in a set of other values?
   * </p>
   * <p>
   * When using the SqlService, this corresponds to
   * {@link org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle#createIn(String, String)}
   * </p>
   */
  int OPERATOR_IN = 24;
  /**
   * <p>
   * Is a value less or equal to another value?
   * </p>
   * <p>
   * When using the SqlService, this corresponds to
   * {@link org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle#createLE(String, String)}
   * </p>
   */
  int OPERATOR_LE = 25;
  /**
   * <p>
   * Is a date less or equal to another date?
   * </p>
   * <p>
   * When using the SqlService, this corresponds to
   * {@link org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle#createDateLE(String, String)}
   * </p>
   */
  int OPERATOR_DATE_LE = 55;
  /**
   * <p>
   * Is a date with time less or equal to another date with time ?
   * </p>
   * <p>
   * When using the SqlService, this corresponds to
   * {@link org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle#createDateTimeLE(String, String)}
   * </p>
   */
  int OPERATOR_DATE_TIME_LE = 56;
  /**
   * <p>
   * Is a value less than another value?
   * </p>
   * <p>
   * When using the SqlService, this corresponds to
   * {@link org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle#createLT(String, String)}
   * </p>
   */
  int OPERATOR_LT = 26;
  /**
   * <p>
   * Is a date less than another date?
   * </p>
   * <p>
   * When using the SqlService, this corresponds to
   * {@link org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle#createDateLT(String, String)}
   * </p>
   */
  int OPERATOR_DATE_LT = 57;
  /**
   * <p>
   * Is a date with time less than another date with time ?
   * </p>
   * <p>
   * When using the SqlService, this corresponds to
   * {@link org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle#createDateTimeLT(String, String)}
   * </p>
   */
  int OPERATOR_DATE_TIME_LT = 58;
  /**
   * <p>
   * Is a value not equal to another value?
   * </p>
   * <p>
   * When using the SqlService, this corresponds to
   * {@link org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle#createNEQ(String, String)}
   * </p>
   */
  int OPERATOR_NEQ = 27;
  /**
   * <p>
   * Is a date not equal to another date?
   * </p>
   * <p>
   * When using the SqlService, this corresponds to
   * {@link org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle#createDateNEQ(String, String)}
   * </p>
   */
  int OPERATOR_DATE_NEQ = 59;
  /**
   * <p>
   * Is a date with time not equal to another date with time ?
   * </p>
   * <p>
   * When using the SqlService, this corresponds to
   * {@link org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle#createDateTimeNEQ(String, String)}
   * </p>
   */
  int OPERATOR_DATE_TIME_NEQ = 60;
  /**
   * <p>
   * Does a string <b>not</b> contain another string?
   * </p>
   * <p>
   * When using the SqlService, this corresponds to
   * {@link org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle#createNotContains(String, String)}
   * </p>
   */
  int OPERATOR_NOT_CONTAINS = 28;
  /**
   * <p>
   * Does the end of a string <b>not</b> match another string?
   * </p>
   * <p>
   * When using the SqlService, this corresponds to
   * {@link org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle#createNotEndsWith(String, String)}
   * </p>
   */
  int OPERATOR_NOT_ENDS_WITH = 29;
  /**
   * <p>
   * Is a value not contained in a set of values?
   * </p>
   * <p>
   * When using the SqlService, this corresponds to
   * {@link org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle#createNotIn(String, String)}
   * </p>
   */
  int OPERATOR_NOT_IN = 30;
  /**
   * <p>
   * Is a value not <code>null</code>?
   * </p>
   * <p>
   * When using the SqlService, this corresponds to
   * {@link org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle#createNotNull(String)}
   * </p>
   */
  int OPERATOR_NOT_NULL = 31;
  /**
   * <p>
   * Does the beginning of a string <b>not</b> match another string?
   * </p>
   * <p>
   * When using the SqlService, this corresponds to
   * {@link org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle#createNotStartsWith(String, String)}
   * </p>
   */
  int OPERATOR_NOT_STARTS_WITH = 32;
  /**
   * <p>
   * Is a value <code>null</code>?
   * </p>
   * <p>
   * When using the SqlService, this corresponds to
   * {@link org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle#createNull(String)}
   * </p>
   */
  int OPERATOR_NULL = 33;
  /**
   * <p>
   * Is a value not <code>null</code> and not 0? (<code>null</code> and 0 treated equally)
   * </p>
   * <p>
   * When using the SqlService, this corresponds to
   * {@link org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle#createNumberNotNull(String)}
   * </p>
   */
  int OPERATOR_NUMBER_NOT_NULL = 34;
  /**
   * <p>
   * Is a value <code>null</code> or 0? (<code>null</code> and 0 treated equally)
   * </p>
   * <p>
   * When using the SqlService, this corresponds to
   * {@link org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle#createNumberNull(String)}
   * </p>
   */
  int OPERATOR_NUMBER_NULL = 35;
  /**
   * <p>
   * Does the beginning of a string match another string?
   * </p>
   * <p>
   * When using the SqlService, this corresponds to
   * {@link org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle#createStartsWith(String, String)}
   * </p>
   */
  int OPERATOR_STARTS_WITH = 36;
  /**
   * <p>
   * Is a time value (a float, 1 = one day) larger or equal to a time as many hours in the future as specified by a
   * value?
   * </p>
   * <p>
   * When using the SqlService, this corresponds to
   * {@link org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle#createTimeIsInGEHours(String, String)}
   * </p>
   */
  int OPERATOR_TIME_IS_IN_GE_HOURS = 37;
  /**
   * <p>
   * Is a time value (a float, 1 = one day) larger or equal to a time as many minutes in the future as specified by a
   * value?
   * </p>
   * <p>
   * When using the SqlService, this corresponds to
   * {@link org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle#createTimeIsInGEMinutes(String, String)}
   * </p>
   */
  int OPERATOR_TIME_IS_IN_GE_MINUTES = 38;
  /**
   * <p>
   * Is a time value (a float, 1 = one day) less or equal hours in the future as a value specified?
   * </p>
   * <p>
   * When using the SqlService, this corresponds to
   * {@link org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle#createTimeIsInHours(String, String)}
   * </p>
   */
  int OPERATOR_TIME_IS_IN_HOURS = 39;
  /**
   * <p>
   * Is a time value (a float, 1 = one day) less or equal to a time as many hours in the future as specified by a value?
   * </p>
   * <p>
   * When using the SqlService, this corresponds to
   * {@link org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle#createTimeIsInLEHours(String, String)}
   * </p>
   */
  int OPERATOR_TIME_IS_IN_LE_HOURS = 40;
  /**
   * <p>
   * Is a time value (a float, 1 = one day) less or equal to a time as many minutes in the future as specified by a
   * value?
   * </p>
   * <p>
   * When using the SqlService, this corresponds to
   * {@link org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle#createTimeIsInLEMinutes(String, String)}
   * </p>
   */
  int OPERATOR_TIME_IS_IN_LE_MINUTES = 41;
  /**
   * <p>
   * Is a time value (a float, 1 = one day) a number of minutes in the future equal to a value?
   * </p>
   * <p>
   * When using the SqlService, this corresponds to
   * {@link org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle#createTimeIsInMinutes(String, String)}
   * </p>
   */
  int OPERATOR_TIME_IS_IN_MINUTES = 42;
  /**
   * <p>
   * Is a time value (a float, 1 = one day) not equal to the current time?
   * </p>
   * <p>
   * When using the SqlService, this corresponds to
   * {@link org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle#createTimeIsNotNow(String)}
   * </p>
   */
  int OPERATOR_TIME_IS_NOT_NOW = 43;
  /**
   * <p>
   * Is a time value (a float, 1 = one day) equal to the current time?
   * </p>
   * <p>
   * When using the SqlService, this corresponds to
   * {@link org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle#createTimeIsNow(String)}
   * </p>
   */
  int OPERATOR_TIME_IS_NOW = 44;
  /**
   * <p>
   * Is a value between two other values (including those values)?
   * </p>
   * <p>
   * When using the SqlService, this corresponds to
   * {@link org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle#createBetween(String, String, String)} or
   * {@link org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle#createLE(String, String)} and
   * {@link org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle#createGE(String, String)} if only one
   * parameter is set.
   * </p>
   */
  int OPERATOR_BETWEEN = 45;
  /**
   * <p>
   * Is a date between two other dates (including those dates)?
   * </p>
   * <p>
   * When using the SqlService, this corresponds to
   * {@link org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle#createDateBetween(String, String, String)}
   * or {@link org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle#createDateLE(String, String)} and
   * {@link org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle#createDateGE(String, String)} if only one
   * parameter is set.
   * </p>
   */
  int OPERATOR_DATE_BETWEEN = 47;
  /**
   * <p>
   * Is a date with time between two other dates with time (including those date with times)?
   * </p>
   * <p>
   * When using the SqlService, this corresponds to
   * {@link org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle#createDateTimeBetween(String, String, String)}
   * or {@link org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle#createDateTimeLE(String, String)} and
   * {@link org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle#createDateTimeGE(String, String)} if only
   * one parameter is set.
   * </p>
   */
  int OPERATOR_DATE_TIME_BETWEEN = 48;
  /**
   * <p>
   * Is a string like another string?
   * </p>
   * <p>
   * When using the SqlService, this corresponds to
   * {@link org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle#createLike(String, String)}
   * </p>
   */
  int OPERATOR_LIKE = 46;
  /**
   * <p>
   * Is a string not like another string?
   * </p>
   * <p>
   * When using the SqlService, this corresponds to
   * {@link org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle#createNotLike(String, String)}
   * </p>
   */
  int OPERATOR_NOT_LIKE = 61;
  //max is 61

  int AGGREGATION_NONE = 0;
  int AGGREGATION_COUNT = 1;
  int AGGREGATION_SUM = 2;
  int AGGREGATION_MIN = 3;
  int AGGREGATION_MAX = 4;
  int AGGREGATION_AVG = 5;
  int AGGREGATION_MEDIAN = 6;
  //max is 6

}
