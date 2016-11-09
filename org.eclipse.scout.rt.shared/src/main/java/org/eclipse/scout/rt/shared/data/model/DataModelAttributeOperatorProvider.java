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

import java.util.List;

import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.shared.ScoutTexts;

@SuppressWarnings("squid:S2160")
public class DataModelAttributeOperatorProvider implements IDataModelAttributeOperatorProvider, DataModelConstants {

  @Override
  @SuppressWarnings("squid:S138")
  public void injectOperators(IDataModelAttribute attribute, List<IDataModelAttributeOp> operatorList) {
    switch (attribute.getType()) {
      case IDataModelAttribute.TYPE_SMART: {
        operatorList.add(DataModelAttributeOp.create(OPERATOR_EQ, ScoutTexts.get("LogicIn"), ScoutTexts.get("LogicIn")));
        if (attribute.isNotOperatorEnabled()) {
          operatorList.add(DataModelAttributeOp.create(OPERATOR_NEQ, ScoutTexts.get("LogicNotIn"), ScoutTexts.get("LogicNotIn")));
        }
        if (attribute.isNullOperatorEnabled()) {
          operatorList.add(DataModelAttributeOp.create(OPERATOR_NUMBER_NULL));
        }
        if (attribute.isNotOperatorEnabled() && attribute.isNullOperatorEnabled()) {
          operatorList.add(DataModelAttributeOp.create(OPERATOR_NUMBER_NOT_NULL));
        }
        break;
      }
      case IDataModelAttribute.TYPE_CODE_LIST:
      case IDataModelAttribute.TYPE_CODE_TREE:
      case IDataModelAttribute.TYPE_NUMBER_LIST:
      case IDataModelAttribute.TYPE_NUMBER_TREE: {
        operatorList.add(DataModelAttributeOp.create(OPERATOR_IN));
        if (attribute.isNotOperatorEnabled()) {
          operatorList.add(DataModelAttributeOp.create(OPERATOR_NOT_IN));
        }
        if (attribute.isNullOperatorEnabled()) {
          operatorList.add(DataModelAttributeOp.create(OPERATOR_NUMBER_NULL));
        }
        if (attribute.isNotOperatorEnabled() && attribute.isNullOperatorEnabled()) {
          operatorList.add(DataModelAttributeOp.create(OPERATOR_NUMBER_NOT_NULL));
        }
        break;
      }
      case IDataModelAttribute.TYPE_TIME: {
        operatorList.add(DataModelAttributeOp.create(OPERATOR_EQ));
        if (attribute.isNotOperatorEnabled()) {
          operatorList.add(DataModelAttributeOp.create(OPERATOR_NEQ));
        }
        operatorList.add(DataModelAttributeOp.create(OPERATOR_BETWEEN));
        operatorList.add(DataModelAttributeOp.create(OPERATOR_LT));
        operatorList.add(DataModelAttributeOp.create(OPERATOR_LE));
        operatorList.add(DataModelAttributeOp.create(OPERATOR_GT));
        operatorList.add(DataModelAttributeOp.create(OPERATOR_GE));
        if (attribute.isNullOperatorEnabled()) {
          operatorList.add(DataModelAttributeOp.create(OPERATOR_NULL));
        }
        if (attribute.isNotOperatorEnabled() && attribute.isNullOperatorEnabled()) {
          operatorList.add(DataModelAttributeOp.create(OPERATOR_NOT_NULL));
        }
        operatorList.add(DataModelAttributeOp.create(OPERATOR_TIME_IS_IN_GE_HOURS));
        operatorList.add(DataModelAttributeOp.create(OPERATOR_TIME_IS_IN_GE_MINUTES));
        operatorList.add(DataModelAttributeOp.create(OPERATOR_TIME_IS_IN_HOURS));
        operatorList.add(DataModelAttributeOp.create(OPERATOR_TIME_IS_IN_LE_HOURS));
        operatorList.add(DataModelAttributeOp.create(OPERATOR_TIME_IS_IN_LE_MINUTES));
        operatorList.add(DataModelAttributeOp.create(OPERATOR_TIME_IS_NOW));
        break;
      }
      case IDataModelAttribute.TYPE_DATE: {
        operatorList.add(DataModelAttributeOp.create(OPERATOR_DATE_EQ));
        if (attribute.isNotOperatorEnabled()) {
          operatorList.add(DataModelAttributeOp.create(OPERATOR_DATE_NEQ));
        }
        operatorList.add(DataModelAttributeOp.create(OPERATOR_DATE_BETWEEN));
        operatorList.add(DataModelAttributeOp.create(OPERATOR_DATE_LT));
        operatorList.add(DataModelAttributeOp.create(OPERATOR_DATE_LE));
        operatorList.add(DataModelAttributeOp.create(OPERATOR_DATE_GT));
        operatorList.add(DataModelAttributeOp.create(OPERATOR_DATE_GE));
        if (attribute.isNullOperatorEnabled()) {
          operatorList.add(DataModelAttributeOp.create(OPERATOR_NULL));
        }
        if (attribute.isNotOperatorEnabled() && attribute.isNullOperatorEnabled()) {
          operatorList.add(DataModelAttributeOp.create(OPERATOR_NOT_NULL));
        }
        operatorList.add(DataModelAttributeOp.create(OPERATOR_DATE_IS_TODAY));
        operatorList.add(DataModelAttributeOp.create(OPERATOR_DATE_IS_IN_LAST_DAYS));
        operatorList.add(DataModelAttributeOp.create(OPERATOR_DATE_IS_IN_LAST_MONTHS));
        operatorList.add(DataModelAttributeOp.create(OPERATOR_DATE_IS_IN_NEXT_DAYS));
        operatorList.add(DataModelAttributeOp.create(OPERATOR_DATE_IS_IN_NEXT_MONTHS));
        operatorList.add(DataModelAttributeOp.create(OPERATOR_DATE_IS_IN_DAYS));
        operatorList.add(DataModelAttributeOp.create(OPERATOR_DATE_IS_IN_MONTHS));
        operatorList.add(DataModelAttributeOp.create(OPERATOR_DATE_IS_IN_LE_DAYS));
        operatorList.add(DataModelAttributeOp.create(OPERATOR_DATE_IS_IN_LE_MONTHS));
        operatorList.add(DataModelAttributeOp.create(OPERATOR_DATE_IS_IN_GE_DAYS));
        operatorList.add(DataModelAttributeOp.create(OPERATOR_DATE_IS_IN_GE_MONTHS));
        if (attribute.isNotOperatorEnabled()) {
          operatorList.add(DataModelAttributeOp.create(OPERATOR_DATE_IS_NOT_TODAY));
        }
        break;
      }
      case IDataModelAttribute.TYPE_DATE_TIME: {
        operatorList.add(DataModelAttributeOp.create(OPERATOR_DATE_TIME_EQ));
        if (attribute.isNotOperatorEnabled()) {
          operatorList.add(DataModelAttributeOp.create(OPERATOR_DATE_TIME_NEQ));
        }
        operatorList.add(DataModelAttributeOp.create(OPERATOR_DATE_TIME_BETWEEN));
        operatorList.add(DataModelAttributeOp.create(OPERATOR_DATE_TIME_LT));
        operatorList.add(DataModelAttributeOp.create(OPERATOR_DATE_TIME_LE));
        operatorList.add(DataModelAttributeOp.create(OPERATOR_DATE_TIME_GT));
        operatorList.add(DataModelAttributeOp.create(OPERATOR_DATE_TIME_GE));
        if (attribute.isNullOperatorEnabled()) {
          operatorList.add(DataModelAttributeOp.create(OPERATOR_NULL));
        }
        if (attribute.isNotOperatorEnabled() && attribute.isNullOperatorEnabled()) {
          operatorList.add(DataModelAttributeOp.create(OPERATOR_NOT_NULL));
        }
        operatorList.add(DataModelAttributeOp.create(OPERATOR_DATE_TIME_IS_NOW));
        operatorList.add(DataModelAttributeOp.create(OPERATOR_DATE_IS_TODAY));
        operatorList.add(DataModelAttributeOp.create(OPERATOR_DATE_IS_IN_LAST_DAYS));
        operatorList.add(DataModelAttributeOp.create(OPERATOR_DATE_IS_IN_LAST_MONTHS));
        operatorList.add(DataModelAttributeOp.create(OPERATOR_DATE_IS_IN_NEXT_DAYS));
        operatorList.add(DataModelAttributeOp.create(OPERATOR_DATE_IS_IN_NEXT_MONTHS));
        operatorList.add(DataModelAttributeOp.create(OPERATOR_DATE_IS_IN_DAYS));
        operatorList.add(DataModelAttributeOp.create(OPERATOR_DATE_IS_IN_MONTHS));
        operatorList.add(DataModelAttributeOp.create(OPERATOR_DATE_TIME_IS_IN_LE_MINUTES));
        operatorList.add(DataModelAttributeOp.create(OPERATOR_DATE_TIME_IS_IN_LE_HOURS));
        operatorList.add(DataModelAttributeOp.create(OPERATOR_DATE_IS_IN_LE_DAYS));
        operatorList.add(DataModelAttributeOp.create(OPERATOR_DATE_IS_IN_LE_MONTHS));
        operatorList.add(DataModelAttributeOp.create(OPERATOR_DATE_TIME_IS_IN_GE_MINUTES));
        operatorList.add(DataModelAttributeOp.create(OPERATOR_DATE_TIME_IS_IN_GE_HOURS));
        operatorList.add(DataModelAttributeOp.create(OPERATOR_DATE_IS_IN_GE_DAYS));
        operatorList.add(DataModelAttributeOp.create(OPERATOR_DATE_IS_IN_GE_MONTHS));
        if (attribute.isNotOperatorEnabled()) {
          operatorList.add(DataModelAttributeOp.create(OPERATOR_DATE_IS_NOT_TODAY));
        }
        break;
      }
      case IDataModelAttribute.TYPE_INTEGER:
      case IDataModelAttribute.TYPE_LONG:
      case IDataModelAttribute.TYPE_BIG_DECIMAL:
      case IDataModelAttribute.TYPE_PLAIN_INTEGER:
      case IDataModelAttribute.TYPE_PLAIN_LONG:
      case IDataModelAttribute.TYPE_PLAIN_BIG_DECIMAL:
      case IDataModelAttribute.TYPE_PERCENT: {
        operatorList.add(DataModelAttributeOp.create(OPERATOR_EQ));
        if (attribute.isNotOperatorEnabled()) {
          operatorList.add(DataModelAttributeOp.create(OPERATOR_NEQ));
        }
        operatorList.add(DataModelAttributeOp.create(OPERATOR_BETWEEN));
        operatorList.add(DataModelAttributeOp.create(OPERATOR_LT));
        operatorList.add(DataModelAttributeOp.create(OPERATOR_LE));
        operatorList.add(DataModelAttributeOp.create(OPERATOR_GT));
        operatorList.add(DataModelAttributeOp.create(OPERATOR_GE));
        if (attribute.isNullOperatorEnabled()) {
          operatorList.add(DataModelAttributeOp.create(OPERATOR_NULL));
        }
        if (attribute.isNotOperatorEnabled() && attribute.isNullOperatorEnabled()) {
          operatorList.add(DataModelAttributeOp.create(OPERATOR_NOT_NULL));
        }
        break;
      }
      case IDataModelAttribute.TYPE_AGGREGATE_COUNT: {
        operatorList.add(DataModelAttributeOp.create(OPERATOR_EQ));
        if (attribute.isNotOperatorEnabled()) {
          operatorList.add(DataModelAttributeOp.create(OPERATOR_NEQ));
        }
        operatorList.add(DataModelAttributeOp.create(OPERATOR_BETWEEN));
        operatorList.add(DataModelAttributeOp.create(OPERATOR_LT));
        operatorList.add(DataModelAttributeOp.create(OPERATOR_LE));
        operatorList.add(DataModelAttributeOp.create(OPERATOR_GT));
        operatorList.add(DataModelAttributeOp.create(OPERATOR_GE));
        break;
      }
      case IDataModelAttribute.TYPE_STRING: {
        operatorList.add(DataModelAttributeOp.create(OPERATOR_LIKE));
        if (attribute.isNotOperatorEnabled()) {
          operatorList.add(DataModelAttributeOp.create(OPERATOR_NOT_LIKE));
        }
        operatorList.add(DataModelAttributeOp.create(OPERATOR_CONTAINS));
        if (attribute.isNotOperatorEnabled()) {
          operatorList.add(DataModelAttributeOp.create(OPERATOR_NOT_CONTAINS));
        }
        operatorList.add(DataModelAttributeOp.create(OPERATOR_STARTS_WITH));
        if (attribute.isNotOperatorEnabled()) {
          operatorList.add(DataModelAttributeOp.create(OPERATOR_NOT_STARTS_WITH));
        }
        operatorList.add(DataModelAttributeOp.create(OPERATOR_ENDS_WITH));
        if (attribute.isNotOperatorEnabled()) {
          operatorList.add(DataModelAttributeOp.create(OPERATOR_NOT_ENDS_WITH));
        }
        if (attribute.isNullOperatorEnabled()) {
          operatorList.add(DataModelAttributeOp.create(OPERATOR_NULL));
        }
        if (attribute.isNotOperatorEnabled() && attribute.isNullOperatorEnabled()) {
          operatorList.add(DataModelAttributeOp.create(OPERATOR_NOT_NULL));
        }
        break;
      }
      case IDataModelAttribute.TYPE_RICH_TEXT:
      case IDataModelAttribute.TYPE_FULL_TEXT: {
        operatorList.add(DataModelAttributeOp.create(OPERATOR_CONTAINS));
        if (attribute.isNotOperatorEnabled()) {
          operatorList.add(DataModelAttributeOp.create(OPERATOR_NOT_CONTAINS));
        }
        if (attribute.isNullOperatorEnabled()) {
          operatorList.add(DataModelAttributeOp.create(OPERATOR_NULL));
        }
        if (attribute.isNotOperatorEnabled() && attribute.isNullOperatorEnabled()) {
          operatorList.add(DataModelAttributeOp.create(OPERATOR_NOT_NULL));
        }
        break;
      }
    }
  }

