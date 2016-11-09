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

public class DataModelAttributeAggregationTypeProvider implements IDataModelAttributeAggregationTypeProvider, DataModelConstants {

  @Override
  public void injectAggregationTypes(IDataModelAttribute attribute, List<Integer> aggregationTypeList) {
    if (!attribute.isAggregationEnabled()) {
      return;
    }

    switch (attribute.getType()) {
      case IDataModelAttribute.TYPE_DATE:
      case IDataModelAttribute.TYPE_TIME:
      case IDataModelAttribute.TYPE_DATE_TIME: {
        aggregationTypeList.add(AGGREGATION_MIN);
        aggregationTypeList.add(AGGREGATION_MAX);
        aggregationTypeList.add(AGGREGATION_MEDIAN);
        break;
      }
      case IDataModelAttribute.TYPE_INTEGER:
      case IDataModelAttribute.TYPE_LONG:
      case IDataModelAttribute.TYPE_BIG_DECIMAL:
      case IDataModelAttribute.TYPE_PLAIN_INTEGER:
      case IDataModelAttribute.TYPE_PLAIN_LONG:
      case IDataModelAttribute.TYPE_PLAIN_BIG_DECIMAL:
      case IDataModelAttribute.TYPE_PERCENT: {
        aggregationTypeList.add(AGGREGATION_SUM);
        aggregationTypeList.add(AGGREGATION_MIN);
        aggregationTypeList.add(AGGREGATION_MAX);
        aggregationTypeList.add(AGGREGATION_AVG);
        aggregationTypeList.add(AGGREGATION_MEDIAN);
        break;
      }
      case IDataModelAttribute.TYPE_AGGREGATE_COUNT: {
        aggregationTypeList.add(AGGREGATION_COUNT);
        break;
      }
    }
  }
}
