/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSI CRM Software License v1.0
 * which accompanies this distribution as bsi-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package com.bsiag.scout.rt.client.ui.basic.chart;

/**
 * @since 5.2
 */
@FunctionalInterface
public interface IChartUIFacade {

  /**
   * For all parameters: use -1 when parameter is not used or set by a chart type.
   *
   * @param axisIndex
   *          number
   * @param valueIndex
   *          number
   * @param groupIndex
   *          number
   */
  void fireValueClickFromUI(int axisIndex, int valueIndex, int groupIndex);
}
