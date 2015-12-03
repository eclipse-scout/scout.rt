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
package org.eclipse.scout.rt.client.ui.basic.table.columns;

import org.eclipse.scout.rt.client.ui.valuecontainer.INumberValueContainer;

public interface INumberColumn<NUMBER extends Number> extends IColumn<NUMBER>, INumberValueContainer<NUMBER> {

  /**
   * supported values for {@link INumberColumn#setAggregationFunction(String))} and
   * {@link AbstractNumberColumn#getConfiguredAggregationFunction()}
   *
   * @since 5.2
   */
  interface AggregationFunction {

    String SUM = "sum";
    String AVG = "avg";
    String MIN = "min";
    String MAX = "max";

  }

  /**
   * supported values for {@link INumberColumn#setBackgroundEffect(String)} and
   * {@link AbstractNumberColumn#getConfiguredBackgroundEffect()}
   *
   * @since 5.2
   */
  interface BackgroundEffect {
    String COLOR_GRADIENT_1 = "colorGradient1";
    String COLOR_GRADIENT_2 = "colorGradient2";
    String BAR_CHART = "barChart";
  }

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

  /**
   * Set the aggregation function for this column
   *
   * @param effect
   *          one of the constant values in {@link AggregationFunction}
   * @since 5.2
   */
  void setAggregationFunction(String f);

  /**
   * Set the background effect for this column
   *
   * @param effect
   *          one of the constant values in {@link BackgroundEffect} or <code>null</code>
   * @since 5.2
   */
  void setBackgroundEffect(String effect);

  /**
   * @return The background effect of this column. May be <code>null</code>.
   */
  String getBackgroundEffect();

  void setInitialBackgroundEffect(String effect);

  String getInitialBackgroundEffect();

}