  @Override
  public IDataModelAttributeOp createOperator(int operator, String shortText, String text) {
    switch (operator) {
      case OPERATOR_BETWEEN:
        return new Between(OPERATOR_BETWEEN, (shortText == null) ? (ScoutTexts.get("LogicBetweenShort")) : (shortText), (text == null) ? (ScoutTexts.get("LogicBetween")) : text);
      case OPERATOR_DATE_BETWEEN:
        return new Between(OPERATOR_DATE_BETWEEN, (shortText == null) ? (ScoutTexts.get("LogicBetweenShort")) : (shortText), (text == null) ? (ScoutTexts.get("LogicBetween")) : text);
      case OPERATOR_DATE_TIME_BETWEEN:
        return new Between(OPERATOR_DATE_TIME_BETWEEN, (shortText == null) ? (ScoutTexts.get("LogicBetweenShort")) : (shortText), (text == null) ? (ScoutTexts.get("LogicBetween")) : text);
      case OPERATOR_NEQ:
        return new NEQ(OPERATOR_NEQ, (shortText == null) ? (ScoutTexts.get("LogicNEQ")) : (shortText), (text == null) ? (ScoutTexts.get("LogicNEQ")) : text);
      case OPERATOR_DATE_NEQ:
        return new NEQ(OPERATOR_DATE_NEQ, (shortText == null) ? (ScoutTexts.get("LogicNEQ")) : (shortText), (text == null) ? (ScoutTexts.get("LogicNEQ")) : text);
      case OPERATOR_DATE_TIME_NEQ:
        return new NEQ(OPERATOR_DATE_TIME_NEQ, (shortText == null) ? (ScoutTexts.get("LogicNEQ")) : (shortText), (text == null) ? (ScoutTexts.get("LogicNEQ")) : text);
      case OPERATOR_LT:
        return new LT(OPERATOR_LT, (shortText == null) ? (ScoutTexts.get("LogicLT")) : (shortText), (text == null) ? (ScoutTexts.get("LogicLT")) : text);
      case OPERATOR_DATE_LT:
        return new LT(OPERATOR_DATE_LT, (shortText == null) ? (ScoutTexts.get("LogicLT")) : (shortText), (text == null) ? (ScoutTexts.get("LogicLT")) : text);
      case OPERATOR_DATE_TIME_LT:
        return new LT(OPERATOR_DATE_TIME_LT, (shortText == null) ? (ScoutTexts.get("LogicLT")) : (shortText), (text == null) ? (ScoutTexts.get("LogicLT")) : text);
      case OPERATOR_LE:
        return new LE(OPERATOR_LE, (shortText == null) ? (ScoutTexts.get("LogicLE")) : (shortText), (text == null) ? (ScoutTexts.get("LogicLE")) : text);
      case OPERATOR_DATE_LE:
        return new LE(OPERATOR_DATE_LE, (shortText == null) ? (ScoutTexts.get("LogicLE")) : (shortText), (text == null) ? (ScoutTexts.get("LogicLE")) : text);
      case OPERATOR_DATE_TIME_LE:
        return new LE(OPERATOR_DATE_TIME_LE, (shortText == null) ? (ScoutTexts.get("LogicLE")) : (shortText), (text == null) ? (ScoutTexts.get("LogicLE")) : text);
      case OPERATOR_EQ:
        return new EQ(OPERATOR_EQ, (shortText == null) ? (ScoutTexts.get("LogicEQ")) : (shortText), (text == null) ? (ScoutTexts.get("LogicEQ")) : text);
      case OPERATOR_DATE_EQ:
        return new EQ(OPERATOR_DATE_EQ, (shortText == null) ? (ScoutTexts.get("LogicEQ")) : (shortText), (text == null) ? (ScoutTexts.get("LogicEQ")) : text);
      case OPERATOR_DATE_TIME_EQ:
        return new EQ(OPERATOR_DATE_TIME_EQ, (shortText == null) ? (ScoutTexts.get("LogicEQ")) : (shortText), (text == null) ? (ScoutTexts.get("LogicEQ")) : text);
      case OPERATOR_GT:
        return new GT(OPERATOR_GT, (shortText == null) ? (ScoutTexts.get("LogicGT")) : (shortText), (text == null) ? (ScoutTexts.get("LogicGT")) : text);
      case OPERATOR_DATE_GT:
        return new GT(OPERATOR_DATE_GT, (shortText == null) ? (ScoutTexts.get("LogicGT")) : (shortText), (text == null) ? (ScoutTexts.get("LogicGT")) : text);
      case OPERATOR_DATE_TIME_GT:
        return new GT(OPERATOR_DATE_TIME_GT, (shortText == null) ? (ScoutTexts.get("LogicGT")) : (shortText), (text == null) ? (ScoutTexts.get("LogicGT")) : text);
      case OPERATOR_GE:
        return new GE(OPERATOR_GE, (shortText == null) ? (ScoutTexts.get("LogicGE")) : (shortText), (text == null) ? (ScoutTexts.get("LogicGE")) : text);
      case OPERATOR_DATE_GE:
        return new GE(OPERATOR_DATE_GE, (shortText == null) ? (ScoutTexts.get("LogicGE")) : (shortText), (text == null) ? (ScoutTexts.get("LogicGE")) : text);
      case OPERATOR_DATE_TIME_GE:
        return new GE(OPERATOR_DATE_TIME_GE, (shortText == null) ? (ScoutTexts.get("LogicGE")) : (shortText), (text == null) ? (ScoutTexts.get("LogicGE")) : text);
      case OPERATOR_DATE_IS_IN_DAYS:
        return new DateIsInDays(OPERATOR_DATE_IS_IN_DAYS, (shortText == null) ? (ScoutTexts.get("LogicDateIsInDays")) : (shortText), (text == null) ? (ScoutTexts.get("LogicDateIsInDays")) : text);
      case OPERATOR_DATE_IS_IN_GE_DAYS:
        return new DateIsInGEDays(OPERATOR_DATE_IS_IN_GE_DAYS, (shortText == null) ? (ScoutTexts.get("LogicDateIsInGEDays")) : (shortText), (text == null) ? (ScoutTexts.get("LogicDateIsInGEDays")) : text);
      case OPERATOR_DATE_IS_IN_GE_MONTHS:
        return new DateIsInGEMonths(OPERATOR_DATE_IS_IN_GE_MONTHS, (shortText == null) ? (ScoutTexts.get("LogicDateIsInGEMonths")) : (shortText), (text == null) ? (ScoutTexts.get("LogicDateIsInGEMonths")) : text);
      case OPERATOR_DATE_IS_IN_LE_DAYS:
        return new DateIsInLEDays(OPERATOR_DATE_IS_IN_LE_DAYS, (shortText == null) ? (ScoutTexts.get("LogicDateIsInLEDays")) : (shortText), (text == null) ? (ScoutTexts.get("LogicDateIsInLEDays")) : text);
      case OPERATOR_DATE_IS_IN_LE_MONTHS:
        return new DateIsInLEMonths(OPERATOR_DATE_IS_IN_LE_MONTHS, (shortText == null) ? (ScoutTexts.get("LogicDateIsInLEMonths")) : (shortText), (text == null) ? (ScoutTexts.get("LogicDateIsInLEMonths")) : text);
      case OPERATOR_DATE_IS_IN_LAST_DAYS:
        return new DateIsInLastDays(OPERATOR_DATE_IS_IN_LAST_DAYS, (shortText == null) ? (ScoutTexts.get("LogicDateIsInLastDays")) : (shortText), (text == null) ? (ScoutTexts.get("LogicDateIsInLastDays")) : text);
      case OPERATOR_DATE_IS_IN_LAST_MONTHS:
        return new DateIsInLastMonths(OPERATOR_DATE_IS_IN_LAST_MONTHS, (shortText == null) ? (ScoutTexts.get("LogicDateIsInLastMonths")) : (shortText), (text == null) ? (ScoutTexts.get("LogicDateIsInLastMonths")) : text);
      case OPERATOR_DATE_IS_IN_MONTHS:
        return new DateIsInMonths(OPERATOR_DATE_IS_IN_MONTHS, (shortText == null) ? (ScoutTexts.get("LogicDateIsInMonths")) : (shortText), (text == null) ? (ScoutTexts.get("LogicDateIsInMonths")) : text);
      case OPERATOR_DATE_IS_IN_NEXT_DAYS:
        return new DateIsInNextDays(OPERATOR_DATE_IS_IN_NEXT_DAYS, (shortText == null) ? (ScoutTexts.get("LogicDateIsInNextDays")) : (shortText), (text == null) ? (ScoutTexts.get("LogicDateIsInNextDays")) : text);
      case OPERATOR_DATE_IS_IN_NEXT_MONTHS:
        return new DateIsInNextMonths(OPERATOR_DATE_IS_IN_NEXT_MONTHS, (shortText == null) ? (ScoutTexts.get("LogicDateIsInNextMonths")) : (shortText), (text == null) ? (ScoutTexts.get("LogicDateIsInNextMonths")) : text);
      case OPERATOR_DATE_IS_NOT_TODAY:
        return new DateIsNotToday(OPERATOR_DATE_IS_NOT_TODAY, (shortText == null) ? (ScoutTexts.get("LogicDateIsNotToday")) : (shortText), (text == null) ? (ScoutTexts.get("LogicDateIsNotToday")) : text);
      case OPERATOR_DATE_IS_TODAY:
        return new DateIsToday(OPERATOR_DATE_IS_TODAY, (shortText == null) ? (ScoutTexts.get("LogicDateIsToday")) : (shortText), (text == null) ? (ScoutTexts.get("LogicDateIsToday")) : text);
      case OPERATOR_DATE_TIME_IS_IN_GE_HOURS:
        return new DateTimeIsInGEHours(OPERATOR_DATE_TIME_IS_IN_GE_HOURS, (shortText == null) ? (ScoutTexts.get("LogicDateTimeIsInGEHours")) : (shortText), (text == null) ? (ScoutTexts.get("LogicDateTimeIsInGEHours")) : text);
      case OPERATOR_DATE_TIME_IS_IN_GE_MINUTES:
        return new DateTimeIsInGEMinutes(OPERATOR_DATE_TIME_IS_IN_GE_MINUTES, (shortText == null) ? (ScoutTexts.get("LogicDateTimeIsInGEMinutes")) : (shortText), (text == null) ? (ScoutTexts.get("LogicDateTimeIsInGEMinutes")) : text);
      case OPERATOR_DATE_TIME_IS_IN_LE_HOURS:
        return new DateTimeIsInLEHours(OPERATOR_DATE_TIME_IS_IN_LE_HOURS, (shortText == null) ? (ScoutTexts.get("LogicDateTimeIsInLEHours")) : (shortText), (text == null) ? (ScoutTexts.get("LogicDateTimeIsInLEHours")) : text);
      case OPERATOR_DATE_TIME_IS_IN_LE_MINUTES:
        return new DateTimeIsInLEMinutes(OPERATOR_DATE_TIME_IS_IN_LE_MINUTES, (shortText == null) ? (ScoutTexts.get("LogicDateTimeIsInLEMinutes")) : (shortText), (text == null) ? (ScoutTexts.get("LogicDateTimeIsInLEMinutes")) : text);
      case OPERATOR_DATE_TIME_IS_NOT_NOW:
        return new DateTimeIsNotNow(OPERATOR_DATE_TIME_IS_NOT_NOW, (shortText == null) ? (ScoutTexts.get("LogicDateTimeIsNotNow")) : (shortText), (text == null) ? (ScoutTexts.get("LogicDateTimeIsNotNow")) : text);
      case OPERATOR_DATE_TIME_IS_NOW:
        return new DateTimeIsNow(OPERATOR_DATE_TIME_IS_NOW, (shortText == null) ? (ScoutTexts.get("LogicDateTimeIsNow")) : (shortText), (text == null) ? (ScoutTexts.get("LogicDateTimeIsNow")) : text);
      case OPERATOR_ENDS_WITH:
        return new EndsWith(OPERATOR_ENDS_WITH, (shortText == null) ? (ScoutTexts.get("LogicEndsWith")) : (shortText), (text == null) ? (ScoutTexts.get("LogicEndsWith")) : text);
      case OPERATOR_NOT_ENDS_WITH:
        return new NotEndsWith(OPERATOR_NOT_ENDS_WITH, (shortText == null) ? (ScoutTexts.get("LogicNotEndsWith")) : (shortText), (text == null) ? (ScoutTexts.get("LogicNotEndsWith")) : text);
      case OPERATOR_IN:
        return new In(OPERATOR_IN, (shortText == null) ? (ScoutTexts.get("LogicIn")) : (shortText), (text == null) ? (ScoutTexts.get("LogicIn")) : text);
      case OPERATOR_CONTAINS:
        return new Contains(OPERATOR_CONTAINS, (shortText == null) ? (ScoutTexts.get("LogicLike")) : (shortText), (text == null) ? (ScoutTexts.get("LogicLike")) : text);
      case OPERATOR_LIKE:
        return new Like(OPERATOR_LIKE, (shortText == null) ? (ScoutTexts.get("LogicEQ")) : (shortText), (text == null) ? (ScoutTexts.get("LogicEQ")) : text);
      case OPERATOR_NOT_LIKE:
        return new Like(OPERATOR_NOT_LIKE, (shortText == null) ? (ScoutTexts.get("LogicNEQ")) : (shortText), (text == null) ? (ScoutTexts.get("LogicNEQ")) : text);
      case OPERATOR_NOT_IN:
        return new NotIn(OPERATOR_NOT_IN, (shortText == null) ? (ScoutTexts.get("LogicNotIn")) : (shortText), (text == null) ? (ScoutTexts.get("LogicNotIn")) : text);
      case OPERATOR_NOT_CONTAINS:
        return new NotContains(OPERATOR_NOT_CONTAINS, (shortText == null) ? (ScoutTexts.get("LogicNotLike")) : (shortText), (text == null) ? (ScoutTexts.get("LogicNotLike")) : text);
      case OPERATOR_NOT_NULL:
        return new NotNull(OPERATOR_NOT_NULL, (shortText == null) ? (ScoutTexts.get("LogicNotNull")) : (shortText), (text == null) ? (ScoutTexts.get("LogicNotNull")) : text);
      case OPERATOR_NUMBER_NOT_NULL:
        return new NumberNotNull(OPERATOR_NUMBER_NOT_NULL, (shortText == null) ? (ScoutTexts.get("LogicNotNull")) : (shortText), (text == null) ? (ScoutTexts.get("LogicNotNull")) : text);
      case OPERATOR_NULL:
        return new Null(OPERATOR_NULL, (shortText == null) ? (ScoutTexts.get("LogicNull")) : (shortText), (text == null) ? (ScoutTexts.get("LogicNull")) : text);
      case OPERATOR_NUMBER_NULL:
        return new NumberNull(OPERATOR_NUMBER_NULL, (shortText == null) ? (ScoutTexts.get("LogicNull")) : (shortText), (text == null) ? (ScoutTexts.get("LogicNull")) : text);
      case OPERATOR_STARTS_WITH:
        return new StartsWith(OPERATOR_STARTS_WITH, (shortText == null) ? (ScoutTexts.get("LogicStartsWith")) : (shortText), (text == null) ? (ScoutTexts.get("LogicStartsWith")) : text);
      case OPERATOR_NOT_STARTS_WITH:
        return new NotStartsWith(OPERATOR_NOT_STARTS_WITH, (shortText == null) ? (ScoutTexts.get("LogicNotStartsWith")) : (shortText), (text == null) ? (ScoutTexts.get("LogicNotStartsWith")) : text);
      case OPERATOR_TIME_IS_IN_GE_HOURS:
        return new TimeIsInGEHours(OPERATOR_TIME_IS_IN_GE_HOURS, (shortText == null) ? (ScoutTexts.get("LogicTimeIsInGEHours")) : (shortText), (text == null) ? (ScoutTexts.get("LogicTimeIsInGEHours")) : text);
      case OPERATOR_TIME_IS_IN_GE_MINUTES:
        return new TimeIsInGEMinutes(OPERATOR_TIME_IS_IN_GE_MINUTES, (shortText == null) ? (ScoutTexts.get("LogicTimeIsInGEMinutes")) : (shortText), (text == null) ? (ScoutTexts.get("LogicTimeIsInGEMinutes")) : text);
      case OPERATOR_TIME_IS_IN_HOURS:
        return new TimeIsInHours(OPERATOR_TIME_IS_IN_HOURS, (shortText == null) ? (ScoutTexts.get("LogicTimeIsInHours")) : (shortText), (text == null) ? (ScoutTexts.get("LogicTimeIsInHours")) : text);
      case OPERATOR_TIME_IS_IN_LE_HOURS:
        return new TimeIsInLEHours(OPERATOR_TIME_IS_IN_LE_HOURS, (shortText == null) ? (ScoutTexts.get("LogicTimeIsInLEHours")) : (shortText), (text == null) ? (ScoutTexts.get("LogicTimeIsInLEHours")) : text);
      case OPERATOR_TIME_IS_IN_LE_MINUTES:
        return new TimeIsInLEMinutes(OPERATOR_TIME_IS_IN_LE_MINUTES, (shortText == null) ? (ScoutTexts.get("LogicTimeIsInLEMinutes")) : (shortText), (text == null) ? (ScoutTexts.get("LogicTimeIsInLEMinutes")) : text);
      case OPERATOR_TIME_IS_IN_MINUTES:
        return new TimeIsInMinutes(OPERATOR_TIME_IS_IN_MINUTES, (shortText == null) ? (ScoutTexts.get("LogicTimeIsInMinutes")) : (shortText), (text == null) ? (ScoutTexts.get("LogicTimeIsInMinutes")) : text);
      case OPERATOR_TIME_IS_NOW:
        return new TimeIsNow(OPERATOR_TIME_IS_NOW, (shortText == null) ? (ScoutTexts.get("LogicTimeIsNow")) : (shortText), (text == null) ? (ScoutTexts.get("LogicTimeIsNow")) : text);
      case OPERATOR_TIME_IS_NOT_NOW:
        return new TimeIsNotNow(OPERATOR_TIME_IS_NOT_NOW, (shortText == null) ? (ScoutTexts.get("LogicTimeIsNotNow")) : (shortText), (text == null) ? (ScoutTexts.get("LogicTimeIsNotNow")) : text);
    }
    return null;
  }

