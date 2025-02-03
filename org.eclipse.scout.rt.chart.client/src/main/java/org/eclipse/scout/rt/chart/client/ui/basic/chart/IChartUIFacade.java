/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.chart.client.ui.basic.chart;

import java.math.BigDecimal;

/**
 * @since 5.2
 */
@FunctionalInterface
public interface IChartUIFacade {

  /**
   * For all parameters: use null when parameter is not used or set by a chart type.
   *
   * @param xIndex
   *     number
   * @param yIndex
   *     number
   * @param datasetIndex
   *     number
   */
  void fireValueClickFromUI(BigDecimal xIndex, BigDecimal yIndex, Integer datasetIndex);
}
