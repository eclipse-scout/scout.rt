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

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.rt.shared.ScoutTexts;

public final class DataModelAttributeOp implements DataModelConstants {

  private DataModelAttributeOp() {
  }

  /**
   * @return a new {@link IDataModelAttributeOp} for a {@link DataModelConstants#OPERATOR_*}
   */
  public static IDataModelAttributeOp create(int operator) {
    switch (operator) {
      case OPERATOR_BETWEEN:
        return new Between(OPERATOR_BETWEEN);
      case OPERATOR_DATE_BETWEEN:
        return new Between(OPERATOR_DATE_BETWEEN);
      case OPERATOR_DATE_TIME_BETWEEN:
        return new Between(OPERATOR_DATE_TIME_BETWEEN);
      case OPERATOR_NEQ:
        return new NEQ(OPERATOR_NEQ);
      case OPERATOR_DATE_NEQ:
        return new NEQ(OPERATOR_DATE_NEQ);
      case OPERATOR_DATE_TIME_NEQ:
        return new NEQ(OPERATOR_DATE_TIME_NEQ);
      case OPERATOR_LT:
        return new LT(OPERATOR_LT);
      case OPERATOR_DATE_LT:
        return new LT(OPERATOR_DATE_LT);
      case OPERATOR_DATE_TIME_LT:
        return new LT(OPERATOR_DATE_TIME_LT);
      case OPERATOR_LE:
        return new LE(OPERATOR_LE);
      case OPERATOR_DATE_LE:
        return new LE(OPERATOR_DATE_LE);
      case OPERATOR_DATE_TIME_LE:
        return new LE(OPERATOR_DATE_TIME_LE);
      case OPERATOR_EQ:
        return new EQ(OPERATOR_EQ);
      case OPERATOR_DATE_EQ:
        return new EQ(OPERATOR_DATE_EQ);
      case OPERATOR_DATE_TIME_EQ:
        return new EQ(OPERATOR_DATE_TIME_EQ);
      case OPERATOR_GT:
        return new GT(OPERATOR_GT);
      case OPERATOR_DATE_GT:
        return new GT(OPERATOR_DATE_GT);
      case OPERATOR_DATE_TIME_GT:
        return new GT(OPERATOR_DATE_TIME_GT);
      case OPERATOR_GE:
        return new GE(OPERATOR_GE);
      case OPERATOR_DATE_GE:
        return new GE(OPERATOR_DATE_GE);
      case OPERATOR_DATE_TIME_GE:
        return new GE(OPERATOR_DATE_TIME_GE);
      case OPERATOR_DATE_IS_IN_DAYS:
        return new DateIsInDays(OPERATOR_DATE_IS_IN_DAYS);
      case OPERATOR_DATE_IS_IN_GE_DAYS:
        return new DateIsInGEDays(OPERATOR_DATE_IS_IN_GE_DAYS);
      case OPERATOR_DATE_IS_IN_GE_MONTHS:
        return new DateIsInGEMonths(OPERATOR_DATE_IS_IN_GE_MONTHS);
      case OPERATOR_DATE_IS_IN_LE_DAYS:
        return new DateIsInLEDays(OPERATOR_DATE_IS_IN_LE_DAYS);
      case OPERATOR_DATE_IS_IN_LE_MONTHS:
        return new DateIsInLEMonths(OPERATOR_DATE_IS_IN_LE_MONTHS);
      case OPERATOR_DATE_IS_IN_LAST_DAYS:
        return new DateIsInLastDays(OPERATOR_DATE_IS_IN_LAST_DAYS);
      case OPERATOR_DATE_IS_IN_LAST_MONTHS:
        return new DateIsInLastMonths(OPERATOR_DATE_IS_IN_LAST_MONTHS);
      case OPERATOR_DATE_IS_IN_MONTHS:
        return new DateIsInMonths(OPERATOR_DATE_IS_IN_MONTHS);
      case OPERATOR_DATE_IS_IN_NEXT_DAYS:
        return new DateIsInNextDays(OPERATOR_DATE_IS_IN_NEXT_DAYS);
      case OPERATOR_DATE_IS_IN_NEXT_MONTHS:
        return new DateIsInNextMonths(OPERATOR_DATE_IS_IN_NEXT_MONTHS);
      case OPERATOR_DATE_IS_NOT_TODAY:
        return new DateIsNotToday(OPERATOR_DATE_IS_NOT_TODAY);
      case OPERATOR_DATE_IS_TODAY:
        return new DateIsToday(OPERATOR_DATE_IS_TODAY);
      case OPERATOR_DATE_TIME_IS_IN_GE_HOURS:
        return new DateTimeIsInGEHours(OPERATOR_DATE_TIME_IS_IN_GE_HOURS);
      case OPERATOR_DATE_TIME_IS_IN_GE_MINUTES:
        return new DateTimeIsInGEMinutes(OPERATOR_DATE_TIME_IS_IN_GE_MINUTES);
      case OPERATOR_DATE_TIME_IS_IN_LE_HOURS:
        return new DateTimeIsInLEHours(OPERATOR_DATE_TIME_IS_IN_LE_HOURS);
      case OPERATOR_DATE_TIME_IS_IN_LE_MINUTES:
        return new DateTimeIsInLEMinutes(OPERATOR_DATE_TIME_IS_IN_LE_MINUTES);
      case OPERATOR_DATE_TIME_IS_NOT_NOW:
        return new DateTimeIsNotNow(OPERATOR_DATE_TIME_IS_NOT_NOW);
      case OPERATOR_DATE_TIME_IS_NOW:
        return new DateTimeIsNow(OPERATOR_DATE_TIME_IS_NOW);
      case OPERATOR_ENDS_WITH:
        return new EndsWith(OPERATOR_ENDS_WITH);
      case OPERATOR_NOT_ENDS_WITH:
        return new NotEndsWith(OPERATOR_NOT_ENDS_WITH);
      case OPERATOR_IN:
        return new In(OPERATOR_IN);
      case OPERATOR_CONTAINS:
        return new Contains(OPERATOR_CONTAINS);
      case OPERATOR_LIKE:
        return new Like(OPERATOR_LIKE);
      case OPERATOR_NOT_IN:
        return new NotIn(OPERATOR_NOT_IN);
      case OPERATOR_NOT_CONTAINS:
        return new NotContains(OPERATOR_NOT_CONTAINS);
      case OPERATOR_NOT_NULL:
        return new NotNull(OPERATOR_NOT_NULL);
      case OPERATOR_NUMBER_NOT_NULL:
        return new NumberNotNull(OPERATOR_NUMBER_NOT_NULL);
      case OPERATOR_NULL:
        return new Null(OPERATOR_NULL);
      case OPERATOR_NUMBER_NULL:
        return new NumberNull(OPERATOR_NUMBER_NULL);
      case OPERATOR_STARTS_WITH:
        return new StartsWith(OPERATOR_STARTS_WITH);
      case OPERATOR_NOT_STARTS_WITH:
        return new NotStartsWith(OPERATOR_NOT_STARTS_WITH);
      case OPERATOR_TIME_IS_IN_GE_HOURS:
        return new TimeIsInGEHours(OPERATOR_TIME_IS_IN_GE_HOURS);
      case OPERATOR_TIME_IS_IN_GE_MINUTES:
        return new TimeIsInGEMinutes(OPERATOR_TIME_IS_IN_GE_MINUTES);
      case OPERATOR_TIME_IS_IN_HOURS:
        return new TimeIsInHours(OPERATOR_TIME_IS_IN_HOURS);
      case OPERATOR_TIME_IS_IN_LE_HOURS:
        return new TimeIsInLEHours(OPERATOR_TIME_IS_IN_LE_HOURS);
      case OPERATOR_TIME_IS_IN_LE_MINUTES:
        return new TimeIsInLEMinutes(OPERATOR_TIME_IS_IN_LE_MINUTES);
      case OPERATOR_TIME_IS_IN_MINUTES:
        return new TimeIsInMinutes(OPERATOR_TIME_IS_IN_MINUTES);
      case OPERATOR_TIME_IS_NOW:
        return new TimeIsNow(OPERATOR_TIME_IS_NOW);
      case OPERATOR_TIME_IS_NOT_NOW:
        return new TimeIsNotNow(OPERATOR_TIME_IS_NOT_NOW);
    }
    return null;
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

    @Override
    public String getShortText() {
      return getText();
    }

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

    /**
     * @param aggregationType
     */
    NEQ(int operator) {
      super(operator);
    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    @Override
    public String getText() {
      return ScoutTexts.get("LogicNEQ");
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_INHERITED;
    }
  }

  private static class LT extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    /**
     * @param aggregationType
     */
    LT(int operator) {
      super(operator);
    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    @Override
    public String getText() {
      return ScoutTexts.get("LogicLT");
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_INHERITED;
    }
  }

  private static class LE extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    /**
     * @param aggregationType
     */
    LE(int operator) {
      super(operator);
    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    @Override
    public String getText() {
      return ScoutTexts.get("LogicLE");
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_INHERITED;
    }
  }

  private static class EQ extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    /**
     * @param aggregationType
     */
    EQ(int operator) {
      super(operator);
    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    @Override
    public String getText() {
      return ScoutTexts.get("LogicEQ");
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_INHERITED;
    }
  }

  private static class GT extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    /**
     * @param aggregationType
     */
    GT(int operator) {
      super(operator);
    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    @Override
    public String getText() {
      return ScoutTexts.get("LogicGT");
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_INHERITED;
    }
  }

  private static class GE extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    /**
     * @param aggregationType
     */
    GE(int operator) {
      super(operator);
    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    @Override
    public String getText() {
      return ScoutTexts.get("LogicGE");
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_INHERITED;
    }
  }

  private static class DateIsInDays extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    /**
     * @param aggregationType
     */
    DateIsInDays(int operator) {
      super(operator);
    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    @Override
    public String getText() {
      return ScoutTexts.get("LogicDateIsInDays");
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_INTEGER;
    }
  }

  private static class DateIsInGEDays extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    /**
     * @param aggregationType
     */
    DateIsInGEDays(int operator) {
      super(operator);
    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    @Override
    public String getText() {
      return ScoutTexts.get("LogicDateIsInGEDays");
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_INTEGER;
    }
  }

  private static class DateIsInGEMonths extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    /**
     * @param aggregationType
     */
    DateIsInGEMonths(int operator) {
      super(operator);
    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    @Override
    public String getText() {
      return ScoutTexts.get("LogicDateIsInGEMonths");
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_INTEGER;
    }
  }

  private static class DateIsInLEDays extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    /**
     * @param aggregationType
     */
    DateIsInLEDays(int operator) {
      super(operator);
    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    @Override
    public String getText() {
      return ScoutTexts.get("LogicDateIsInLEDays");
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_INTEGER;
    }
  }

  private static class DateIsInLEMonths extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    /**
     * @param aggregationType
     */
    DateIsInLEMonths(int operator) {
      super(operator);
    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    @Override
    public String getText() {
      return ScoutTexts.get("LogicDateIsInLEMonths");
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_INTEGER;
    }
  }

  private static class DateIsInLastDays extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    /**
     * @param aggregationType
     */
    DateIsInLastDays(int operator) {
      super(operator);
    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    @Override
    public String getText() {
      return ScoutTexts.get("LogicDateIsInLastDays");
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_INTEGER;
    }
  }

  private static class DateIsInLastMonths extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    /**
     * @param aggregationType
     */
    DateIsInLastMonths(int operator) {
      super(operator);
    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    @Override
    public String getText() {
      return ScoutTexts.get("LogicDateIsInLastMonths");
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_INTEGER;
    }
  }

  private static class DateIsInMonths extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    /**
     * @param aggregationType
     */
    DateIsInMonths(int operator) {
      super(operator);
    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    @Override
    public String getText() {
      return ScoutTexts.get("LogicDateIsInMonths");
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_INTEGER;
    }
  }

  private static class DateIsInNextDays extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    /**
     * @param aggregationType
     */
    DateIsInNextDays(int operator) {
      super(operator);
    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    @Override
    public String getText() {
      return ScoutTexts.get("LogicDateIsInNextDays");
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_INTEGER;
    }
  }

  private static class DateIsInNextMonths extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    /**
     * @param aggregationType
     */
    DateIsInNextMonths(int operator) {
      super(operator);
    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    @Override
    public String getText() {
      return ScoutTexts.get("LogicDateIsInNextMonths");
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_INTEGER;
    }
  }

  private static class DateIsNotToday extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    /**
     * @param aggregationType
     */
    DateIsNotToday(int operator) {
      super(operator);
    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, ScoutTexts.get("LogicDateIsNotToday"));
    }

