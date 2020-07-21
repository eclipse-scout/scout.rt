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
    this.minSpaceBetweenXTicks = 150;
    this.numSupportedColors = 6;
    this.colorSchemeCssClass = '';
    this.minRadialChartDatalabelSpace = 25;
    this.bubbleScalingFactor = 1;

    this.resetDatasetAfterHover = false;

    this._labelFormatter = this._formatLabel.bind(this);
    this._xLabelFormatter = this._formatXLabel.bind(this);
    this._yLabelFormatter = this._formatYLabel.bind(this);

    this._radialChartDatalabelsDisplayHandler = this._displayDatalabelsOnRadialChart.bind(this);
    this._radialChartDatalabelsFormatter = this._formatDatalabelsOnRadialChart.bind(this);

    this._datalabelBackgroundColorHandler = this._getBackgroundColorOfDataset.bind(this);

    this._legendLabelGenerator = this._generateLegendLabels.bind(this);

    this._clickHandler = this._onClick.bind(this);
    this._hoverHandler = this._onHover.bind(this);
    this._pointerHoverHandler = this._onHoverPointer.bind(this);

    this.legendHoverDatasets = [];

    this._legendClickHandler = this._onLegendClick.bind(this);
    this._legendHoverHandler = this._onLegendHover.bind(this);
    this._legendPointerHoverHandler = this._onLegendHoverPointer.bind(this);
    this._legendLeaveHandler = this._onLegendLeave.bind(this);
    this._legendPointerLeaveHandler = this._onLegendLeavePointer.bind(this);
  }

  _validateChartData() {
    let chartDataValid = true;
    let chartData = this.chart && this.chart.data;

    if (!chartData || !chartData.chartValueGroups || chartData.chartValueGroups.length === 0) {
      chartDataValid = false;
    }

    if (chartDataValid && (this.chart.config.type === Chart.Type.POLAR_AREA || this.chart.config.type === Chart.Type.RADAR)) {
      // check lengths
      let i, length = 0;
      for (i = 0; i < chartData.chartValueGroups.length; i++) {
        let chartValueGroup = chartData.chartValueGroups[i];
        if (!chartValueGroup.values) {
          chartDataValid = false;
        }
        // Length of all "values" arrays have to be equal
        if (i === 0) {
          length = chartValueGroup.values.length;
        } else {
          if (chartValueGroup.values.length !== length) {
            chartDataValid = false;
          }
        }
      }
      for (i = 0; i < chartData.axes.length; i++) {
        if (chartData.axes[i].length !== length) {
          chartDataValid = false;
        }
      }
    }

    if (chartDataValid) {
      return true;
    }

    let chartConfigDataValid = true;
    let config = this.chart && this.chart.config;

    if (!config || !config.data || !config.data.datasets || config.data.datasets.length === 0) {
      chartConfigDataValid = false;
    }

    if (chartConfigDataValid && (this.chart.config.type === Chart.Type.POLAR_AREA || this.chart.config.type === Chart.Type.RADAR)) {
      // check lengths
      let i, length = 0;
      for (i = 0; i < config.data.datasets.length; i++) {
        let dataset = config.data.datasets.length[i];
        if (!dataset.data) {
          chartConfigDataValid = false;
        }
        // Length of all "data" arrays have to be equal
        if (i === 0) {
          length = dataset.data.length;
        } else {
          if (dataset.data.length !== length) {
            chartConfigDataValid = false;
          }
        }
      }
    }

    return chartConfigDataValid;
  }

  _render() {
    if (!this.$canvas) {
      this.$canvas = this.chart.$container.appendElement('<canvas>');
    }
    if (!chartJsGlobalsInitialized) {
      ChartJs.defaults.global.defaultFontFamily = this.$canvas.css('font-family');
      chartJsGlobalsInitialized = true;
    }
    /**
     * @property {number} options.bubble.sizeOfLargestBubble
     * @property {object} options.scales.xLabelMap
     * @property {object} options.scales.yLabelMap
     */
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
    if (this.chart.data) {
      this._computeDatasets(this.chart.data, config);
    }
    this._adjustData(config);
    this._adjustLayout(config);
    this._adjustColors(config);
    this._adjustClickHandler(config);
  }

  _computeDatasets(chartData, config) {
    let labels = [],
      datasets = [];

    (chartData.axes[0] || []).forEach(elem => labels.push(elem.label));

    let xLabelMap = this._computeLabelMap(chartData.axes[0]);

    if (!$.isEmptyObject(xLabelMap)) {
      config.options = $.extend(true, {}, config.options, {
        scales: {}
      });

      config.options.scales.xLabelMap = xLabelMap;
    }

    let yLabelMap = this._computeLabelMap(chartData.axes[1]);

    if (!$.isEmptyObject(yLabelMap)) {
      config.options = $.extend(true, {}, config.options, {
        scales: {}
      });

      config.options.scales.yLabelMap = yLabelMap;
    }

    chartData.chartValueGroups.forEach(elem => datasets.push({
      label: elem.groupName,
      data: elem.values
    }));

    config.data = {
      labels: labels,
      datasets: datasets
    };
  }

  _computeLabelMap(axis) {
    let labelMap = {};
    (axis || []).forEach((elem, idx) => {
      labelMap[idx] = elem.label;
    });
    return labelMap;
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
    if (config.type === Chart.Type.BUBBLE) {
      let maxR = this._computeMaxMinValue(config.data, 'r', true).maxValue;
      if (maxR > 0 && (config.bubble || {}).sizeOfLargestBubble) {
        this.bubbleScalingFactor = config.bubble.sizeOfLargestBubble / maxR;
        config.data.datasets.forEach(dataset => dataset.data.forEach(data => {
          data.r = data.r * this.bubbleScalingFactor;
        }));
      }
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
    } else if (type === Chart.Type.BAR || type === Chart.Type.LINE || type === Chart.Type.BUBBLE) {
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
        },
        pointLabels: {
          fontSize: ChartJs.defaults.global.defaultFontSize
        }
      });
    }
    for (let i = 0; i < ((config.options.scales || {}).xAxes || []).length; i++) {
      if (type === Chart.Type.BUBBLE) {
        config.options.scales.xAxes[i] = $.extend(true, {}, config.options.scales.xAxes[i], {
          offset: true,
          gridLines: {
            drawBorder: false,
            drawTicks: false,
            zeroLineBorderDash: [2, 4],
            borderDash: [2, 4]
          },
          ticks: {
            padding: 5,
            callback: this._xLabelFormatter
          }
        });
      } else {
        config.options.scales.xAxes[i] = $.extend(true, {}, config.options.scales.xAxes[i], {
          offset: true,
          gridLines: {
            display: false
          }
        });
      }
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
          callback: this._yLabelFormatter
        }
      });
    }

    if (config.options.plugins && config.options.plugins.datalabels && config.options.plugins.datalabels.display) {
      if (config.type === Chart.Type.PIE || config.type === Chart.Type.DOUGHNUT) {
        config.options.plugins.datalabels.display = this._radialChartDatalabelsDisplayHandler;
        config.options.plugins.datalabels.formatter = this._radialChartDatalabelsFormatter;
      } else if (config.type === Chart.Type.BAR || config.type === Chart.Type.LINE || config.type === Chart.Type.POLAR_AREA || config.type === Chart.Type.RADAR || config.type === Chart.Type.BUBBLE) {
        config.options.plugins.datalabels.display = 'auto';
        config.options.plugins.datalabels.backgroundColor = this._datalabelBackgroundColorHandler;
        config.options.plugins.datalabels.borderRadius = 4;
      }
    }
  }

  _formatLabel(label) {
    return this._formatLabelMap(label);
  }

  _formatXLabel(label) {
    return this._formatLabelMap(label, this.chart.config.options.scales.xLabelMap);
  }

  _formatYLabel(label) {
    return this._formatLabelMap(label, this.chart.config.options.scales.yLabelMap);
  }

  _formatLabelMap(label, labelMap) {
    if (labelMap) {
      return labelMap[label];
    }
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
        gridColor: undefined,
        pointHoverColor: undefined
      };

    colors.labelColor = styles.get([this.colorSchemeCssClass, type + '-chart', 'elements', 'label'], 'fill').fill;
    colors.labelBackdropColor = styles.get([this.colorSchemeCssClass, type + '-chart', 'elements', 'label-backdrop'], 'fill').fill;
    colors.datalabelColor = styles.get([this.colorSchemeCssClass, type + '-chart', 'elements', 'datalabel'], 'fill').fill;
    colors.axisLabelColor = styles.get([this.colorSchemeCssClass, type + '-chart', 'elements', 'axis-label'], 'fill').fill;
    colors.gridColor = styles.get([this.colorSchemeCssClass, type + '-chart', 'elements', 'grid'], 'fill').fill;
    colors.pointHoverColor = styles.get([this.colorSchemeCssClass, type + '-chart', 'elements', 'point hover'], 'fill').fill;

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
      } else if (type === Chart.Type.LINE || type === Chart.Type.RADAR || type === Chart.Type.BUBBLE) {
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
      elem.pointHoverBackgroundColor = colors.pointHoverColor;
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
          fontColor: colors.gridColor,
          backdropColor: colors.labelBackdropColor
        });
        config.options.scale.pointLabels = $.extend(true, {}, config.options.scale.pointLabels, {
          fontColor: colors.labelColor
        });
        config.options.scale.gridLines = $.extend(true, {}, config.options.scale.gridLines, {
          color: colors.gridColor
        });
      }
      [...((config.options.scales || {}).xAxes || []), ...((config.options.scales || {}).yAxes || [])].forEach(elem => {
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

    config.options = $.extend(true, {}, config.options, {
      legend: {
        labels: {
          generateLabels: this._legendLabelGenerator
        }
      }
    });
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

  _generateLegendLabels(chart) {
    let defaultGenerateLabels = (((ChartJs.defaults[chart.config.type] || {}).legend || {}).labels || {}).generateLabels || ChartJs.defaults.global.legend.labels.generateLabels;
    let labels = defaultGenerateLabels.call(chart, chart);
    labels.forEach((elem, idx) => {
      if (chart.config.type === Chart.Type.LINE || chart.config.type === Chart.Type.RADAR || chart.config.type === Chart.Type.BUBBLE) {
        elem.fillStyle = chart.config.data.datasets[idx].borderColor;
      } else if (chart.config.type === Chart.Type.POLAR_AREA) {
        if (chart.config.options.autoColor) {
          let rgba = styles.rgb(chart.config.data.datasets[0].backgroundColor[idx]);
          rgba.alpha = 1;
          elem.fillStyle = 'rgba(' + rgba.red + ', ' + rgba.green + ', ' + rgba.blue + ', ' + rgba.alpha + ')';
        }
      }
      elem.strokeStyle = elem.fillStyle;
    });
    return labels;
  }

  _adjustGrid(config, chartArea) {
    if (!config || !config.type || !config.options || (!config.options.scale && !config.options.scales) || !chartArea) {
      return;
    }
    if (!(config.type === Chart.Type.BAR || config.type === Chart.Type.LINE || config.type === Chart.Type.POLAR_AREA || config.type === Chart.Type.RADAR || config.type === Chart.Type.BUBBLE)) {
      return;
    }

    let height = Math.abs(chartArea.top - chartArea.bottom),
      maxYTicks = Math.floor(height / this.minSpaceBetweenYTicks),
      yBoundaries;

    let padding = 0;
    if (config.type === Chart.Type.BUBBLE) {
      let maxR = this._computeMaxMinValue(config.data, 'r', true);
      padding = maxR.maxValue + (((config.options.elements || {}).point || {}).hoverRadius || 0);
      yBoundaries = this._computeMaxMinValue(config.data, 'y', config.options.scales.yLabelMap, padding, height);
    } else {
      yBoundaries = this._computeMaxMinValue(config.data);
    }

    if (config.options.scale) {
      config.options.scale.ticks = $.extend(true, {}, config.options.scale.ticks, {
        maxTicksLimit: Math.ceil(maxYTicks / 2)
      });
      if (yBoundaries) {
        config.options.scale.ticks.suggestedMax = yBoundaries.maxValue;
        config.options.scale.ticks.suggestedMin = yBoundaries.minValue;
      }
    } else {
      this._adjustAxes(config.options.scales.yAxes, maxYTicks, yBoundaries);
    }

    if (!(config.type === Chart.Type.BUBBLE)) {
      return;
    }

    let width = Math.abs(chartArea.right - chartArea.left),
      maxXTicks = Math.floor(width / this.minSpaceBetweenXTicks),
      xBoundaries = this._computeMaxMinValue(config.data, 'x', config.options.scales.xLabelMap, padding, width);
    this._adjustAxes(config.options.scales.xAxes, maxXTicks, xBoundaries);
  }

  _adjustAxes(axes, maxTicks, maxMinValue) {
    for (let i = 0; i < (axes || []).length; i++) {
      axes[i] = $.extend(true, {}, axes[i], {
        ticks: {
          maxTicksLimit: maxTicks
        }
      });
      if (maxMinValue) {
        axes[i].ticks.suggestedMax = maxMinValue.maxValue;
        axes[i].ticks.suggestedMin = maxMinValue.minValue;
      }
    }
  }

  _computeMaxMinValue(data, identifier, exact, padding, space) {
    if (!data) {
      return;
    }

    let maxValue, minValue;
    for (let i = 0; i < data.datasets.length; i++) {
      for (let j = 0; j < data.datasets[i].data.length; j++) {
        let value;
        if (identifier) {
          value = data.datasets[i].data[j][identifier];
        } else {
          value = data.datasets[i].data[j];
        }
        if (isNaN(maxValue)) {
          maxValue = value;
        } else {
          maxValue = Math.max(value, maxValue);
        }
        if (isNaN(minValue)) {
          minValue = value;
        } else {
          minValue = Math.min(value, minValue);
        }
      }
    }

    if (isNaN(maxValue)) {
      maxValue = 0;
    }
    if (isNaN(minValue)) {
      minValue = 0;
    }

    if (padding && space && space > 2 * padding) {
      let valuePerPixel = (maxValue - minValue) / (space - 2 * padding),
        paddingValue = valuePerPixel * padding;
      maxValue = maxValue + paddingValue;
      minValue = minValue - paddingValue;
    }

    if (!exact) {
      if (maxValue > 0) {
        maxValue = this._calculateBoundary(maxValue);
      }

      if (minValue < 0) {
        minValue = minValue * (-1);
        minValue = this._calculateBoundary(minValue);
        minValue = minValue * (-1);
      }
    }

    return {
      maxValue: Math.ceil(maxValue),
      minValue: Math.floor(minValue)
    };
  }

  _calculateBoundary(value) {
    let f = Math.ceil(Math.log(value) / Math.LN10) - 1;
    value = Math.ceil(value / Math.pow(10, f)) * Math.pow(10, f);
    value = Math.ceil(value / 4) * 4;
    return value;
  }

  _adjustClickHandler(config) {
    if (!config || !config.options) {
      return;
    }

    if (config.options.clickable) {
      config.options.onClick = this._clickHandler;
      config.options.onHover = this._pointerHoverHandler;
    } else {
      config.options.onHover = this._hoverHandler;
    }

    if (!config.options.legend) {
      return;
    }

    if (config.options.legend.clickable) {
      config.options.legend.onClick = this._legendClickHandler;
      config.options.legend.onHover = this._legendPointerHoverHandler;
      config.options.legend.onLeave = this._legendPointerLeaveHandler;
    } else {
      config.options.legend.onClick = e => e.stopPropagation();
      config.options.legend.onHover = this._legendHoverHandler;
      config.options.legend.onLeave = this._legendLeaveHandler;
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

      let itemIndex = items[0]._index,
        datasetIndex = items[0]._datasetIndex,
        clickObject = {
          datasetIndex: datasetIndex
        };
      if (this.chartJs.config.type === Chart.Type.BUBBLE) {
        let data = this.chartJs.config.data.datasets[datasetIndex].data[itemIndex];
        clickObject.xIndex = data.x;
        clickObject.yIndex = data.y;
      } else {
        clickObject.xIndex = itemIndex;
      }

      let e = new Event();
      e.data = clickObject;
      this.chart._onValueClick(e);
    }
  }

  _onHover(event, items) {
    if (this.chartJs.config && this.chartJs.config.type && (this.chartJs.config.type === Chart.Type.LINE || this.chartJs.config.type === Chart.Type.RADAR) && this.chartJs.config.options && this.chartJs.config.options.autoColor) {
      let update = false;
      if (this.resetDatasetAfterHover) {
        this._adjustColors(this.chartJs.config);
        this.resetDatasetAfterHover = false;
        update = true;
      }
      items.forEach(item => {
        let dataset = this.chartJs.config.data.datasets[item._datasetIndex];
        dataset.backgroundColor = dataset.hoverBackgroundColor;
        this.resetDatasetAfterHover = true;
        update = true;
      });
      if (update) {
        this.chartJs.update();
      }
    }
  }

  _onHoverPointer(event, items) {
    this._onHover(event, items);
    if (items.length && !this._isMaxSegmentsExceeded(this.chartJs.config, items[0]._index)) {
      this.$canvas.css('cursor', 'pointer');
    } else {
      this.$canvas.css('cursor', 'default');
    }
  }

  _onLegendClick(event, item) {
    let defaultLegendClick = ((ChartJs.defaults[this.chartJs.config.type] || {}).legend || {}).onClick || ChartJs.defaults.global.legend.onClick;
    defaultLegendClick.call(this.chartJs, event, item);
    this._onLegendLeave(event, item);
    this._onLegendHoverPointer(event, item, true);
  }

  _onLegendHover(event, item, animated) {
    let index = item.datasetIndex;
    if (this.chartJs.config.type === Chart.Type.PIE || this.chartJs.config.type === Chart.Type.DOUGHNUT || this.chartJs.config.type === Chart.Type.POLAR_AREA) {
      index = item.index;
    }
    if (this.legendHoverDatasets.indexOf(index) < 0) {
      if (this.chartJs.config.type === Chart.Type.LINE) {
        let dataset = this.chartJs.config.data.datasets[index];
        dataset.backgroundColor = dataset.hoverBackgroundColor;
        this.chartJs.update();
      }
      this._updateHoverStyle(index, true);
      if (animated) {
        this.chartJs.render();
      } else {
        this.chartJs.render({duration: 0});
      }
      this.legendHoverDatasets.push(index);
    }
  }

  _onLegendHoverPointer(event, item, animated) {
    this._onLegendHover(event, item, animated);
    this.$canvas.css('cursor', 'pointer');
  }

  _onLegendLeave(event, item) {
    let index = item.datasetIndex;
    if (this.chartJs.config.type === Chart.Type.PIE || this.chartJs.config.type === Chart.Type.DOUGHNUT || this.chartJs.config.type === Chart.Type.POLAR_AREA) {
      index = item.index;
    }
    if (this.legendHoverDatasets.indexOf(index) > -1) {
      if (this.chartJs.config.type === Chart.Type.LINE) {
        this._adjustColors(this.chartJs.config);
        this.chartJs.update();
      }
      this._updateHoverStyle(index, false);
      this.chartJs.render();
      this.legendHoverDatasets.splice(this.legendHoverDatasets.indexOf(index), 1);
    }
  }

  _onLegendLeavePointer(event, item) {
    this._onLegendLeave(event, item);
    this.$canvas.css('cursor', 'default');
  }

  _updateHoverStyle(index, enabled) {
    if (this.chartJs.config.type === Chart.Type.LINE) {
      this.chartJs.updateHoverStyle(this.chartJs.getDatasetMeta(index).data, 'point', enabled);
    } else if (this.chartJs.config.type === Chart.Type.PIE || this.chartJs.config.type === Chart.Type.DOUGHNUT || this.chartJs.config.type === Chart.Type.POLAR_AREA) {
      let elements = [];
      for (let i = 0; i < this.chartJs.config.data.datasets.length; i++) {
        elements.push(this.chartJs.getDatasetMeta(i).data[index]);
      }
      this.chartJs.updateHoverStyle(elements, 'point', enabled);
    } else {
      let elements = this.chartJs.getDatasetMeta(index).data;
      if (elements && elements.length) {
        this.chartJs.updateHoverStyle(this.chartJs.getDatasetMeta(index).data, 'dataset', enabled);
      }
    }
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
