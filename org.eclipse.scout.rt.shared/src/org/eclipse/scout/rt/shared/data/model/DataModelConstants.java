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
package org.eclipse.scout.rt.shared.data.model;

/**
 *
 */
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
  int TYPE_DOUBLE = 10;
  int TYPE_PLAIN_INTEGER = 11;
  int TYPE_PLAIN_LONG = 12;
  int TYPE_PLAIN_DOUBLE = 13;
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
   * marker operator that does nothing, no "attribute" "op" "value" pattern is used but simply "attribute"
   */
  int OPERATOR_NONE = 0;

  int OPERATOR_CONTAINS = 1;
  int OPERATOR_DATE_IS_IN_DAYS = 2;
  int OPERATOR_DATE_IS_IN_GE_DAYS = 3;
  int OPERATOR_DATE_IS_IN_GE_MONTHS = 4;
  int OPERATOR_DATE_IS_IN_LE_DAYS = 5;
  int OPERATOR_DATE_IS_IN_LE_MONTHS = 6;
  int OPERATOR_DATE_IS_IN_LAST_DAYS = 7;
  int OPERATOR_DATE_IS_IN_LAST_MONTHS = 8;
  int OPERATOR_DATE_IS_IN_MONTHS = 9;
  int OPERATOR_DATE_IS_IN_NEXT_DAYS = 10;
  int OPERATOR_DATE_IS_IN_NEXT_MONTHS = 11;
  int OPERATOR_DATE_IS_NOT_TODAY = 12;
  int OPERATOR_DATE_IS_TODAY = 13;
  int OPERATOR_DATE_TIME_IS_IN_GE_HOURS = 14;
  int OPERATOR_DATE_TIME_IS_IN_GE_MINUTES = 15;
  int OPERATOR_DATE_TIME_IS_IN_LE_HOURS = 16;
  int OPERATOR_DATE_TIME_IS_IN_LE_MINUTES = 17;
  int OPERATOR_DATE_TIME_IS_NOT_NOW = 18;
  int OPERATOR_DATE_TIME_IS_NOW = 19;
  int OPERATOR_EQ = 20;
  int OPERATOR_DATE_EQ = 49;
  int OPERATOR_DATE_TIME_EQ = 50;
  int OPERATOR_ENDS_WITH = 21;
  int OPERATOR_GE = 22;
  int OPERATOR_DATE_GE = 51;
  int OPERATOR_DATE_TIME_GE = 52;
  int OPERATOR_GT = 23;
  int OPERATOR_DATE_GT = 53;
  int OPERATOR_DATE_TIME_GT = 54;
  int OPERATOR_IN = 24;
  int OPERATOR_LE = 25;
  int OPERATOR_DATE_LE = 55;
  int OPERATOR_DATE_TIME_LE = 56;
  int OPERATOR_LT = 26;
  int OPERATOR_DATE_LT = 57;
  int OPERATOR_DATE_TIME_LT = 58;
  int OPERATOR_NEQ = 27;
  int OPERATOR_DATE_NEQ = 59;
  int OPERATOR_DATE_TIME_NEQ = 60;
  int OPERATOR_NOT_CONTAINS = 28;
  int OPERATOR_NOT_ENDS_WITH = 29;
  int OPERATOR_NOT_IN = 30;
  int OPERATOR_NOT_NULL = 31;
  int OPERATOR_NOT_STARTS_WITH = 32;
  int OPERATOR_NULL = 33;
  int OPERATOR_NUMBER_NOT_NULL = 34;
  int OPERATOR_NUMBER_NULL = 35;
  int OPERATOR_STARTS_WITH = 36;
  int OPERATOR_TIME_IS_IN_GE_HOURS = 37;
  int OPERATOR_TIME_IS_IN_GE_MINUTES = 38;
  int OPERATOR_TIME_IS_IN_HOURS = 39;
  int OPERATOR_TIME_IS_IN_LE_HOURS = 40;
  int OPERATOR_TIME_IS_IN_LE_MINUTES = 41;
  int OPERATOR_TIME_IS_IN_MINUTES = 42;
  int OPERATOR_TIME_IS_NOT_NOW = 43;
  int OPERATOR_TIME_IS_NOW = 44;
  int OPERATOR_BETWEEN = 45;
  int OPERATOR_DATE_BETWEEN = 47;
  int OPERATOR_DATE_TIME_BETWEEN = 48;
  int OPERATOR_LIKE = 46;
  //max is 60

  int AGGREGATION_NONE = 0;
  int AGGREGATION_COUNT = 1;
  int AGGREGATION_SUM = 2;
  int AGGREGATION_MIN = 3;
  int AGGREGATION_MAX = 4;
  int AGGREGATION_AVG = 5;
  int AGGREGATION_MEDIAN = 6;
  //max is 6

}