  private static class NEQ extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;
    private String m_shortText;
    private String m_text;

    /**
     * @param aggregationType
     */
    NEQ(int operator, String shortText, String text) {
      super(operator);
      m_shortText = shortText;
      m_text = text;
    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    @Override
    public String getText() {
      return m_text;
    }

    @Override
    public String getShortText() {
      return m_shortText;
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_INHERITED;
    }
  }

  private static class LT extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;
    private String m_shortText;
    private String m_text;

    /**
     * @param aggregationType
     */
    LT(int operator, String shortText, String text) {
      super(operator);
      m_shortText = shortText;
      m_text = text;
    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    @Override
    public String getText() {
      return m_text;
    }

    @Override
    public String getShortText() {
      return m_shortText;
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_INHERITED;
    }
  }

  private static class LE extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private String m_shortText;
    private String m_text;

    /**
     * @param aggregationType
     */
    LE(int operator, String shortText, String text) {
      super(operator);
      m_shortText = shortText;
      m_text = text;

    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    @Override
    public String getText() {
      return m_text;
    }

    @Override
    public String getShortText() {
      return m_shortText;
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_INHERITED;
    }
  }

  private static class EQ extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private String m_shortText;
    private String m_text;

    /**
     * @param aggregationType
     */
    EQ(int operator, String shortText, String text) {
      super(operator);
      m_shortText = shortText;
      m_text = text;

    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    @Override
    public String getText() {
      return m_text;
    }

    @Override
    public String getShortText() {
      return m_shortText;
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_INHERITED;
    }
  }

