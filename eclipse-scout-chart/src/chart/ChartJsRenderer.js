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
import {AbstractChartRenderer, Chart} from '../index';
import ChartJs from 'chart.js';
import {Event, styles, arrays, strings} from '@eclipse-scout/core';
// noinspection ES6UnusedImports
import chartjs_plugin_datalabels from 'chartjs-plugin-datalabels';
// noinspection ES6UnusedImports
import ChartJsTooltipDelay from './ChartJsTooltipDelay';

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
 * @property {object} [defaults.global.tooltips]
 */
ChartJs.defaults.global.maintainAspectRatio = false;
ChartJs.defaults.global.legend.labels.usePointStyle = true;
ChartJs.defaults.global.legend.labels.boxWidth = 7;
ChartJs.defaults.global.elements.line.tension = 0;
ChartJs.defaults.global.elements.line.fill = false;
ChartJs.defaults.global.elements.line.borderWidth = 2;
ChartJs.defaults.global.elements.point.radius = 0;
ChartJs.defaults.global.elements.point.hitRadius = 10;
ChartJs.defaults.global.elements.point.hoverRadius = 5;
ChartJs.defaults.global.elements.point.hoverBorderWidth = 2;
ChartJs.defaults.global.elements.arc.borderWidth = 1;
ChartJs.defaults.global.elements.rectangle.borderWidth = 1;
ChartJs.defaults.global.elements.rectangle.borderSkipped = '';
ChartJs.defaults.horizontalBar.elements.rectangle.borderSkipped = '';
ChartJs.defaults.global.tooltips.borderWidth = 1;
ChartJs.defaults.global.tooltips.cornerRadius = 4;
ChartJs.defaults.global.tooltips.xPadding = 8;
ChartJs.defaults.global.tooltips.yPadding = 8;
ChartJs.defaults.global.tooltips.titleSpacing = 4;
ChartJs.defaults.global.tooltips.titleMarginBottom = 8;
ChartJs.defaults.global.tooltips.bodySpacing = 4;

let chartJsGlobalsInitialized = false;

/**
 * @typedef Dataset
 * @property {array|string} [pointBackgroundColor]
 * @property {array|string} [pointHoverBackgroundColor]
 * @property {array|number} [pointRadius]
 *
 * @property {array|string} [uncheckedBackgroundColor]
 * @property {array|string} [uncheckedHoverBackgroundColor]
 * @property {array|string} [uncheckedPointBackgroundColor]
 * @property {array|string} [uncheckedPointHoverBackgroundColor]
 * @property {array|number} [uncheckedPointRadius]
 *
 * @property {array|string} [checkedBackgroundColor]
 * @property {array|string} [checkedHoverBackgroundColor]
 * @property {array|string} [checkedPointBackgroundColor]
 * @property {array|string} [checkedPointHoverBackgroundColor]
 * @property {array|number} [checkedPointRadius]
 *
 * @property {array|string} [legendColor]
 */

export default class ChartJsRenderer extends AbstractChartRenderer {

  static ARROW_LEFT_RIGHT = '\u2194';
  static ARROW_UP_DOWN = '\u2195';

