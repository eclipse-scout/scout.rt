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
package com.bsiag.scout.rt.client.ui.basic.chart;

import java.math.BigDecimal;

import org.eclipse.scout.rt.shared.extension.IExtension;

import com.bsiag.scout.rt.client.ui.basic.chart.ChartChains.ChartValueClickChain;

public interface IChartExtension<CHART extends AbstractChart> extends IExtension<CHART> {

  void execValueClick(ChartValueClickChain chain, BigDecimal xIndex, BigDecimal yIndex, Integer datasetIndex);

}
