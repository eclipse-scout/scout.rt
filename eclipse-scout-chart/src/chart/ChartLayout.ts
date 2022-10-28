/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {AbstractLayout} from '@eclipse-scout/core';
import {Chart} from '../index';
import {UpdateChartOptions} from './Chart';

export default class ChartLayout extends AbstractLayout {
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
    this.chart.updateChart(opts);
  }
}
