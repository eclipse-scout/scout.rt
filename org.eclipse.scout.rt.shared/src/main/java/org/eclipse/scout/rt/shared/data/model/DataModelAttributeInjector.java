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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.shared.ScoutTexts;

public class DataModelAttributeInjector implements DataModelConstants {

  public void injectOperators(IDataModelAttribute attribute) {
    List<IDataModelAttributeOp> opList = new ArrayList<IDataModelAttributeOp>();
    switch (attribute.getType()) {
      case IDataModelAttribute.TYPE_SMART: {
        opList.add(DataModelAttributeOp.create(OPERATOR_EQ, ScoutTexts.get("LogicIn"), ScoutTexts.get("LogicIn")));
        if (attribute.isNotOperatorEnabled()) {
          opList.add(DataModelAttributeOp.create(OPERATOR_NEQ, ScoutTexts.get("LogicNotIn"), ScoutTexts.get("LogicNotIn")));
        }
        if (attribute.isNullOperatorEnabled()) {
          opList.add(DataModelAttributeOp.create(OPERATOR_NUMBER_NULL));
        }
        if (attribute.isNotOperatorEnabled() && attribute.isNullOperatorEnabled()) {
          opList.add(DataModelAttributeOp.create(OPERATOR_NUMBER_NOT_NULL));
        }
        break;
      }
      case IDataModelAttribute.TYPE_CODE_LIST:
      case IDataModelAttribute.TYPE_CODE_TREE:
      case IDataModelAttribute.TYPE_NUMBER_LIST:
      case IDataModelAttribute.TYPE_NUMBER_TREE: {
        opList.add(DataModelAttributeOp.create(OPERATOR_IN));
        if (attribute.isNotOperatorEnabled()) {
          opList.add(DataModelAttributeOp.create(OPERATOR_NOT_IN));
        }
        if (attribute.isNullOperatorEnabled()) {
          opList.add(DataModelAttributeOp.create(OPERATOR_NUMBER_NULL));
        }
        if (attribute.isNotOperatorEnabled() && attribute.isNullOperatorEnabled()) {
          opList.add(DataModelAttributeOp.create(OPERATOR_NUMBER_NOT_NULL));
        }
        break;
      }
      case IDataModelAttribute.TYPE_TIME: {
        opList.add(DataModelAttributeOp.create(OPERATOR_EQ));
        if (attribute.isNotOperatorEnabled()) {
          opList.add(DataModelAttributeOp.create(OPERATOR_NEQ));
        }
        opList.add(DataModelAttributeOp.create(OPERATOR_BETWEEN));
        opList.add(DataModelAttributeOp.create(OPERATOR_LT));
        opList.add(DataModelAttributeOp.create(OPERATOR_LE));
        opList.add(DataModelAttributeOp.create(OPERATOR_GT));
        opList.add(DataModelAttributeOp.create(OPERATOR_GE));
        if (attribute.isNullOperatorEnabled()) {
          opList.add(DataModelAttributeOp.create(OPERATOR_NULL));
        }
        if (attribute.isNotOperatorEnabled() && attribute.isNullOperatorEnabled()) {
          opList.add(DataModelAttributeOp.create(OPERATOR_NOT_NULL));
        }
        opList.add(DataModelAttributeOp.create(OPERATOR_TIME_IS_IN_GE_HOURS));
        opList.add(DataModelAttributeOp.create(OPERATOR_TIME_IS_IN_GE_MINUTES));
        opList.add(DataModelAttributeOp.create(OPERATOR_TIME_IS_IN_HOURS));
        opList.add(DataModelAttributeOp.create(OPERATOR_TIME_IS_IN_LE_HOURS));
        opList.add(DataModelAttributeOp.create(OPERATOR_TIME_IS_IN_LE_MINUTES));
        opList.add(DataModelAttributeOp.create(OPERATOR_TIME_IS_NOW));
        break;
      }
      case IDataModelAttribute.TYPE_DATE: {
        opList.add(DataModelAttributeOp.create(OPERATOR_DATE_EQ));
        if (attribute.isNotOperatorEnabled()) {
          opList.add(DataModelAttributeOp.create(OPERATOR_DATE_NEQ));
        }
        opList.add(DataModelAttributeOp.create(OPERATOR_DATE_BETWEEN));
        opList.add(DataModelAttributeOp.create(OPERATOR_DATE_LT));
        opList.add(DataModelAttributeOp.create(OPERATOR_DATE_LE));
        opList.add(DataModelAttributeOp.create(OPERATOR_DATE_GT));
        opList.add(DataModelAttributeOp.create(OPERATOR_DATE_GE));
        if (attribute.isNullOperatorEnabled()) {
          opList.add(DataModelAttributeOp.create(OPERATOR_NULL));
        }
        if (attribute.isNotOperatorEnabled() && attribute.isNullOperatorEnabled()) {
          opList.add(DataModelAttributeOp.create(OPERATOR_NOT_NULL));
        }
        opList.add(DataModelAttributeOp.create(OPERATOR_DATE_IS_TODAY));
        opList.add(DataModelAttributeOp.create(OPERATOR_DATE_IS_IN_LAST_DAYS));
        opList.add(DataModelAttributeOp.create(OPERATOR_DATE_IS_IN_LAST_MONTHS));
        opList.add(DataModelAttributeOp.create(OPERATOR_DATE_IS_IN_NEXT_DAYS));
        opList.add(DataModelAttributeOp.create(OPERATOR_DATE_IS_IN_NEXT_MONTHS));
        opList.add(DataModelAttributeOp.create(OPERATOR_DATE_IS_IN_DAYS));
        opList.add(DataModelAttributeOp.create(OPERATOR_DATE_IS_IN_MONTHS));
        opList.add(DataModelAttributeOp.create(OPERATOR_DATE_IS_IN_LE_DAYS));
        opList.add(DataModelAttributeOp.create(OPERATOR_DATE_IS_IN_LE_MONTHS));
        opList.add(DataModelAttributeOp.create(OPERATOR_DATE_IS_IN_GE_DAYS));
        opList.add(DataModelAttributeOp.create(OPERATOR_DATE_IS_IN_GE_MONTHS));
        if (attribute.isNotOperatorEnabled()) {
          opList.add(DataModelAttributeOp.create(OPERATOR_DATE_IS_NOT_TODAY));
        }
        break;
      }
      case IDataModelAttribute.TYPE_DATE_TIME: {
        opList.add(DataModelAttributeOp.create(OPERATOR_DATE_TIME_EQ));
        if (attribute.isNotOperatorEnabled()) {
          opList.add(DataModelAttributeOp.create(OPERATOR_DATE_TIME_NEQ));
        }
        opList.add(DataModelAttributeOp.create(OPERATOR_DATE_TIME_BETWEEN));
        opList.add(DataModelAttributeOp.create(OPERATOR_DATE_TIME_LT));
        opList.add(DataModelAttributeOp.create(OPERATOR_DATE_TIME_LE));
        opList.add(DataModelAttributeOp.create(OPERATOR_DATE_TIME_GT));
        opList.add(DataModelAttributeOp.create(OPERATOR_DATE_TIME_GE));
        if (attribute.isNullOperatorEnabled()) {
          opList.add(DataModelAttributeOp.create(OPERATOR_NULL));
        }
        if (attribute.isNotOperatorEnabled() && attribute.isNullOperatorEnabled()) {
          opList.add(DataModelAttributeOp.create(OPERATOR_NOT_NULL));
        }
        opList.add(DataModelAttributeOp.create(OPERATOR_DATE_TIME_IS_NOW));
        opList.add(DataModelAttributeOp.create(OPERATOR_DATE_IS_TODAY));
        opList.add(DataModelAttributeOp.create(OPERATOR_DATE_IS_IN_LAST_DAYS));
        opList.add(DataModelAttributeOp.create(OPERATOR_DATE_IS_IN_LAST_MONTHS));
        opList.add(DataModelAttributeOp.create(OPERATOR_DATE_IS_IN_NEXT_DAYS));
        opList.add(DataModelAttributeOp.create(OPERATOR_DATE_IS_IN_NEXT_MONTHS));
        opList.add(DataModelAttributeOp.create(OPERATOR_DATE_IS_IN_DAYS));
        opList.add(DataModelAttributeOp.create(OPERATOR_DATE_IS_IN_MONTHS));
        opList.add(DataModelAttributeOp.create(OPERATOR_DATE_TIME_IS_IN_LE_MINUTES));
        opList.add(DataModelAttributeOp.create(OPERATOR_DATE_TIME_IS_IN_LE_HOURS));
        opList.add(DataModelAttributeOp.create(OPERATOR_DATE_IS_IN_LE_DAYS));
        opList.add(DataModelAttributeOp.create(OPERATOR_DATE_IS_IN_LE_MONTHS));
        opList.add(DataModelAttributeOp.create(OPERATOR_DATE_TIME_IS_IN_GE_MINUTES));
        opList.add(DataModelAttributeOp.create(OPERATOR_DATE_TIME_IS_IN_GE_HOURS));
        opList.add(DataModelAttributeOp.create(OPERATOR_DATE_IS_IN_GE_DAYS));
        opList.add(DataModelAttributeOp.create(OPERATOR_DATE_IS_IN_GE_MONTHS));
        if (attribute.isNotOperatorEnabled()) {
          opList.add(DataModelAttributeOp.create(OPERATOR_DATE_IS_NOT_TODAY));
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
        opList.add(DataModelAttributeOp.create(OPERATOR_EQ));
        if (attribute.isNotOperatorEnabled()) {
          opList.add(DataModelAttributeOp.create(OPERATOR_NEQ));
        }
        opList.add(DataModelAttributeOp.create(OPERATOR_BETWEEN));
        opList.add(DataModelAttributeOp.create(OPERATOR_LT));
        opList.add(DataModelAttributeOp.create(OPERATOR_LE));
        opList.add(DataModelAttributeOp.create(OPERATOR_GT));
        opList.add(DataModelAttributeOp.create(OPERATOR_GE));
        if (attribute.isNullOperatorEnabled()) {
          opList.add(DataModelAttributeOp.create(OPERATOR_NULL));
        }
        if (attribute.isNotOperatorEnabled() && attribute.isNullOperatorEnabled()) {
          opList.add(DataModelAttributeOp.create(OPERATOR_NOT_NULL));
        }
        break;
      }
      case IDataModelAttribute.TYPE_AGGREGATE_COUNT: {
        opList.add(DataModelAttributeOp.create(OPERATOR_EQ));
        if (attribute.isNotOperatorEnabled()) {
          opList.add(DataModelAttributeOp.create(OPERATOR_NEQ));
        }
        opList.add(DataModelAttributeOp.create(OPERATOR_BETWEEN));
        opList.add(DataModelAttributeOp.create(OPERATOR_LT));
        opList.add(DataModelAttributeOp.create(OPERATOR_LE));
        opList.add(DataModelAttributeOp.create(OPERATOR_GT));
        opList.add(DataModelAttributeOp.create(OPERATOR_GE));
        break;
      }
      case IDataModelAttribute.TYPE_STRING: {
        opList.add(DataModelAttributeOp.create(OPERATOR_LIKE));
        if (attribute.isNotOperatorEnabled()) {
          opList.add(DataModelAttributeOp.create(OPERATOR_NOT_LIKE));
        }
        opList.add(DataModelAttributeOp.create(OPERATOR_CONTAINS));
        if (attribute.isNotOperatorEnabled()) {
          opList.add(DataModelAttributeOp.create(OPERATOR_NOT_CONTAINS));
        }
        opList.add(DataModelAttributeOp.create(OPERATOR_STARTS_WITH));
        if (attribute.isNotOperatorEnabled()) {
          opList.add(DataModelAttributeOp.create(OPERATOR_NOT_STARTS_WITH));
        }
        opList.add(DataModelAttributeOp.create(OPERATOR_ENDS_WITH));
        if (attribute.isNotOperatorEnabled()) {
          opList.add(DataModelAttributeOp.create(OPERATOR_NOT_ENDS_WITH));
        }
        if (attribute.isNullOperatorEnabled()) {
          opList.add(DataModelAttributeOp.create(OPERATOR_NULL));
        }
        if (attribute.isNotOperatorEnabled() && attribute.isNullOperatorEnabled()) {
          opList.add(DataModelAttributeOp.create(OPERATOR_NOT_NULL));
        }
        break;
      }
      case IDataModelAttribute.TYPE_RICH_TEXT:
      case IDataModelAttribute.TYPE_FULL_TEXT: {
        opList.add(DataModelAttributeOp.create(OPERATOR_CONTAINS));
        if (attribute.isNotOperatorEnabled()) {
          opList.add(DataModelAttributeOp.create(OPERATOR_NOT_CONTAINS));
        }
        if (attribute.isNullOperatorEnabled()) {
          opList.add(DataModelAttributeOp.create(OPERATOR_NULL));
        }
        if (attribute.isNotOperatorEnabled() && attribute.isNullOperatorEnabled()) {
          opList.add(DataModelAttributeOp.create(OPERATOR_NOT_NULL));
        }
        break;
      }
    }
    //
    attribute.setOperators(opList);
  }

  public void injectAggregationTypes(IDataModelAttribute attribute) {
    ArrayList<Integer> agList = new ArrayList<Integer>();
    if (attribute.isAggregationEnabled()) {
      switch (attribute.getType()) {
        case IDataModelAttribute.TYPE_DATE:
        case IDataModelAttribute.TYPE_TIME:
        case IDataModelAttribute.TYPE_DATE_TIME: {
          agList.add(AGGREGATION_MIN);
          agList.add(AGGREGATION_MAX);
          agList.add(AGGREGATION_MEDIAN);
          break;
        }
        case IDataModelAttribute.TYPE_INTEGER:
        case IDataModelAttribute.TYPE_LONG:
        case IDataModelAttribute.TYPE_BIG_DECIMAL:
        case IDataModelAttribute.TYPE_PLAIN_INTEGER:
        case IDataModelAttribute.TYPE_PLAIN_LONG:
        case IDataModelAttribute.TYPE_PLAIN_BIG_DECIMAL:
        case IDataModelAttribute.TYPE_PERCENT: {
          agList.add(AGGREGATION_SUM);
          agList.add(AGGREGATION_MIN);
          agList.add(AGGREGATION_MAX);
          agList.add(AGGREGATION_AVG);
          agList.add(AGGREGATION_MEDIAN);
          break;
        }
        case IDataModelAttribute.TYPE_AGGREGATE_COUNT: {
          agList.add(AGGREGATION_COUNT);
          break;
        }
      }
    }
    //
    int[] a = new int[agList.size()];
    for (int i = 0; i < a.length; i++) {
      a[i] = agList.get(i);
    }
    attribute.setAggregationTypes(a);
  }
}
