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
import {Event, styles} from '@eclipse-scout/core';
// noinspection ES6UnusedImports
import chartjs_plugin_datalabels from 'chartjs-plugin-datalabels';

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
    this.minSpaceBetweenYTicks = 35;
    this.numSupportedColors = 6;
    this.colorSchemeCssClass = '';
    this.minRadialChartDatalabelSpace = 25;

    this._labelFormatter = this._formatLabel.bind(this);

    this._radialChartDatalabelsDisplayHandler = this._displayDatalabelsOnRadialChart.bind(this);
    this._radialChartDatalabelsFormatter = this._formatDatalabelsOnRadialChart.bind(this);

    this._datalabelBackgroundColorHandler = this._getBackgroundColorOfDataset.bind(this);

    this._clickHandler = this._onClick.bind(this);
    this._hoverHandler = this._onHover.bind(this);

    this._legendClickHandler = this._onLegendClick.bind(this);
    this._legendHoverHandler = this._onLegendHover.bind(this);
    this._legendLeaveHandler = this._onLegendLeave.bind(this);
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
    this._adjustClickHandler(config);
  }

  _computeDatasets(chartData) {
    let labels = [],
      datasets = [];

    (chartData.axes[0] || []).forEach(elem => labels.push(elem.label));

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

    if (config.type === Chart.Type.PIE || config.type === Chart.Type.DOUGHNUT || config.type === Chart.Type.POLAR_AREA || config.type === Chart.Type.RADAR) {
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
      config.data.maxSegmentsExceeded = true;
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
    if (type === Chart.Type.POLAR_AREA || type === Chart.Type.RADAR) {
      config.options = $.extend(true, {}, config.options, {
        scale: {}
      });
    } else if (type === Chart.Type.BAR || type === Chart.Type.LINE) {
      config.options = $.extend(true, {}, config.options, {
        scales: {
          xAxes: [{}],
          yAxes: [{}]
        }
      });
    }
    if (config.options.scale) {
      config.options.scale = $.extend(true, {}, config.options.scale, {
        angleLines: {
          display: false
        }, gridLines: {
          borderDash: [2, 4]
        },
        ticks: {
          callback: this._labelFormatter
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
          padding: 5,
          callback: this._labelFormatter
        }
      });
    }

    if (config.options.plugins && config.options.plugins.datalabels && config.options.plugins.datalabels.display) {
      if (config.type === Chart.Type.PIE || config.type === Chart.Type.DOUGHNUT) {
        config.options.plugins.datalabels.display = this._radialChartDatalabelsDisplayHandler;
        config.options.plugins.datalabels.formatter = this._radialChartDatalabelsFormatter;
      } else if (config.type === Chart.Type.BAR || config.type === Chart.Type.LINE || config.type === Chart.Type.POLAR_AREA || config.type === Chart.Type.RADAR) {
        config.options.plugins.datalabels.display = 'auto';
        config.options.plugins.datalabels.backgroundColor = this._datalabelBackgroundColorHandler;
        config.options.plugins.datalabels.borderRadius = 4;
      }
    }
  }

  _formatLabel(label) {
    if (isNaN(label)) {
      return label;
    }
    let abs = Math.abs(label);
    let abbreviation = '';
    if (abs >= 1000000) {
      abs = abs / 1000000;
      abbreviation = ' ' + this.chart.session.text('ui.Mio');
      let abbreviations = [
        this.chart.session.text('ui.Mrd'),
        this.chart.session.text('ui.Bio'),
        this.chart.session.text('ui.Brd'),
        this.chart.session.text('ui.Tri'),
        this.chart.session.text('ui.Trd')];
      for (let i = 0; i < abbreviations.length; i++) {
        if (abs >= 1000000) {
          abs = abs / 1000;
          abbreviation = ' ' + abbreviations[i];
        } else {
          break;
        }
      }
    }
    return this.session.locale.decimalFormat.format(Math.sign(label) * abs) + abbreviation;
  }

  _displayDatalabelsOnRadialChart(context) {
    let data = context.chart.getDatasetMeta(context.datasetIndex).data[context.dataIndex],
      model = data._model,
      // Compute the biggest circle that fits inside sector/arc with center in the middle between inner and outer radius.
      // First compute a circle C1 that touches the straight boundaries of the sector/arc. Then compute a circle C2 that touches the inner and the outer radius.
      // The smaller one of these two circles is the biggest possible circle that fits inside sector/arc with center in the middle between inner and outer radius.
      // circle C1:
      midRadius = (model.outerRadius + model.innerRadius) / 2,
      // If the difference between the angles is greater than pi, it is no longer possible for a circle to be inside the sector/arc and touch both straight boundaries.
      angle = Math.min((model.endAngle - model.startAngle), Math.PI) / 2,
      radius1 = Math.abs(Math.sin(angle)) * midRadius,
      diameter1 = radius1 * 2,
      // circle C2:
      diameter2 = model.outerRadius - model.innerRadius;
    return Math.min(diameter1, diameter2) > this.minRadialChartDatalabelSpace;
  }

  _formatDatalabelsOnRadialChart(value, context) {
    let sum = this._computeSumOfVisibleElements(context),
      dataset = context.dataset,
      roundingError = 0,
      roundedResults = [];
    for (let i = 0; i < context.dataIndex + 1; i++) {
      let result = dataset.data[i] / sum * 100 - roundingError,
        roundedResult = Math.round(result);
      roundingError = roundedResult - result;
      roundedResults.push(roundedResult + '%');
    }
    return roundedResults[context.dataIndex];
  }

  _computeSumOfVisibleElements(context) {
    let dataset = context.dataset,
      meta = context.chart.getDatasetMeta(context.datasetIndex),
      sum = 0;
    for (let i = 0; i < dataset.data.length; i++) {
      if (meta.data[i] && !meta.data[i].hidden) {
        sum += dataset.data[i];
      }
    }
    return sum;
  }

  _getBackgroundColorOfDataset(context) {
    return context.dataset.backgroundColor;
  }

  _adjustColors(config) {
    if (!config || !config.data || !config.type) {
      return;
    }

    let data = config.data,
      type = config.type,
      autoColor = config.options && config.options.autoColor;

    let multipleColorsPerDataset = autoColor && (type === Chart.Type.PIE || type === Chart.Type.DOUGHNUT || type === Chart.Type.POLAR_AREA),
      colors = {
        backgroundColors: [],
        borderColors: [],
        hoverBackgroundColors: [],
        hoverBorderColors: [],
        labelColor: undefined,
        labelBackdropColor: undefined,
        datalabelColor: undefined,
        axisLabelColor: undefined,
        gridColor: undefined
      };

    colors.labelColor = styles.get([this.colorSchemeCssClass, type + '-chart', 'elements', 'label'], 'fill').fill;
    colors.labelBackdropColor = styles.get([this.colorSchemeCssClass, type + '-chart', 'elements', 'label-backdrop'], 'fill').fill;
    colors.datalabelColor = styles.get([this.colorSchemeCssClass, type + '-chart', 'elements', 'datalabel'], 'fill').fill;
    colors.axisLabelColor = styles.get([this.colorSchemeCssClass, type + '-chart', 'elements', 'axis-label'], 'fill').fill;
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
      colors.borderColors = this._computeColors(this.chart.data);
      if (type === Chart.Type.POLAR_AREA) {
        colors.backgroundColors = this._computeColors(this.chart.data, 0.7);
      } else if (type === Chart.Type.RADAR) {
        colors.backgroundColors = this._computeColors(this.chart.data, 0.2);
      } else {
        colors.backgroundColors = colors.borderColors;
      }
      if (type === Chart.Type.PIE || type === Chart.Type.DOUGHNUT || type === Chart.Type.POLAR_AREA) {
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
      if (config.options.scale) {
        config.options.scale.ticks = $.extend(true, {}, config.options.scale.ticks, {
          fontColor: colors.labelColor,
          backdropColor: colors.labelBackdropColor
        });
        config.options.scale.pointLabels = $.extend(true, {}, config.options.scale.pointLabels, {
          fontColor: colors.labelColor
        });
        config.options.scale.gridLines = $.extend(true, {}, config.options.scale.gridLines, {
          color: colors.gridColor
        });
      }
      ((config.options.scales || {}).xAxes || []).forEach(elem => {
        elem.ticks = $.extend(true, {}, elem.ticks, {
          fontColor: colors.labelColor
        });
        elem.scaleLabel = $.extend(true, {}, elem.scaleLabel, {
          fontColor: colors.axisLabelColor
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
        elem.scaleLabel = $.extend(true, {}, elem.scaleLabel, {
          fontColor: colors.axisLabelColor
        });
      });
      if (config.options.plugins && config.options.plugins.datalabels) {
        config.options.plugins.datalabels = $.extend(true, {}, config.options.plugins.datalabels, {
          color: colors.datalabelColor
        });
      }
    }
  }

  _computeColors(chartData, opacity = 1) {
    let colors = [],
      opacityHex = Math.round(opacity * 255).toString(16);
    if (opacityHex.length === 1) {
      opacityHex = '0' + opacityHex;
    }
    chartData.chartValueGroups.forEach(elem => colors.push(elem.colorHexValue + opacityHex));
    return colors;
  }

  _adjustGrid(config, chartArea) {
    if (!config || !config.type || !config.options || (!config.options.scale && !config.options.scales) || !chartArea) {
      return;
    }
    if (!(config.type === Chart.Type.BAR || config.type === Chart.Type.LINE || config.type === Chart.Type.POLAR_AREA || config.type === Chart.Type.RADAR)) {
      return;
    }

    let height = Math.abs(chartArea.top - chartArea.bottom),
      maxYTicks = Math.floor(height / this.minSpaceBetweenYTicks),
      maxMinValue = this._computeMaxMinValue(config.data);

    if (config.options.scale) {
      config.options.scale.ticks = $.extend(true, {}, config.options.scale.ticks, {
        maxTicksLimit: Math.ceil(maxYTicks / 2)
      });
      if (maxMinValue) {
        config.options.scale.ticks.suggestedMax = maxMinValue.maxValue;
        config.options.scale.ticks.suggestedMin = maxMinValue.minValue;
      }
    } else {
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

  _adjustClickHandler(config) {
    if (!config || !config.options) {
      return;
    }

    if (config.options.clickable) {
      config.options.onClick = this._clickHandler;
      config.options.onHover = this._hoverHandler;
    }

    if (!config.options.legend) {
      return;
    }

    if (config.options.legend.clickable) {
      config.options.legend.onClick = this._legendClickHandler;
      config.options.legend.onHover = this._legendHoverHandler;
      config.options.legend.onLeave = this._legendLeaveHandler;
    } else {
      config.options.legend.onClick = e => e.stopPropagation();
    }
  }

  /**
   * @param {object[]} items
   * @param {number} items._index
   * @param {number} items._datasetIndex
   */
  _onClick(event, items) {
    if (items.length) {
      if (this._isMaxSegmentsExceeded(this.chartJs.config, items[0]._index)) {
        return;
      }
      let clickObject = {
        axisIndex: 0,
        valueIndex: items[0]._index,
        groupIndex: items[0]._datasetIndex
      };
      let e = new Event();
      e.data = clickObject;
      this.chart._onValueClick(e);
    }
  }

  _onHover(event, items) {
    if (items.length && !this._isMaxSegmentsExceeded(this.chartJs.config, items[0]._index)) {
      this.$canvas.css('cursor', 'pointer');
    } else {
      this.$canvas.css('cursor', 'default');
    }
  }

  _onLegendClick(event, item) {
    let defaultLegendClick = ((ChartJs.defaults[this.chartJs.config.type] || {}).legend || {}).onClick || ChartJs.defaults.global.legend.onClick;
    defaultLegendClick.call(this.chartJs, event, item);
    this._onLegendHover(event, item);
  }

  _onLegendHover(event, item) {
    this.$canvas.css('cursor', 'pointer');
  }

  _onLegendLeave(event, item) {
    this.$canvas.css('cursor', 'default');
  }

  _isMaxSegmentsExceeded(config, index) {
    if (config.type === Chart.Type.PIE || config.type === Chart.Type.DOUGHNUT || config.type === Chart.Type.POLAR_AREA || config.type === Chart.Type.RADAR) {
      if (!config.data.maxSegmentsExceeded || !config.options.maxSegments) {
        return false;
      }
      return config.options.maxSegments - 1 <= index;
    }
    return false;
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
