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
package org.eclipse.scout.rt.client.ui.form.fields.composer.operator;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.rt.client.ui.form.fields.composer.attribute.IComposerAttribute;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.data.form.fields.composer.ComposerConstants;

public final class ComposerOp implements ComposerConstants {

  private ComposerOp() {
  }

  /**
   * @return a new {@link IComposerOp} for a {@link ComposerConstants#OPERATOR_*}
   */
  public static IComposerOp create(int operator) {
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

  private abstract static class AbstractComposerOp implements IComposerOp, ComposerConstants {
    private final int m_operator;

    AbstractComposerOp(int operator) {
      m_operator = operator;
    }

    public final int getOperator() {
      return m_operator;
    }

    public String getShortText() {
      return getText();
    }

    @Override
    public boolean equals(Object obj) {
      return (obj == null ? false : this.getClass() == obj.getClass());
    }

    @Override
    public int hashCode() {
      return this.getClass().hashCode();
    }

    public static String buildText(Integer aggregationType, String attributeText, String opText, String... valueTexts) {
      String text1 = null;
      if (valueTexts != null && valueTexts.length > 0) {
        text1 = valueTexts[0];
      }
      String text2 = null;
      if (valueTexts != null && valueTexts.length > 1) {
        text2 = valueTexts[1];
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

  private static class NEQ extends AbstractComposerOp {

    /**
     * @param aggregationType
     */
    NEQ(int operator) {
      super(operator);
    }

    public String createVerboseText(Integer aggregationType, String attributeText, String[] valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    public String getText() {
      return ScoutTexts.get("LogicNEQ");
    }

    public int getType() {
      return IComposerAttribute.TYPE_INHERITED;
    }
  }

  private static class LT extends AbstractComposerOp {

    /**
     * @param aggregationType
     */
    LT(int operator) {
      super(operator);
    }

    public String createVerboseText(Integer aggregationType, String attributeText, String[] valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    public String getText() {
      return ScoutTexts.get("LogicLT");
    }

    public int getType() {
      return IComposerAttribute.TYPE_INHERITED;
    }
  }

  private static class LE extends AbstractComposerOp {

    /**
     * @param aggregationType
     */
    LE(int operator) {
      super(operator);
    }

    public String createVerboseText(Integer aggregationType, String attributeText, String[] valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    public String getText() {
      return ScoutTexts.get("LogicLE");
    }

    public int getType() {
      return IComposerAttribute.TYPE_INHERITED;
    }
  }

  private static class EQ extends AbstractComposerOp {

    /**
     * @param aggregationType
     */
    EQ(int operator) {
      super(operator);
    }

    public String createVerboseText(Integer aggregationType, String attributeText, String[] valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    public String getText() {
      return ScoutTexts.get("LogicEQ");
    }

    public int getType() {
      return IComposerAttribute.TYPE_INHERITED;
    }
  }

  private static class GT extends AbstractComposerOp {

    /**
     * @param aggregationType
     */
    GT(int operator) {
      super(operator);
    }

    public String createVerboseText(Integer aggregationType, String attributeText, String[] valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    public String getText() {
      return ScoutTexts.get("LogicGT");
    }

    public int getType() {
      return IComposerAttribute.TYPE_INHERITED;
    }
  }

  private static class GE extends AbstractComposerOp {

    /**
     * @param aggregationType
     */
    GE(int operator) {
      super(operator);
    }

    public String createVerboseText(Integer aggregationType, String attributeText, String[] valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    public String getText() {
      return ScoutTexts.get("LogicGE");
    }

    public int getType() {
      return IComposerAttribute.TYPE_INHERITED;
    }
  }

  private static class DateIsInDays extends AbstractComposerOp {
    /**
     * @param aggregationType
     */
    DateIsInDays(int operator) {
      super(operator);
    }

    public String createVerboseText(Integer aggregationType, String attributeText, String[] valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    public String getText() {
      return ScoutTexts.get("LogicDateIsInDays");
    }

    public int getType() {
      return IComposerAttribute.TYPE_INTEGER;
    }
  }

  private static class DateIsInGEDays extends AbstractComposerOp {
    /**
     * @param aggregationType
     */
    DateIsInGEDays(int operator) {
      super(operator);
    }

    public String createVerboseText(Integer aggregationType, String attributeText, String[] valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    public String getText() {
      return ScoutTexts.get("LogicDateIsInGEDays");
    }

    public int getType() {
      return IComposerAttribute.TYPE_INTEGER;
    }
  }

  private static class DateIsInGEMonths extends AbstractComposerOp {
    /**
     * @param aggregationType
     */
    DateIsInGEMonths(int operator) {
      super(operator);
    }

    public String createVerboseText(Integer aggregationType, String attributeText, String[] valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    public String getText() {
      return ScoutTexts.get("LogicDateIsInGEMonths");
    }

    public int getType() {
      return IComposerAttribute.TYPE_INTEGER;
    }
  }

  private static class DateIsInLEDays extends AbstractComposerOp {
    /**
     * @param aggregationType
     */
    DateIsInLEDays(int operator) {
      super(operator);
    }

    public String createVerboseText(Integer aggregationType, String attributeText, String[] valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    public String getText() {
      return ScoutTexts.get("LogicDateIsInLEDays");
    }

    public int getType() {
      return IComposerAttribute.TYPE_INTEGER;
    }
  }

  private static class DateIsInLEMonths extends AbstractComposerOp {
    /**
     * @param aggregationType
     */
    DateIsInLEMonths(int operator) {
      super(operator);
    }

    public String createVerboseText(Integer aggregationType, String attributeText, String[] valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    public String getText() {
      return ScoutTexts.get("LogicDateIsInLEMonths");
    }

    public int getType() {
      return IComposerAttribute.TYPE_INTEGER;
    }
  }

  private static class DateIsInLastDays extends AbstractComposerOp {
    /**
     * @param aggregationType
     */
    DateIsInLastDays(int operator) {
      super(operator);
    }

    public String createVerboseText(Integer aggregationType, String attributeText, String[] valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    public String getText() {
      return ScoutTexts.get("LogicDateIsInLastDays");
    }

    public int getType() {
      return IComposerAttribute.TYPE_INTEGER;
    }
  }

  private static class DateIsInLastMonths extends AbstractComposerOp {
    /**
     * @param aggregationType
     */
    DateIsInLastMonths(int operator) {
      super(operator);
    }

    public String createVerboseText(Integer aggregationType, String attributeText, String[] valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    public String getText() {
      return ScoutTexts.get("LogicDateIsInLastMonths");
    }

    public int getType() {
      return IComposerAttribute.TYPE_INTEGER;
    }
  }

  private static class DateIsInMonths extends AbstractComposerOp {
    /**
     * @param aggregationType
     */
    DateIsInMonths(int operator) {
      super(operator);
    }

    public String createVerboseText(Integer aggregationType, String attributeText, String[] valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    public String getText() {
      return ScoutTexts.get("LogicDateIsInMonths");
    }

    public int getType() {
      return IComposerAttribute.TYPE_INTEGER;
    }
  }

  private static class DateIsInNextDays extends AbstractComposerOp {
    /**
     * @param aggregationType
     */
    DateIsInNextDays(int operator) {
      super(operator);
    }

    public String createVerboseText(Integer aggregationType, String attributeText, String[] valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    public String getText() {
      return ScoutTexts.get("LogicDateIsInNextDays");
    }

    public int getType() {
      return IComposerAttribute.TYPE_INTEGER;
    }
  }

  private static class DateIsInNextMonths extends AbstractComposerOp {
    /**
     * @param aggregationType
     */
    DateIsInNextMonths(int operator) {
      super(operator);
    }

    public String createVerboseText(Integer aggregationType, String attributeText, String[] valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    public String getText() {
      return ScoutTexts.get("LogicDateIsInNextMonths");
    }

    public int getType() {
      return IComposerAttribute.TYPE_INTEGER;
    }
  }

  private static class DateIsNotToday extends AbstractComposerOp {
    /**
     * @param aggregationType
     */
    DateIsNotToday(int operator) {
      super(operator);
    }

    public String createVerboseText(Integer aggregationType, String attributeText, String[] valueTexts) {
      return buildText(aggregationType, attributeText, ScoutTexts.get("LogicDateIsNotToday"));
    }

    public String getText() {
      return ScoutTexts.get("LogicDateIsNotToday");
    }

    public int getType() {
      return IComposerAttribute.TYPE_NONE;
    }
  }

  private static class DateIsToday extends AbstractComposerOp {
    /**
     * @param aggregationType
     */
    DateIsToday(int operator) {
      super(operator);
    }

    public String createVerboseText(Integer aggregationType, String attributeText, String[] valueTexts) {
      return buildText(aggregationType, attributeText, ScoutTexts.get("LogicDateIsToday"));
    }

    public String getText() {
      return ScoutTexts.get("LogicDateIsToday");
    }

    public int getType() {
      return IComposerAttribute.TYPE_NONE;
    }
  }

  private static class DateTimeIsInGEHours extends AbstractComposerOp {
    /**
     * @param aggregationType
     */
    DateTimeIsInGEHours(int operator) {
      super(operator);
    }

    public String createVerboseText(Integer aggregationType, String attributeText, String[] valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    public String getText() {
      return ScoutTexts.get("LogicDateTimeIsInGEHours");
    }

    public int getType() {
      return IComposerAttribute.TYPE_INTEGER;
    }
  }

  private static class DateTimeIsInGEMinutes extends AbstractComposerOp {
    /**
     * @param aggregationType
     */
    DateTimeIsInGEMinutes(int operator) {
      super(operator);
    }

    public String createVerboseText(Integer aggregationType, String attributeText, String[] valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    public String getText() {
      return ScoutTexts.get("LogicDateTimeIsInGEMinutes");
    }

    public int getType() {
      return IComposerAttribute.TYPE_INTEGER;
    }
  }

  private static class DateTimeIsInLEHours extends AbstractComposerOp {
    /**
     * @param aggregationType
     */
    DateTimeIsInLEHours(int operator) {
      super(operator);
    }

    public String createVerboseText(Integer aggregationType, String attributeText, String[] valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    public String getText() {
      return ScoutTexts.get("LogicDateTimeIsInLEHours");
    }

    public int getType() {
      return IComposerAttribute.TYPE_INTEGER;
    }
  }

  private static class DateTimeIsInLEMinutes extends AbstractComposerOp {
    /**
     * @param aggregationType
     */
    DateTimeIsInLEMinutes(int operator) {
      super(operator);
    }

    public String createVerboseText(Integer aggregationType, String attributeText, String[] valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    public String getText() {
      return ScoutTexts.get("LogicDateTimeIsInLEMinutes");
    }

    public int getType() {
      return IComposerAttribute.TYPE_INTEGER;
    }
  }

  private static class DateTimeIsNotNow extends AbstractComposerOp {
    /**
     * @param aggregationType
     */
    DateTimeIsNotNow(int operator) {
      super(operator);
    }

    public String createVerboseText(Integer aggregationType, String attributeText, String[] valueTexts) {
      return buildText(aggregationType, attributeText, ScoutTexts.get("LogicDateTimeIsNotNow"));
    }

    public String getText() {
      return ScoutTexts.get("LogicDateTimeIsNotNow");
    }

    public int getType() {
      return IComposerAttribute.TYPE_NONE;
    }
  }

  private static class DateTimeIsNow extends AbstractComposerOp {
    /**
     * @param aggregationType
     */
    DateTimeIsNow(int operator) {
      super(operator);
    }

    public String createVerboseText(Integer aggregationType, String attributeText, String[] valueTexts) {
      return buildText(aggregationType, attributeText, ScoutTexts.get("LogicDateTimeIsNow"));
    }

    public String getText() {
      return ScoutTexts.get("LogicDateTimeIsNow");
    }

    public int getType() {
      return IComposerAttribute.TYPE_NONE;
    }
  }

  private static class EndsWith extends AbstractComposerOp {
    /**
     * @param aggregationType
     */
    EndsWith(int operator) {
      super(operator);
    }

    public String createVerboseText(Integer aggregationType, String attributeText, String[] valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    public String getText() {
      return ScoutTexts.get("LogicEndsWith");
    }

    public int getType() {
      return IComposerAttribute.TYPE_INHERITED;
    }
  }

  private static class NotEndsWith extends AbstractComposerOp {
    /**
     * @param aggregationType
     */
    NotEndsWith(int operator) {
      super(operator);
    }

    public String createVerboseText(Integer aggregationType, String attributeText, String[] valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    public String getText() {
      return ScoutTexts.get("LogicNotEndsWith");
    }

    public int getType() {
      return IComposerAttribute.TYPE_INHERITED;
    }
  }

  private static class In extends AbstractComposerOp {
    /**
     * @param aggregationType
     */
    In(int operator) {
      super(operator);
    }

    public String createVerboseText(Integer aggregationType, String attributeText, String[] valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    public String getText() {
      return ScoutTexts.get("LogicIn");
    }

    public int getType() {
      return IComposerAttribute.TYPE_INHERITED;
    }
  }

  private static class Contains extends AbstractComposerOp {
    /**
     * @param aggregationType
     */
    Contains(int operator) {
      super(operator);
    }

    public String createVerboseText(Integer aggregationType, String attributeText, String[] valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    public String getText() {
      return ScoutTexts.get("LogicLike");
    }

    public int getType() {
      return IComposerAttribute.TYPE_INHERITED;
    }
  }

  private static class NotIn extends AbstractComposerOp {
    /**
     * @param aggregationType
     */
    NotIn(int operator) {
      super(operator);
    }

    public String createVerboseText(Integer aggregationType, String attributeText, String[] valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    public String getText() {
      return ScoutTexts.get("LogicNotIn");
    }

    public int getType() {
      return IComposerAttribute.TYPE_INHERITED;
    }
  }

  private static class NotContains extends AbstractComposerOp {
    /**
     * @param aggregationType
     */
    NotContains(int operator) {
      super(operator);
    }

    public String createVerboseText(Integer aggregationType, String attributeText, String[] valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    public String getText() {
      return ScoutTexts.get("LogicNotLike");
    }

    public int getType() {
      return IComposerAttribute.TYPE_INHERITED;
    }
  }

  private static class NotNull extends AbstractComposerOp {
    /**
     * @param aggregationType
     */
    NotNull(int operator) {
      super(operator);
    }

    public String createVerboseText(Integer aggregationType, String attributeText, String[] valueTexts) {
      return buildText(aggregationType, attributeText, ScoutTexts.get("LogicNotNull"));
    }

    public String getText() {
      return ScoutTexts.get("LogicNotNull");
    }

    public int getType() {
      return IComposerAttribute.TYPE_NONE;
    }
  }

  /**
   * nvl(x,0)<>0
   */
  private static class NumberNotNull extends AbstractComposerOp {
    /**
     * @param aggregationType
     */
    NumberNotNull(int operator) {
      super(operator);
    }

    public String createVerboseText(Integer aggregationType, String attributeText, String[] valueTexts) {
      return buildText(aggregationType, attributeText, ScoutTexts.get("LogicNotNull"));
    }

    public String getText() {
      return ScoutTexts.get("LogicNotNull");
    }

    public int getType() {
      return IComposerAttribute.TYPE_NONE;
    }
  }

  private static class Null extends AbstractComposerOp {
    /**
     * @param aggregationType
     */
    Null(int operator) {
      super(operator);
    }

    public String createVerboseText(Integer aggregationType, String attributeText, String[] valueTexts) {
      return buildText(aggregationType, attributeText, ScoutTexts.get("LogicNull"));
    }

    public String getText() {
      return ScoutTexts.get("LogicNull");
    }

    public int getType() {
      return IComposerAttribute.TYPE_NONE;
    }
  }

  /**
   * nvl(x,0)==0
   */
  private static class NumberNull extends AbstractComposerOp {
    /**
     * @param aggregationType
     */
    NumberNull(int operator) {
      super(operator);
    }

    public String createVerboseText(Integer aggregationType, String attributeText, String[] valueTexts) {
      return buildText(aggregationType, attributeText, ScoutTexts.get("LogicNull"));
    }

    public String getText() {
      return ScoutTexts.get("LogicNull");
    }

    public int getType() {
      return IComposerAttribute.TYPE_NONE;
    }
  }

  private static class StartsWith extends AbstractComposerOp {
    /**
     * @param aggregationType
     */
    StartsWith(int operator) {
      super(operator);
    }

    public String createVerboseText(Integer aggregationType, String attributeText, String[] valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    public String getText() {
      return ScoutTexts.get("LogicStartsWith");
    }

    public int getType() {
      return IComposerAttribute.TYPE_INHERITED;
    }
  }

  private static class NotStartsWith extends AbstractComposerOp {
    /**
     * @param aggregationType
     */
    NotStartsWith(int operator) {
      super(operator);
    }

    public String createVerboseText(Integer aggregationType, String attributeText, String[] valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    public String getText() {
      return ScoutTexts.get("LogicNotStartsWith");
    }

    public int getType() {
      return IComposerAttribute.TYPE_INHERITED;
    }
  }

  private static class TimeIsInGEHours extends AbstractComposerOp {
    /**
     * @param aggregationType
     */
    TimeIsInGEHours(int operator) {
      super(operator);
    }

    public String createVerboseText(Integer aggregationType, String attributeText, String[] valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    public String getText() {
      return ScoutTexts.get("LogicTimeIsInGEHours");
    }

    public int getType() {
      return IComposerAttribute.TYPE_INTEGER;
    }
  }

  private static class TimeIsInGEMinutes extends AbstractComposerOp {
    /**
     * @param aggregationType
     */
    TimeIsInGEMinutes(int operator) {
      super(operator);
    }

    public String createVerboseText(Integer aggregationType, String attributeText, String[] valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    public String getText() {
      return ScoutTexts.get("LogicTimeIsInGEMinutes");
    }

    public int getType() {
      return IComposerAttribute.TYPE_INTEGER;
    }
  }

  private static class TimeIsInHours extends AbstractComposerOp {
    /**
     * @param aggregationType
     */
    TimeIsInHours(int operator) {
      super(operator);
    }

    public String createVerboseText(Integer aggregationType, String attributeText, String[] valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    public String getText() {
      return ScoutTexts.get("LogicTimeIsInHours");
    }

    public int getType() {
      return IComposerAttribute.TYPE_INTEGER;
    }
  }

  private static class TimeIsInLEHours extends AbstractComposerOp {
    /**
     * @param aggregationType
     */
    TimeIsInLEHours(int operator) {
      super(operator);
    }

    public String createVerboseText(Integer aggregationType, String attributeText, String[] valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    public String getText() {
      return ScoutTexts.get("LogicTimeIsInLEHours");
    }

    public int getType() {
      return IComposerAttribute.TYPE_INTEGER;
    }
  }

  private static class TimeIsInLEMinutes extends AbstractComposerOp {
    /**
     * @param aggregationType
     */
    TimeIsInLEMinutes(int operator) {
      super(operator);
    }

    public String createVerboseText(Integer aggregationType, String attributeText, String[] valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    public String getText() {
      return ScoutTexts.get("LogicTimeIsInLEMinutes");
    }

    public int getType() {
      return IComposerAttribute.TYPE_INTEGER;
    }
  }

  private static class TimeIsInMinutes extends AbstractComposerOp {
    /**
     * @param aggregationType
     */
    TimeIsInMinutes(int operator) {
      super(operator);
    }

    public String createVerboseText(Integer aggregationType, String attributeText, String[] valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    public String getText() {
      return ScoutTexts.get("LogicTimeIsInMinutes");
    }

    public int getType() {
      return IComposerAttribute.TYPE_INTEGER;
    }
  }

  private static class TimeIsNow extends AbstractComposerOp {
    /**
     * @param aggregationType
     */
    TimeIsNow(int operator) {
      super(operator);
    }

    public String createVerboseText(Integer aggregationType, String attributeText, String[] valueTexts) {
      return buildText(aggregationType, attributeText, ScoutTexts.get("LogicTimeIsNow"));
    }

    public String getText() {
      return ScoutTexts.get("LogicTimeIsNow");
    }

    public int getType() {
      return IComposerAttribute.TYPE_NONE;
    }
  }

  private static class TimeIsNotNow extends AbstractComposerOp {
    /**
     * @param aggregationType
     */
    TimeIsNotNow(int operator) {
      super(operator);
    }

    public String createVerboseText(Integer aggregationType, String attributeText, String[] valueTexts) {
      return buildText(aggregationType, attributeText, ScoutTexts.get("LogicTimeIsNotNow"));
    }

    public String getText() {
      return ScoutTexts.get("LogicTimeIsNotNow");
    }

    public int getType() {
      return IComposerAttribute.TYPE_NONE;
    }
  }

  private static class Between extends AbstractComposerOp {

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

    public String createVerboseText(Integer aggregationType, String attributeText, String[] valueTexts) {
      if (StringUtility.isNullOrEmpty(valueTexts[0])) {
        return buildText(aggregationType, attributeText, ScoutTexts.get("LogicLE"), valueTexts[1]);
      }
      else if (StringUtility.isNullOrEmpty(valueTexts[1])) {
        return buildText(aggregationType, attributeText, ScoutTexts.get("LogicGE"), valueTexts[0]);
      }
      else {
        return buildText(aggregationType, attributeText, getText(), valueTexts);
      }
    }

    public String getText() {
      return ScoutTexts.get("LogicBetween");
    }

    public int getType() {
      return IComposerAttribute.TYPE_INHERITED;
    }
  }

  private static class Like extends AbstractComposerOp {
    /**
     * @param aggregationType
     */
    Like(int operator) {
      super(operator);
    }

    public String createVerboseText(Integer aggregationType, String attributeText, String[] valueTexts) {
      return buildText(aggregationType, attributeText, getText(), valueTexts);
    }

    public String getText() {
      return ScoutTexts.get("LogicEQ");
    }

    public int getType() {
      return IComposerAttribute.TYPE_STRING;
    }
  }
}
