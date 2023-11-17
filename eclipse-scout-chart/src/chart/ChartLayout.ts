/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AbstractLayout, Tile} from '@eclipse-scout/core';
import {Chart, ChartJsRenderer, UpdateChartOptions} from '../index';

export class ChartLayout extends AbstractLayout {
  chart: Chart;

  constructor(chart: Chart) {
    super();
    this.chart = chart;
  }

  override layout($container: JQuery) {
    let opts: UpdateChartOptions = {
      requestAnimation: true,
      debounce: Chart.DEFAULT_DEBOUNCE_TIMEOUT,
      onlyUpdateData: true
    };
    // Don't request animations when the session is not ready or the chart was already updated once
    if (!this.chart.session.ready || this.chart._updatedOnce) {
      opts.requestAnimation = false;
    }
    // Don't debounce while session is not yet ready. Otherwise, it might happen that the area to layout is not
    // attached anymore because some other view tab is in front when the setTimeout() functions is finally called.
    if (!this.chart.session.ready) {
      opts.debounce = false;
    }
    // Don't debounce if the chart is inside a tile that is being dragged so the state is correct immediately when it is dropped
    if (this.chart.findParent(Tile)?.$container.hasClass('dragged')) {
      opts.debounce = false;
    }
    // Ensure chart has the correct size right after the layout.
    if (this.chart.chartRenderer instanceof ChartJsRenderer) {
      this.chart.chartRenderer.chartJs?.resize();
    }
    this.chart.updateChart(opts);
  }
}
