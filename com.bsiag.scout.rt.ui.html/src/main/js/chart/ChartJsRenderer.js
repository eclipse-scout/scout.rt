/*
 * Copyright (c) 2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSI CRM Software License v1.0
 * which accompanies this distribution as bsi-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {AbstractChartRenderer, Chart} from '../index';
import ChartJs from 'chart.js';
import {styles} from '@eclipse-scout/core';

/**
 * @typedef ChartJs
 * @property {object} [defaults]
 * @property {object} [defaults.global]
 * @property {object} [defaults.global.legend]
 * @property {object} [defaults.global.legend.labels]
 * @property {object} [defaults.global.elements]
 * @property {object} [defaults.global.elements.line]
 * @property {object} [defaults.global.elements.point]
 * @property {object} [defaults.global.elements.arc]
 * @property {object} [defaults.global.elements.rectangle]
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
ChartJs.defaults.global.elements.point.hoverBorderWidth = 2;
ChartJs.defaults.global.elements.arc.borderWidth = 1;
ChartJs.defaults.global.elements.rectangle.borderWidth = 1;

let chartJsGlobalsInitialized = false;

export default class ChartJsRenderer extends AbstractChartRenderer {

  constructor(chart) {
    super(chart);
    this.chartJs = null;
    this.minSpaceBetweenYTicks = 40;
    this.numSupportedColors = 6;
    this.colorSchemeCssClass = '';
  }

  _render() {
    if (!this.$canvas) {
      this.$canvas = this.chart.$container.appendElement('<canvas>');
    }
    if (!chartJsGlobalsInitialized) {
      ChartJs.defaults.global.defaultFontFamily = this.$canvas.css('font-family');
      chartJsGlobalsInitialized = true;
    }
    let config = this.chart.config;
    this._adjustConfig(config);
    this._renderChart(config, true);
  }

  _renderChart(config, animated) {
    if (this.chartJs) {
      this.chartJs.destroy();
    }
    config = $.extend(true, {}, config, {
      options: {
        animation: {}
      }
    });
    config.options.animation.duration = animated ? this.animationDuration : 0;

    /**
     * @type {Chart}
     * @property {object} config
     * @property {object} chartArea
     */
    this.chartJs = new ChartJs(this.$canvas[0].getContext('2d'), config);
    this._adjustGrid(this.chartJs.config, this.chartJs.chartArea);
    this.chartJs.update();
  }

  _updateChart(animated) {
    let config = this.chartJs.config;
    this._adjustColors(config);
    this._renderChart(config, animated);
  }

  _adjustConfig(config) {
    if (!config || !config.type) {
      return;
    }
    config.data = this._computeDatasets(this.chart.data);
    this._adjustData(config);
    this._adjustLayout(config);
    this._adjustColors(config);
  }

  _computeDatasets(chartData) {
    let labels = [],
      datasets = [];

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

  _adjustData(config) {
    if (!config || !config.data || !config.type) {
      return;
    }

    if (config.type === Chart.Type.PIE || config.type === Chart.Type.DOUGHNUT) {
      let maxSegments = config.options.maxSegments;
      if (!(maxSegments && config.data.datasets.length && maxSegments < config.data.datasets[0].data.length)) {
        return;
      }
      config.data.datasets.forEach(elem => {
        let newData = elem.data.slice(0, maxSegments);
        newData[maxSegments - 1] = elem.data.slice(maxSegments - 1, elem.data.length).reduce((x, y) => {
          return x + y;
        }, 0);
        elem.data = newData;
      });

      let newLabels = config.data.labels.slice(0, maxSegments);
      newLabels[maxSegments - 1] = this.chart.session.text('ui.OtherValues');
      config.data.labels = newLabels;
    }
  }

  _adjustLayout(config) {
    if (!config || !config.type || !config.options) {
      return;
    }

    let type = config.type;

    config.options = $.extend(true, {}, config.options, {
      hover: {
        mode: 'nearest'
      }
    });
    if (type === Chart.Type.BAR || type === Chart.Type.LINE) {
      config.options = $.extend(true, {}, config.options, {
        scales: {
          xAxes: [{}],
          yAxes: [{}]
        }
      });
    }
    for (let i = 0; i < ((config.options.scales || {}).xAxes || []).length; i++) {
      config.options.scales.xAxes[i] = $.extend(true, {}, config.options.scales.xAxes[i], {
        offset: true,
        gridLines: {
          display: false
        }
      });
    }
    for (let i = 0; i < ((config.options.scales || {}).yAxes || []).length; i++) {
      config.options.scales.yAxes[i] = $.extend(true, {}, config.options.scales.yAxes[i], {
        gridLines: {
          drawBorder: false,
          drawTicks: false,
          zeroLineBorderDash: [2, 4],
          borderDash: [2, 4]
        },
        ticks: {
          padding: 5
        }
      });
    }
  }

  _adjustColors(config) {
    if (!config || !config.data || !config.type) {
      return;
    }

    let data = config.data,
      type = config.type,
      autoColor = config.options && config.options.autoColor;

    let multipleColorsPerDataset = autoColor && (type === Chart.Type.PIE || type === Chart.Type.DOUGHNUT),
      colors = {
        backgroundColors: [],
        borderColors: [],
        hoverBackgroundColors: [],
        hoverBorderColors: [],
        labelColor: undefined,
        gridColor: undefined
      };

    colors.labelColor = styles.get([this.colorSchemeCssClass, type + '-chart', 'elements', 'label'], 'fill').fill;
    colors.gridColor = styles.get([this.colorSchemeCssClass, type + '-chart', 'elements', 'grid'], 'fill').fill;

    if (autoColor) {
      let colorsLength = multipleColorsPerDataset ? ((data.datasets.length && data.datasets[0].data.length) || 0) : data.datasets.length;
      for (let i = 0; i < colorsLength; i++) {
        colors.backgroundColors.push(styles.get([this.colorSchemeCssClass, type + '-chart', 'elements', 'color' + (i % this.numSupportedColors)], 'fill').fill);
        colors.borderColors.push(styles.get([this.colorSchemeCssClass, type + '-chart', 'elements', 'stroke-color' + (i % this.numSupportedColors)], 'stroke').stroke);
        colors.hoverBackgroundColors.push(styles.get([this.colorSchemeCssClass, type + '-chart', 'elements', 'color' + (i % this.numSupportedColors) + ' hover'], 'fill').fill);
        colors.hoverBorderColors.push(styles.get([this.colorSchemeCssClass, type + '-chart', 'elements', 'stroke-color' + (i % this.numSupportedColors) + ' hover'], 'stroke').stroke);
      }
    } else {
      colors.backgroundColors = this._computeColors(this.chart.data);
      colors.borderColors = colors.backgroundColors;
      if (type === Chart.Type.PIE || type === Chart.Type.DOUGHNUT) {
        let borderColor = styles.get([this.colorSchemeCssClass, type + '-chart', 'elements', 'stroke-color0'], 'stroke').stroke;
        colors.borderColors = Array(colors.borderColors.length).fill(borderColor);
        colors.hoverBorderColors = colors.borderColors;
      }
    }

    data.datasets.forEach((elem, idx) => {
      elem.backgroundColor = (multipleColorsPerDataset ? colors.backgroundColors : colors.backgroundColors[idx]);
      elem.borderColor = (multipleColorsPerDataset ? colors.borderColors : colors.borderColors[idx]);
      elem.hoverBackgroundColor = (multipleColorsPerDataset ? colors.hoverBackgroundColors : colors.hoverBackgroundColors[idx]);
      elem.hoverBorderColor = (multipleColorsPerDataset ? colors.hoverBorderColors : colors.hoverBorderColors[idx]);
    });

    if (config.options) {
      config.options = $.extend(true, {}, config.options, {
        legend: {
          labels: {
            fontColor: colors.labelColor
          }
        }
      });
      ((config.options.scales || {}).xAxes || []).forEach(elem => {
        elem.ticks = $.extend(true, {}, elem.ticks, {
          fontColor: colors.labelColor
        });
      });
      ((config.options.scales || {}).yAxes || []).forEach(elem => {
        elem.gridLines = $.extend(true, {}, elem.gridLines, {
          zeroLineColor: colors.gridColor,
          color: colors.gridColor
        });
        elem.ticks = $.extend(true, {}, elem.ticks, {
          fontColor: colors.labelColor
        });
      });
    }
  }

  _computeColors(chartData) {
    let colors = [];
    chartData.chartValueGroups.forEach(elem => colors.push(elem.colorHexValue));
    return colors;
  }

  _adjustGrid(config, chartArea) {
    if (!config || !config.type || !config.options || !config.options.scales || !chartArea) {
      return;
    }
    if (!(config.type === Chart.Type.BAR || config.type === Chart.Type.LINE)) {
      return;
    }

    let height = Math.abs(chartArea.top - chartArea.bottom),
      maxYTicks = Math.floor(height / this.minSpaceBetweenYTicks),
      maxMinValue = this._computeMaxMinValue(config.data);

    for (let i = 0; i < (config.options.scales.yAxes || []).length; i++) {
      config.options.scales.yAxes[i] = $.extend(true, {}, config.options.scales.yAxes[i], {
        ticks: {
          maxTicksLimit: maxYTicks
        }
      });
      if (maxMinValue) {
        config.options.scales.yAxes[i].ticks.suggestedMax = maxMinValue.maxValue;
        config.options.scales.yAxes[i].ticks.suggestedMin = maxMinValue.minValue;
      }
    }
  }

  _computeMaxMinValue(data) {
    if (!data) {
      return;
    }

    let maxValue = 0,
      minValue = 0,
      i = 0,
      j = 0,
      f;
    for (i = 0; i < data.datasets.length; i++) {
      for (j = 0; j < data.datasets[i].data.length; j++) {
        maxValue = Math.max(data.datasets[i].data[j], maxValue);
        minValue = Math.min(data.datasets[i].data[j], minValue);
      }
    }

    if (maxValue > 0) {
      f = Math.ceil(Math.log(maxValue) / Math.LN10) - 1;
      maxValue = Math.ceil(maxValue / Math.pow(10, f)) * Math.pow(10, f);
      maxValue = Math.ceil(maxValue / 4) * 4;
    }

    if (minValue < 0) {
      minValue = minValue * (-1);
      f = Math.ceil(Math.log(minValue) / Math.LN10) - 1;
      minValue = Math.ceil(minValue / Math.pow(10, f)) * Math.pow(10, f);
      minValue = Math.ceil(minValue / 4) * 4;
      minValue = minValue * (-1);
    }

    return {
      maxValue: Math.ceil(maxValue),
      minValue: Math.floor(minValue)
    };
  }

  renderColorScheme(colorSchemeCssClass) {
    this.colorSchemeCssClass = colorSchemeCssClass;
    if (this.rendered && this.chartJs) {
      this._updateChart(false);
    }
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
