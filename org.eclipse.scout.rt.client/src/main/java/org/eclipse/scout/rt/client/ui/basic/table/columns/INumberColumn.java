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
package org.eclipse.scout.rt.client.ui.basic.table.columns;

import org.eclipse.scout.rt.client.ui.valuecontainer.INumberValueContainer;

public interface INumberColumn<NUMBER extends Number> extends IColumn<NUMBER>, INumberValueContainer<NUMBER> {

  public static final String AGGREGATION_FUNCTION_SUM = "sum";
  public static final String AGGREGATION_FUNCTION_AVG = "avg";
  public static final String AGGREGATION_FUNCTION_MIN = "min";
  public static final String AGGREGATION_FUNCTION_MAX = "max";

  /**
   * type String
   */
  String PROP_AGGREGATION_FUNCTION = "aggregationFunction";

  void setValidateOnAnyKey(boolean b);

  boolean isValidateOnAnyKey();

  String getInitialAggregationFunction();

  void setInitialAggregationFunction(String f);

  String getAggregationFunction();

  void setAggregationFunction(String f);

}