  private static class GT extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private String m_shortText;
    private String m_text;

    /**
     * @param aggregationType
     */
    GT(int operator, String shortText, String text) {
      super(operator);
      m_shortText = shortText;
      m_text = text;

    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    @Override
    public String getText() {
      return m_text;
    }

    @Override
    public String getShortText() {
      return m_shortText;
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_INHERITED;
    }
  }

  private static class GE extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private String m_shortText;
    private String m_text;

    /**
     * @param aggregationType
     */
    GE(int operator, String shortText, String text) {
      super(operator);
      m_shortText = shortText;
      m_text = text;

    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    @Override
    public String getText() {
      return m_text;
    }

    @Override
    public String getShortText() {
      return m_shortText;
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_INHERITED;
    }
  }

  private static class DateIsInDays extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private String m_shortText;
    private String m_text;

    /**
     * @param aggregationType
     */
    DateIsInDays(int operator, String shortText, String text) {
      super(operator);
      m_shortText = shortText;
      m_text = text;

    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    @Override
    public String getText() {
      return m_text;
    }

    @Override
    public String getShortText() {
      return m_shortText;
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_INTEGER;
    }
  }

  private static class DateIsInGEDays extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private String m_shortText;
    private String m_text;

    /**
     * @param aggregationType
     */
    DateIsInGEDays(int operator, String shortText, String text) {
      super(operator);
      m_shortText = shortText;
      m_text = text;

    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    @Override
    public String getText() {
      return m_text;
    }

    @Override
    public String getShortText() {
      return m_shortText;
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_INTEGER;
    }
  }

