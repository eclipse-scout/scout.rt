/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.data.model;

import java.util.List;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.text.TEXTS;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;

@SuppressWarnings("squid:S2160")
public class DataModelAttributeOperatorProvider implements IDataModelAttributeOperatorProvider, DataModelConstants {

  @Override
  @SuppressWarnings("squid:S138")
  public void injectOperators(IDataModelAttribute attribute, List<IDataModelAttributeOp> operatorList) {
    switch (attribute.getType()) {
      case TYPE_SMART: {
        operatorList.add(DataModelAttributeOp.create(OPERATOR_EQ, TEXTS.get("LogicIn"), TEXTS.get("LogicIn")));
        if (attribute.isNotOperatorEnabled()) {
          operatorList.add(DataModelAttributeOp.create(OPERATOR_NEQ, TEXTS.get("LogicNotIn"), TEXTS.get("LogicNotIn")));
        }
        if (attribute.isNullOperatorEnabled()) {
          operatorList.add(DataModelAttributeOp.create(OPERATOR_NUMBER_NULL));
        }
        if (attribute.isNotOperatorEnabled() && attribute.isNullOperatorEnabled()) {
          operatorList.add(DataModelAttributeOp.create(OPERATOR_NUMBER_NOT_NULL));
        }
        break;
      }
      case TYPE_LIST:
      case TYPE_TREE: {
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
      case TYPE_TIME: {
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
      case TYPE_DATE: {
        operatorList.add(DataModelAttributeOp.create(OPERATOR_DATE_EQ));
        if (attribute.isNotOperatorEnabled()) {
          operatorList.add(DataModelAttributeOp.create(OPERATOR_DATE_NEQ));
        }
        operatorList.add(DataModelAttributeOp.create(OPERATOR_DATE_BETWEEN));
        operatorList.add(DataModelAttributeOp.create(OPERATOR_YEAR_TO_DATE));
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
      case TYPE_DATE_TIME: {
        operatorList.add(DataModelAttributeOp.create(OPERATOR_DATE_TIME_EQ));
        if (attribute.isNotOperatorEnabled()) {
          operatorList.add(DataModelAttributeOp.create(OPERATOR_DATE_TIME_NEQ));
        }
        operatorList.add(DataModelAttributeOp.create(OPERATOR_DATE_TIME_BETWEEN));
        operatorList.add(DataModelAttributeOp.create(OPERATOR_YEAR_TO_DATE));
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
      case TYPE_INTEGER:
      case TYPE_LONG:
      case TYPE_BIG_DECIMAL:
      case TYPE_PLAIN_INTEGER:
      case TYPE_PLAIN_LONG:
      case TYPE_PLAIN_BIG_DECIMAL:
      case TYPE_PERCENT: {
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
      case TYPE_AGGREGATE_COUNT: {
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
      case TYPE_STRING: {
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
      case TYPE_RICH_TEXT:
      case TYPE_HTML:
      case TYPE_FULL_TEXT: {
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
  public IDataModelAttributeOp createOperator(int operator, String shortText, String text, String explanationText) {
    switch (operator) {
      case OPERATOR_BETWEEN:
        return new Between(OPERATOR_BETWEEN, (shortText == null) ? TEXTS.get("LogicBetweenShort") : shortText, (text == null) ? TEXTS.get("LogicBetween") : text, explanationText);
      case OPERATOR_DATE_BETWEEN:
        return new Between(OPERATOR_DATE_BETWEEN, (shortText == null) ? TEXTS.get("LogicBetweenShort") : shortText, (text == null) ? TEXTS.get("LogicBetween") : text, explanationText);
      case OPERATOR_DATE_TIME_BETWEEN:
        return new Between(OPERATOR_DATE_TIME_BETWEEN, (shortText == null) ? TEXTS.get("LogicBetweenShort") : shortText, (text == null) ? TEXTS.get("LogicBetween") : text, explanationText);
      case OPERATOR_NEQ:
        return new NEQ(OPERATOR_NEQ, (shortText == null) ? TEXTS.get("LogicNEQ") : shortText, (text == null) ? TEXTS.get("LogicNEQ") : text, explanationText);
      case OPERATOR_DATE_NEQ:
        return new NEQ(OPERATOR_DATE_NEQ, (shortText == null) ? TEXTS.get("LogicNEQ") : shortText, (text == null) ? TEXTS.get("LogicNEQ") : text, explanationText);
      case OPERATOR_DATE_TIME_NEQ:
        return new NEQ(OPERATOR_DATE_TIME_NEQ, (shortText == null) ? TEXTS.get("LogicNEQ") : shortText, (text == null) ? TEXTS.get("LogicNEQ") : text, explanationText);
      case OPERATOR_LT:
        return new LT(OPERATOR_LT, (shortText == null) ? TEXTS.get("LogicLT") : shortText, (text == null) ? TEXTS.get("LogicLT") : text, explanationText);
      case OPERATOR_DATE_LT:
        return new LT(OPERATOR_DATE_LT, (shortText == null) ? TEXTS.get("LogicLT") : shortText, (text == null) ? TEXTS.get("LogicLT") : text, explanationText);
      case OPERATOR_DATE_TIME_LT:
        return new LT(OPERATOR_DATE_TIME_LT, (shortText == null) ? TEXTS.get("LogicLT") : shortText, (text == null) ? TEXTS.get("LogicLT") : text, explanationText);
      case OPERATOR_LE:
        return new LE(OPERATOR_LE, (shortText == null) ? TEXTS.get("LogicLE") : shortText, (text == null) ? TEXTS.get("LogicLE") : text, explanationText);
      case OPERATOR_DATE_LE:
        return new LE(OPERATOR_DATE_LE, (shortText == null) ? TEXTS.get("LogicLE") : shortText, (text == null) ? TEXTS.get("LogicLE") : text, explanationText);
      case OPERATOR_DATE_TIME_LE:
        return new LE(OPERATOR_DATE_TIME_LE, (shortText == null) ? TEXTS.get("LogicLE") : shortText, (text == null) ? TEXTS.get("LogicLE") : text, explanationText);
      case OPERATOR_EQ:
        return new EQ(OPERATOR_EQ, (shortText == null) ? TEXTS.get("LogicEQ") : shortText, (text == null) ? TEXTS.get("LogicEQ") : text, explanationText);
      case OPERATOR_DATE_EQ:
        return new EQ(OPERATOR_DATE_EQ, (shortText == null) ? TEXTS.get("LogicEQ") : shortText, (text == null) ? TEXTS.get("LogicEQ") : text, explanationText);
      case OPERATOR_DATE_TIME_EQ:
        return new EQ(OPERATOR_DATE_TIME_EQ, (shortText == null) ? TEXTS.get("LogicEQ") : shortText, (text == null) ? TEXTS.get("LogicEQ") : text, explanationText);
      case OPERATOR_GT:
        return new GT(OPERATOR_GT, (shortText == null) ? TEXTS.get("LogicGT") : shortText, (text == null) ? TEXTS.get("LogicGT") : text, explanationText);
      case OPERATOR_DATE_GT:
        return new GT(OPERATOR_DATE_GT, (shortText == null) ? TEXTS.get("LogicGT") : shortText, (text == null) ? TEXTS.get("LogicGT") : text, explanationText);
      case OPERATOR_DATE_TIME_GT:
        return new GT(OPERATOR_DATE_TIME_GT, (shortText == null) ? TEXTS.get("LogicGT") : shortText, (text == null) ? TEXTS.get("LogicGT") : text, explanationText);
      case OPERATOR_GE:
        return new GE(OPERATOR_GE, (shortText == null) ? TEXTS.get("LogicGE") : shortText, (text == null) ? TEXTS.get("LogicGE") : text, explanationText);
      case OPERATOR_DATE_GE:
        return new GE(OPERATOR_DATE_GE, (shortText == null) ? TEXTS.get("LogicGE") : shortText, (text == null) ? TEXTS.get("LogicGE") : text, explanationText);
      case OPERATOR_DATE_TIME_GE:
        return new GE(OPERATOR_DATE_TIME_GE, (shortText == null) ? TEXTS.get("LogicGE") : shortText, (text == null) ? TEXTS.get("LogicGE") : text, explanationText);
      case OPERATOR_DATE_IS_IN_DAYS:
        return new DateIsInDays(OPERATOR_DATE_IS_IN_DAYS, (shortText == null) ? TEXTS.get("LogicDateIsInDays") : shortText, (text == null) ? TEXTS.get("LogicDateIsInDays") : text, explanationText);
      case OPERATOR_DATE_IS_IN_GE_DAYS:
        return new DateIsInGEDays(OPERATOR_DATE_IS_IN_GE_DAYS, (shortText == null) ? TEXTS.get("LogicDateIsInGEDays") : shortText, (text == null) ? TEXTS.get("LogicDateIsInGEDays") : text, explanationText);
      case OPERATOR_DATE_IS_IN_GE_MONTHS:
        return new DateIsInGEMonths(OPERATOR_DATE_IS_IN_GE_MONTHS, (shortText == null) ? TEXTS.get("LogicDateIsInGEMonths") : shortText, (text == null) ? TEXTS.get("LogicDateIsInGEMonths") : text, explanationText);
      case OPERATOR_DATE_IS_IN_LE_DAYS:
        return new DateIsInLEDays(OPERATOR_DATE_IS_IN_LE_DAYS, (shortText == null) ? TEXTS.get("LogicDateIsInLEDays") : shortText, (text == null) ? TEXTS.get("LogicDateIsInLEDays") : text, explanationText);
      case OPERATOR_DATE_IS_IN_LE_MONTHS:
        return new DateIsInLEMonths(OPERATOR_DATE_IS_IN_LE_MONTHS, (shortText == null) ? TEXTS.get("LogicDateIsInLEMonths") : shortText, (text == null) ? TEXTS.get("LogicDateIsInLEMonths") : text, explanationText);
      case OPERATOR_DATE_IS_IN_LAST_DAYS:
        return new DateIsInLastDays(OPERATOR_DATE_IS_IN_LAST_DAYS, (shortText == null) ? TEXTS.get("LogicDateIsInLastDays") : shortText, (text == null) ? TEXTS.get("LogicDateIsInLastDays") : text, explanationText);
      case OPERATOR_DATE_IS_IN_LAST_MONTHS:
        return new DateIsInLastMonths(OPERATOR_DATE_IS_IN_LAST_MONTHS, (shortText == null) ? TEXTS.get("LogicDateIsInLastMonths") : shortText, (text == null) ? TEXTS.get("LogicDateIsInLastMonths") : text, explanationText);
      case OPERATOR_DATE_IS_IN_MONTHS:
        return new DateIsInMonths(OPERATOR_DATE_IS_IN_MONTHS, (shortText == null) ? TEXTS.get("LogicDateIsInMonths") : shortText, (text == null) ? TEXTS.get("LogicDateIsInMonths") : text, explanationText);
      case OPERATOR_DATE_IS_IN_NEXT_DAYS:
        return new DateIsInNextDays(OPERATOR_DATE_IS_IN_NEXT_DAYS, (shortText == null) ? TEXTS.get("LogicDateIsInNextDays") : shortText, (text == null) ? TEXTS.get("LogicDateIsInNextDays") : text, explanationText);
      case OPERATOR_DATE_IS_IN_NEXT_MONTHS:
        return new DateIsInNextMonths(OPERATOR_DATE_IS_IN_NEXT_MONTHS, (shortText == null) ? TEXTS.get("LogicDateIsInNextMonths") : shortText, (text == null) ? TEXTS.get("LogicDateIsInNextMonths") : text, explanationText);
      case OPERATOR_DATE_IS_NOT_TODAY:
        return new DateIsNotToday(OPERATOR_DATE_IS_NOT_TODAY, (shortText == null) ? TEXTS.get("LogicDateIsNotToday") : shortText, (text == null) ? TEXTS.get("LogicDateIsNotToday") : text, explanationText);
      case OPERATOR_DATE_IS_TODAY:
        return new DateIsToday(OPERATOR_DATE_IS_TODAY, (shortText == null) ? TEXTS.get("LogicDateIsToday") : shortText, (text == null) ? TEXTS.get("LogicDateIsToday") : text, explanationText);
      case OPERATOR_YEAR_TO_DATE:
        return new YearToDate(OPERATOR_YEAR_TO_DATE, (shortText == null) ? TEXTS.get("LogicYearToDateShort") : shortText, (text == null) ? TEXTS.get("LogicYearToDate") : text,
            (explanationText == null) ? TEXTS.get("LogicYearToDateExplanation") : explanationText);
      case OPERATOR_DATE_TIME_IS_IN_GE_HOURS:
        return new DateTimeIsInGEHours(OPERATOR_DATE_TIME_IS_IN_GE_HOURS, (shortText == null) ? TEXTS.get("LogicDateTimeIsInGEHours") : shortText, (text == null) ? TEXTS.get("LogicDateTimeIsInGEHours") : text, explanationText);
      case OPERATOR_DATE_TIME_IS_IN_GE_MINUTES:
        return new DateTimeIsInGEMinutes(OPERATOR_DATE_TIME_IS_IN_GE_MINUTES, (shortText == null) ? TEXTS.get("LogicDateTimeIsInGEMinutes") : shortText, (text == null) ? TEXTS.get("LogicDateTimeIsInGEMinutes") : text, explanationText);
      case OPERATOR_DATE_TIME_IS_IN_LE_HOURS:
        return new DateTimeIsInLEHours(OPERATOR_DATE_TIME_IS_IN_LE_HOURS, (shortText == null) ? TEXTS.get("LogicDateTimeIsInLEHours") : shortText, (text == null) ? TEXTS.get("LogicDateTimeIsInLEHours") : text, explanationText);
      case OPERATOR_DATE_TIME_IS_IN_LE_MINUTES:
        return new DateTimeIsInLEMinutes(OPERATOR_DATE_TIME_IS_IN_LE_MINUTES, (shortText == null) ? TEXTS.get("LogicDateTimeIsInLEMinutes") : shortText, (text == null) ? TEXTS.get("LogicDateTimeIsInLEMinutes") : text, explanationText);
      case OPERATOR_DATE_TIME_IS_NOT_NOW:
        return new DateTimeIsNotNow(OPERATOR_DATE_TIME_IS_NOT_NOW, (shortText == null) ? TEXTS.get("LogicDateTimeIsNotNow") : shortText, (text == null) ? TEXTS.get("LogicDateTimeIsNotNow") : text, explanationText);
      case OPERATOR_DATE_TIME_IS_NOW:
        return new DateTimeIsNow(OPERATOR_DATE_TIME_IS_NOW, (shortText == null) ? TEXTS.get("LogicDateTimeIsNow") : shortText, (text == null) ? TEXTS.get("LogicDateTimeIsNow") : text, explanationText);
      case OPERATOR_ENDS_WITH:
        return new EndsWith(OPERATOR_ENDS_WITH, (shortText == null) ? TEXTS.get("LogicEndsWith") : shortText, (text == null) ? TEXTS.get("LogicEndsWith") : text, explanationText);
      case OPERATOR_NOT_ENDS_WITH:
        return new NotEndsWith(OPERATOR_NOT_ENDS_WITH, (shortText == null) ? TEXTS.get("LogicNotEndsWith") : shortText, (text == null) ? TEXTS.get("LogicNotEndsWith") : text, explanationText);
      case OPERATOR_IN:
        return new In(OPERATOR_IN, (shortText == null) ? TEXTS.get("LogicIn") : shortText, (text == null) ? TEXTS.get("LogicIn") : text, explanationText);
      case OPERATOR_CONTAINS:
        return new Contains(OPERATOR_CONTAINS, (shortText == null) ? TEXTS.get("LogicLike") : shortText, (text == null) ? TEXTS.get("LogicLike") : text, explanationText);
      case OPERATOR_LIKE:
        return new Like(OPERATOR_LIKE, (shortText == null) ? TEXTS.get("LogicEQ") : shortText, (text == null) ? TEXTS.get("LogicEQ") : text, explanationText);
      case OPERATOR_NOT_LIKE:
        return new Like(OPERATOR_NOT_LIKE, (shortText == null) ? TEXTS.get("LogicNEQ") : shortText, (text == null) ? TEXTS.get("LogicNEQ") : text, explanationText);
      case OPERATOR_NOT_IN:
        return new NotIn(OPERATOR_NOT_IN, (shortText == null) ? TEXTS.get("LogicNotIn") : shortText, (text == null) ? TEXTS.get("LogicNotIn") : text, explanationText);
      case OPERATOR_NOT_CONTAINS:
        return new NotContains(OPERATOR_NOT_CONTAINS, (shortText == null) ? TEXTS.get("LogicNotLike") : shortText, (text == null) ? TEXTS.get("LogicNotLike") : text, explanationText);
      case OPERATOR_NOT_NULL:
        return new NotNull(OPERATOR_NOT_NULL, (shortText == null) ? TEXTS.get("LogicNotNull") : shortText, (text == null) ? TEXTS.get("LogicNotNull") : text, explanationText);
      case OPERATOR_NUMBER_NOT_NULL:
        return new NumberNotNull(OPERATOR_NUMBER_NOT_NULL, (shortText == null) ? TEXTS.get("LogicNotNull") : shortText, (text == null) ? TEXTS.get("LogicNotNull") : text, explanationText);
      case OPERATOR_NULL:
        return new Null(OPERATOR_NULL, (shortText == null) ? TEXTS.get("LogicNull") : shortText, (text == null) ? TEXTS.get("LogicNull") : text, explanationText);
      case OPERATOR_NUMBER_NULL:
        return new NumberNull(OPERATOR_NUMBER_NULL, (shortText == null) ? TEXTS.get("LogicNull") : shortText, (text == null) ? TEXTS.get("LogicNull") : text, explanationText);
      case OPERATOR_STARTS_WITH:
        return new StartsWith(OPERATOR_STARTS_WITH, (shortText == null) ? TEXTS.get("LogicStartsWith") : shortText, (text == null) ? TEXTS.get("LogicStartsWith") : text, explanationText);
      case OPERATOR_NOT_STARTS_WITH:
        return new NotStartsWith(OPERATOR_NOT_STARTS_WITH, (shortText == null) ? TEXTS.get("LogicNotStartsWith") : shortText, (text == null) ? TEXTS.get("LogicNotStartsWith") : text, explanationText);
    }
    return null;
  }

  private static class NEQ extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;
    private final String m_shortText;
    private final String m_text;
    private final String m_explanationText;

    /**
     * @param aggregationType
     */
    NEQ(int operator, String shortText, String text, String explanationText) {
      super(operator);
      m_shortText = shortText;
      m_text = text;
      m_explanationText = explanationText;
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
    public String getExplanationText() {
      return m_explanationText;
    }

    @Override
    public int getType() {
      return TYPE_INHERITED;
    }
  }

  private static class LT extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;
    private final String m_shortText;
    private final String m_text;
    private final String m_explanationText;

    /**
     * @param aggregationType
     */
    LT(int operator, String shortText, String text, String explanationText) {
      super(operator);
      m_shortText = shortText;
      m_text = text;
      m_explanationText = explanationText;
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
    public String getExplanationText() {
      return m_explanationText;
    }

    @Override
    public int getType() {
      return TYPE_INHERITED;
    }
  }

  private static class LE extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private final String m_shortText;
    private final String m_text;
    private final String m_explanationText;

    /**
     * @param aggregationType
     */
    LE(int operator, String shortText, String text, String explanationText) {
      super(operator);
      m_shortText = shortText;
      m_text = text;
      m_explanationText = explanationText;
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
    public String getExplanationText() {
      return m_explanationText;
    }

    @Override
    public int getType() {
      return TYPE_INHERITED;
    }
  }

  private static class EQ extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private final String m_shortText;
    private final String m_text;
    private final String m_explanationText;

    /**
     * @param aggregationType
     */
    EQ(int operator, String shortText, String text, String explanationText) {
      super(operator);
      m_shortText = shortText;
      m_text = text;
      m_explanationText = explanationText;
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
    public String getExplanationText() {
      return m_explanationText;
    }

    @Override
    public int getType() {
      return TYPE_INHERITED;
    }
  }

  private static class GT extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private final String m_shortText;
    private final String m_text;
    private final String m_explanationText;

    /**
     * @param aggregationType
     */
    GT(int operator, String shortText, String text, String explanationText) {
      super(operator);
      m_shortText = shortText;
      m_text = text;
      m_explanationText = explanationText;
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
    public String getExplanationText() {
      return m_explanationText;
    }

    @Override
    public int getType() {
      return TYPE_INHERITED;
    }
  }

  private static class GE extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private final String m_shortText;
    private final String m_text;
    private final String m_explanationText;

    /**
     * @param aggregationType
     */
    GE(int operator, String shortText, String text, String explanationText) {
      super(operator);
      m_shortText = shortText;
      m_text = text;
      m_explanationText = explanationText;
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
    public String getExplanationText() {
      return m_explanationText;
    }

    @Override
    public int getType() {
      return TYPE_INHERITED;
    }
  }

  private static class DateIsInDays extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private final String m_shortText;
    private final String m_text;
    private final String m_explanationText;

    /**
     * @param aggregationType
     */
    DateIsInDays(int operator, String shortText, String text, String explanationText) {
      super(operator);
      m_shortText = shortText;
      m_text = text;
      m_explanationText = explanationText;
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
    public String getExplanationText() {
      return m_explanationText;
    }

    @Override
    public int getType() {
      return TYPE_INTEGER;
    }
  }

  private static class DateIsInGEDays extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private final String m_shortText;
    private final String m_text;
    private final String m_explanationText;

    /**
     * @param aggregationType
     */
    DateIsInGEDays(int operator, String shortText, String text, String explanationText) {
      super(operator);
      m_shortText = shortText;
      m_text = text;
      m_explanationText = explanationText;
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
    public String getExplanationText() {
      return m_explanationText;
    }

    @Override
    public int getType() {
      return TYPE_INTEGER;
    }
  }

  private static class DateIsInGEMonths extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private final String m_shortText;
    private final String m_text;
    private final String m_explanationText;

    /**
     * @param aggregationType
     */
    DateIsInGEMonths(int operator, String shortText, String text, String explanationText) {
      super(operator);
      m_shortText = shortText;
      m_text = text;
      m_explanationText = explanationText;
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
    public String getExplanationText() {
      return m_explanationText;
    }

    @Override
    public int getType() {
      return TYPE_INTEGER;
    }
  }

  private static class DateIsInLEDays extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private final String m_shortText;
    private final String m_text;
    private final String m_explanationText;

    /**
     * @param aggregationType
     */
    DateIsInLEDays(int operator, String shortText, String text, String explanationText) {
      super(operator);
      m_shortText = shortText;
      m_text = text;
      m_explanationText = explanationText;
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
    public String getExplanationText() {
      return m_explanationText;
    }

    @Override
    public int getType() {
      return TYPE_INTEGER;
    }
  }

  private static class DateIsInLEMonths extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private final String m_shortText;
    private final String m_text;
    private final String m_explanationText;

    /**
     * @param aggregationType
     */
    DateIsInLEMonths(int operator, String shortText, String text, String explanationText) {
      super(operator);
      m_shortText = shortText;
      m_text = text;
      m_explanationText = explanationText;
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
    public String getExplanationText() {
      return m_explanationText;
    }

    @Override
    public int getType() {
      return TYPE_INTEGER;
    }
  }

  private static class DateIsInLastDays extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private final String m_shortText;
    private final String m_text;
    private final String m_explanationText;

    /**
     * @param aggregationType
     */
    DateIsInLastDays(int operator, String shortText, String text, String explanationText) {
      super(operator);
      m_shortText = shortText;
      m_text = text;
      m_explanationText = explanationText;
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
    public String getExplanationText() {
      return m_explanationText;
    }

    @Override
    public int getType() {
      return TYPE_INTEGER;
    }
  }

  private static class DateIsInLastMonths extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private final String m_shortText;
    private final String m_text;
    private final String m_explanationText;

    /**
     * @param aggregationType
     */
    DateIsInLastMonths(int operator, String shortText, String text, String explanationText) {
      super(operator);
      m_shortText = shortText;
      m_text = text;
      m_explanationText = explanationText;
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
    public String getExplanationText() {
      return m_explanationText;
    }

    @Override
    public int getType() {
      return TYPE_INTEGER;
    }
  }

  private static class DateIsInMonths extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private final String m_shortText;
    private final String m_text;
    private final String m_explanationText;

    /**
     * @param aggregationType
     */
    DateIsInMonths(int operator, String shortText, String text, String explanationText) {
      super(operator);
      m_shortText = shortText;
      m_text = text;
      m_explanationText = explanationText;
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
    public String getExplanationText() {
      return m_explanationText;
    }

    @Override
    public int getType() {
      return TYPE_INTEGER;
    }
  }

  private static class DateIsInNextDays extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private final String m_shortText;
    private final String m_text;
    private final String m_explanationText;

    /**
     * @param aggregationType
     */
    DateIsInNextDays(int operator, String shortText, String text, String explanationText) {
      super(operator);
      m_shortText = shortText;
      m_text = text;
      m_explanationText = explanationText;
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
    public String getExplanationText() {
      return m_explanationText;
    }

    @Override
    public int getType() {
      return TYPE_INTEGER;
    }
  }

  private static class DateIsInNextMonths extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private final String m_shortText;
    private final String m_text;
    private final String m_explanationText;

    /**
     * @param aggregationType
     */
    DateIsInNextMonths(int operator, String shortText, String text, String explanationText) {
      super(operator);
      m_shortText = shortText;
      m_text = text;
      m_explanationText = explanationText;
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
    public String getExplanationText() {
      return m_explanationText;
    }

    @Override
    public int getType() {
      return TYPE_INTEGER;
    }
  }

  private static class DateIsNotToday extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private final String m_shortText;
    private final String m_text;
    private final String m_explanationText;

    /**
     * @param aggregationType
     */
    DateIsNotToday(int operator, String shortText, String text, String explanationText) {
      super(operator);
      m_shortText = shortText;
      m_text = text;
      m_explanationText = explanationText;
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
    public String getExplanationText() {
      return m_explanationText;
    }

    @Override
    public int getType() {
      return TYPE_NONE;
    }
  }

  private static class DateIsToday extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private final String m_shortText;
    private final String m_text;
    private final String m_explanationText;

    /**
     * @param aggregationType
     */
    DateIsToday(int operator, String shortText, String text, String explanationText) {
      super(operator);
      m_shortText = shortText;
      m_text = text;
      m_explanationText = explanationText;
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
    public String getExplanationText() {
      return m_explanationText;
    }

    @Override
    public int getType() {
      return TYPE_NONE;
    }
  }

  private static class YearToDate extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private final String m_shortText;
    private final String m_text;
    private final String m_explanationText;

    /**
     * @param aggregationType
     */
    YearToDate(int operator, String shortText, String text, String explanationText) {
      super(operator);
      m_shortText = shortText;
      m_text = text;
      m_explanationText = explanationText;
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
    public String getExplanationText() {
      return m_explanationText;
    }

    @Override
    public int getType() {
      return TYPE_SMART;
    }

    @Override
    public ILookupCall<?> getLookupCall() {
      return BEANS.get(YearToDateCodeTypeLookupCall.class);
    }
  }

  private static class DateTimeIsInGEHours extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private final String m_shortText;
    private final String m_text;
    private final String m_explanationText;

    /**
     * @param aggregationType
     */
    DateTimeIsInGEHours(int operator, String shortText, String text, String explanationText) {
      super(operator);
      m_shortText = shortText;
      m_text = text;
      m_explanationText = explanationText;
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
    public String getExplanationText() {
      return m_explanationText;
    }

    @Override
    public int getType() {
      return TYPE_INTEGER;
    }
  }

  private static class DateTimeIsInGEMinutes extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private final String m_shortText;
    private final String m_text;
    private final String m_explanationText;

    /**
     * @param aggregationType
     */
    DateTimeIsInGEMinutes(int operator, String shortText, String text, String explanationText) {
      super(operator);
      m_shortText = shortText;
      m_text = text;
      m_explanationText = explanationText;
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
    public String getExplanationText() {
      return m_explanationText;
    }

    @Override
    public int getType() {
      return TYPE_INTEGER;
    }
  }

  private static class DateTimeIsInLEHours extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private final String m_shortText;
    private final String m_text;
    private final String m_explanationText;

    /**
     * @param aggregationType
     */
    DateTimeIsInLEHours(int operator, String shortText, String text, String explanationText) {
      super(operator);
      m_shortText = shortText;
      m_text = text;
      m_explanationText = explanationText;
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
    public String getExplanationText() {
      return m_explanationText;
    }

    @Override
    public int getType() {
      return TYPE_INTEGER;
    }
  }

  private static class DateTimeIsInLEMinutes extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private final String m_shortText;
    private final String m_text;
    private final String m_explanationText;

    /**
     * @param aggregationType
     */
    DateTimeIsInLEMinutes(int operator, String shortText, String text, String explanationText) {
      super(operator);
      m_shortText = shortText;
      m_text = text;
      m_explanationText = explanationText;
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
    public String getExplanationText() {
      return m_explanationText;
    }

    @Override
    public int getType() {
      return TYPE_INTEGER;
    }
  }

  private static class DateTimeIsNotNow extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private final String m_shortText;
    private final String m_text;
    private final String m_explanationText;

    /**
     * @param aggregationType
     */
    DateTimeIsNotNow(int operator, String shortText, String text, String explanationText) {
      super(operator);
      m_shortText = shortText;
      m_text = text;
      m_explanationText = explanationText;
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
    public String getExplanationText() {
      return m_explanationText;
    }

    @Override
    public int getType() {
      return TYPE_NONE;
    }
  }

  private static class DateTimeIsNow extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private final String m_shortText;
    private final String m_text;
    private final String m_explanationText;

    /**
     * @param aggregationType
     */
    DateTimeIsNow(int operator, String shortText, String text, String explanationText) {
      super(operator);
      m_shortText = shortText;
      m_text = text;
      m_explanationText = explanationText;
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
    public String getExplanationText() {
      return m_explanationText;
    }

    @Override
    public int getType() {
      return TYPE_NONE;
    }
  }

  private static class EndsWith extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private final String m_shortText;
    private final String m_text;
    private final String m_explanationText;

    /**
     * @param aggregationType
     */
    EndsWith(int operator, String shortText, String text, String explanationText) {
      super(operator);
      m_shortText = shortText;
      m_text = text;
      m_explanationText = explanationText;
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
    public String getExplanationText() {
      return m_explanationText;
    }

    @Override
    public int getType() {
      return TYPE_INHERITED;
    }
  }

  private static class NotEndsWith extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private final String m_shortText;
    private final String m_text;
    private final String m_explanationText;

    /**
     * @param aggregationType
     */
    NotEndsWith(int operator, String shortText, String text, String explanationText) {
      super(operator);
      m_shortText = shortText;
      m_text = text;
      m_explanationText = explanationText;
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
    public String getExplanationText() {
      return m_explanationText;
    }

    @Override
    public int getType() {
      return TYPE_INHERITED;
    }
  }

  private static class In extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private final String m_shortText;
    private final String m_text;
    private final String m_explanationText;

    /**
     * @param aggregationType
     */
    In(int operator, String shortText, String text, String explanationText) {
      super(operator);
      m_shortText = shortText;
      m_text = text;
      m_explanationText = explanationText;
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
    public String getExplanationText() {
      return m_explanationText;
    }

    @Override
    public int getType() {
      return TYPE_INHERITED;
    }
  }

  private static class Contains extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private final String m_shortText;
    private final String m_text;
    private final String m_explanationText;

    /**
     * @param aggregationType
     */
    Contains(int operator, String shortText, String text, String explanationText) {
      super(operator);
      m_shortText = shortText;
      m_text = text;
      m_explanationText = explanationText;
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
    public String getExplanationText() {
      return m_explanationText;
    }

    @Override
    public int getType() {
      return TYPE_INHERITED;
    }
  }

  private static class NotIn extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private final String m_shortText;
    private final String m_text;
    private final String m_explanationText;

    /**
     * @param aggregationType
     */
    NotIn(int operator, String shortText, String text, String explanationText) {
      super(operator);
      m_shortText = shortText;
      m_text = text;
      m_explanationText = explanationText;
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
    public String getExplanationText() {
      return m_explanationText;
    }

    @Override
    public int getType() {
      return TYPE_INHERITED;
    }
  }

  private static class NotContains extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private final String m_shortText;
    private final String m_text;
    private final String m_explanationText;

    /**
     * @param aggregationType
     */
    NotContains(int operator, String shortText, String text, String explanationText) {
      super(operator);
      m_shortText = shortText;
      m_text = text;
      m_explanationText = explanationText;
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
    public String getExplanationText() {
      return m_explanationText;
    }

    @Override
    public int getType() {
      return TYPE_INHERITED;
    }
  }

  private static class NotNull extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private final String m_shortText;
    private final String m_text;
    private final String m_explanationText;

    /**
     * @param aggregationType
     */
    NotNull(int operator, String shortText, String text, String explanationText) {
      super(operator);
      m_shortText = shortText;
      m_text = text;
      m_explanationText = explanationText;
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
    public String getExplanationText() {
      return m_explanationText;
    }

    @Override
    public int getType() {
      return TYPE_NONE;
    }
  }

  /**
   * nvl(x,0)<>0
   */
  private static class NumberNotNull extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private final String m_shortText;
    private final String m_text;
    private final String m_explanationText;

    /**
     * @param aggregationType
     */
    NumberNotNull(int operator, String shortText, String text, String explanationText) {
      super(operator);
      m_shortText = shortText;
      m_text = text;
      m_explanationText = explanationText;
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
    public String getExplanationText() {
      return m_explanationText;
    }

    @Override
    public int getType() {
      return TYPE_NONE;
    }
  }

  private static class Null extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private final String m_shortText;
    private final String m_text;
    private final String m_explanationText;

    /**
     * @param aggregationType
     */
    Null(int operator, String shortText, String text, String explanationText) {
      super(operator);
      m_shortText = shortText;
      m_text = text;
      m_explanationText = explanationText;
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
    public String getExplanationText() {
      return m_explanationText;
    }

    @Override
    public int getType() {
      return TYPE_NONE;
    }
  }

  /**
   * nvl(x,0)==0
   */
  private static class NumberNull extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private final String m_shortText;
    private final String m_text;
    private final String m_explanationText;

    /**
     * @param aggregationType
     */
    NumberNull(int operator, String shortText, String text, String explanationText) {
      super(operator);
      m_shortText = shortText;
      m_text = text;
      m_explanationText = explanationText;
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
    public String getExplanationText() {
      return m_explanationText;
    }

    @Override
    public int getType() {
      return TYPE_NONE;
    }
  }

  private static class StartsWith extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private final String m_shortText;
    private final String m_text;
    private final String m_explanationText;

    /**
     * @param aggregationType
     */
    StartsWith(int operator, String shortText, String text, String explanationText) {
      super(operator);
      m_shortText = shortText;
      m_text = text;
      m_explanationText = explanationText;
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
    public String getExplanationText() {
      return m_explanationText;
    }

    @Override
    public int getType() {
      return TYPE_INHERITED;
    }
  }

  private static class NotStartsWith extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private final String m_shortText;
    private final String m_text;
    private final String m_explanationText;

    /**
     * @param aggregationType
     */
    NotStartsWith(int operator, String shortText, String text, String explanationText) {
      super(operator);
      m_shortText = shortText;
      m_text = text;
      m_explanationText = explanationText;
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
    public String getExplanationText() {
      return m_explanationText;
    }

    @Override
    public int getType() {
      return TYPE_INHERITED;
    }
  }

  private static class TimeIsInGEHours extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private final String m_shortText;
    private final String m_text;
    private final String m_explanationText;

    /**
     * @param aggregationType
     */
    TimeIsInGEHours(int operator, String shortText, String text, String explanationText) {
      super(operator);
      m_shortText = shortText;
      m_text = text;
      m_explanationText = explanationText;
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
    public String getExplanationText() {
      return m_explanationText;
    }

    @Override
    public int getType() {
      return TYPE_INTEGER;
    }
  }

  private static class TimeIsInGEMinutes extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private final String m_shortText;
    private final String m_text;
    private final String m_explanationText;

    /**
     * @param aggregationType
     */
    TimeIsInGEMinutes(int operator, String shortText, String text, String explanationText) {
      super(operator);
      m_shortText = shortText;
      m_text = text;
      m_explanationText = explanationText;
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
    public String getExplanationText() {
      return m_explanationText;
    }

    @Override
    public int getType() {
      return TYPE_INTEGER;
    }
  }

  private static class TimeIsInHours extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private final String m_shortText;
    private final String m_text;
    private final String m_explanationText;

    /**
     * @param aggregationType
     */
    TimeIsInHours(int operator, String shortText, String text, String explanationText) {
      super(operator);
      m_shortText = shortText;
      m_text = text;
      m_explanationText = explanationText;
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
    public String getExplanationText() {
      return m_explanationText;
    }

    @Override
    public int getType() {
      return TYPE_INTEGER;
    }
  }

  private static class TimeIsInLEHours extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private final String m_shortText;
    private final String m_text;
    private final String m_explanationText;

    /**
     * @param aggregationType
     */
    TimeIsInLEHours(int operator, String shortText, String text, String explanationText) {
      super(operator);
      m_shortText = shortText;
      m_text = text;
      m_explanationText = explanationText;
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
    public String getExplanationText() {
      return m_explanationText;
    }

    @Override
    public int getType() {
      return TYPE_INTEGER;
    }
  }

  private static class TimeIsInLEMinutes extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private final String m_shortText;
    private final String m_text;
    private final String m_explanationText;

    /**
     * @param aggregationType
     */
    TimeIsInLEMinutes(int operator, String shortText, String text, String explanationText) {
      super(operator);
      m_shortText = shortText;
      m_text = text;
      m_explanationText = explanationText;
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
    public String getExplanationText() {
      return m_explanationText;
    }

    @Override
    public int getType() {
      return TYPE_INTEGER;
    }
  }

  private static class TimeIsInMinutes extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private final String m_shortText;
    private final String m_text;
    private final String m_explanationText;

    /**
     * @param aggregationType
     */
    TimeIsInMinutes(int operator, String shortText, String text, String explanationText) {
      super(operator);
      m_shortText = shortText;
      m_text = text;
      m_explanationText = explanationText;
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
    public String getExplanationText() {
      return m_explanationText;
    }

    @Override
    public int getType() {
      return TYPE_INTEGER;
    }
  }

  private static class TimeIsNow extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private final String m_shortText;
    private final String m_text;
    private final String m_explanationText;

    /**
     * @param aggregationType
     */
    TimeIsNow(int operator, String shortText, String text, String explanationText) {
      super(operator);
      m_shortText = shortText;
      m_text = text;
      m_explanationText = explanationText;
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
    public String getExplanationText() {
      return m_explanationText;
    }

    @Override
    public int getType() {
      return TYPE_NONE;
    }
  }

  private static class TimeIsNotNow extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private final String m_shortText;
    private final String m_text;
    private final String m_explanationText;

    /**
     * @param aggregationType
     */
    TimeIsNotNow(int operator, String shortText, String text, String explanationText) {
      super(operator);
      m_shortText = shortText;
      m_text = text;
      m_explanationText = explanationText;
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
    public String getExplanationText() {
      return m_explanationText;
    }

    @Override
    public int getType() {
      return TYPE_NONE;
    }
  }

  private static class Between extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;
    private final String m_shortText;
    private final String m_text;
    private final String m_explanationText;

    /**
     * @param aggregationType
     */
    Between(int operator, String shortText, String text, String explanationText) {
      super(operator);
      m_shortText = shortText;
      m_text = text;
      m_explanationText = explanationText;
    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      if (StringUtility.isNullOrEmpty(valueTexts.get(0)) && StringUtility.hasText(valueTexts.get(1))) {
        return buildText(aggregationType, attributeText, TEXTS.get("LogicLE"), valueTexts.get(1));
      }
      else if (StringUtility.isNullOrEmpty(valueTexts.get(1)) && StringUtility.hasText(valueTexts.get(0))) {
        return buildText(aggregationType, attributeText, TEXTS.get("LogicGE"), valueTexts.get(0));
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
    public String getExplanationText() {
      return m_explanationText;
    }

    @Override
    public int getType() {
      return TYPE_INHERITED;
    }
  }

  private static class Like extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    private final String m_shortText;
    private final String m_text;
    private final String m_explanationText;

    /**
     * @param aggregationType
     */
    Like(int operator, String shortText, String text, String explanationText) {
      super(operator);
      m_shortText = shortText;
      m_text = text;
      m_explanationText = explanationText;
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
    public String getExplanationText() {
      return m_explanationText;
    }

    @Override
    public int getType() {
      return TYPE_STRING;
    }
  }
}