  constructor(chart) {
    super(chart);
    this.chartJs = null;
    this.minSpaceBetweenYTicks = 35;
    this.minSpaceBetweenXTicks = 150;
    this.numSupportedColors = 6;
    this.colorSchemeCssClass = '';
    this.minRadialChartDatalabelSpace = 25;

    this.resetDatasetAfterHover = false;

    this._resizeHandler = this._onResize.bind(this);

    this._labelFormatter = this._formatLabel.bind(this);
    this._xLabelFormatter = this._formatXLabel.bind(this);
    this._yLabelFormatter = this._formatYLabel.bind(this);

    this._yAxisFitter = this._fitYAxis.bind(this);

    this._radialChartDatalabelsDisplayHandler = this._displayDatalabelsOnRadialChart.bind(this);
    this._radialChartDatalabelsFormatter = this._formatDatalabelsOnRadialChart.bind(this);
    this._datalabelsFormatter = this._formatDatalabels.bind(this);

    this._datalabelBackgroundColorHandler = this._getBackgroundColorOfDataset.bind(this);

    this._legendLabelGenerator = this._generateLegendLabels.bind(this);

    this._tooltipTitle = this._formatTooltipTitle.bind(this);
    this._tooltipLabel = this._formatTooltipLabel.bind(this);
    this._tooltipLabelColor = this._computeTooltipLabelColor.bind(this);

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

    if (chartDataValid && scout.isOneOf(this.chart.config.type, Chart.Type.POLAR_AREA, Chart.Type.RADAR)) {
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

    if (chartConfigDataValid && scout.isOneOf(this.chart.config.type, Chart.Type.POLAR_AREA, Chart.Type.RADAR)) {
      // check lengths
      let i, length = 0;
      for (i = 0; i < config.data.datasets.length; i++) {
        let dataset = config.data.datasets[i];
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
     * @property {object} options.numberFormatter
     * @property {number} options.tooltips.titleFontFamily
     * @property {object} options.scales.scaleLabelByTypeMap
     * @property {object} options.scales.xLabelMap
     * @property {object} options.scales.yLabelMap
     */
    let config = $.extend(true, {}, this.chart.config);
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
    this._adjustSize(this.chartJs.config, this.chartJs.chartArea);
    this.chartJs.update();
  }

  _updateChart(animated) {
    let config = this.chartJs.config;
    this._adjustColors(config);
    this._renderChart(config, animated);
  }

  _renderCheckedItems() {
    if (this.chartJs && this._checkItems(this.chartJs.config)) {
      this.chartJs.update();
    }
  }

  _checkItems(config) {
    if (!config || !config.data) {
      return false;
    }
    let transferArrayValues = (target, source, indices) => {
      if (Array.isArray(target) && Array.isArray(source)) {
        let changed = 0;
        arrays.ensure(indices)
          .filter(index => !isNaN(index) && index < Math.min(target.length, source.length))
          .forEach(index => {
            if (target[index] !== source[index]) {
              target[index] = source[index];
              changed++;
            }
          });
        return changed;
      }
      return 0;
    };
    let changed = 0;
    config.data.datasets.forEach((dataset, datasetIndex) => {
      let checkedIndices = this.chart.checkedItems.filter(item => item.datasetIndex === datasetIndex)
          .map(item => item.dataIndex),
        uncheckedIndices = arrays.init(dataset.data.length).map((elem, idx) => idx);
      arrays.removeAll(uncheckedIndices, checkedIndices);

      changed = changed +
        // check
        transferArrayValues(dataset.backgroundColor, dataset.checkedBackgroundColor, checkedIndices) +
        transferArrayValues(dataset.hoverBackgroundColor, dataset.checkedHoverBackgroundColor, checkedIndices) +
        transferArrayValues(dataset.pointBackgroundColor, dataset.checkedPointBackgroundColor, checkedIndices) +
        transferArrayValues(dataset.pointHoverBackgroundColor, dataset.checkedPointHoverBackgroundColor, checkedIndices) +
        transferArrayValues(dataset.pointRadius, dataset.checkedPointRadius, checkedIndices) +
        // uncheck
        transferArrayValues(dataset.backgroundColor, dataset.uncheckedBackgroundColor, uncheckedIndices) +
        transferArrayValues(dataset.hoverBackgroundColor, dataset.uncheckedHoverBackgroundColor, uncheckedIndices) +
        transferArrayValues(dataset.pointBackgroundColor, dataset.uncheckedPointBackgroundColor, uncheckedIndices) +
        transferArrayValues(dataset.pointHoverBackgroundColor, dataset.uncheckedPointHoverBackgroundColor, uncheckedIndices) +
        transferArrayValues(dataset.pointRadius, dataset.uncheckedPointRadius, uncheckedIndices);
    });

    return 0 < changed;
  }

  _adjustConfig(config) {
    if (!config || !config.type) {
      return;
    }
    this._adjustType(config);
    if (this.chart.data) {
      this._computeDatasets(this.chart.data, config);
    }
    this._adjustData(config);
    this._adjustLayout(config);
    this._adjustColors(config);
    this._adjustClickHandler(config);
  }

  _adjustType(config) {
    if (config.type === Chart.Type.COMBO_BAR_LINE) {
      config.type = Chart.Type.BAR;

      let scaleLabelByTypeMap = ((config.options || {}).scales || {}).scaleLabelByTypeMap;
      if (scaleLabelByTypeMap) {
        scaleLabelByTypeMap[Chart.Type.BAR] = scaleLabelByTypeMap[Chart.Type.COMBO_BAR_LINE];
      }
    }
  }

  _computeDatasets(chartData, config) {
    let labels = [],
      datasets = [];

    let setLabelMap = (identifier, labelMap) => {
      if (!$.isEmptyObject(labelMap)) {
        config.options = $.extend(true, {}, config.options, {
          scales: {}
        });
        config.options.scales[identifier] = labelMap;
      }
    };

    (chartData.axes[0] || []).forEach(elem => labels.push(elem.label));

    setLabelMap(config.type === Chart.Type.BAR_HORIZONTAL ? 'yLabelMap' : 'xLabelMap', this._computeLabelMap(chartData.axes[0]));
    setLabelMap(config.type === Chart.Type.BAR_HORIZONTAL ? 'xLabelMap' : 'yLabelMap', this._computeLabelMap(chartData.axes[1]));

    chartData.chartValueGroups.forEach(elem => datasets.push({
      type: elem.type,
      label: elem.groupName,
      data: $.extend(true, [], elem.values)
    }));

    /**
     * @type {object}
     * @property {Dataset[]} datasets
     */
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

    if (scout.isOneOf(config.type, Chart.Type.BAR, Chart.Type.BAR_HORIZONTAL)) {
      config.data.datasets.forEach(dataset => {
        if ((dataset.type || Chart.Type.BAR) === Chart.Type.BAR) {
          dataset.hoverBorderWidth = 2;
        }
      });
    }
    if (scout.isOneOf(config.type, Chart.Type.PIE, Chart.Type.DOUGHNUT, Chart.Type.POLAR_AREA, Chart.Type.RADAR)) {
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

    if (config.type !== Chart.Type.BUBBLE) {
      return;
    }

    config.data.datasets.forEach(dataset => dataset.data.forEach(data => {
      if (!isNaN(data.r)) {
        data.z = Math.pow(data.r, 2);
      } else if (!isNaN(data.z)) {
        data.r = Math.sqrt(data.z);
      }
    }));
  }

  _adjustLayout(config) {
    if (!config || !config.type || !config.options) {
      return;
    }

    let type = config.type;

    config.options = $.extend(true, {}, config.options, {
      hover: {
        mode: 'nearest'
      },
      tooltips: {
        mode: 'nearest',
        callbacks: {
          title: this._tooltipTitle,
          label: this._tooltipLabel,
          labelColor: this._tooltipLabelColor
        }
      }
    });
    if (config.options.handleResize) {
      config.options.onResize = this._resizeHandler;
    }
    if (scout.isOneOf(type, Chart.Type.POLAR_AREA, Chart.Type.RADAR)) {
      config.options = $.extend(true, {}, config.options, {
        scale: {}
      });
    } else if (scout.isOneOf(type, Chart.Type.BAR, Chart.Type.BAR_HORIZONTAL, Chart.Type.LINE, Chart.Type.BUBBLE)) {
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
          beginAtZero: true
        },
        pointLabels: {
          fontSize: ChartJs.defaults.global.defaultFontSize
        }
      });
      config.options.scale.ticks = $.extend(true, {}, {
        callback: this._labelFormatter
      }, config.options.scale.ticks);
    }
    for (let i = 0; i < ((config.options.scales || {}).xAxes || []).length; i++) {
      if (scout.isOneOf(type, Chart.Type.BAR_HORIZONTAL, Chart.Type.BUBBLE)) {
        config.options.scales.xAxes[i] = $.extend(true, {}, config.options.scales.xAxes[i], {
          offset: type === Chart.Type.BUBBLE,
          gridLines: {
            drawBorder: false,
            drawTicks: false,
            zeroLineBorderDash: [2, 4],
            borderDash: [2, 4]
          },
          ticks: {
            padding: 5,
            beginAtZero: type === Chart.Type.BAR_HORIZONTAL
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
      if (scout.isOneOf(type, Chart.Type.BAR_HORIZONTAL, Chart.Type.BUBBLE) || config.data.reformatLabels) {
        config.options.scales.xAxes[i] = $.extend(true, {}, {
          ticks: {
            callback: this._xLabelFormatter
          }
        }, config.options.scales.xAxes[i]);
      }
    }
    for (let i = 0; i < ((config.options.scales || {}).yAxes || []).length; i++) {
      if (type === Chart.Type.BAR_HORIZONTAL) {
        config.options.scales.yAxes[i] = $.extend(true, {}, config.options.scales.yAxes[i], {
          gridLines: {
            display: false
          }
        });
      } else {
        config.options.scales.yAxes[i] = $.extend(true, {}, config.options.scales.yAxes[i], {
          gridLines: {
            drawBorder: false,
            drawTicks: false,
            zeroLineBorderDash: [2, 4],
            borderDash: [2, 4]
          },
          ticks: {
            padding: 5,
            beginAtZero: type !== Chart.Type.BUBBLE
          }
        });
      }
      if (type !== Chart.Type.BAR_HORIZONTAL || config.data.reformatLabels) {
        config.options.scales.yAxes[i] = $.extend(true, {}, {
          ticks: {
            callback: this._yLabelFormatter
          }
        }, config.options.scales.yAxes[i]);
      }
      config.options.scales.yAxes[i].afterFit = this._yAxisFitter;
    }

    if (config.options.plugins && config.options.plugins.datalabels && config.options.plugins.datalabels.display) {
      config.options.plugins.datalabels.formatter = this._datalabelsFormatter;
      if (scout.isOneOf(config.type, Chart.Type.PIE, Chart.Type.DOUGHNUT)) {
        config.options.plugins.datalabels.display = this._radialChartDatalabelsDisplayHandler;
        config.options.plugins.datalabels.formatter = this._radialChartDatalabelsFormatter;
      } else if (scout.isOneOf(config.type, Chart.Type.BAR, Chart.Type.BAR_HORIZONTAL, Chart.Type.LINE, Chart.Type.POLAR_AREA, Chart.Type.RADAR, Chart.Type.BUBBLE)) {
        config.options.plugins.datalabels.display = 'auto';
        config.options.plugins.datalabels.backgroundColor = this._datalabelBackgroundColorHandler;
        config.options.plugins.datalabels.borderRadius = 4;
      }
    }
  }

  _onResize(chart, size) {
    chart.update();
    this._adjustSize(chart.config, chart.chartArea);
  }

  _formatLabel(label) {
    return this._formatLabelMap(label, null, ((this.chartJs || {config: {}}).config.options || {}).numberFormatter);
  }

  _formatXLabel(label) {
    return this._formatLabelMap(label, (((this.chartJs || {config: {}}).config.options || {}).scales || {}).xLabelMap, ((this.chartJs || {config: {}}).config.options || {}).numberFormatter);
  }

  _formatYLabel(label) {
    return this._formatLabelMap(label, (((this.chartJs || {config: {}}).config.options || {}).scales || {}).yLabelMap, ((this.chartJs || {config: {}}).config.options || {}).numberFormatter);
  }

  _formatLabelMap(label, labelMap, numberFormatter) {
    if (labelMap) {
      return labelMap[label];
    }
    if (isNaN(label)) {
      return label;
    }
    if (numberFormatter) {
      return numberFormatter(label, this._formatNumberLabel.bind(this));
    }
    return this._formatNumberLabel(label);
  }

  _formatNumberLabel(label) {
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

  _fitYAxis(yAxis) {
    if (yAxis && yAxis.longestLabelWidth > yAxis.maxWidth - (((yAxis.options || {}).ticks || {}).padding || 0)) {
      let horizontalSpace = yAxis.maxWidth - (((yAxis.options || {}).ticks || {}).padding || 0) - (((yAxis.options || {}).gridLines || {}).tickMarkLength || 0),
        measureText = yAxis.ctx.measureText.bind(yAxis.ctx);
      yAxis._ticks.forEach(tick => {
        tick.label = strings.truncateText(tick.label, horizontalSpace, measureText);
      });
    }
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

  _formatDatalabels(value, context) {
    if (context.chart.config.type === Chart.Type.BUBBLE) {
      return this._formatLabel(value.z);
    }
    return this._formatLabel(value);
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
      autoColor = config.options && config.options.autoColor,
      checkable = config.options && config.options.checkable;

    let multipleColorsPerDataset = autoColor && scout.isOneOf(type, Chart.Type.PIE, Chart.Type.DOUGHNUT, Chart.Type.POLAR_AREA),
      colors = {
        backgroundColors: [],
        borderColors: [],
        hoverBackgroundColors: [],
        hoverBorderColors: [],
        checkedBackgroundColors: [],
        checkedHoverBackgroundColors: [],
        legendColors: [],
        labelColor: undefined,
        labelBackdropColor: undefined,
        datalabelColor: undefined,
        axisLabelColor: undefined,
        gridColor: undefined,
        pointHoverColor: undefined,
        tooltipBackgroundColor: undefined,
        tooltipBorderColor: undefined
      };

    colors.labelColor = styles.get([this.colorSchemeCssClass, type + '-chart', 'elements', 'label'], 'fill').fill;
    colors.labelBackdropColor = styles.get([this.colorSchemeCssClass, type + '-chart', 'elements', 'label-backdrop'], 'fill').fill;
    colors.datalabelColor = styles.get([this.colorSchemeCssClass, type + '-chart', 'elements', 'datalabel'], 'fill').fill;
    colors.axisLabelColor = styles.get([this.colorSchemeCssClass, type + '-chart', 'elements', 'axis-label'], 'fill').fill;
    colors.gridColor = styles.get([this.colorSchemeCssClass, type + '-chart', 'elements', 'grid'], 'fill').fill;
    colors.pointHoverColor = styles.get([this.colorSchemeCssClass, type + '-chart', 'elements', 'point hover'], 'fill').fill;
    colors.tooltipBackgroundColor = styles.get([this.colorSchemeCssClass, type + '-chart', 'elements', 'tooltip-background'], 'fill').fill;
    colors.tooltipBorderColor = styles.get([this.colorSchemeCssClass, type + '-chart', 'elements', 'tooltip-border'], 'fill').fill;

    if (autoColor) {
      let types = [];
      if (multipleColorsPerDataset) {
        types = arrays.init((data.datasets.length && data.datasets[0].data.length) || 0, type);
      } else {
        data.datasets.forEach(dataset => types.push(dataset.type || type));
      }
      types.forEach((type, index) => {
        colors.backgroundColors.push(styles.get([this.colorSchemeCssClass, type + '-chart' + (checkable ? ' checkable' : ''), 'elements', 'color' + (index % this.numSupportedColors)], 'fill').fill);
        colors.borderColors.push(styles.get([this.colorSchemeCssClass, type + '-chart', 'elements', 'stroke-color' + (index % this.numSupportedColors)], 'stroke').stroke);
        colors.hoverBackgroundColors.push(styles.get([this.colorSchemeCssClass, type + '-chart' + (checkable ? ' checkable' : ''), 'elements', 'color' + (index % this.numSupportedColors) + ' hover'], 'fill').fill);
        colors.hoverBorderColors.push(styles.get([this.colorSchemeCssClass, type + '-chart', 'elements', 'stroke-color' + (index % this.numSupportedColors) + ' hover'], 'stroke').stroke);

        colors.checkedBackgroundColors.push(styles.get([this.colorSchemeCssClass, type + '-chart' + (checkable ? ' checkable' : ''), 'elements', 'color' + (index % this.numSupportedColors) + ' checked'], 'fill').fill);
        colors.checkedHoverBackgroundColors.push(styles.get([this.colorSchemeCssClass, type + '-chart' + (checkable ? ' checkable' : ''), 'elements', 'color' + (index % this.numSupportedColors) + ' hover checked'], 'fill').fill);

        colors.legendColors.push(styles.get([this.colorSchemeCssClass, type + '-chart', 'elements', 'color' + (index % this.numSupportedColors) + ' legend'], 'fill').fill);
      });
    } else {
      if (this.chart.data) {
        this.chart.data.chartValueGroups.forEach(elem => {
          let rgbColor = styles.hexToRgb(elem.colorHexValue),
            adjustColor = (opacity, darker) => this._adjustColorOpacity(styles.darkerColor(rgbColor, darker), opacity);

          let backgroundOpacity = 1,
            hoverBackgroundOpacity = 1,
            hoverBackgroundDarker = 0.1,
            hoverBorderDarker = 0.1,

            uncheckedBackgroundOpacity = 0.2,
            uncheckedHoverBackgroundOpacity = 0.35,

            checkedBackgroundOpacity = 1,
            checkedBackgroundDarker = 0,
            checkedHoverBackgroundOpacity = 1,
            checkedHoverBackgroundDarker = 0.1;

          if (scout.isOneOf(type, Chart.Type.PIE, Chart.Type.DOUGHNUT)) {
            uncheckedBackgroundOpacity = 0.7;
            uncheckedHoverBackgroundOpacity = 0.85;

            checkedBackgroundDarker = 0.1;
            checkedHoverBackgroundDarker = 0;
          } else if (type === Chart.Type.POLAR_AREA) {
            backgroundOpacity = 0.7;
            hoverBackgroundOpacity = 0.85;

            uncheckedBackgroundOpacity = 0.7;
            uncheckedHoverBackgroundOpacity = 0.85;

            checkedBackgroundDarker = 0.1;
            checkedHoverBackgroundDarker = 0;
          } else if (scout.isOneOf((elem.type || type), Chart.Type.LINE, Chart.Type.RADAR)) {
            backgroundOpacity = 0.2;
            hoverBackgroundOpacity = 0.35;
            hoverBackgroundDarker = 0;
            hoverBorderDarker = 0;

            checkedBackgroundOpacity = 0.2;
            checkedHoverBackgroundOpacity = 0.35;
            checkedHoverBackgroundDarker = 0;
          } else if (type === Chart.Type.BUBBLE) {
            backgroundOpacity = 0.2;
            hoverBackgroundOpacity = 0.35;
            hoverBackgroundDarker = 0;
          }

          colors.backgroundColors.push(adjustColor(checkable ? uncheckedBackgroundOpacity : backgroundOpacity, 0));
          colors.borderColors.push(adjustColor(1, 0));
          colors.hoverBackgroundColors.push(adjustColor(checkable ? uncheckedHoverBackgroundOpacity : hoverBackgroundOpacity, checkable ? 0 : hoverBackgroundDarker));
          colors.hoverBorderColors.push(adjustColor(1, hoverBorderDarker));

          colors.checkedBackgroundColors.push(adjustColor(checkedBackgroundOpacity, checkedBackgroundDarker));
          colors.checkedHoverBackgroundColors.push(adjustColor(checkedHoverBackgroundOpacity, checkedHoverBackgroundDarker));

          colors.legendColors.push(adjustColor(1, 0));
        });
      }
      if (scout.isOneOf(type, Chart.Type.PIE, Chart.Type.DOUGHNUT, Chart.Type.POLAR_AREA)) {
        let borderColor = styles.get([this.colorSchemeCssClass, type + '-chart', 'elements', 'stroke-color0'], 'stroke').stroke;
        colors.borderColors = arrays.init(data.datasets.length, borderColor);
        colors.hoverBorderColors = colors.borderColors;
      }
    }

    data.datasets.forEach((elem, idx) => {
      let backgroundColor = (multipleColorsPerDataset ? colors.backgroundColors : colors.backgroundColors[idx]),
        borderColor = (multipleColorsPerDataset ? colors.borderColors : colors.borderColors[idx]),
        hoverBackgroundColor = (multipleColorsPerDataset ? colors.hoverBackgroundColors : colors.hoverBackgroundColors[idx]),
        hoverBorderColor = (multipleColorsPerDataset ? colors.hoverBorderColors : colors.hoverBorderColors[idx]),
        legendColor = (multipleColorsPerDataset ? colors.legendColors : colors.legendColors[idx]),
        pointHoverBackgroundColor = colors.pointHoverColor;

      let setProperty = (identifier, value) => {
        if (value && value.length) {
          elem[identifier] = Array.isArray(value) ? [...value] : value;
        }
      };
      setProperty('backgroundColor', backgroundColor);
      setProperty('borderColor', borderColor);
      setProperty('hoverBackgroundColor', hoverBackgroundColor);
      setProperty('hoverBorderColor', hoverBorderColor);
      setProperty('legendColor', legendColor);
      setProperty('pointHoverBackgroundColor', pointHoverBackgroundColor);
      if (checkable) {
        let datasetLength = elem.data.length;
        if (scout.isOneOf(type, Chart.Type.PIE, Chart.Type.BAR_HORIZONTAL, Chart.Type.DOUGHNUT, Chart.Type.POLAR_AREA, Chart.Type.BUBBLE) || (type === Chart.Type.BAR && (elem.type || Chart.Type.BAR) === Chart.Type.BAR)) {
          let uncheckedBackgroundColor = (multipleColorsPerDataset ? colors.backgroundColors : arrays.init(datasetLength, colors.backgroundColors[idx])),
            uncheckedHoverBackgroundColor = (multipleColorsPerDataset ? colors.hoverBackgroundColors : arrays.init(datasetLength, colors.hoverBackgroundColors[idx])),

            checkedBackgroundColor = (multipleColorsPerDataset ? colors.checkedBackgroundColors : arrays.init(datasetLength, colors.checkedBackgroundColors[idx])),
            checkedHoverBackgroundColor = (multipleColorsPerDataset ? colors.checkedHoverBackgroundColors : arrays.init(datasetLength, colors.checkedHoverBackgroundColors[idx]));

          setProperty('uncheckedBackgroundColor', uncheckedBackgroundColor);
          setProperty('uncheckedHoverBackgroundColor', uncheckedHoverBackgroundColor);
          setProperty('checkedBackgroundColor', checkedBackgroundColor);
          setProperty('checkedHoverBackgroundColor', checkedHoverBackgroundColor);

          setProperty('backgroundColor', elem.uncheckedBackgroundColor);
          setProperty('hoverBackgroundColor', elem.uncheckedHoverBackgroundColor);
        } else if (scout.isOneOf(type, Chart.Type.LINE, Chart.Type.RADAR) || (type === Chart.Type.BAR && elem.type === Chart.Type.LINE)) {
          let uncheckedPointBackgroundColor = arrays.init(datasetLength, pointHoverBackgroundColor),
            uncheckedPointHoverBackgroundColor = arrays.init(datasetLength, pointHoverBackgroundColor),
            checkedPointBackgroundColor = arrays.init(datasetLength, borderColor),
            checkedPointHoverBackgroundColor = arrays.init(datasetLength, hoverBorderColor || borderColor);
          setProperty('uncheckedPointBackgroundColor', uncheckedPointBackgroundColor);
          setProperty('uncheckedPointHoverBackgroundColor', uncheckedPointHoverBackgroundColor);
          setProperty('checkedPointBackgroundColor', checkedPointBackgroundColor);
          setProperty('checkedPointHoverBackgroundColor', checkedPointHoverBackgroundColor);

          setProperty('pointBackgroundColor', elem.uncheckedPointBackgroundColor);
          setProperty('pointHoverBackgroundColor', elem.uncheckedPointHoverBackgroundColor);

          let uncheckedPointRadius = arrays.init(datasetLength, ((config.options.elements || {}).point || {}).radius || ChartJs.defaults.global.elements.point.radius),
            checkedPointRadius = arrays.init(datasetLength, ((config.options.elements || {}).point || {}).hoverRadius || ChartJs.defaults.global.elements.point.hoverRadius);
          setProperty('uncheckedPointRadius', uncheckedPointRadius);
          setProperty('checkedPointRadius', checkedPointRadius);

          setProperty('pointRadius', elem.uncheckedPointRadius);
        }
      }
    });
    if (checkable) {
      this._checkItems(config);
    }

    if (config.options) {
      config.options = $.extend(true, {}, config.options, {
        legend: {
          labels: {
            fontColor: colors.labelColor,
            generateLabels: this._legendLabelGenerator
          }
        },
        tooltips: {
          backgroundColor: colors.tooltipBackgroundColor,
          borderColor: colors.tooltipBorderColor,
          multiKeyBackground: colors.tooltipBackgroundColor
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
  }

  _adjustColorOpacity(color, opacity = 1) {
    if (!color) {
      return color;
    }

    if (color.indexOf('rgb') === 0) {
      return this._adjustRgbColorOpacity(color, opacity);
    }
    if (color.indexOf('#') === 0) {
      return this._adjustHexColorOpacity(color, opacity);
    }
    return color;
  }

  _adjustRgbColorOpacity(rgbColor, opacity = 1) {
    if (!rgbColor || rgbColor.indexOf('rgb') !== 0) {
      return rgbColor;
    }
    let rgba = styles.rgb(rgbColor);
    rgba.alpha = opacity;
    return 'rgba(' + rgba.red + ', ' + rgba.green + ', ' + rgba.blue + ', ' + rgba.alpha + ')';
  }

  _adjustHexColorOpacity(hexColor, opacity = 1) {
    if (!hexColor || hexColor.indexOf('#') !== 0 || !(hexColor.length === 4 || hexColor.length === 5 || hexColor.length === 7 || hexColor.length === 9)) {
      return hexColor;
    }
    return this._adjustRgbColorOpacity(styles.hexToRgb(hexColor), opacity);
  }

  _generateLegendLabels(chart) {
    let config = chart.config,
      data = config.data,
      measureText = chart.ctx.measureText.bind(chart.ctx),
      horizontalSpace;
    if (scout.isOneOf(config.options.legend.position, Chart.Position.LEFT, Chart.Position.RIGHT)) {
      horizontalSpace = Math.min(250, this.$canvas.cssWidth() / 3);
    } else {
      horizontalSpace = Math.min(250, this.$canvas.cssWidth() * 2 / 3);
    }
    let defaultGenerateLabels = (((ChartJs.defaults[config.type] || {}).legend || {}).labels || {}).generateLabels || ChartJs.defaults.global.legend.labels.generateLabels;
    let labels = defaultGenerateLabels.call(chart, chart);
    labels.forEach((elem, idx) => {
      elem.text = strings.truncateText(elem.text, horizontalSpace, measureText);
      if (scout.isOneOf(((data.datasets[idx] || {}).type || config.type), Chart.Type.LINE, Chart.Type.BAR, Chart.Type.BAR_HORIZONTAL, Chart.Type.RADAR, Chart.Type.BUBBLE)) {
        elem.fillStyle = data.datasets[idx].legendColor || this._adjustColorOpacity(data.datasets[idx].borderColor, 1);
      } else if (scout.isOneOf(config.type, Chart.Type.PIE, Chart.Type.DOUGHNUT, Chart.Type.POLAR_AREA)) {
        elem.fillStyle = (Array.isArray(data.datasets[0].legendColor) ? data.datasets[0].legendColor[idx] : data.datasets[0].legendColor) ||
          this._adjustColorOpacity(Array.isArray(data.datasets[0].backgroundColor) ? data.datasets[0].backgroundColor[idx] : data.datasets[0].backgroundColor, 1);
      }
      elem.strokeStyle = elem.fillStyle;
    });
    return labels;
  }

  _formatTooltipTitle(tooltipItems, data) {
    let config = this.chartJs.config,
      ctx = this.chartJs.ctx,
      tooltipItem = tooltipItems[0],
      title;
    if (scout.isOneOf(config.type, Chart.Type.PIE, Chart.Type.DOUGHNUT, Chart.Type.POLAR_AREA, Chart.Type.LINE, Chart.Type.BAR, Chart.Type.BAR_HORIZONTAL, Chart.Type.RADAR)) {
      title = config.data.reformatLabels ? this._formatLabel(data.labels[tooltipItem.index]) : data.labels[tooltipItem.index];
    } else if (config.type === Chart.Type.BUBBLE) {
      let xAxisLabel = config.options.scales.xAxes[0].scaleLabel.labelString,
        yAxisLabel = config.options.scales.yAxes[0].scaleLabel.labelString;
      xAxisLabel = xAxisLabel ? (xAxisLabel + ':') : ChartJsRenderer.ARROW_LEFT_RIGHT;
      yAxisLabel = yAxisLabel ? (yAxisLabel + ':') : ' ' + ChartJsRenderer.ARROW_UP_DOWN + ' ';
      title = [xAxisLabel + ' ' + config.options.scales.xAxes[0].ticks.callback(tooltipItem.xLabel),
        yAxisLabel + ' ' + config.options.scales.yAxes[0].ticks.callback(tooltipItem.yLabel)];
    } else {
      let defaultTitle = (((ChartJs.defaults[config.type] || {}).tooltips || {}).callbacks || {}).title || ChartJs.defaults.global.tooltips.callbacks.title;
      title = defaultTitle.call(this.chartJs, tooltipItems, data);
    }
    let horizontalSpace = this.$canvas.cssWidth() - (2 * config.options.tooltips.xPadding),
      measureText = ctx.measureText.bind(ctx),
      oldFont = ctx.font,
      titleFontStyle = config.options.tooltips.titleFontStyle ||
        ((ChartJs.defaults[config.type] || {}).tooltips || {}).titleFontStyle || ChartJs.defaults.global.tooltips.titleFontStyle ||
        ChartJs.defaults.global.defaultFontStyle,
      titleFontSize = config.options.tooltips.titleFontSize ||
        ((ChartJs.defaults[config.type] || {}).tooltips || {}).titleFontSize || ChartJs.defaults.global.tooltips.titleFontSize ||
        ChartJs.defaults.global.defaultFontSize,
      titleFontFamily = config.options.tooltips.titleFontFamily ||
        ((ChartJs.defaults[config.type] || {}).tooltips || {}).titleFontFamily || ChartJs.defaults.global.tooltips.titleFontFamily ||
        ChartJs.defaults.global.defaultFontFamily,
      result = [];
    ctx.font = titleFontStyle + ' ' + titleFontSize + 'px ' + titleFontFamily;
    arrays.ensure(title).forEach(titleLine => result.push(strings.truncateText(titleLine, horizontalSpace, measureText)));
    ctx.font = oldFont;
    return result;
  }

  _formatTooltipLabel(tooltipItem, data) {
    let config = this.chartJs.config,
      ctx = this.chartJs.ctx,
      dataset = ((data || {}).datasets || [])[tooltipItem.datasetIndex],
      label, value;
    if (scout.isOneOf(config.type, Chart.Type.PIE, Chart.Type.DOUGHNUT, Chart.Type.POLAR_AREA, Chart.Type.LINE, Chart.Type.BAR, Chart.Type.BAR_HORIZONTAL, Chart.Type.RADAR)) {
      label = dataset.label;
      value = this._formatLabel(dataset.data[tooltipItem.index]);
    } else if (config.type === Chart.Type.BUBBLE) {
      label = dataset.label;
      value = this._formatLabel(dataset.data[tooltipItem.index].z);
    } else {
      let defaultLabel = (((ChartJs.defaults[config.type] || {}).tooltips || {}).callbacks || {}).label || ChartJs.defaults.global.tooltips.callbacks.label;
      label = defaultLabel.call(this.chartJs, tooltipItem, data);
    }
    label = ' ' + label;
    value = value ? ' ' + value : '';
    let colorRectSize = config.options.tooltips.displayColors ? config.options.tooltips.bodyFontSize ||
      ((ChartJs.defaults[config.type] || {}).tooltips || {}).bodyFontSize || ChartJs.defaults.global.tooltips.bodyFontSize ||
      ChartJs.defaults.global.defaultFontSize : 0,
      horizontalSpace = this.$canvas.cssWidth() - (2 * config.options.tooltips.xPadding) - colorRectSize,
      measureText = ctx.measureText.bind(ctx),
      result = label + (value ? ':' + value : '');
    if (measureText(result).width > horizontalSpace) {
      if (measureText(value).width > horizontalSpace / 2) {
        return strings.truncateText(value, horizontalSpace, measureText);
      }
      return strings.truncateText(label, horizontalSpace - measureText(value ? ':' + value : '').width, measureText) + (value ? ':' + value : '');
    }
    return result;
  }

  _computeTooltipLabelColor(tooltipItem, chart) {
    let config = chart.config,
      tooltipBackgroundColor = ((config.options || {}).tooltips || {}).backgroundColor,
      dataset = ((chart.data || {}).datasets || [])[tooltipItem.datasetIndex];
    if (scout.isOneOf((dataset.type || config.type), Chart.Type.LINE, Chart.Type.BAR, Chart.Type.BAR_HORIZONTAL, Chart.Type.RADAR, Chart.Type.BUBBLE)) {
      return {
        borderColor: tooltipBackgroundColor,
        backgroundColor: dataset.legendColor || dataset.borderColor
      };
    }
    if (scout.isOneOf(config.type, Chart.Type.PIE, Chart.Type.DOUGHNUT, Chart.Type.POLAR_AREA)) {
      return {
        borderColor: tooltipBackgroundColor,
        backgroundColor: (Array.isArray(dataset.legendColor) ? dataset.legendColor[tooltipItem.index] : dataset.legendColor) ||
          this._adjustColorOpacity(Array.isArray(dataset.backgroundColor) ? dataset.backgroundColor[tooltipItem.index] : dataset.backgroundColor, 1)
      };
    }
    let defaultLabelColor = (((ChartJs.defaults[config.type] || {}).tooltips || {}).callbacks || {}).labelColor || ChartJs.defaults.global.tooltips.callbacks.labelColor;
    return defaultLabelColor.call(chart, tooltipItem, chart);
  }

  _adjustSize(config, chartArea) {
    this._adjustBubbleSizes(config, chartArea);
    this._adjustGrid(config, chartArea);
  }

  _adjustBubbleSizes(config, chartArea) {
    if (config.type !== Chart.Type.BUBBLE) {
      return;
    }
    // Scale all bubbles so that the largest radius is equal to sizeOfLargestBubble and the smallest greater than or equal to minBubbleSize.
    // First reset all radii.
    config.data.datasets.forEach(dataset => dataset.data.forEach(data => {
      if (!isNaN(data.z)) {
        data.r = Math.sqrt(data.z);
      }
    }));
    let maxMinR = this._computeMaxMinValue(config.data.datasets, 'r', true),
      maxR = maxMinR.maxValue,
      minR = maxMinR.minValue,
      // Compute a scalingFactor and an offset to get the new radius newR = r * scalingFactor + offset.
      bubbleScalingFactor = 1,
      bubbleRadiusOffset = 0;
    if ((config.bubble || {}).sizeOfLargestBubble) {
      let width = Math.abs(chartArea.right - chartArea.left),
        height = Math.abs(chartArea.top - chartArea.bottom),
        sizeOfLargestBubble = Math.min(config.bubble.sizeOfLargestBubble, Math.floor(Math.min(width, height) / 6));
      if (maxR === 0) {
        // If maxR is equal to 0, all radii are equal to 0, therefore set bubbleRadiusOffset to sizeOfLargestBubble.
        bubbleRadiusOffset = sizeOfLargestBubble;
      } else if ((config.bubble || {}).minBubbleSize && config.bubble.sizeOfLargestBubble > config.bubble.minBubbleSize && (minR / maxR) < (config.bubble.minBubbleSize / sizeOfLargestBubble)) {
        // If minR/maxR is smaller than minBubbleSize/sizeOfLargestBubble, then it is not sufficient to scale all radii.

        // The scalingFactor and the result from the following two conditions:
        // (1) minBubbleSize = offset + scalingFactor * minR
        // (2) sizeOfLargestBubble = offset + scalingFactor * maxR

        // Therefore
        // (1*) offset = minBubbleSize - scalingFactor * minR
        // (2*) offset = sizeOfLargestBubble - scalingFactor * maxR

        // (1*) = (2*):
        // minBubbleSize - scalingFactor * minR = sizeOfLargestBubble - scalingFactor * maxR
        // <=> scalingFactor * maxR - scalingFactor * minR = sizeOfLargestBubble - minBubbleSize
        // <=> scalingFactor * (maxR - minR) = sizeOfLargestBubble - minBubbleSize
        // <=> scalingFactor = (sizeOfLargestBubble - minBubbleSize) / (maxR - minR)
        bubbleScalingFactor = (sizeOfLargestBubble - config.bubble.minBubbleSize) / (maxR - minR);
        bubbleRadiusOffset = config.bubble.minBubbleSize - bubbleScalingFactor * minR;
      } else {
        // Scaling is sufficient.
        bubbleScalingFactor = sizeOfLargestBubble / maxR;
      }
    } else if ((config.bubble || {}).minBubbleSize && config.bubble.minBubbleSize > minR) {
      // sizeOfLargestBubble is not set
      if (minR === 0) {
        // If the smallest radius equals 0 scaling will have no effect.
        bubbleRadiusOffset = config.bubble.minBubbleSize;
      } else {
        // Scaling is sufficient.
        bubbleScalingFactor = config.bubble.minBubbleSize / minR;
      }
    }
    config.data.datasets.forEach(dataset => dataset.data.forEach(data => {
      data.r = data.r * bubbleScalingFactor + bubbleRadiusOffset;
    }));
  }

  _adjustGrid(config, chartArea) {
    if (!config || !config.type || !config.options || (!config.options.scale && !config.options.scales) || !chartArea) {
      return;
    }
    if (!scout.isOneOf(config.type, Chart.Type.BAR, Chart.Type.BAR_HORIZONTAL, Chart.Type.LINE, Chart.Type.POLAR_AREA, Chart.Type.RADAR, Chart.Type.BUBBLE)) {
      return;
    }

    let width = Math.abs(chartArea.right - chartArea.left),
      height = Math.abs(chartArea.top - chartArea.bottom),
      maxXTicks = Math.max(Math.floor(width / this.minSpaceBetweenXTicks), 3),
      maxYTicks = Math.max(Math.floor(height / this.minSpaceBetweenYTicks), 3),
      yBoundaries,
      yBoundaries2;

    let padding = 0;
    if (config.type === Chart.Type.BUBBLE) {
      let maxR = this._computeMaxMinValue(config.data.datasets, 'r', true);
      padding = maxR.maxValue + (((config.options.elements || {}).point || {}).hoverRadius || 0);
      if (((config.options.scales.yAxes || [])[0] || {}).offset) {
        yBoundaries = this._computeMaxMinValue(config.data.datasets, 'y', config.options.scales.yLabelMap, true);
      } else {
        yBoundaries = this._computeMaxMinValue(config.data.datasets, 'y', config.options.scales.yLabelMap, true, padding, height);
      }
      if (config.options.scales.yLabelMap) {
        yBoundaries.maxValue = Math.ceil(yBoundaries.maxValue);
        yBoundaries.minValue = Math.floor(yBoundaries.minValue);
      }
    } else {
      let datasets = [],
        datasetsDiffType = [];
      config.data.datasets.forEach(dataset => {
        if (dataset.type && dataset.type !== config.type) {
          datasetsDiffType.push(dataset);
        } else {
          datasets.push(dataset);
        }
      });

      yBoundaries = this._computeMaxMinValue(datasets);

      if (datasets.length && datasetsDiffType.length) {
        yBoundaries2 = this._computeMaxMinValue(datasetsDiffType);
        let yBoundariesRange = yBoundaries.maxValue - yBoundaries.minValue,
          yBoundariesRange2 = yBoundaries2.maxValue - yBoundaries2.minValue;
        if ((yBoundariesRange / yBoundariesRange2 > 10 || yBoundariesRange2 / yBoundariesRange > 10) && config.options.scales.yAxes.length === 1) {
          let yAxis = config.options.scales.yAxes[0],
            yAxisDiffType = $.extend(true, {}, yAxis);
          config.options.scales.yAxes.push(yAxisDiffType);

          yAxis.id = 'yAxis';
          yAxisDiffType.id = 'yAxisDiffType';

          if (config.data.datasets[0].type && config.data.datasets[0].type !== config.type) {
            yAxisDiffType.position = 'left';
            yAxis.position = 'right';
            yAxis.gridLines.drawOnChartArea = false;
          } else {
            yAxis.position = 'left';
            yAxisDiffType.position = 'right';
            yAxisDiffType.gridLines.drawOnChartArea = false;
          }

          yAxis.gridLines.drawBorder = true;
          yAxis.gridLines.drawTicks = true;
          yAxisDiffType.gridLines.drawBorder = true;
          yAxisDiffType.gridLines.drawTicks = true;

          let yAxisType = (datasets[0].type || config.type),
            yAxisDiffTypeType = (datasetsDiffType[0].type || config.type),
            yAxisTypeLabel = this.chart.session.text('ui.' + yAxisType),
            yAxisDiffTypeTypeLabel = this.chart.session.text('ui.' + yAxisDiffTypeType),
            yAxisScaleLabel = (config.options.scales.scaleLabelByTypeMap || {})[yAxisType],
            yAxisDiffTypeScaleLabel = (config.options.scales.scaleLabelByTypeMap || {})[yAxisDiffTypeType];

          yAxis.scaleLabel.display = true;
          yAxis.scaleLabel.labelString = yAxisScaleLabel ? yAxisScaleLabel + ' (' + yAxisTypeLabel + ')' : yAxisTypeLabel;
          yAxisDiffType.scaleLabel.display = true;
          yAxisDiffType.scaleLabel.labelString = yAxisDiffTypeScaleLabel ? yAxisDiffTypeScaleLabel + ' (' + yAxisDiffTypeTypeLabel + ')' : yAxisDiffTypeTypeLabel;

          datasets.forEach(dataset => {
            dataset.yAxisID = 'yAxis';
          });
          datasetsDiffType.forEach(dataset => {
            dataset.yAxisID = 'yAxisDiffType';
          });
        }
      }
    }

    if (config.options.scale) {
      config.options.scale.ticks = $.extend(true, {}, config.options.scale.ticks, {
        maxTicksLimit: Math.ceil(maxYTicks / 2)
      });
      if (yBoundaries) {
        config.options.scale.ticks.suggestedMax = yBoundaries.maxValue;
        config.options.scale.ticks.suggestedMin = yBoundaries.minValue;
      }
    } else if (yBoundaries2) {
      this._adjustAxes([config.options.scales.yAxes[0]], maxYTicks, yBoundaries);
      this._adjustAxes([config.options.scales.yAxes[1]], maxYTicks, yBoundaries2);
    } else if (config.type === Chart.Type.BAR_HORIZONTAL) {
      this._adjustAxes(config.options.scales.xAxes, maxXTicks, yBoundaries);
    } else {
      this._adjustAxes(config.options.scales.yAxes, maxYTicks, yBoundaries);
    }

    if (!(config.type === Chart.Type.BUBBLE)) {
      return;
    }

    let xBoundaries;
    if (((config.options.scales.xAxes || [])[0] || {}).offset) {
      xBoundaries = this._computeMaxMinValue(config.data.datasets, 'x', config.options.scales.xLabelMap, true);
    } else {
      xBoundaries = this._computeMaxMinValue(config.data.datasets, 'x', config.options.scales.xLabelMap, true, padding, width);
    }
    if (config.options.scales.xLabelMap) {
      xBoundaries.maxValue = Math.ceil(xBoundaries.maxValue);
      xBoundaries.minValue = Math.floor(xBoundaries.minValue);
    }
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

  _computeMaxMinValue(datasets, identifier, exact, boundRange, padding, space) {
    if (!datasets) {
      return;
    }

    let maxValue, minValue;
    for (let i = 0; i < datasets.length; i++) {
      for (let j = 0; j < datasets[i].data.length; j++) {
        let value;
        if (identifier) {
          value = datasets[i].data[j][identifier];
        } else {
          value = datasets[i].data[j];
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

    let adjust = 0,
      maxBoundary = maxValue,
      minBoundary = minValue;

    if (!exact) {
      if (boundRange && Math.sign(minValue) === Math.sign(maxValue)) {
        adjust = Math.floor(minValue);
      }
      maxBoundary = this._calculateBoundary(maxValue - adjust, Math.ceil, Math.floor);
      minBoundary = this._calculateBoundary(minValue - adjust, Math.floor, Math.ceil);
    }

    if (padding && space && space > 2 * padding) {
      let valuePerPixel = (maxValue - minValue) / (space - 2 * padding),
        paddingValue = valuePerPixel * padding;
      maxBoundary = Math.max(maxBoundary, maxValue - adjust + paddingValue);
      minBoundary = Math.min(minBoundary, minValue - adjust - paddingValue);
    }

    if (!exact) {
      return {
        maxValue: maxBoundary + adjust,
        minValue: minBoundary + adjust
      };
    }

    return {
      maxValue: maxBoundary,
      minValue: minBoundary
    };
  }

  _calculateBoundary(value, roundingFunctionPositive, roundingFunctionNegative) {
    let roundingFunction = roundingFunctionPositive;
    let changeValueSign = false;
    if (value < 0) {
      changeValueSign = true;
      value = value * (-1);
      roundingFunction = roundingFunctionNegative;
    }
    value = this._calculateBoundaryPositive(value, roundingFunction);
    if (changeValueSign) {
      value = value * (-1);
    }
    return value;
  }

  _calculateBoundaryPositive(value, roundingFunction) {
    if (!(value > 0) || !roundingFunction) {
      return value;
    }
    // example: the value 32689 should be rounded to 30000 for the roundingFunction Math.floor or 35000 for Math.ceil or Math.round

    // first calculate the exponent p of the largest 1ep smaller than the given value
    // example: the largest 1ep smaller than the value 32689 is 10000 = 1e4 and therefore p = 4
    let p = Math.floor(Math.log(value) / Math.LN10);
    // divide by 5e(p-1), round and multiply with 5e(p-1) to round the value in 5e(p-1) steps
    // example: the value is now divided by 5e(p-1) which means 32689 / 5e(4-1) = 32689 / 5e3 = 32689 / 5000 = 6.5378
    //          this result is now rounded (Math.floor gives 6, Math.ceil and Math.round gives 7) and multiplied again with 5000 which results in 30000 or 35000 respectively
    if (p < 0) {
      value = roundingFunction(value * Math.pow(10, Math.abs(p)) / 5) * 5 / Math.pow(10, Math.abs(p));
    } else {
      value = roundingFunction(value / (5 * Math.pow(10, p - 1))) * 5 * Math.pow(10, p - 1);
    }
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
          datasetIndex: datasetIndex,
          dataIndex: itemIndex
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
      e.originalEvent = event;
      this.chart._onValueClick(e);
    }
  }

  _onHover(event, items) {
    if (this.chartJs.config && this.chartJs.config.type && scout.isOneOf(this.chartJs.config.type, Chart.Type.LINE, Chart.Type.BAR, Chart.Type.BAR_HORIZONTAL, Chart.Type.RADAR)) {
      let update = false;
      if (this.resetDatasetAfterHover) {
        this._adjustColors(this.chartJs.config);
        this.resetDatasetAfterHover = false;
        update = true;
      }
      items.forEach(item => {
        let dataset = this.chartJs.config.data.datasets[item._datasetIndex];
        if (scout.isOneOf((dataset.type || this.chartJs.config.type), Chart.Type.LINE, Chart.Type.RADAR)) {
          dataset.backgroundColor = dataset.hoverBackgroundColor;
          this.resetDatasetAfterHover = true;
          update = true;
        }
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
    if (scout.isOneOf(this.chartJs.config.type, Chart.Type.PIE, Chart.Type.DOUGHNUT, Chart.Type.POLAR_AREA)) {
      index = item.index;
    }
    if (this.legendHoverDatasets.indexOf(index) < 0) {
      if (((this.chartJs.config.data.datasets[index] || {}).type || this.chartJs.config.type) === Chart.Type.LINE) {
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
    if (scout.isOneOf(this.chartJs.config.type, Chart.Type.PIE, Chart.Type.DOUGHNUT, Chart.Type.POLAR_AREA)) {
      index = item.index;
    }
    if (this.legendHoverDatasets.indexOf(index) > -1) {
      if (((this.chartJs.config.data.datasets[index] || {}).type || this.chartJs.config.type) === Chart.Type.LINE) {
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
    if (((this.chartJs.config.data.datasets[index] || {}).type || this.chartJs.config.type) === Chart.Type.LINE) {
      this.chartJs.updateHoverStyle(this.chartJs.getDatasetMeta(index).data, 'point', enabled);
    } else if (scout.isOneOf(this.chartJs.config.type, Chart.Type.PIE, Chart.Type.DOUGHNUT, Chart.Type.POLAR_AREA)) {
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
    if (scout.isOneOf(config.type, Chart.Type.PIE, Chart.Type.DOUGHNUT, Chart.Type.POLAR_AREA, Chart.Type.RADAR)) {
      if (config.options.otherSegmentClickable) {
        return false;
      }
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
