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

import org.eclipse.scout.rt.client.ui.form.fields.composer.operator.ComposerOp;
import org.eclipse.scout.rt.client.ui.form.fields.composer.operator.IComposerOp;
import org.eclipse.scout.rt.shared.data.form.fields.composer.ComposerConstants;

/**
 *
 */
public class LegacyComposerAttributeInjector implements ComposerConstants {

  public void injectOperators(IComposerAttribute attribute) {
    ArrayList<IComposerOp> opList = new ArrayList<IComposerOp>();
    switch (attribute.getType()) {
      case IComposerAttribute.TYPE_SMART: {
        opList.add(ComposerOp.create(OPERATOR_EQ));
        if (attribute.isNotOperatorEnabled()) {
          opList.add(ComposerOp.create(OPERATOR_NEQ));
        }
        if (attribute.isNullOperatorEnabled()) {
          opList.add(ComposerOp.create(OPERATOR_NUMBER_NULL));
        }
        if (attribute.isNotOperatorEnabled() && attribute.isNullOperatorEnabled()) {
          opList.add(ComposerOp.create(OPERATOR_NUMBER_NOT_NULL));
        }
        break;
      }
      case IComposerAttribute.TYPE_CODE_LIST:
      case IComposerAttribute.TYPE_CODE_TREE:
      case IComposerAttribute.TYPE_NUMBER_LIST:
      case IComposerAttribute.TYPE_NUMBER_TREE: {
        opList.add(ComposerOp.create(OPERATOR_IN));
        if (attribute.isNotOperatorEnabled()) {
          opList.add(ComposerOp.create(OPERATOR_NOT_IN));
        }
        if (attribute.isNullOperatorEnabled()) {
          opList.add(ComposerOp.create(OPERATOR_NUMBER_NULL));
        }
        if (attribute.isNotOperatorEnabled() && attribute.isNullOperatorEnabled()) {
          opList.add(ComposerOp.create(OPERATOR_NUMBER_NOT_NULL));
        }
        break;
      }
      case IComposerAttribute.TYPE_DATE:
      case IComposerAttribute.TYPE_TIME:
      case IComposerAttribute.TYPE_DATE_TIME: {
        opList.add(ComposerOp.create(OPERATOR_EQ));
        if (attribute.isNotOperatorEnabled()) {
          opList.add(ComposerOp.create(OPERATOR_NEQ));
        }
        opList.add(ComposerOp.create(OPERATOR_LT));
        opList.add(ComposerOp.create(OPERATOR_LE));
        opList.add(ComposerOp.create(OPERATOR_GT));
        opList.add(ComposerOp.create(OPERATOR_GE));
        if (attribute.isNullOperatorEnabled()) {
          opList.add(ComposerOp.create(OPERATOR_NULL));
        }
        if (attribute.isNotOperatorEnabled() && attribute.isNullOperatorEnabled()) {
          opList.add(ComposerOp.create(OPERATOR_NOT_NULL));
        }
        switch (attribute.getType()) {
          case IComposerAttribute.TYPE_DATE: {
            opList.add(ComposerOp.create(OPERATOR_DATE_IS_TODAY));
            opList.add(ComposerOp.create(OPERATOR_DATE_IS_IN_LAST_DAYS));
            opList.add(ComposerOp.create(OPERATOR_DATE_IS_IN_LAST_MONTHS));
            opList.add(ComposerOp.create(OPERATOR_DATE_IS_IN_NEXT_DAYS));
            opList.add(ComposerOp.create(OPERATOR_DATE_IS_IN_NEXT_MONTHS));
            opList.add(ComposerOp.create(OPERATOR_DATE_IS_IN_DAYS));
            opList.add(ComposerOp.create(OPERATOR_DATE_IS_IN_MONTHS));
            opList.add(ComposerOp.create(OPERATOR_DATE_IS_IN_LE_DAYS));
            opList.add(ComposerOp.create(OPERATOR_DATE_IS_IN_LE_MONTHS));
            opList.add(ComposerOp.create(OPERATOR_DATE_IS_IN_GE_DAYS));
            opList.add(ComposerOp.create(OPERATOR_DATE_IS_IN_GE_MONTHS));
            if (attribute.isNotOperatorEnabled()) {
              opList.add(ComposerOp.create(OPERATOR_DATE_IS_NOT_TODAY));
            }
            break;
          }
          case IComposerAttribute.TYPE_TIME: {
            opList.add(ComposerOp.create(OPERATOR_TIME_IS_IN_GE_HOURS));
            opList.add(ComposerOp.create(OPERATOR_TIME_IS_IN_GE_MINUTES));
            opList.add(ComposerOp.create(OPERATOR_TIME_IS_IN_HOURS));
            // opList.add(ComposerOp.create(OPERATOR_Time_Is_In_Minutes,AGGREGATION_NONE));
            opList.add(ComposerOp.create(OPERATOR_TIME_IS_IN_LE_HOURS));
            opList.add(ComposerOp.create(OPERATOR_TIME_IS_IN_LE_MINUTES));
            opList.add(ComposerOp.create(OPERATOR_TIME_IS_NOW));
            break;
          }
          case IComposerAttribute.TYPE_DATE_TIME: {
            opList.add(ComposerOp.create(OPERATOR_DATE_TIME_IS_NOW));
            opList.add(ComposerOp.create(OPERATOR_DATE_IS_TODAY));
            opList.add(ComposerOp.create(OPERATOR_DATE_IS_IN_LAST_DAYS));
            opList.add(ComposerOp.create(OPERATOR_DATE_IS_IN_LAST_MONTHS));
            opList.add(ComposerOp.create(OPERATOR_DATE_IS_IN_NEXT_DAYS));
            opList.add(ComposerOp.create(OPERATOR_DATE_IS_IN_NEXT_MONTHS));
            opList.add(ComposerOp.create(OPERATOR_DATE_IS_IN_DAYS));
            opList.add(ComposerOp.create(OPERATOR_DATE_IS_IN_MONTHS));
            opList.add(ComposerOp.create(OPERATOR_DATE_TIME_IS_IN_LE_MINUTES));
            opList.add(ComposerOp.create(OPERATOR_DATE_TIME_IS_IN_LE_HOURS));
            opList.add(ComposerOp.create(OPERATOR_DATE_IS_IN_LE_DAYS));
            opList.add(ComposerOp.create(OPERATOR_DATE_IS_IN_LE_MONTHS));
            opList.add(ComposerOp.create(OPERATOR_DATE_TIME_IS_IN_GE_MINUTES));
            opList.add(ComposerOp.create(OPERATOR_DATE_TIME_IS_IN_GE_HOURS));
            opList.add(ComposerOp.create(OPERATOR_DATE_IS_IN_GE_DAYS));
            opList.add(ComposerOp.create(OPERATOR_DATE_IS_IN_GE_MONTHS));
            if (attribute.isNotOperatorEnabled()) {
              // opList.add(ComposerOp.create(OPERATOR_Date_TimeIs_NotNow,AGGREGATION_NONE));
              opList.add(ComposerOp.create(OPERATOR_DATE_IS_NOT_TODAY));
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
        opList.add(ComposerOp.create(OPERATOR_EQ));
        if (attribute.isNotOperatorEnabled()) {
          opList.add(ComposerOp.create(OPERATOR_NEQ));
        }
        opList.add(ComposerOp.create(OPERATOR_LT));
        opList.add(ComposerOp.create(OPERATOR_LE));
        opList.add(ComposerOp.create(OPERATOR_GT));
        opList.add(ComposerOp.create(OPERATOR_GE));
        if (attribute.isNullOperatorEnabled()) {
          opList.add(ComposerOp.create(OPERATOR_NULL));
        }
        if (attribute.isNotOperatorEnabled() && attribute.isNullOperatorEnabled()) {
          opList.add(ComposerOp.create(OPERATOR_NOT_NULL));
        }
        break;
      }
      case IComposerAttribute.TYPE_AGGREGATE_COUNT: {
        opList.add(ComposerOp.create(OPERATOR_EQ));
        if (attribute.isNotOperatorEnabled()) {
          opList.add(ComposerOp.create(OPERATOR_NEQ));
        }
        opList.add(ComposerOp.create(OPERATOR_LT));
        opList.add(ComposerOp.create(OPERATOR_LE));
        opList.add(ComposerOp.create(OPERATOR_GT));
        opList.add(ComposerOp.create(OPERATOR_GE));
        break;
      }
      case IComposerAttribute.TYPE_STRING: {
        opList.add(ComposerOp.create(OPERATOR_EQ));
        if (attribute.isNotOperatorEnabled()) {
          opList.add(ComposerOp.create(OPERATOR_NEQ));
        }
        opList.add(ComposerOp.create(OPERATOR_CONTAINS));
        if (attribute.isNotOperatorEnabled()) {
          opList.add(ComposerOp.create(OPERATOR_NOT_CONTAINS));
        }
        opList.add(ComposerOp.create(OPERATOR_STARTS_WITH));
        if (attribute.isNotOperatorEnabled()) {
          opList.add(ComposerOp.create(OPERATOR_NOT_STARTS_WITH));
        }
        opList.add(ComposerOp.create(OPERATOR_ENDS_WITH));
        if (attribute.isNotOperatorEnabled()) {
          opList.add(ComposerOp.create(OPERATOR_NOT_ENDS_WITH));
        }
        if (attribute.isNullOperatorEnabled()) {
          opList.add(ComposerOp.create(OPERATOR_NULL));
        }
        if (attribute.isNotOperatorEnabled() && attribute.isNullOperatorEnabled()) {
          opList.add(ComposerOp.create(OPERATOR_NOT_NULL));
        }
        break;
      }
      case IComposerAttribute.TYPE_FULL_TEXT: {
        opList.add(ComposerOp.create(OPERATOR_CONTAINS));
        if (attribute.isNotOperatorEnabled()) {
          opList.add(ComposerOp.create(OPERATOR_NOT_CONTAINS));
        }
        if (attribute.isNullOperatorEnabled()) {
          opList.add(ComposerOp.create(OPERATOR_NULL));
        }
        if (attribute.isNotOperatorEnabled() && attribute.isNullOperatorEnabled()) {
          opList.add(ComposerOp.create(OPERATOR_NOT_NULL));
        }
        break;
      }
    }
    //
    attribute.setOperators(opList.toArray(new IComposerOp[opList.size()]));
  }

  public void injectAggregationTypes(IComposerAttribute attribute) {
    attribute.setAggregationTypes(new int[0]);
  }
}
