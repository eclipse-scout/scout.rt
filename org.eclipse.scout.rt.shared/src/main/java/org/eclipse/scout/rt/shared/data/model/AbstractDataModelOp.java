/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
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
import org.eclipse.scout.rt.shared.TEXTS;

public abstract class AbstractDataModelOp implements IDataModelAttributeOp, DataModelConstants, Serializable {

  private static final long serialVersionUID = 1L;
  private final int m_operator;

  protected AbstractDataModelOp(int operator) {
    m_operator = operator;
  }

  @Override
  public final int getOperator() {
    return m_operator;
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
    if (valueTexts != null && !valueTexts.isEmpty()) {
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
          b.append(TEXTS.get("ComposerFieldAggregationAvg", attributeText));
          break;
        }
        case AGGREGATION_COUNT: {
          b.append(TEXTS.get("ComposerFieldAggregationCount", attributeText));
          break;
        }
        case AGGREGATION_MAX: {
          b.append(TEXTS.get("ComposerFieldAggregationMax", attributeText));
          break;
        }
        case AGGREGATION_MEDIAN: {
          b.append(TEXTS.get("ComposerFieldAggregationMedian", attributeText));
          break;
        }
        case AGGREGATION_MIN: {
          b.append(TEXTS.get("ComposerFieldAggregationMin", attributeText));
          break;
        }
        case AGGREGATION_SUM: {
          b.append(TEXTS.get("ComposerFieldAggregationSum", attributeText));
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
    if (opText.contains("{0}")) {
      verboseValue = opText;
      if (verboseValue.contains("{0}") && text1 != null) {
        verboseValue = verboseValue.replace("{0}", text1);
      }
      if (verboseValue.contains("{1}") && text2 != null) {
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