  private static class DateIsInGEMonths extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private String m_shortText;
    private String m_text;

    /**
     * @param aggregationType
     */
    DateIsInGEMonths(int operator, String shortText, String text) {
      super(operator);
      m_shortText = shortText;
      m_text = text;

    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    @Override
    public String getText() {
      return m_text;
    }

    @Override
    public String getShortText() {
      return m_shortText;
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_INTEGER;
    }
  }

  private static class DateIsInLEDays extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private String m_shortText;
    private String m_text;

    /**
     * @param aggregationType
     */
    DateIsInLEDays(int operator, String shortText, String text) {
      super(operator);
      m_shortText = shortText;
      m_text = text;

    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    @Override
    public String getText() {
      return m_text;
    }

    @Override
    public String getShortText() {
      return m_shortText;
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_INTEGER;
    }
  }

  private static class DateIsInLEMonths extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private String m_shortText;
    private String m_text;

    /**
     * @param aggregationType
     */
    DateIsInLEMonths(int operator, String shortText, String text) {
      super(operator);
      m_shortText = shortText;
      m_text = text;

    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    @Override
    public String getText() {
      return m_text;
    }

    @Override
    public String getShortText() {
      return m_shortText;
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_INTEGER;
    }
  }

  private static class DateIsInLastDays extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private String m_shortText;
    private String m_text;

