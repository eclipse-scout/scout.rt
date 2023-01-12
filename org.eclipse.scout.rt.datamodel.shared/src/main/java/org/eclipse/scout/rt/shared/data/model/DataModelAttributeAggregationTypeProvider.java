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

public class DataModelAttributeAggregationTypeProvider implements IDataModelAttributeAggregationTypeProvider, DataModelConstants {

  @Override
  public void injectAggregationTypes(IDataModelAttribute attribute, List<Integer> aggregationTypeList) {
    if (!attribute.isAggregationEnabled()) {
      return;
    }

    switch (attribute.getType()) {
      case TYPE_DATE:
      case TYPE_TIME:
      case TYPE_DATE_TIME: {
        aggregationTypeList.add(AGGREGATION_MIN);
        aggregationTypeList.add(AGGREGATION_MAX);
        aggregationTypeList.add(AGGREGATION_MEDIAN);
        break;
      }
      case TYPE_INTEGER:
      case TYPE_LONG:
      case TYPE_BIG_DECIMAL:
      case TYPE_PLAIN_INTEGER:
      case TYPE_PLAIN_LONG:
      case TYPE_PLAIN_BIG_DECIMAL:
      case TYPE_PERCENT: {
        aggregationTypeList.add(AGGREGATION_SUM);
        aggregationTypeList.add(AGGREGATION_MIN);
        aggregationTypeList.add(AGGREGATION_MAX);
        aggregationTypeList.add(AGGREGATION_AVG);
        aggregationTypeList.add(AGGREGATION_MEDIAN);
        break;
      }
      case TYPE_AGGREGATE_COUNT: {
        aggregationTypeList.add(AGGREGATION_COUNT);
        break;
      }
    }
  }
}
