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

import org.eclipse.scout.rt.chart.client.ui.basic.chart.ChartChains.ChartValueClickChain;
import org.eclipse.scout.rt.shared.extension.IExtension;

public interface IChartExtension<CHART extends AbstractChart> extends IExtension<CHART> {

  void execValueClick(ChartValueClickChain chain, BigDecimal xIndex, BigDecimal yIndex, Integer datasetIndex);
}
