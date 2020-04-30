/*
 * Copyright (c) 2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSI CRM Software License v1.0
 * which accompanies this distribution as bsi-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {AbstractChartRenderer} from '../index';
import ChartJs from 'chart.js';

/**
 * @typedef ChartJs
 * @property {object} [defaults]
 */
ChartJs.defaults.global.maintainAspectRatio = false;
ChartJs.defaults.global.legend.labels.usePointStyle = true;
ChartJs.defaults.global.legend.labels.boxWidth = 7;
ChartJs.defaults.global.elements.line.tension = 0;
ChartJs.defaults.global.elements.line.fill = false;
ChartJs.defaults.global.elements.line.borderWidth = 2;
ChartJs.defaults.global.elements.point.radius = 0;
ChartJs.defaults.global.elements.point.hitRadius = 5;
ChartJs.defaults.global.elements.point.hoverRadius = 5;
ChartJs.defaults.global.elements.arc.borderWidth = 1;

export default class ChartJsRenderer extends AbstractChartRenderer {

  constructor(chart) {
    super(chart);
    this.chartJs = null;
  }

  _render() {
    if (!this.$canvas) {
      this.$canvas = this.chart.$container.appendElement('<canvas>');
    }
    let config = this.chart.config;
    config.data = this._computeDatasets(this.chart.data);
    this.chartJs = new ChartJs(this.$canvas[0].getContext('2d'), config);
  }

  _computeDatasets(chartData) {
    let labels = [];
    let datasets = [];

    chartData.axes[0].forEach(elem => labels.push(elem.label));

    chartData.chartValueGroups.forEach(elem => datasets.push({
      label: elem.groupName,
      data: elem.values
    }));

    return {
      labels: labels,
      datasets: datasets
    };
  }

  _remove(afterRemoveFunc) {
    if (this.rendered) {
      this.$canvas.remove();
      this.$canvas = null;
      this.chartJs.destroy();
      this.chartJs = null;
    }
    super._remove(afterRemoveFunc);
  }
}
