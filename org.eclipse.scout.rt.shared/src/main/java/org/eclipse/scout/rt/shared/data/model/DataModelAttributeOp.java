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

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.shared.ScoutTexts;

public final class DataModelAttributeOp implements DataModelConstants {

  private DataModelAttributeOp() {
  }

  public static IDataModelAttributeOp create(int operator, String shortText, String text) {
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

  /**
   * @return a new {@link IDataModelAttributeOp} for a {@link DataModelConstants#OPERATOR_*}
   */
  public static IDataModelAttributeOp create(int operator) {
    return create(operator, null, null);
  }

  private abstract static class AbstractDataModelOp implements IDataModelAttributeOp, DataModelConstants, Serializable {

    private static final long serialVersionUID = 1L;
    private final int m_operator;

    AbstractDataModelOp(int operator) {
      m_operator = operator;
    }

    @Override
    public final int getOperator() {
      return m_operator;
    }

//    @Override
//    public abstract String getShortText() {
//      return getText();
//    }

    @Override
    public boolean equals(Object obj) {
      if (obj == null) {
        return false;
      }
      return (this.getClass() == obj.getClass() && this.m_operator == ((AbstractDataModelOp) obj).m_operator);
    }

    @Override
    public int hashCode() {
      return this.getClass().hashCode();
    }

    public static String buildText(Integer aggregationType, String attributeText, String opText) {
      List<String> valueTexts = CollectionUtility.emptyArrayList();
      return buildText(aggregationType, attributeText, opText, valueTexts);

    }

    public static String buildText(Integer aggregationType, String attributeText, String opText, String valueTexts) {
      return buildText(aggregationType, attributeText, opText, Collections.singletonList(valueTexts));
    }

    public static String buildText(Integer aggregationType, String attributeText, String opText, List<String> valueTexts) {
      String text1 = null;
      if (valueTexts != null && valueTexts.size() > 0) {
        text1 = valueTexts.get(0);
      }
      String text2 = null;
      if (valueTexts != null && valueTexts.size() > 1) {
        text2 = valueTexts.get(1);
      }
      StringBuilder b = new StringBuilder();
      if (aggregationType != null) {
        switch (aggregationType.intValue()) {
          case AGGREGATION_AVG: {
            b.append(ScoutTexts.get("ComposerFieldAggregationAvg", attributeText));
            break;
          }
          case AGGREGATION_COUNT: {
            b.append(ScoutTexts.get("ComposerFieldAggregationCount", attributeText));
            break;
          }
          case AGGREGATION_MAX: {
            b.append(ScoutTexts.get("ComposerFieldAggregationMax", attributeText));
            break;
          }
          case AGGREGATION_MEDIAN: {
            b.append(ScoutTexts.get("ComposerFieldAggregationMedian", attributeText));
            break;
          }
          case AGGREGATION_MIN: {
            b.append(ScoutTexts.get("ComposerFieldAggregationMin", attributeText));
            break;
          }
          case AGGREGATION_SUM: {
            b.append(ScoutTexts.get("ComposerFieldAggregationSum", attributeText));
            break;
          }
          default: {
            b.append(attributeText);
            break;
          }
        }
      }
      else {
        b.append(attributeText);
      }
      String verboseValue;
      if (opText.indexOf("{0}") >= 0) {
        verboseValue = opText;
        if (verboseValue.indexOf("{0}") >= 0 && text1 != null) {
          verboseValue = verboseValue.replace("{0}", text1);
        }
        if (verboseValue.indexOf("{1}") >= 0 && text2 != null) {
          verboseValue = verboseValue.replace("{1}", text2);
        }
      }
      else {
        verboseValue = opText;
        if (text1 != null) {
          verboseValue += " " + text1;
        }
        if (text2 != null) {
          verboseValue += " " + text2;
        }
      }
      b.append(" ");
      b.append(verboseValue);
      return b.toString();
    }
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
