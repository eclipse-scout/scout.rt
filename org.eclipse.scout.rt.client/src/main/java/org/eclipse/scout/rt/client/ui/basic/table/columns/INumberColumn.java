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

  String AGGREGATION_FUNCTION_SUM = "sum";
  String AGGREGATION_FUNCTION_AVG = "avg";
  String AGGREGATION_FUNCTION_MIN = "min";
  String AGGREGATION_FUNCTION_MAX = "max";

  String BACKGROUND_EFFECT_COLOR_GRADIENT_1 = "colorGradient1";
  String BACKGROUND_EFFECT_COLOR_GRADIENT_2 = "colorGradient2";
  String BACKGROUND_EFFECT_BAR_CHART = "barChart";

  /**
   * type String
   */
  String PROP_AGGREGATION_FUNCTION = "aggregationFunction";
  String PROP_BACKGROUND_EFFECT = "backgroundEffect";

  void setValidateOnAnyKey(boolean b);

  boolean isValidateOnAnyKey();

  String getInitialAggregationFunction();

  void setInitialAggregationFunction(String f);

  String getAggregationFunction();

  void setAggregationFunction(String f);

  /**
   * Set the background effect for this column. May be null.
   * <p>
   * Supported by the UI are {@link INumberColumn#BACKGROUND_EFFECT_COLOR_GRADIENT_1},
   * {@link INumberColumn#BACKGROUND_EFFECT_COLOR_GRADIENT_2} and {@link INumberColumn#BACKGROUND_EFFECT_BAR_CHART}
   *
   * @param effect
   * @since 5.2
   */
  void setBackgroundEffect(String effect);

  /**
   * @return The background effect of this column. May be null.
   */
  String getBackgroundEffect();

  void setInitialBackgroundEffect(String effect);

  String getInitialBackgroundEffect();

}
