/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.chart.client.ui.form.fields.chartfield;

import org.eclipse.scout.rt.chart.client.ui.basic.chart.IChart;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;

/**
 * @since 5.2
 */
public interface IChartField<T extends IChart> extends IFormField {

  String PROP_CHART = "chart";

  void setChart(T chart);

  T getChart();
}
