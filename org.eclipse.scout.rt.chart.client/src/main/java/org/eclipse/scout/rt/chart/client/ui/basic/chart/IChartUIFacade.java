/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
   *          number
   * @param yIndex
   *          number
   * @param datasetIndex
   *          number
   */
  void fireValueClickFromUI(BigDecimal xIndex, BigDecimal yIndex, Integer datasetIndex);
}