    /**
     * @param aggregationType
     */
    DateIsInLastDays(int operator, String shortText, String text) {
      super(operator);
      m_shortText = shortText;
      m_text = text;

    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    @Override
    public String getText() {
      return m_text;
    }

    @Override
    public String getShortText() {
      return m_shortText;
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_INTEGER;
    }
  }

  private static class DateIsInLastMonths extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private String m_shortText;
    private String m_text;

    /**
     * @param aggregationType
     */
    DateIsInLastMonths(int operator, String shortText, String text) {
      super(operator);
      m_shortText = shortText;
      m_text = text;

    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    @Override
    public String getText() {
      return m_text;
    }

    @Override
    public String getShortText() {
      return m_shortText;
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_INTEGER;
    }
  }

  private static class DateIsInMonths extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private String m_shortText;
    private String m_text;

    /**
     * @param aggregationType
     */
    DateIsInMonths(int operator, String shortText, String text) {
      super(operator);
      m_shortText = shortText;
      m_text = text;

    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    @Override
    public String getText() {
      return m_text;
    }

    @Override
    public String getShortText() {
      return m_shortText;
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_INTEGER;
    }
  }

  private static class DateIsInNextDays extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private String m_shortText;
    private String m_text;

    /**
     * @param aggregationType
     */
    DateIsInNextDays(int operator, String shortText, String text) {
      super(operator);
      m_shortText = shortText;
      m_text = text;

    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    @Override
    public String getText() {
      return m_text;
    }

    @Override
    public String getShortText() {
      return m_shortText;
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_INTEGER;
    }
  }

  private static class DateIsInNextMonths extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private String m_shortText;
    private String m_text;

    /**
     * @param aggregationType
     */
    DateIsInNextMonths(int operator, String shortText, String text) {
      super(operator);
      m_shortText = shortText;
      m_text = text;

    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    @Override
    public String getText() {
      return m_text;
    }

    @Override
    public String getShortText() {
      return m_shortText;
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_INTEGER;
    }
  }