    @Override
    public String getText() {
      return ScoutTexts.get("LogicDateIsNotToday");
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_NONE;
    }
  }

  private static class DateIsToday extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    /**
     * @param aggregationType
     */
    DateIsToday(int operator) {
      super(operator);
    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, ScoutTexts.get("LogicDateIsToday"));
    }

    @Override
    public String getText() {
      return ScoutTexts.get("LogicDateIsToday");
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_NONE;
    }
  }

  private static class DateTimeIsInGEHours extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    /**
     * @param aggregationType
     */
    DateTimeIsInGEHours(int operator) {
      super(operator);
    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    @Override
    public String getText() {
      return ScoutTexts.get("LogicDateTimeIsInGEHours");
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_INTEGER;
    }
  }

  private static class DateTimeIsInGEMinutes extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    /**
     * @param aggregationType
     */
    DateTimeIsInGEMinutes(int operator) {
      super(operator);
    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    @Override
    public String getText() {
      return ScoutTexts.get("LogicDateTimeIsInGEMinutes");
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_INTEGER;
    }
  }

  private static class DateTimeIsInLEHours extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    /**
     * @param aggregationType
     */
    DateTimeIsInLEHours(int operator) {
      super(operator);
    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    @Override
    public String getText() {
      return ScoutTexts.get("LogicDateTimeIsInLEHours");
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_INTEGER;
    }
  }

  private static class DateTimeIsInLEMinutes extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    /**
     * @param aggregationType
     */
    DateTimeIsInLEMinutes(int operator) {
      super(operator);
    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    @Override
    public String getText() {
      return ScoutTexts.get("LogicDateTimeIsInLEMinutes");
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_INTEGER;
    }
  }

  private static class DateTimeIsNotNow extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    /**
     * @param aggregationType
     */
    DateTimeIsNotNow(int operator) {
      super(operator);
    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, ScoutTexts.get("LogicDateTimeIsNotNow"));
    }

    @Override
    public String getText() {
      return ScoutTexts.get("LogicDateTimeIsNotNow");
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_NONE;
    }
  }

  private static class DateTimeIsNow extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    /**
     * @param aggregationType
     */
    DateTimeIsNow(int operator) {
      super(operator);
    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, ScoutTexts.get("LogicDateTimeIsNow"));
    }

    @Override
    public String getText() {
      return ScoutTexts.get("LogicDateTimeIsNow");
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_NONE;
    }
  }

  private static class EndsWith extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    /**
     * @param aggregationType
     */
    EndsWith(int operator) {
      super(operator);
    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    @Override
    public String getText() {
      return ScoutTexts.get("LogicEndsWith");
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_INHERITED;
    }
  }

  private static class NotEndsWith extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    /**
     * @param aggregationType
     */
    NotEndsWith(int operator) {
      super(operator);
    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    @Override
    public String getText() {
      return ScoutTexts.get("LogicNotEndsWith");
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_INHERITED;
    }
  }

  private static class In extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    /**
     * @param aggregationType
     */
    In(int operator) {
      super(operator);
    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    @Override
    public String getText() {
      return ScoutTexts.get("LogicIn");
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_INHERITED;
    }
  }

  private static class Contains extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    /**
     * @param aggregationType
     */
    Contains(int operator) {
      super(operator);
    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    @Override
    public String getText() {
      return ScoutTexts.get("LogicLike");
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_INHERITED;
    }
  }

  private static class NotIn extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    /**
     * @param aggregationType
     */
    NotIn(int operator) {
      super(operator);
    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    @Override
    public String getText() {
      return ScoutTexts.get("LogicNotIn");
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_INHERITED;
    }
  }

  private static class NotContains extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    /**
     * @param aggregationType
     */
    NotContains(int operator) {
      super(operator);
    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    @Override
    public String getText() {
      return ScoutTexts.get("LogicNotLike");
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_INHERITED;
    }
  }

  private static class NotNull extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    /**
     * @param aggregationType
     */
    NotNull(int operator) {
      super(operator);
    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, ScoutTexts.get("LogicNotNull"));
    }

    @Override
    public String getText() {
      return ScoutTexts.get("LogicNotNull");
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

    /**
     * @param aggregationType
     */
    NumberNotNull(int operator) {
      super(operator);
    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, ScoutTexts.get("LogicNotNull"));
    }

    @Override
    public String getText() {
      return ScoutTexts.get("LogicNotNull");
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_NONE;
    }
  }

  private static class Null extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    /**
     * @param aggregationType
     */
    Null(int operator) {
      super(operator);
    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, ScoutTexts.get("LogicNull"));
    }

    @Override
    public String getText() {
      return ScoutTexts.get("LogicNull");
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

    /**
     * @param aggregationType
     */
    NumberNull(int operator) {
      super(operator);
    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, ScoutTexts.get("LogicNull"));
    }

    @Override
    public String getText() {
      return ScoutTexts.get("LogicNull");
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_NONE;
    }
  }

  private static class StartsWith extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    /**
     * @param aggregationType
     */
    StartsWith(int operator) {
      super(operator);
    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    @Override
    public String getText() {
      return ScoutTexts.get("LogicStartsWith");
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_INHERITED;
    }
  }

  private static class NotStartsWith extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    /**
     * @param aggregationType
     */
    NotStartsWith(int operator) {
      super(operator);
    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    @Override
    public String getText() {
      return ScoutTexts.get("LogicNotStartsWith");
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_INHERITED;
    }
  }

  private static class TimeIsInGEHours extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    /**
     * @param aggregationType
     */
    TimeIsInGEHours(int operator) {
      super(operator);
    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    @Override
    public String getText() {
      return ScoutTexts.get("LogicTimeIsInGEHours");
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_INTEGER;
    }
  }

  private static class TimeIsInGEMinutes extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    /**
     * @param aggregationType
     */
    TimeIsInGEMinutes(int operator) {
      super(operator);
    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    @Override
    public String getText() {
      return ScoutTexts.get("LogicTimeIsInGEMinutes");
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_INTEGER;
    }
  }

  private static class TimeIsInHours extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    /**
     * @param aggregationType
     */
    TimeIsInHours(int operator) {
      super(operator);
    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    @Override
    public String getText() {
      return ScoutTexts.get("LogicTimeIsInHours");
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_INTEGER;
    }
  }

  private static class TimeIsInLEHours extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    /**
     * @param aggregationType
     */
    TimeIsInLEHours(int operator) {
      super(operator);
    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    @Override
    public String getText() {
      return ScoutTexts.get("LogicTimeIsInLEHours");
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_INTEGER;
    }
  }

  private static class TimeIsInLEMinutes extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    /**
     * @param aggregationType
     */
    TimeIsInLEMinutes(int operator) {
      super(operator);
    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    @Override
    public String getText() {
      return ScoutTexts.get("LogicTimeIsInLEMinutes");
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_INTEGER;
    }
  }

  private static class TimeIsInMinutes extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    /**
     * @param aggregationType
     */
    TimeIsInMinutes(int operator) {
      super(operator);
    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    @Override
    public String getText() {
      return ScoutTexts.get("LogicTimeIsInMinutes");
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_INTEGER;
    }
  }

  private static class TimeIsNow extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    /**
     * @param aggregationType
     */
    TimeIsNow(int operator) {
      super(operator);
    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, ScoutTexts.get("LogicTimeIsNow"));
    }

    @Override
    public String getText() {
      return ScoutTexts.get("LogicTimeIsNow");
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_NONE;
    }
  }

  private static class TimeIsNotNow extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    /**
     * @param aggregationType
     */
    TimeIsNotNow(int operator) {
      super(operator);
    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, ScoutTexts.get("LogicTimeIsNotNow"));
    }

    @Override
    public String getText() {
      return ScoutTexts.get("LogicTimeIsNotNow");
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_NONE;
    }
  }

  private static class Between extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    /**
     * @param aggregationType
     */
    Between(int operator) {
      super(operator);
    }

    @Override
    public String getShortText() {
      return ScoutTexts.get("LogicBetweenShort");
    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      if (StringUtility.isNullOrEmpty(valueTexts.get(0))) {
        return buildText(aggregationType, attributeText, ScoutTexts.get("LogicLE"), valueTexts.get(1));
      }
      else if (StringUtility.isNullOrEmpty(valueTexts.get(1))) {
        return buildText(aggregationType, attributeText, ScoutTexts.get("LogicGE"), valueTexts.get(0));
      }
      else {
        return buildText(aggregationType, attributeText, getText(), valueTexts);
      }
    }

    @Override
    public String getText() {
      return ScoutTexts.get("LogicBetween");
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_INHERITED;
    }
  }

  private static class Like extends AbstractDataModelOp {
    private static final long serialVersionUID = 1L;

    /**
     * @param aggregationType
     */
    Like(int operator) {
      super(operator);
    }

    @Override
    public String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    @Override
    public String getText() {
      return ScoutTexts.get("LogicEQ");
    }

    @Override
    public int getType() {
      return IDataModelAttribute.TYPE_STRING;
    }
  }
}
