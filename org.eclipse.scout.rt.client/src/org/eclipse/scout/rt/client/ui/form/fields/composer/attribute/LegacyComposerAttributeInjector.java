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
package org.eclipse.scout.rt.client.ui.form.fields.composer.attribute;

import java.util.ArrayList;

import org.eclipse.scout.rt.shared.data.model.DataModelAttributeOp;
import org.eclipse.scout.rt.shared.data.model.DataModelConstants;
import org.eclipse.scout.rt.shared.data.model.IDataModelAttributeOp;

/**
 *
 */
@SuppressWarnings("deprecation")
public class LegacyComposerAttributeInjector implements DataModelConstants {

  public void injectOperators(IComposerAttribute attribute) {
    ArrayList<IDataModelAttributeOp> opList = new ArrayList<IDataModelAttributeOp>();
    switch (attribute.getType()) {
      case IComposerAttribute.TYPE_SMART: {
        opList.add(DataModelAttributeOp.create(OPERATOR_EQ));
        if (attribute.isNotOperatorEnabled()) {
          opList.add(DataModelAttributeOp.create(OPERATOR_NEQ));
        }
        if (attribute.isNullOperatorEnabled()) {
          opList.add(DataModelAttributeOp.create(OPERATOR_NUMBER_NULL));
        }
        if (attribute.isNotOperatorEnabled() && attribute.isNullOperatorEnabled()) {
          opList.add(DataModelAttributeOp.create(OPERATOR_NUMBER_NOT_NULL));
        }
        break;
      }
      case IComposerAttribute.TYPE_CODE_LIST:
      case IComposerAttribute.TYPE_CODE_TREE:
      case IComposerAttribute.TYPE_NUMBER_LIST:
      case IComposerAttribute.TYPE_NUMBER_TREE: {
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
      case IComposerAttribute.TYPE_DATE:
      case IComposerAttribute.TYPE_TIME:
      case IComposerAttribute.TYPE_DATE_TIME: {
        opList.add(DataModelAttributeOp.create(OPERATOR_EQ));
        if (attribute.isNotOperatorEnabled()) {
          opList.add(DataModelAttributeOp.create(OPERATOR_NEQ));
        }
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
        switch (attribute.getType()) {
          case IComposerAttribute.TYPE_DATE: {
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
          case IComposerAttribute.TYPE_TIME: {
            opList.add(DataModelAttributeOp.create(OPERATOR_TIME_IS_IN_GE_HOURS));
            opList.add(DataModelAttributeOp.create(OPERATOR_TIME_IS_IN_GE_MINUTES));
            opList.add(DataModelAttributeOp.create(OPERATOR_TIME_IS_IN_HOURS));
            // opList.add(DataModelAttributeOp.create(OPERATOR_Time_Is_In_Minutes,AGGREGATION_NONE));
            opList.add(DataModelAttributeOp.create(OPERATOR_TIME_IS_IN_LE_HOURS));
            opList.add(DataModelAttributeOp.create(OPERATOR_TIME_IS_IN_LE_MINUTES));
            opList.add(DataModelAttributeOp.create(OPERATOR_TIME_IS_NOW));
            break;
          }
          case IComposerAttribute.TYPE_DATE_TIME: {
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
              // opList.add(DataModelAttributeOp.create(OPERATOR_Date_TimeIs_NotNow,AGGREGATION_NONE));
              opList.add(DataModelAttributeOp.create(OPERATOR_DATE_IS_NOT_TODAY));
            }
            break;
          }
        }
        break;
      }
      case IComposerAttribute.TYPE_INTEGER:
      case IComposerAttribute.TYPE_LONG:
      case IComposerAttribute.TYPE_DOUBLE:
      case IComposerAttribute.TYPE_PLAIN_INTEGER:
      case IComposerAttribute.TYPE_PLAIN_LONG:
      case IComposerAttribute.TYPE_PLAIN_DOUBLE:
      case IComposerAttribute.TYPE_PERCENT: {
        opList.add(DataModelAttributeOp.create(OPERATOR_EQ));
        if (attribute.isNotOperatorEnabled()) {
          opList.add(DataModelAttributeOp.create(OPERATOR_NEQ));
        }
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
      case IComposerAttribute.TYPE_AGGREGATE_COUNT: {
        opList.add(DataModelAttributeOp.create(OPERATOR_EQ));
        if (attribute.isNotOperatorEnabled()) {
          opList.add(DataModelAttributeOp.create(OPERATOR_NEQ));
        }
        opList.add(DataModelAttributeOp.create(OPERATOR_LT));
        opList.add(DataModelAttributeOp.create(OPERATOR_LE));
        opList.add(DataModelAttributeOp.create(OPERATOR_GT));
        opList.add(DataModelAttributeOp.create(OPERATOR_GE));
        break;
      }
      case IComposerAttribute.TYPE_STRING: {
        opList.add(DataModelAttributeOp.create(OPERATOR_EQ));
        if (attribute.isNotOperatorEnabled()) {
          opList.add(DataModelAttributeOp.create(OPERATOR_NEQ));
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
      case IComposerAttribute.TYPE_FULL_TEXT: {
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
    attribute.setOperators(opList.toArray(new IDataModelAttributeOp[opList.size()]));
  }

  public void injectAggregationTypes(IComposerAttribute attribute) {
    attribute.setAggregationTypes(new int[0]);
  }
}