  private static class DateIsNotToday extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private String m_shortText;
    private String m_text;

    /**
     * @param aggregationType
     */
    DateIsNotToday(int operator, String shortText, String text) {
      super(operator);
      m_shortText = shortText;
      m_text = text;

    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText());
    }

    @Override
    public String getText() {
      return m_text;
    }

    @Override
    public String getShortText() {
      return m_shortText;
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_NONE;
    }
  }

  private static class DateIsToday extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private String m_shortText;
    private String m_text;

    /**
     * @param aggregationType
     */
    DateIsToday(int operator, String shortText, String text) {
      super(operator);
      m_shortText = shortText;
      m_text = text;

    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText());
    }

    @Override
    public String getText() {
      return m_text;
    }

    @Override
    public String getShortText() {
      return m_shortText;
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_NONE;
    }
  }

  private static class DateTimeIsInGEHours extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private String m_shortText;
    private String m_text;

    /**
     * @param aggregationType
     */
    DateTimeIsInGEHours(int operator, String shortText, String text) {
      super(operator);
      m_shortText = shortText;
      m_text = text;

    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    @Override
    public String getText() {
      return m_text;
    }

    @Override
    public String getShortText() {
      return m_shortText;
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_INTEGER;
    }
  }

  private static class DateTimeIsInGEMinutes extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private String m_shortText;
    private String m_text;

    /**
     * @param aggregationType
     */
    DateTimeIsInGEMinutes(int operator, String shortText, String text) {
      super(operator);
      m_shortText = shortText;
      m_text = text;

    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    @Override
    public String getText() {
      return m_text;
    }

    @Override
    public String getShortText() {
      return m_shortText;
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_INTEGER;
    }
  }

  private static class DateTimeIsInLEHours extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private String m_shortText;
    private String m_text;

    /**
     * @param aggregationType
     */
    DateTimeIsInLEHours(int operator, String shortText, String text) {
      super(operator);
      m_shortText = shortText;
      m_text = text;

    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    @Override
    public String getText() {
      return m_text;
    }

    @Override
    public String getShortText() {
      return m_shortText;
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_INTEGER;
    }
  }

  private static class DateTimeIsInLEMinutes extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private String m_shortText;
    private String m_text;

    /**
     * @param aggregationType
     */
    DateTimeIsInLEMinutes(int operator, String shortText, String text) {
      super(operator);
      m_shortText = shortText;
      m_text = text;

    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    @Override
    public String getText() {
      return m_text;
    }

    @Override
    public String getShortText() {
      return m_shortText;
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_INTEGER;
    }
  }

  private static class DateTimeIsNotNow extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private String m_shortText;
    private String m_text;

    /**
     * @param aggregationType
     */
    DateTimeIsNotNow(int operator, String shortText, String text) {
      super(operator);
      m_shortText = shortText;
      m_text = text;

    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText());
    }

    @Override
    public String getText() {
      return m_text;
    }

    @Override
    public String getShortText() {
      return m_shortText;
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_NONE;
    }
  }

  private static class DateTimeIsNow extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private String m_shortText;
    private String m_text;

    /**
     * @param aggregationType
     */
    DateTimeIsNow(int operator, String shortText, String text) {
      super(operator);
      m_shortText = shortText;
      m_text = text;

    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText());
    }

    @Override
    public String getText() {
      return m_text;
    }

    @Override
    public String getShortText() {
      return m_shortText;
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_NONE;
    }
  }

  private static class EndsWith extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private String m_shortText;
    private String m_text;

    /**
     * @param aggregationType
     */
    EndsWith(int operator, String shortText, String text) {
      super(operator);
      m_shortText = shortText;
      m_text = text;

    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    @Override
    public String getText() {
      return m_text;
    }

    @Override
    public String getShortText() {
      return m_shortText;
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_INHERITED;
    }
  }

  private static class NotEndsWith extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private String m_shortText;
    private String m_text;

    /**
     * @param aggregationType
     */
    NotEndsWith(int operator, String shortText, String text) {
      super(operator);
      m_shortText = shortText;
      m_text = text;

    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    @Override
    public String getText() {
      return m_text;
    }

    @Override
    public String getShortText() {
      return m_shortText;
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_INHERITED;
    }
  }

  private static class In extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private String m_shortText;
    private String m_text;

    /**
     * @param aggregationType
     */
    In(int operator, String shortText, String text) {
      super(operator);
      m_shortText = shortText;
      m_text = text;

    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    @Override
    public String getText() {
      return m_text;
    }

    @Override
    public String getShortText() {
      return m_shortText;
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_INHERITED;
    }
  }

  private static class Contains extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private String m_shortText;
    private String m_text;

    /**
     * @param aggregationType
     */
    Contains(int operator, String shortText, String text) {
      super(operator);
      m_shortText = shortText;
      m_text = text;

    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    @Override
    public String getText() {
      return m_text;
    }

    @Override
    public String getShortText() {
      return m_shortText;
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_INHERITED;
    }
  }

  private static class NotIn extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private String m_shortText;
    private String m_text;

    /**
     * @param aggregationType
     */
    NotIn(int operator, String shortText, String text) {
      super(operator);
      m_shortText = shortText;
      m_text = text;

    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    @Override
    public String getText() {
      return m_text;
    }

    @Override
    public String getShortText() {
      return m_shortText;
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_INHERITED;
    }
  }

  private static class NotContains extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private String m_shortText;
    private String m_text;

    /**
     * @param aggregationType
     */
    NotContains(int operator, String shortText, String text) {
      super(operator);
      m_shortText = shortText;
      m_text = text;

    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    @Override
    public String getText() {
      return m_text;
    }

    @Override
    public String getShortText() {
      return m_shortText;
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_INHERITED;
    }
  }

  private static class NotNull extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private String m_shortText;
    private String m_text;

    /**
     * @param aggregationType
     */
    NotNull(int operator, String shortText, String text) {
      super(operator);
      m_shortText = shortText;
      m_text = text;

    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText());
    }

    @Override
    public String getText() {
      return m_text;
    }

    @Override
    public String getShortText() {
      return m_shortText;
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_NONE;
    }
  }

  /**
   * nvl(x,0)<>0
   */
  private static class NumberNotNull extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private String m_shortText;
    private String m_text;

    /**
     * @param aggregationType
     */
    NumberNotNull(int operator, String shortText, String text) {
      super(operator);
      m_shortText = shortText;
      m_text = text;

    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText());
    }

    @Override
    public String getText() {
      return m_text;
    }

    @Override
    public String getShortText() {
      return m_shortText;
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_NONE;
    }
  }

  private static class Null extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private String m_shortText;
    private String m_text;

    /**
     * @param aggregationType
     */
    Null(int operator, String shortText, String text) {
      super(operator);
      m_shortText = shortText;
      m_text = text;

    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText());
    }

    @Override
    public String getText() {
      return m_text;
    }

    @Override
    public String getShortText() {
      return m_shortText;
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_NONE;
    }
  }

  /**
   * nvl(x,0)==0
   */
  private static class NumberNull extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private String m_shortText;
    private String m_text;

    /**
     * @param aggregationType
     */
    NumberNull(int operator, String shortText, String text) {
      super(operator);
      m_shortText = shortText;
      m_text = text;

    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText());
    }

    @Override
    public String getText() {
      return m_text;
    }

    @Override
    public String getShortText() {
      return m_shortText;
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_NONE;
    }
  }

  private static class StartsWith extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private String m_shortText;
    private String m_text;

    /**
     * @param aggregationType
     */
    StartsWith(int operator, String shortText, String text) {
      super(operator);
      m_shortText = shortText;
      m_text = text;

    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    @Override
    public String getText() {
      return m_text;
    }

    @Override
    public String getShortText() {
      return m_shortText;
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_INHERITED;
    }
  }

  private static class NotStartsWith extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private String m_shortText;
    private String m_text;

    /**
     * @param aggregationType
     */
    NotStartsWith(int operator, String shortText, String text) {
      super(operator);
      m_shortText = shortText;
      m_text = text;

    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    @Override
    public String getText() {
      return m_text;
    }

    @Override
    public String getShortText() {
      return m_shortText;
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_INHERITED;
    }
  }

  private static class TimeIsInGEHours extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private String m_shortText;
    private String m_text;

    /**
     * @param aggregationType
     */
    TimeIsInGEHours(int operator, String shortText, String text) {
      super(operator);
      m_shortText = shortText;
      m_text = text;

    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    @Override
    public String getText() {
      return m_text;
    }

    @Override
    public String getShortText() {
      return m_shortText;
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_INTEGER;
    }
  }

  private static class TimeIsInGEMinutes extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private String m_shortText;
    private String m_text;

    /**
     * @param aggregationType
     */
    TimeIsInGEMinutes(int operator, String shortText, String text) {
      super(operator);
      m_shortText = shortText;
      m_text = text;

    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    @Override
    public String getText() {
      return m_text;
    }

    @Override
    public String getShortText() {
      return m_shortText;
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_INTEGER;
    }
  }

  private static class TimeIsInHours extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private String m_shortText;
    private String m_text;

    /**
     * @param aggregationType
     */
    TimeIsInHours(int operator, String shortText, String text) {
      super(operator);
      m_shortText = shortText;
      m_text = text;

    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    @Override
    public String getText() {
      return m_text;
    }

    @Override
    public String getShortText() {
      return m_shortText;
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_INTEGER;
    }
  }

  private static class TimeIsInLEHours extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private String m_shortText;
    private String m_text;

    /**
     * @param aggregationType
     */
    TimeIsInLEHours(int operator, String shortText, String text) {
      super(operator);
      m_shortText = shortText;
      m_text = text;

    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    @Override
    public String getText() {
      return m_text;
    }

    @Override
    public String getShortText() {
      return m_shortText;
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_INTEGER;
    }
  }

  private static class TimeIsInLEMinutes extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private String m_shortText;
    private String m_text;

    /**
     * @param aggregationType
     */
    TimeIsInLEMinutes(int operator, String shortText, String text) {
      super(operator);
      m_shortText = shortText;
      m_text = text;

    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    @Override
    public String getText() {
      return m_text;
    }

    @Override
    public String getShortText() {
      return m_shortText;
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_INTEGER;
    }
  }

  private static class TimeIsInMinutes extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private String m_shortText;
    private String m_text;

    /**
     * @param aggregationType
     */
    TimeIsInMinutes(int operator, String shortText, String text) {
      super(operator);
      m_shortText = shortText;
      m_text = text;

    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    @Override
    public String getText() {
      return m_text;
    }

    @Override
    public String getShortText() {
      return m_shortText;
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_INTEGER;
    }
  }

  private static class TimeIsNow extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private String m_shortText;
    private String m_text;

    /**
     * @param aggregationType
     */
    TimeIsNow(int operator, String shortText, String text) {
      super(operator);
      m_shortText = shortText;
      m_text = text;

    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText());
    }

    @Override
    public String getText() {
      return m_text;
    }

    @Override
    public String getShortText() {
      return m_shortText;
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_NONE;
    }
  }

  private static class TimeIsNotNow extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private String m_shortText;
    private String m_text;

    /**
     * @param aggregationType
     */
    TimeIsNotNow(int operator, String shortText, String text) {
      super(operator);
      m_shortText = shortText;
      m_text = text;

    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText());
    }

    @Override
    public String getText() {
      return m_text;
    }

    @Override
    public String getShortText() {
      return m_shortText;
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_NONE;
    }
  }

  private static class Between extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;
    private String m_shortText;
    private String m_text;

    /**
     * @param aggregationType
     */
    Between(int operator, String shortText, String text) {
      super(operator);
      m_shortText = shortText;
      m_text = text;
    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      if (StringUtility.isNullOrEmpty(valueTexts.get(0)) && StringUtility.hasText(valueTexts.get(1))) {
        return buildText(aggregationType, attributeText, ScoutTexts.get("LogicLE"), valueTexts.get(1));
      }
      else if (StringUtility.isNullOrEmpty(valueTexts.get(1)) && StringUtility.hasText(valueTexts.get(0))) {
        return buildText(aggregationType, attributeText, ScoutTexts.get("LogicGE"), valueTexts.get(0));
      }
      else {
        return buildText(aggregationType, attributeText, getText(), valueTexts);
      }
    }

    @Override
    public String getText() {
      return m_text;
    }

    @Override
    public String getShortText() {
      return m_shortText;
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_INHERITED;
    }
  }

  private static class Like extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private String m_shortText;
    private String m_text;

    /**
     * @param aggregationType
     */
    Like(int operator, String shortText, String text) {
      super(operator);
      m_shortText = shortText;
      m_text = text;

    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    @Override
    public String getText() {
      return m_text;
    }

    @Override
    public String getShortText() {
      return m_shortText;
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_STRING;
    }
  }
}
