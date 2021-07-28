/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
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
import {arrays, colorSchemes, Event, numbers, objects, strings, styles} from '@eclipse-scout/core';
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
$.extend(true, ChartJs.defaults, {
  global: {
    maintainAspectRatio: false,
    legend: {
      labels: {
        usePointStyle: true,
        boxWidth: 7
      }
    },
    elements: {
      line: {
        tension: 0,
        fill: false,
        borderWidth: 2
      },
      point: {
        radius: 0,
        hitRadius: 10,
        hoverRadius: 7,
        borderWidth: 1,
        hoverBorderWidth: 2
      },
      arc: {
        borderWidth: 1
      },
      rectangle: {
        borderWidth: 1,
        borderSkipped: ''
      }
    },
    tooltips: {
      borderWidth: 1,
      cornerRadius: 4,
      xPadding: 8,
      yPadding: 8,
      titleSpacing: 4,
      titleMarginBottom: 8,
      bodySpacing: 4
    }
  },
  line: {
    elements: {
      point: {
        borderWidth: 2
      }
    }
  },
  horizontalBar: {
    elements: {
      rectangle: {
        borderSkipped: ''
      }
    }
  }
});

let chartJsGlobalsInitialized = false;

/**
 * @typedef Dataset
 * @property {string} [datasetId]
 *
 * @property {array|string} [backgroundColor]
 * @property {array|string} [backgroundColorBackup]
 * @property {array|string} [hoverBackgroundColor]
 *
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
    this.onlyIntegers = true;
    this.maxXAxesTicksHeigth = 75;
    this.numSupportedColors = 6;
    this.colorSchemeCssClass = '';
    this.minRadialChartDatalabelSpace = 25;

    this._tooltipTitle = this._formatTooltipTitle.bind(this);
    this._tooltipLabel = this._formatTooltipLabel.bind(this);
    this._tooltipLabelColor = this._computeTooltipLabelColor.bind(this);

    this._labelFormatter = this._formatLabel.bind(this);
    this._xLabelFormatter = this._formatXLabel.bind(this);
    this._yLabelFormatter = this._formatYLabel.bind(this);

    this._xAxisFitter = this._fitXAxis.bind(this);
    this._yAxisFitter = this._fitYAxis.bind(this);

    this._radialChartDatalabelsDisplayHandler = this._displayDatalabelsOnRadialChart.bind(this);
    this._radialChartDatalabelsFormatter = this._formatDatalabelsOnRadialChart.bind(this);
    this._datalabelsFormatter = this._formatDatalabels.bind(this);
    this._datalabelBackgroundColorHandler = this._getBackgroundColorOfDataset.bind(this);

    this._legendLabelGenerator = this._generateLegendLabels.bind(this);

    this.resetDatasetAfterHover = false;

    this._clickHandler = this._onClick.bind(this);
    this._hoverHandler = this._onHover.bind(this);
    this._pointerHoverHandler = this._onHoverPointer.bind(this);

    this.legendHoverDatasets = [];

    this._legendClickHandler = this._onLegendClick.bind(this);
    this._legendHoverHandler = this._onLegendHover.bind(this);
    this._legendPointerHoverHandler = this._onLegendHoverPointer.bind(this);
    this._legendLeaveHandler = this._onLegendLeave.bind(this);
    this._legendPointerLeaveHandler = this._onLegendLeavePointer.bind(this);

    this._resizeHandler = this._onResize.bind(this);
  }

  _validateChartData() {
    let chartDataValid = true;
    let chartData = this.chart && this.chart.data;

    if (!chartData || !chartData.chartValueGroups || chartData.chartValueGroups.length === 0 || !chartData.axes) {
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
    this.firstOpaqueBackgroundColor = styles.getFirstOpaqueBackgroundColor(this.$canvas);
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
    }, config);
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

  _updateData() {
    if (!this.chartJs) {
      return;
    }
    let config = $.extend(true, {}, this.chart.config);
    this._adjustConfig(config);

    let hiddenDataIndices = [];

    let targetData = this.chartJs.config.data,
      sourceData = config.data;

    // Transfer property from source object to target object:
    // 1. If the property is not set on the target object, copy it from source.
    // 2. If the property is not set on the source object, set it to undefined if setToUndefined = true. Otherwise empty the array if it is an array property or set it to undefined.
    // 3. If the property is not an array on the source or the target object, copy the property from the source to the target object.
    // 4. If the property is an array on both objects, do not update the array, but transfer the elements (update elements directly, use pop(), push() or splice() if one array is longer than the other).
    let transferProperty = (source, target, property, setToUndefined) => {
      if (!source || !target || !property) {
        return;
      }
      // 1. Property not set on target
      if (!target[property]) {
        target[property] = source[property];
        return;
      }
      // 2. Property not set on source
      if (!source[property]) {
        if (setToUndefined) {
          // Set to undefined if setToUndefined = true
          target[property] = undefined;
          return;
        }
        // Empty array
        if (Array.isArray(target[property])) {
          target[property].splice(0);
          return;
        }
        // Otherwise set to undefined
        target[property] = undefined;
        return;
      }
      // 3. Property is not an array on the source or the target object
      if (!Array.isArray(source[property]) || !Array.isArray(target[property])) {
        target[property] = source[property];
        return;
      }
      // 4. Property is an array on the source and the target object
      for (let i = 0; i < Math.min(source[property].length, target[property].length); i++) {
        // Update elements directly
        target[property][i] = source[property][i];
      }
      let targetLength = target[property].length,
        sourceLength = source[property].length;
      if (targetLength > sourceLength) {
        // Target array is longer than source array
        target[property].splice(sourceLength);
      } else if (targetLength < sourceLength) {
        // Source array is longer than target array
        target[property].push(...source[property].splice(targetLength));
      }
    };

    let findDataset = (datasets, datasetId) => arrays.find(datasets, dataset => dataset.datasetId === datasetId);
    let findDatasetIndex = (datasets, datasetId) => arrays.findIndex(datasets, dataset => dataset.datasetId === datasetId);

    if (targetData && sourceData) {
      // Transfer properties from source to target, instead of overwriting the whole data object.
      // This needs to be done to have a smooth animation from the old to the new state and not a complete rebuild of the chart.
      transferProperty(sourceData, targetData, 'labels');

      if (!targetData.datasets) {
        targetData.datasets = [];
      }
      if (!sourceData.datasets) {
        sourceData.datasets = [];
      }

      // If the chart is a pie-, doughnut- or polar-area-chart, not complete datasets are hidden but elements of each dataset.
      // Store these hidden data indices to apply them to newly added datasets.
      if (scout.isOneOf(config.type, Chart.Type.PIE, Chart.Type.DOUGHNUT, Chart.Type.POLAR_AREA) && targetData.datasets.length) {
        let meta = this.chartJs.getDatasetMeta(0);
        if (meta && meta.data && Array.isArray(meta.data)) {
          meta.data.forEach((dataMeta, idx) => {
            if (dataMeta.hidden) {
              hiddenDataIndices.push(idx);
            }
          });
        }
      }

      // if all datasets have no id set, add artificial dataset ids
      if (sourceData.datasets.every(dataset => objects.isNullOrUndefined(dataset.datasetId))) {
        sourceData.datasets.forEach((dataset, idx) => {
          dataset.datasetId = '' + idx;
        });
        targetData.datasets.forEach((dataset, idx) => {
          dataset.datasetId = '' + idx;
        });
      }

      // update existing datasets
      // Important: Update existing datasets first, before removing obsolete datasets
      // (the dataset object has listeners from Chart.js, which do not work well on a partially updated chart (updated datasets, but not yet updated chart)
      targetData.datasets.forEach(targetDataset => {
        let sourceDataset = findDataset(sourceData.datasets, targetDataset.datasetId);

        if (sourceDataset) {
          targetDataset.label = sourceDataset.label;
          targetDataset.type = sourceDataset.type;

          transferProperty(sourceDataset, targetDataset, 'data');

          transferProperty(sourceDataset, targetDataset, 'backgroundColor', true);
          transferProperty(sourceDataset, targetDataset, 'borderColor', true);
          transferProperty(sourceDataset, targetDataset, 'hoverBackgroundColor', true);
          transferProperty(sourceDataset, targetDataset, 'hoverBorderColor', true);
          transferProperty(sourceDataset, targetDataset, 'legendColor', true);
          transferProperty(sourceDataset, targetDataset, 'pointHoverBackgroundColor', true);
          transferProperty(sourceDataset, targetDataset, 'lineTension', true);
        }
      });

      // remove deleted datasets, loop backwards to not compromise the loop when modifying the array
      // datasets without an id get deleted anyway (replaced in every update, because a correct identification is not possible)
      for (let i = targetData.datasets.length - 1; i >= 0; i--) {
        let datasetId = targetData.datasets[i].datasetId;
        let deleted = objects.isNullOrUndefined(datasetId) || findDatasetIndex(sourceData.datasets, datasetId) === -1;
        if (deleted) {
          targetData.datasets.splice(i, 1);
        }
      }

      // sort existing, updated datasets
      targetData.datasets.sort((a, b) => {
        return findDatasetIndex(sourceData.datasets, a.datasetId) - findDatasetIndex(sourceData.datasets, b.datasetId);
      });

      // add all new datasets
      sourceData.datasets.forEach((sourceDataset, idx) => {
        let targetDataset = targetData.datasets[idx];
        // exclude datasets without an id here, to ensure that multiple datasets without an id do not overwrite each other
        if (targetDataset && targetDataset.datasetId && sourceDataset.datasetId === targetDataset.datasetId) {
          return;
        }
        targetData.datasets.splice(idx, 0, sourceDataset);
      });
    } else {
      this.chartJs.config.data = sourceData;
    }

    // update label maps for scales (the label maps, despite being part of the config, can be updated, without redrawing the whole chart)
    transferProperty(config.options.scales, this.chartJs.config.options.scales, 'xLabelMap', true);
    transferProperty(config.options.scales, this.chartJs.config.options.scales, 'yLabelMap', true);

    $.extend(true, this.chartJs.config, {
      options: {
        animation: {
          duration: this.animationDuration
        }
      }
    });
    this.chartJs.update();

    // Apply hidden data indices (only set for pie-, doughnut- or polar-area-chart)
    if (hiddenDataIndices.length) {
      targetData.datasets.forEach((dataset, datasetIndex) => {
        let meta = this.chartJs.getDatasetMeta(datasetIndex);
        if (meta && meta.data && Array.isArray(meta.data)) {
          hiddenDataIndices
            .filter(dataIndex => meta.data.length > dataIndex)
            .forEach(dataIndex => {
              meta.data[dataIndex].hidden = true;
            });
        }
      });
    }

    this._adjustSize(this.chartJs.config, this.chartJs.chartArea);
    this.chartJs.update();
  }

  isDataUpdatable() {
    return true;
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

  stopAnimations() {
    if (this.chartJs) {
      this.chartJs.stop();
    }
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
    this._adjustTooltip(config);
    this._adjustGrid(config);
    this._adjustPlugins(config);
    this._adjustColors(config);
    this._adjustClickHandler(config);
    this._adjustResizeHandler(config);
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
        config.options = $.extend(true, {}, {
          scales: {}
        }, config.options);
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

    this._adjustBarBorderWidth(config);
    this._adjustMaxSegments(config);
    this._adjustBubbleRadii(config);
    this._adjustOnlyIntegers(config);
  }

  _adjustBarBorderWidth(config) {
    if (!config || !config.data || !config.type || !scout.isOneOf(config.type, Chart.Type.BAR, Chart.Type.BAR_HORIZONTAL)) {
      return;
    }

    config.data.datasets.forEach(dataset => {
      if ((dataset.type || Chart.Type.BAR) === Chart.Type.BAR) {
        dataset.hoverBorderWidth = dataset.hoverBorderWidth || 2;
      }
    });
  }

  _adjustMaxSegments(config) {
    if (!config || !config.data || !config.type || !scout.isOneOf(config.type, Chart.Type.PIE, Chart.Type.DOUGHNUT, Chart.Type.POLAR_AREA, Chart.Type.RADAR)) {
      return;
    }

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

  _isMaxSegmentsExceeded(config, index) {
    if (!scout.isOneOf(config.type, Chart.Type.PIE, Chart.Type.DOUGHNUT, Chart.Type.POLAR_AREA, Chart.Type.RADAR)) {
      return false;
    }
    if (config.options.otherSegmentClickable) {
      return false;
    }
    if (!config.data.maxSegmentsExceeded || !config.options.maxSegments) {
      return false;
    }
    return config.options.maxSegments - 1 <= index;
  }

  _adjustBubbleRadii(config) {
    if (!config || !config.data || !config.type || config.type !== Chart.Type.BUBBLE) {
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

  _adjustOnlyIntegers(config) {
    this.onlyIntegers = true;

    if (!config || !config.data || !config.type) {
      return;
    }

    if (config.type === Chart.Type.BUBBLE) {
      this.onlyIntegers = config.data.datasets.every(dataset => dataset.data.every(data => numbers.isInteger(data.x) && numbers.isInteger(data.y)));
    } else {
      this.onlyIntegers = config.data.datasets.every(dataset => dataset.data.every(data => numbers.isInteger(data)));
    }
  }

  _adjustTooltip(config) {
    if (!config) {
      return;
    }

    config.options = $.extend(true, {}, {
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
    }, config.options);
  }

  _formatTooltipTitle(tooltipItems, data) {
    let config = this.chartJs.config,
      ctx = this.chartJs.ctx,
      tooltipItem = tooltipItems[0],
      title,
      defaultGlobal = ChartJs.defaults.global,
      defaultTypeTooltips = {},
      defaultGlobalTooltips = defaultGlobal.tooltips;
    if (ChartJs.defaults[config.type]) {
      defaultTypeTooltips = $.extend(true, {}, defaultTypeTooltips, ChartJs.defaults[config.type].tooltips);
    }
    if (scout.isOneOf(config.type, Chart.Type.PIE, Chart.Type.DOUGHNUT, Chart.Type.POLAR_AREA, Chart.Type.LINE, Chart.Type.BAR, Chart.Type.BAR_HORIZONTAL, Chart.Type.RADAR)) {
      let label = data.labels[tooltipItem.index];
      title = config.options.reformatLabels ? this._formatLabel(label) : label;
    } else if (config.type === Chart.Type.BUBBLE) {
      let xAxis = config.options.scales.xAxes[0],
        yAxis = config.options.scales.yAxes[0],
        xAxisLabel = xAxis.scaleLabel.labelString,
        yAxisLabel = yAxis.scaleLabel.labelString;
      xAxisLabel = xAxisLabel ? (xAxisLabel + ':') : ChartJsRenderer.ARROW_LEFT_RIGHT;
      yAxisLabel = yAxisLabel ? (yAxisLabel + ':') : ' ' + ChartJsRenderer.ARROW_UP_DOWN + ' ';
      title = [];
      let xTickLabel = xAxis.ticks.callback(tooltipItem.xLabel);
      if (xTickLabel) {
        title.push(xAxisLabel + ' ' + xTickLabel);
      }
      let yTickLabel = yAxis.ticks.callback(tooltipItem.yLabel);
      if (yTickLabel) {
        title.push(yAxisLabel + ' ' + yTickLabel);
      }
    } else {
      let defaultTypeTooltipTitle;
      if (defaultTypeTooltips.callbacks) {
        defaultTypeTooltipTitle = defaultTypeTooltips.callbacks.title;
      }
      let defaultTooltipTitle = defaultTypeTooltipTitle || defaultGlobalTooltips.callbacks.title;
      title = defaultTooltipTitle.call(this.chartJs, tooltipItems, data);
    }
    let horizontalSpace = this.$canvas.cssWidth() - (2 * config.options.tooltips.xPadding),
      measureText = ctx.measureText.bind(ctx),
      oldFont = ctx.font,
      titleFontStyle = config.options.tooltips.titleFontStyle || defaultTypeTooltips.titleFontStyle || defaultGlobalTooltips.titleFontStyle || defaultGlobal.defaultFontStyle,
      titleFontSize = config.options.tooltips.titleFontSize || defaultTypeTooltips.titleFontSize || defaultGlobalTooltips.titleFontSize || defaultGlobal.defaultFontSize,
      titleFontFamily = config.options.tooltips.titleFontFamily || defaultTypeTooltips.titleFontFamily || defaultGlobalTooltips.titleFontFamily || defaultGlobal.defaultFontFamily,
      result = [];
    ctx.font = titleFontStyle + ' ' + titleFontSize + 'px ' + titleFontFamily;
    arrays.ensure(title).forEach(titleLine => result.push(strings.truncateText(titleLine, horizontalSpace, measureText)));
    ctx.font = oldFont;
    return result;
  }

  _formatTooltipLabel(tooltipItem, data) {
    let config = this.chartJs.config,
      ctx = this.chartJs.ctx,
      datasets = data ? data.datasets : null,
      dataset = datasets ? datasets[tooltipItem.datasetIndex] : null,
      label, value,
      defaultGlobal = ChartJs.defaults.global,
      defaultTypeTooltips = {},
      defaultGlobalTooltips = defaultGlobal.tooltips;
    if (ChartJs.defaults[config.type]) {
      defaultTypeTooltips = $.extend(true, {}, defaultTypeTooltips, ChartJs.defaults[config.type].tooltips);
    }
    if (scout.isOneOf(config.type, Chart.Type.PIE, Chart.Type.DOUGHNUT, Chart.Type.POLAR_AREA, Chart.Type.LINE, Chart.Type.BAR, Chart.Type.BAR_HORIZONTAL, Chart.Type.RADAR)) {
      label = dataset.label;
      value = this._formatLabel(dataset.data[tooltipItem.index]);
    } else if (config.type === Chart.Type.BUBBLE) {
      label = dataset.label;
      value = this._formatLabel(dataset.data[tooltipItem.index].z);
    } else {
      let defaultTypeTooltipLabel;
      if (defaultTypeTooltips.callbacks) {
        defaultTypeTooltipLabel = defaultTypeTooltips.callbacks.label;
      }
      let defaultTooltipLabel = defaultTypeTooltipLabel || defaultGlobalTooltips.callbacks.label;
      label = defaultTooltipLabel.call(this.chartJs, tooltipItem, data);
    }
    label = ' ' + label;
    value = value ? ' ' + value : '';
    let colorRectSize = config.options.tooltips.displayColors ? config.options.tooltips.bodyFontSize || defaultTypeTooltips.bodyFontSize || defaultGlobalTooltips.bodyFontSize || defaultGlobal.defaultFontSize : 0,
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
      tooltips = config.options ? config.options.tooltips : null,
      tooltipBackgroundColor = tooltips ? tooltips.backgroundColor : null,
      datasets = chart.data ? chart.data.datasets : null,
      dataset = datasets ? datasets[tooltipItem.datasetIndex] : null,
      backgroundColor;
    if (scout.isOneOf((dataset.type || config.type), Chart.Type.LINE, Chart.Type.BAR, Chart.Type.BAR_HORIZONTAL, Chart.Type.RADAR, Chart.Type.BUBBLE)) {
      backgroundColor = dataset.legendColor || dataset.borderColor;
    }
    if (scout.isOneOf(config.type, Chart.Type.PIE, Chart.Type.DOUGHNUT, Chart.Type.POLAR_AREA)) {
      let legendColor = Array.isArray(dataset.legendColor) ? dataset.legendColor[tooltipItem.index] : dataset.legendColor,
        datasetBackgroundColor = Array.isArray(dataset.backgroundColor) ? dataset.backgroundColor[tooltipItem.index] : dataset.backgroundColor;
      backgroundColor = legendColor || this._adjustColorOpacity(datasetBackgroundColor, 1);
    }
    if (!backgroundColor || typeof backgroundColor === 'function') {
      let defaultTypeTooltipLabelColor;
      if (ChartJs.defaults[config.type] && ChartJs.defaults[config.type].callbacks) {
        defaultTypeTooltipLabelColor = ChartJs.defaults[config.type].callbacks.labelColor;
      }
      let defaultTooltipLabelColor = defaultTypeTooltipLabelColor || ChartJs.defaults.global.tooltips.callbacks.labelColor;
      backgroundColor = defaultTooltipLabelColor.call(chart, tooltipItem, chart).backgroundColor;
    }
    return {
      borderColor: tooltipBackgroundColor,
      backgroundColor: backgroundColor
    };
  }

  _adjustGrid(config) {
    if (!config) {
      return;
    }

    config.options = $.extend(true, {}, config.options);

    this._adjustScale(config);
    this._adjustScales(config);
  }

  _adjustScale(config) {
    if (!config || !config.type || !config.options) {
      return;
    }

    if (scout.isOneOf(config.type, Chart.Type.POLAR_AREA, Chart.Type.RADAR)) {
      config.options = $.extend(true, {}, {
        scale: {}
      }, config.options);
    }

    let options = config.options;
    if (options.scale) {
      options.scale = $.extend(true, {}, {
        minSpaceBetweenTicks: 35,
        angleLines: {
          display: false
        },
        ticks: {
          beginAtZero: true,
          callback: this._labelFormatter
        },
        pointLabels: {
          fontSize: ChartJs.defaults.global.defaultFontSize
        }
      }, options.scale);
    }
  }

  _adjustScales(config) {
    if (!config || !config.type || !config.options) {
      return;
    }

    if (scout.isOneOf(config.type, Chart.Type.BAR, Chart.Type.BAR_HORIZONTAL, Chart.Type.LINE, Chart.Type.BUBBLE)) {
      config.options = $.extend(true, {}, {
        scales: {
          minSpaceBetweenXTicks: 150,
          minSpaceBetweenYTicks: 35,
          xAxes: [{}],
          yAxes: [{}]
        }
      }, config.options);
    }

    this._adjustXAxes(config);
    this._adjustYAxes(config);
  }

  _adjustXAxes(config) {
    if (!config || !config.type || !config.options || !config.options.scales || !config.options.scales.xAxes) {
      return;
    }

    let type = config.type,
      xAxes = config.options.scales.xAxes;

    for (let i = 0; i < xAxes.length; i++) {
      if (scout.isOneOf(type, Chart.Type.BAR_HORIZONTAL, Chart.Type.BUBBLE)) {
        xAxes[i] = $.extend(true, {}, {
          offset: type === Chart.Type.BUBBLE,
          gridLines: {
            drawBorder: false,
            drawTicks: false
          },
          ticks: {
            padding: 5,
            beginAtZero: type === Chart.Type.BAR_HORIZONTAL
          }
        }, xAxes[i]);
      } else {
        xAxes[i] = $.extend(true, {}, {
          offset: true,
          gridLines: {
            display: false
          }
        }, xAxes[i]);
      }
      if (scout.isOneOf(type, Chart.Type.BAR_HORIZONTAL, Chart.Type.BUBBLE) || config.options.reformatLabels) {
        xAxes[i] = $.extend(true, {}, {
          ticks: {
            callback: this._xLabelFormatter
          }
        }, xAxes[i]);
      }
      xAxes[i].afterCalculateTickRotation = this._xAxisFitter;
    }
  }

  _adjustYAxes(config) {
    if (!config || !config.type || !config.options || !config.options.scales || !config.options.scales.yAxes) {
      return;
    }

    let type = config.type,
      yAxes = config.options.scales.yAxes;

    for (let i = 0; i < yAxes.length; i++) {
      if (type === Chart.Type.BAR_HORIZONTAL) {
        yAxes[i] = $.extend(true, {}, {
          gridLines: {
            display: false
          }
        }, yAxes[i]);
      } else {
        yAxes[i] = $.extend(true, {}, {
          gridLines: {
            drawBorder: false,
            drawTicks: false
          },
          ticks: {
            padding: 5,
            beginAtZero: type !== Chart.Type.BUBBLE
          }
        }, yAxes[i]);
      }
      if (type !== Chart.Type.BAR_HORIZONTAL || config.options.reformatLabels) {
        yAxes[i] = $.extend(true, {}, {
          ticks: {
            callback: this._yLabelFormatter
          }
        }, yAxes[i]);
      }
      yAxes[i].afterFit = this._yAxisFitter;
    }
  }

  _adjustPlugins(config) {
    this._adjustPluginsDatalabels(config);
  }

  _adjustPluginsDatalabels(config) {
    if (!config || !config.type || !config.options || !config.options.plugins || !config.options.plugins.datalabels || !config.options.plugins.datalabels.display) {
      return;
    }

    let plugins = config.options.plugins;
    if (scout.isOneOf(config.type, Chart.Type.PIE, Chart.Type.DOUGHNUT)) {
      plugins.datalabels = $.extend(true, {}, {
        formatter: this._radialChartDatalabelsFormatter
      }, plugins.datalabels);
      plugins.datalabels.display = this._radialChartDatalabelsDisplayHandler;
    } else if (scout.isOneOf(config.type, Chart.Type.BAR, Chart.Type.BAR_HORIZONTAL, Chart.Type.LINE, Chart.Type.POLAR_AREA, Chart.Type.RADAR, Chart.Type.BUBBLE)) {
      plugins.datalabels = $.extend(true, {}, {
        backgroundColor: this._datalabelBackgroundColorHandler,
        borderRadius: 4
      }, plugins.datalabels);
      plugins.datalabels.display = 'auto';
    }
    if (config.options.reformatLabels) {
      let handleFormatter = formatter => {
        return (value, context) => {
          let label = formatter.call(context.chart, value, context);
          return this._formatLabel(label);
        };
      };

      if (config.data) {
        let datasets = config.data.datasets;
        datasets.forEach(dataset => {
          if (dataset.datalabels && dataset.datalabels.formatter) {
            dataset.datalabels.formatter = handleFormatter(dataset.datalabels.formatter);
          }
        });
      }
      if (plugins.datalabels.formatter) {
        plugins.datalabels.formatter = handleFormatter(plugins.datalabels.formatter);
      }
    }

    plugins.datalabels = $.extend(true, {}, {
      formatter: this._datalabelsFormatter
    }, plugins.datalabels);
  }

  _formatLabel(label) {
    return this._formatLabelMap(label, null, this._getNumberFormatter());
  }

  _getNumberFormatter() {
    if (this.chartJs && this.chartJs.config && this.chartJs.config.options) {
      return this.chartJs.config.options.numberFormatter;
    }
  }

  _formatXLabel(label) {
    return this._formatLabelMap(label, this._getXLabelMap(), this._getNumberFormatter());
  }

  _formatYLabel(label) {
    return this._formatLabelMap(label, this._getYLabelMap(), this._getNumberFormatter());
  }

  _getXLabelMap() {
    return this._getLabelMap('xLabelMap');
  }

  _getYLabelMap() {
    return this._getLabelMap('yLabelMap');
  }

  _getLabelMap(identifier) {
    if (this.chartJs && this.chartJs.config && this.chartJs.config.options && this.chartJs.config.options.scales) {
      return this.chartJs.config.options.scales[identifier];
    }
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

  _fitXAxis(xAxis) {
    if (!xAxis || xAxis.labelRotation === 0) {
      return;
    }
    let maxHeight = this.maxXAxesTicksHeigth,
      defaultGlobal = ChartJs.defaults.global,
      defaultTicks = ChartJs.defaults.scale.ticks,
      fontSize,
      maxRotation;
    if (this.chartJs) {
      let chartArea = this.chartJs.chartArea,
        chartAreaHeight = Math.abs(chartArea.top - chartArea.bottom);
      maxHeight = Math.min(maxHeight, chartAreaHeight / 3);
    }
    if (xAxis.options && xAxis.options.ticks) {
      maxRotation = xAxis.options.ticks.maxRotation;
      fontSize = xAxis.options.ticks.fontSize;
    }
    maxRotation = maxRotation || defaultTicks.maxRotation;
    fontSize = fontSize || defaultTicks.fontSize || defaultGlobal.defaultFontSize;
    // if the chart is very narrow, chart.js sometimes calculates with a negative width of the canvas
    // this causes NaN for labelRotation and height
    if (isNaN(xAxis.labelRotation)) {
      xAxis.labelRotation = maxRotation;
    }
    xAxis.height = isNaN(xAxis.height) ? maxHeight : Math.min(xAxis.height, maxHeight);
    // the rotation (degrees), needs to be transformed to radians ((labelRotation / 180) * pi)
    let labelRotation = xAxis.labelRotation,
      // the label is a rectangle (labelWidth x fontSize) which is rotated
      // => height = sin(labelRotation) * labelLength + sin(90° - labelRotation) * fontSize
      // <=> labelLength = (height - sin(90° - labelRotation) * fontSize) / sin(labelRotation)
      maxLabelLength = (maxHeight - (fontSize * Math.sin(((90 - labelRotation) / 180) * Math.PI))) / Math.sin((labelRotation / 180) * Math.PI);
    if (xAxis.longestLabelWidth > maxLabelLength) {
      let measureText = xAxis.ctx.measureText.bind(xAxis.ctx);
      xAxis._ticks.forEach(tick => {
        tick.label = strings.truncateText(tick.label, maxLabelLength, measureText);
      });
      // reset label sizes, chart.js will recalculate them using the new truncated labels
      xAxis._labelSizes = null;
    }
  }

  _fitYAxis(yAxis) {
    if (!yAxis) {
      return;
    }
    let padding = 0,
      tickMarkLength = 0;
    if (yAxis.options && yAxis.options.ticks) {
      padding = yAxis.options.ticks.padding || 0;
    }
    if (yAxis.options && yAxis.options.gridLines) {
      tickMarkLength = yAxis.options.gridLines.tickMarkLength || 0;
    }
    if (yAxis.longestLabelWidth > yAxis.maxWidth - padding) {
      let horizontalSpace = yAxis.maxWidth - padding - tickMarkLength,
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

  _formatDatalabels(value, context) {
    if (context.chart.config.type === Chart.Type.BUBBLE) {
      return this._formatLabel(value.z);
    }
    return this._formatLabel(value);
  }

  _getBackgroundColorOfDataset(context) {
    return context.dataset.backgroundColor;
  }

  _adjustColors(config) {
    this._adjustColorSchemeCssClass(config);
    this._adjustDatasetColors(config);
    this._adjustLegendColors(config);
    this._adjustTooltipColors(config);
    this._adjustScaleColors(config);
    this._adjustScalesColors(config);
    this._adjustPluginColors(config);
  }

  _adjustColorSchemeCssClass(config) {
    if (!config || !config.options) {
      return;
    }
    this.colorSchemeCssClass = colorSchemes.getCssClasses(config.options.colorScheme).join(' ');
  }

  _adjustDatasetColors(config) {
    if (!config || !config.data || !config.type) {
      return;
    }

    let data = config.data,
      type = config.type,
      autoColor = config.options && config.options.autoColor,
      checkable = config.options && config.options.checkable,
      multipleColorsPerDataset = autoColor && scout.isOneOf(type, Chart.Type.PIE, Chart.Type.DOUGHNUT, Chart.Type.POLAR_AREA),
      colors = {
        backgroundColors: [],
        borderColors: [],
        hoverBackgroundColors: [],
        hoverBorderColors: [],
        checkedBackgroundColors: [],
        checkedHoverBackgroundColors: [],
        legendColors: [],
        pointHoverColors: []
      };

    colors = $.extend(true, colors, this._computeDatasetColors(config, multipleColorsPerDataset));

    data.datasets.forEach((elem, idx) => {
      let backgroundColor = (multipleColorsPerDataset ? colors.backgroundColors : colors.backgroundColors[idx]),
        borderColor = (multipleColorsPerDataset ? colors.borderColors : colors.borderColors[idx]),
        hoverBackgroundColor = (multipleColorsPerDataset ? colors.hoverBackgroundColors : colors.hoverBackgroundColors[idx]),
        hoverBorderColor = (multipleColorsPerDataset ? colors.hoverBorderColors : colors.hoverBorderColors[idx]),
        legendColor = (multipleColorsPerDataset ? colors.legendColors : colors.legendColors[idx]),
        pointHoverBackgroundColor = (multipleColorsPerDataset ? colors.pointHoverColors : colors.legendColors[idx]);

      let setProperty = (identifier, value) => {
        if (typeof elem[identifier] === 'function') {
          return;
        }
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
      setProperty('pointBorderColor', this.firstOpaqueBackgroundColor);
      setProperty('pointHoverBorderColor', this.firstOpaqueBackgroundColor);
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
            checkedPointRadius = arrays.init(datasetLength, (((config.options.elements || {}).point || {}).hoverRadius || ChartJs.defaults.global.elements.point.hoverRadius) - 1);
          setProperty('uncheckedPointRadius', uncheckedPointRadius);
          setProperty('checkedPointRadius', checkedPointRadius);

          setProperty('pointRadius', elem.uncheckedPointRadius);
        }
      }
    });
    if (checkable) {
      this._checkItems(config);
    }
  }

  _computeDatasetColors(config, multipleColorsPerDataset) {
    if (!config || !config.data || !config.type) {
      return {};
    }

    let data = config.data,
      type = config.type,
      colors = {};

    if (config.options && config.options.autoColor) {
      colors = this._computeDatasetColorsAutoColor(config, multipleColorsPerDataset);
    } else {
      colors = this._computeDatasetColorsChartValueGroups(config, multipleColorsPerDataset);
      if (scout.isOneOf(type, Chart.Type.PIE, Chart.Type.DOUGHNUT, Chart.Type.POLAR_AREA)) {
        let borderColor = this._computeBorderColor(type, 0);
        colors.borderColors = arrays.init(data.datasets.length, borderColor);
        colors.hoverBorderColors = colors.borderColors;
      }
    }

    return colors;
  }

  _computeDatasetColorsAutoColor(config, multipleColorsPerDataset) {
    if (!config || !config.data || !config.type || !config.options || !config.options.autoColor) {
      return {};
    }

    let data = config.data,
      type = config.type,
      checkable = config.options && config.options.checkable,
      transparent = config.options && config.options.transparent,
      colors = {
        backgroundColors: [],
        borderColors: [],
        hoverBackgroundColors: [],
        hoverBorderColors: [],
        checkedBackgroundColors: [],
        checkedHoverBackgroundColors: [],
        legendColors: [],
        pointHoverColors: []
      };

    let types = [];
    if (multipleColorsPerDataset) {
      types = arrays.init((data.datasets.length && data.datasets[0].data.length) || 0, type);
    } else {
      data.datasets.forEach(dataset => types.push(dataset.type || type));
    }
    types.forEach((type, index) => {
      colors.backgroundColors.push(this._computeBackgroundColor(type, index, checkable || transparent));
      colors.borderColors.push(this._computeBorderColor(type, index));
      colors.hoverBackgroundColors.push(this._computeHoverBackgroundColor(type, index, checkable || transparent));
      colors.hoverBorderColors.push(this._computeHoverBorderColor(type, index));

      colors.checkedBackgroundColors.push(this._computeCheckedBackgroundColor(type, index, checkable));
      colors.checkedHoverBackgroundColors.push(this._computeCheckedHoverBackgroundColor(type, index, checkable));

      colors.legendColors.push(this._computeLegendColor(type, index));

      colors.pointHoverColors.push(this._computePointHoverColor(type, index));
    });

    return colors;
  }

  _computeBackgroundColor(type, index, checkable) {
    return styles.get([this.colorSchemeCssClass, type + '-chart' + (checkable ? ' checkable' : ''), 'elements', 'color' + (index % this.numSupportedColors)], 'fill').fill;
  }

  _computeBorderColor(type, index) {
    let additionalProperties;
    if (scout.isOneOf(type, Chart.Type.PIE, Chart.Type.DOUGHNUT, Chart.Type.POLAR_AREA)) {
      additionalProperties = {stroke: this.firstOpaqueBackgroundColor};
    }
    return styles.get([this.colorSchemeCssClass, type + '-chart', 'elements', 'stroke-color' + (index % this.numSupportedColors)], 'stroke', additionalProperties).stroke;
  }

  _computeHoverBackgroundColor(type, index, checkable) {
    return styles.get([this.colorSchemeCssClass, type + '-chart' + (checkable ? ' checkable' : ''), 'elements', 'color' + (index % this.numSupportedColors) + ' hover'], 'fill').fill;
  }

  _computeHoverBorderColor(type, index) {
    let additionalProperties;
    if (scout.isOneOf(type, Chart.Type.PIE, Chart.Type.DOUGHNUT, Chart.Type.POLAR_AREA)) {
      additionalProperties = {stroke: this.firstOpaqueBackgroundColor};
    }
    return styles.get([this.colorSchemeCssClass, type + '-chart', 'elements', 'stroke-color' + (index % this.numSupportedColors) + ' hover'], 'stroke', additionalProperties).stroke;
  }

  _computeCheckedBackgroundColor(type, index, checkable) {
    return styles.get([this.colorSchemeCssClass, type + '-chart' + (checkable ? ' checkable' : ''), 'elements', 'color' + (index % this.numSupportedColors) + ' checked'], 'fill').fill;
  }

  _computeCheckedHoverBackgroundColor(type, index, checkable) {
    return styles.get([this.colorSchemeCssClass, type + '-chart' + (checkable ? ' checkable' : ''), 'elements', 'color' + (index % this.numSupportedColors) + ' hover checked'], 'fill').fill;
  }

  _computeLegendColor(type, index) {
    return styles.get([this.colorSchemeCssClass, type + '-chart', 'elements', 'color' + (index % this.numSupportedColors) + ' legend'], 'fill').fill;
  }

  _computePointHoverColor(type, index) {
    return styles.get([this.colorSchemeCssClass, type + '-chart', 'elements', 'color' + (index % this.numSupportedColors) + ' point hover'], 'fill').fill;
  }

  _computeDatasetColorsChartValueGroups(config, multipleColorsPerDataset) {
    if (!config || !config.type || !this.chart.data) {
      return {};
    }

    let type = config.type,
      checkable = config.options && config.options.checkable,
      transparent = config.options && config.options.transparent,
      colors = {
        backgroundColors: [],
        borderColors: [],
        hoverBackgroundColors: [],
        hoverBorderColors: [],
        checkedBackgroundColors: [],
        checkedHoverBackgroundColors: [],
        legendColors: [],
        pointHoverColors: []
      };

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

      colors.backgroundColors.push(adjustColor((checkable || transparent) ? uncheckedBackgroundOpacity : backgroundOpacity, 0));
      colors.borderColors.push(adjustColor(1, 0));
      colors.hoverBackgroundColors.push(adjustColor((checkable || transparent) ? uncheckedHoverBackgroundOpacity : hoverBackgroundOpacity, (checkable || transparent) ? 0 : hoverBackgroundDarker));
      colors.hoverBorderColors.push(adjustColor(1, hoverBorderDarker));

      colors.checkedBackgroundColors.push(adjustColor(checkedBackgroundOpacity, checkedBackgroundDarker));
      colors.checkedHoverBackgroundColors.push(adjustColor(checkedHoverBackgroundOpacity, checkedHoverBackgroundDarker));

      colors.legendColors.push(adjustColor(1, 0));

      colors.pointHoverColors.push(adjustColor(1, 0));
    });
    colors.datalabelColor = this._computeDatalabelColor(type);

    return colors;
  }

  _adjustColorOpacity(color, opacity = 1) {
    if (!color || typeof color === 'function') {
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

  _adjustLegendColors(config) {
    if (!config || !config.type || !config.options) {
      return;
    }

    config.options = $.extend(true, {}, config.options, {
      legend: {
        labels: {
          fontColor: this._computeLabelColor(config.type),
          generateLabels: this._legendLabelGenerator
        }
      }
    });
  }

  _computeLabelColor(type) {
    return styles.get([this.colorSchemeCssClass, type + '-chart', 'elements', 'label'], 'fill').fill;
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
    let defaultTypeGenerateLabels;
    if (ChartJs.defaults[config.type] && ChartJs.defaults[config.type].legend && ChartJs.defaults[config.type].legend.labels) {
      defaultTypeGenerateLabels = ChartJs.defaults[config.type].legend.labels.generateLabels;
    }
    let defaultGenerateLabels = defaultTypeGenerateLabels || ChartJs.defaults.global.legend.labels.generateLabels;
    let labels = defaultGenerateLabels.call(chart, chart);
    labels.forEach((elem, idx) => {
      elem.text = strings.truncateText(elem.text, horizontalSpace, measureText);
      let dataset = data.datasets[idx],
        fillStyle;
      if (dataset && scout.isOneOf((dataset.type || config.type), Chart.Type.LINE, Chart.Type.BAR, Chart.Type.BAR_HORIZONTAL, Chart.Type.RADAR, Chart.Type.BUBBLE)) {
        fillStyle = dataset.legendColor || this._adjustColorOpacity(dataset.borderColor, 1);
      } else if (data.datasets.length && scout.isOneOf(config.type, Chart.Type.PIE, Chart.Type.DOUGHNUT, Chart.Type.POLAR_AREA)) {
        dataset = data.datasets[0];
        let legendColor = Array.isArray(dataset.legendColor) ? dataset.legendColor[idx] : dataset.legendColor,
          backgroundColor = Array.isArray(dataset.backgroundColor) ? dataset.backgroundColor[idx] : dataset.backgroundColor;
        fillStyle = legendColor || this._adjustColorOpacity(backgroundColor, 1);
      }
      if (typeof fillStyle !== 'function') {
        elem.fillStyle = fillStyle;
        elem.strokeStyle = fillStyle;
      }
    });
    return labels;
  }

  _adjustTooltipColors(config) {
    if (!config || !config.type || !config.options) {
      return;
    }

    let tooltipBackgroundColor = this._computeTooltipBackgroundColor(config.type),
      tooltipBorderColor = this._computeTooltipBorderColor(config.type);

    config.options = $.extend(true, {}, config.options, {
      tooltips: {
        backgroundColor: tooltipBackgroundColor,
        borderColor: tooltipBorderColor,
        multiKeyBackground: tooltipBackgroundColor
      }
    });
  }

  _computeTooltipBackgroundColor(type) {
    return styles.get([this.colorSchemeCssClass, type + '-chart', 'elements', 'tooltip-background'], 'fill').fill;
  }

  _computeTooltipBorderColor(type) {
    return styles.get([this.colorSchemeCssClass, type + '-chart', 'elements', 'tooltip-border'], 'fill').fill;
  }

  _adjustScaleColors(config) {
    if (!config || !config.type || !config.options || !config.options.scale) {
      return;
    }

    let labelColor = this._computeLabelColor(config.type),
      labelBackdropColor = this._computeLabelBackdropColor(config.type),
      gridColor = this._computeGridColor(config.type),
      scaleTicksColor = this._computeScaleTicksColor(config.type);

    config.options.scale.ticks = $.extend(true, {}, config.options.scale.ticks, {
      fontColor: scaleTicksColor,
      backdropColor: labelBackdropColor
    });
    config.options.scale.pointLabels = $.extend(true, {}, config.options.scale.pointLabels, {
      fontColor: labelColor
    });
    config.options.scale.gridLines = $.extend(true, {}, config.options.scale.gridLines, {
      color: gridColor
    });
  }

  _computeLabelBackdropColor(type) {
    return styles.get([this.colorSchemeCssClass, type + '-chart', 'elements', 'label-backdrop'], 'fill', {fill: this.firstOpaqueBackgroundColor}).fill;
  }

  _computeGridColor(type) {
    return styles.get([this.colorSchemeCssClass, type + '-chart', 'elements', 'grid'], 'fill').fill;
  }

  _computeScaleTicksColor(type) {
    return styles.get([this.colorSchemeCssClass, type + '-chart', 'elements', 'scale-ticks'], 'fill').fill;
  }

  _adjustScalesColors(config) {
    if (!config || !config.type || !config.options || !config.options.scales) {
      return;
    }

    let xAxes = config.options.scales.xAxes || [],
      yAxes = config.options.scales.yAxes || [],
      axes = [...xAxes, ...yAxes];

    if (!axes.length) {
      return;
    }

    let labelColor = this._computeLabelColor(config.type),
      gridColor = this._computeGridColor(config.type),
      axisLabelColor = this._computeAxisLabelColor(config.type);

    axes.forEach(elem => {
      elem.gridLines = $.extend(true, {}, elem.gridLines, {
        zeroLineColor: gridColor,
        color: gridColor
      });
      elem.ticks = $.extend(true, {}, elem.ticks, {
        fontColor: labelColor
      });
      elem.scaleLabel = $.extend(true, {}, elem.scaleLabel, {
        fontColor: axisLabelColor
      });
    });
  }

  _computeAxisLabelColor(type) {
    return styles.get([this.colorSchemeCssClass, type + '-chart', 'elements', 'axis-label'], 'fill').fill;
  }

  _adjustPluginColors(config) {
    if (!config || !config.type || !config.options || !config.options.plugins) {
      return;
    }

    this._adjustPluginsDatalabelColors(config);
  }

  _adjustPluginsDatalabelColors(config) {
    if (!config || !config.type || !config.options || !config.options.plugins || !config.options.plugins.datalabels) {
      return;
    }

    config.options.plugins.datalabels = $.extend(true, {}, config.options.plugins.datalabels, {
      color: this._computeDatalabelColor(config.type)
    });
  }

  _computeDatalabelColor(type) {
    return styles.get([this.colorSchemeCssClass, type + '-chart', 'elements', 'datalabel'], 'fill').fill;
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
    if (!items.length) {
      return;
    }
    let relevantItem = this._selectRelevantItem(items);

    if (this._isMaxSegmentsExceeded(this.chartJs.config, relevantItem._index)) {
      return;
    }

    let itemIndex = relevantItem._index,
      datasetIndex = relevantItem._datasetIndex,
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

  /**
   * Selects the most relevant item. Default is the first item.
   *
   * @param {object[]} items
   * @param {number} items._index
   * @param {number} items._datasetIndex
   * @private
   */
  _selectRelevantItem(items) {
    let chartDatasets = this.chartJs.config.data.datasets;
    let relevantItem = items[0];

    if (this.chartJs.config.type === Chart.Type.BUBBLE) {
      // The smallest bubble, which is drawn in the foreground, is the most relevant item for the bubble chart.
      // If two bubbles are the same size, we choose the one which comes later in the array (bubble with array index n + 1 is draw in front of bubble with array index n)
      items.forEach(item => {
        if (chartDatasets[item._datasetIndex].data[item._index].z <= chartDatasets[relevantItem._datasetIndex].data[relevantItem._index].z) {
          relevantItem = item;
        }
      });
    }
    return relevantItem;
  }

  _onHover(event, items) {
    if (!this.chartJs.config || !this.chartJs.config.type) {
      return;
    }

    let config = this.chartJs.config,
      type = config.type;
    if (!scout.isOneOf(type, Chart.Type.LINE, Chart.Type.BAR, Chart.Type.RADAR)) {
      return;
    }

    let update = false;
    if (this.resetDatasetAfterHover) {
      this._restoreBackgroundColor();
      this.resetDatasetAfterHover = false;
      update = true;
    }
    items.forEach(item => {
      let dataset = config.data.datasets[item._datasetIndex];
      if (scout.isOneOf((dataset.type || type), Chart.Type.LINE, Chart.Type.RADAR)) {
        this._setHoverBackgroundColor(dataset);
        this.resetDatasetAfterHover = true;
        update = true;
      }
    });
    if (update) {
      this.chartJs.update();
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
    if (!this.chartJs.config || !this.chartJs.config.type) {
      return;
    }

    let type = this.chartJs.config.type,
      defaultTypeLegendClick;
    if (ChartJs.defaults[type] && ChartJs.defaults[type].legend) {
      defaultTypeLegendClick = ChartJs.defaults[type].legend.onClick;
    }
    let defaultLegendClick = defaultTypeLegendClick || ChartJs.defaults.global.legend.onClick;
    defaultLegendClick.call(this.chartJs, event, item);
    this._onLegendLeave(event, item);
    this._onLegendHoverPointer(event, item, true);
  }

  _onLegendHover(event, item, animated) {
    let index = item.datasetIndex,
      config = this.chartJs.config,
      type = config.type;
    if (scout.isOneOf(type, Chart.Type.PIE, Chart.Type.DOUGHNUT, Chart.Type.POLAR_AREA)) {
      index = item.index;
    }

    if (this.legendHoverDatasets.indexOf(index) > -1) {
      return;
    }

    let dataset = config.data.datasets[index],
      datasetType = dataset ? dataset.type : null;
    if ((datasetType || type) === Chart.Type.LINE) {
      this._setHoverBackgroundColor(dataset);
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

  _onLegendHoverPointer(event, item, animated) {
    this._onLegendHover(event, item, animated);
    this.$canvas.css('cursor', 'pointer');
  }

  _onLegendLeave(event, item) {
    let index = item.datasetIndex,
      config = this.chartJs.config,
      type = config.type;
    if (scout.isOneOf(type, Chart.Type.PIE, Chart.Type.DOUGHNUT, Chart.Type.POLAR_AREA)) {
      index = item.index;
    }

    if (this.legendHoverDatasets.indexOf(index) < 0) {
      return;
    }

    let dataset = config.data.datasets[index],
      datasetType = dataset ? dataset.type : null;
    if ((datasetType || type) === Chart.Type.LINE) {
      this._restoreBackgroundColor(dataset);
      this.chartJs.update();
    }
    this._updateHoverStyle(index, false);
    this.chartJs.render();
    this.legendHoverDatasets.splice(this.legendHoverDatasets.indexOf(index), 1);
  }

  /**
   * Sets the hover background color as the datasets background color.
   * This little workaround is necessary for the line chart, which does not support a native hover effect.
   * The previous background color will be backuped on the dataset property "backgroundColorBackup"
   * and can be restored with {@link _restoreBackgroundColor}.
   * @param {Dataset} dataset
   */
  _setHoverBackgroundColor(dataset) {
    if (!dataset) {
      return;
    }
    // backup the old background color first
    dataset.backgroundColorBackup = dataset.backgroundColor;
    // overwrite the current background color with the hover color
    dataset.backgroundColor = dataset.hoverBackgroundColor;
  }

  /**
   * Restores the background color of a dataset or of all datasets,
   * if they were previously overwritten by {@link _setHoverBackgroundColor}.
   * @param {Dataset} [dataset]
   */
  _restoreBackgroundColor(dataset) {
    if (dataset) {
      dataset.backgroundColor = dataset.backgroundColorBackup || dataset.backgroundColor;
      delete dataset.backgroundColorBackup;
    } else {
      this.chartJs.config.data.datasets.forEach(dataset => this._restoreBackgroundColor(dataset));
    }
  }

  _onLegendLeavePointer(event, item) {
    this._onLegendLeave(event, item);
    this.$canvas.css('cursor', 'default');
  }

  _updateHoverStyle(index, enabled) {
    let config = this.chartJs.config,
      type = config.type,
      datasets = config.data.datasets,
      dataset = datasets ? datasets[index] : null,
      datasetType = dataset ? dataset.type : null;
    if ((datasetType || type) === Chart.Type.LINE) {
      this.chartJs.updateHoverStyle(this.chartJs.getDatasetMeta(index).data, 'point', enabled);
    } else if (scout.isOneOf(type, Chart.Type.PIE, Chart.Type.DOUGHNUT, Chart.Type.POLAR_AREA)) {
      let elements = [];
      for (let i = 0; i < datasets.length; i++) {
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

  _adjustResizeHandler(config) {
    if (!config || !config.options) {
      return;
    }

    if (config.options.handleResize) {
      config.options.onResize = this._resizeHandler;
    }
  }

  _onResize(chart, size) {
    chart.update();
    this._adjustSize(chart.config, chart.chartArea);
  }

  _adjustSize(config, chartArea) {
    this._adjustBubbleSizes(config, chartArea);
    this._adjustGridMaxMin(config, chartArea);
  }

  _adjustBubbleSizes(config, chartArea) {
    if (config.type !== Chart.Type.BUBBLE) {
      return;
    }

    let datasets = config.data.datasets;
    // Scale all bubbles so that the largest radius is equal to sizeOfLargestBubble and the smallest greater than or equal to minBubbleSize.
    // First reset all radii.
    datasets.forEach(dataset => dataset.data.forEach(data => {
      if (!isNaN(data.z)) {
        data.r = Math.sqrt(data.z);
      }
    }));
    let maxMinR = this._computeMaxMinValue(datasets, 'r', true),
      maxR = maxMinR.maxValue,
      minR = maxMinR.minValue,
      // Compute a scalingFactor and an offset to get the new radius newR = r * scalingFactor + offset.
      bubbleScalingFactor = 1,
      bubbleRadiusOffset = 0,
      sizeOfLargestBubble = config.bubble ? config.bubble.sizeOfLargestBubble : 0,
      minBubbleSize = config.bubble ? config.bubble.minBubbleSize : 0;
    if (sizeOfLargestBubble) {
      let width = Math.abs(chartArea.right - chartArea.left),
        height = Math.abs(chartArea.top - chartArea.bottom);
      sizeOfLargestBubble = Math.min(sizeOfLargestBubble, Math.floor(Math.min(width, height) / 6));
      if (maxR === 0) {
        // If maxR is equal to 0, all radii are equal to 0, therefore set bubbleRadiusOffset to sizeOfLargestBubble.
        bubbleRadiusOffset = sizeOfLargestBubble;
      } else if (minBubbleSize && sizeOfLargestBubble > minBubbleSize && (minR / maxR) < (minBubbleSize / sizeOfLargestBubble)) {
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
        bubbleScalingFactor = (sizeOfLargestBubble - minBubbleSize) / (maxR - minR);
        bubbleRadiusOffset = minBubbleSize - bubbleScalingFactor * minR;
      } else {
        // Scaling is sufficient.
        bubbleScalingFactor = sizeOfLargestBubble / maxR;
      }
    } else if (minBubbleSize && minBubbleSize > minR) {
      // sizeOfLargestBubble is not set
      if (minR === 0) {
        // If the smallest radius equals 0 scaling will have no effect.
        bubbleRadiusOffset = minBubbleSize;
      } else {
        // Scaling is sufficient.
        bubbleScalingFactor = minBubbleSize / minR;
      }
    }
    datasets.forEach(dataset => dataset.data.forEach(data => {
      if (!objects.isNullOrUndefined(data.r)) {
        data.r = data.r * bubbleScalingFactor + bubbleRadiusOffset;
      }
    }));
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

  _adjustGridMaxMin(config, chartArea) {
    if (!config || !config.type || !config.options || !config.options.adjustGridMaxMin || (!config.options.scale && !config.options.scales) || !chartArea) {
      return;
    }

    let type = config.type;
    if (!scout.isOneOf(type, Chart.Type.BAR, Chart.Type.BAR_HORIZONTAL, Chart.Type.LINE, Chart.Type.POLAR_AREA, Chart.Type.RADAR, Chart.Type.BUBBLE)) {
      return;
    }

    let scales = config.options.scales,
      scale = config.options.scale,
      minSpaceBetweenXTicks, minSpaceBetweenYTicks;
    if (scale) {
      minSpaceBetweenXTicks = scale.minSpaceBetweenTicks;
      minSpaceBetweenYTicks = scale.minSpaceBetweenTicks;
    } else {
      minSpaceBetweenXTicks = scales.minSpaceBetweenXTicks;
      minSpaceBetweenYTicks = scales.minSpaceBetweenYTicks;
    }

    let width = Math.abs(chartArea.right - chartArea.left),
      height = Math.abs(chartArea.top - chartArea.bottom),
      maxXTicks = Math.max(Math.floor(width / minSpaceBetweenXTicks), 3),
      maxYTicks = Math.max(Math.floor(height / minSpaceBetweenYTicks), 3);

    let yBoundaries = this._computeYBoundaries(config, height),
      yBoundary = yBoundaries.yBoundary,
      yBoundaryDiffType = yBoundaries.yBoundaryDiffType;

    if (scale) {
      this._adjustScaleMaxMin(scale, Math.min(maxXTicks, maxYTicks), yBoundary);
      return;
    }

    let xAxes = scales.xAxes,
      yAxes = scales.yAxes;

    if (yBoundaryDiffType) {
      this._adjustAxes(arrays.ensure(yAxes[0]), maxYTicks, yBoundary);
      this._adjustAxes(arrays.ensure(yAxes[1]), maxYTicks, yBoundaryDiffType);
    } else if (type === Chart.Type.BAR_HORIZONTAL) {
      this._adjustAxes(xAxes, maxXTicks, yBoundary);
    } else {
      this._adjustAxes(yAxes, maxYTicks, yBoundary);
    }

    if (type !== Chart.Type.BUBBLE) {
      return;
    }

    let xBoundary = this._computeXBoundaryBubble(config, width);
    this._adjustAxes(xAxes, maxXTicks, xBoundary);
  }

  _computeBoundaryBubble(config, identifier, space) {
    if (!config || !config.type || config.type !== Chart.Type.BUBBLE || !config.data || !config.options || !config.options.scales || !(identifier === 'x' || identifier === 'y') || !space) {
      return;
    }

    let datasets = config.data.datasets,
      axes = config.options.scales[identifier + 'Axes'],
      axis = (axes && axes.length) ? axes[0] : null,
      offset = axis && axis.offset,
      labelMap = config.options.scales[identifier + 'LabelMap'],
      boundary;

    let maxR = this._computeMaxMinValue(datasets, 'r', true).maxValue,
      padding = maxR;
    if (config.options.elements && config.options.elements.point && config.options.elements.point.hoverRadius) {
      padding = padding + config.options.elements.point.hoverRadius;
    }

    if (offset) {
      boundary = this._computeMaxMinValue(datasets, identifier, labelMap, true);
    } else {
      boundary = this._computeMaxMinValue(datasets, identifier, labelMap, true, padding, space);
    }
    if (labelMap) {
      boundary.maxValue = Math.ceil(boundary.maxValue);
      boundary.minValue = Math.floor(boundary.minValue);
    }
    return boundary;
  }

  _computeXBoundaryBubble(config, width) {
    return this._computeBoundaryBubble(config, 'x', width);
  }

  _computeYBoundaryBubble(config, height) {
    return this._computeBoundaryBubble(config, 'y', height);
  }

  _computeYBoundaries(config, height) {
    if (!config || !config.type) {
      return {};
    }

    let type = config.type,
      yBoundary,
      yBoundaryDiffType;

    if (type === Chart.Type.BUBBLE) {
      yBoundary = this._computeYBoundaryBubble(config, height);
    } else {
      let datasets = [],
        datasetsDiffType = [];
      if (config.data && config.data.datasets) {
        config.data.datasets.forEach(dataset => {
          if (dataset.type && dataset.type !== type) {
            datasetsDiffType.push(dataset);
          } else {
            datasets.push(dataset);
          }
        });
      }

      yBoundary = this._computeMaxMinValue(datasets);

      if (datasets.length && datasetsDiffType.length) {
        yBoundaryDiffType = this._computeMaxMinValue(datasetsDiffType);
        let yBoundaryRange = yBoundary.maxValue - yBoundary.minValue,
          yBoundaryRangeDiffType = yBoundaryDiffType.maxValue - yBoundaryDiffType.minValue;
        if (yBoundaryRange && yBoundaryRangeDiffType && (yBoundaryRange / yBoundaryRangeDiffType > 10 || yBoundaryRangeDiffType / yBoundaryRange > 10)) {
          this._adjustYAxisDiffType(config, datasets, datasetsDiffType);
        }
      }
    }

    return {
      yBoundary: yBoundary,
      yBoundaryDiffType: yBoundaryDiffType
    };
  }

  _adjustYAxisDiffType(config, datasets, datasetsDiffType) {
    if (!config || !config.type || !datasets || !datasets.length || !datasetsDiffType || !datasetsDiffType.length) {
      return;
    }

    if (!config.options || !config.options.scales || !config.options.scales.yAxes || config.options.scales.yAxes.length !== 1) {
      return;
    }

    let type = config.type,
      scales = config.options.scales,
      yAxis = scales.yAxes[0],
      yAxisDiffType = $.extend(true, {}, yAxis);
    scales.yAxes.push(yAxisDiffType);

    yAxis.id = 'yAxis';
    yAxisDiffType.id = 'yAxisDiffType';

    if (config.data && config.data.datasets && config.data.datasets.length && config.data.datasets[0].type && config.data.datasets[0].type !== type) {
      yAxisDiffType.position = Chart.Position.LEFT;
      yAxis.position = Chart.Position.RIGHT;
      yAxis.gridLines.drawOnChartArea = false;
    } else {
      yAxis.position = Chart.Position.LEFT;
      yAxisDiffType.position = Chart.Position.RIGHT;
      yAxisDiffType.gridLines.drawOnChartArea = false;
    }

    yAxis.gridLines.drawBorder = true;
    yAxis.gridLines.drawTicks = true;
    yAxisDiffType.gridLines.drawBorder = true;
    yAxisDiffType.gridLines.drawTicks = true;

    let yAxisType = (datasets[0].type || type),
      yAxisDiffTypeType = (datasetsDiffType[0].type || type),
      yAxisTypeLabel = this.chart.session.text('ui.' + yAxisType),
      yAxisDiffTypeTypeLabel = this.chart.session.text('ui.' + yAxisDiffTypeType),
      yAxisScaleLabel = scales.scaleLabelByTypeMap ? scales.scaleLabelByTypeMap[yAxisType] : null,
      yAxisDiffTypeScaleLabel = scales.scaleLabelByTypeMap ? scales.scaleLabelByTypeMap[yAxisDiffTypeType] : null;

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

  _adjustScaleMaxMin(scale, maxTicks, maxMinValue) {
    scale.ticks = $.extend(true, {}, scale.ticks, {
      maxTicksLimit: Math.ceil(maxTicks / 2),
      stepSize: (this.onlyIntegers ? 1 : undefined)
    });
    if (maxMinValue) {
      scale.ticks.suggestedMax = maxMinValue.maxValue;
      scale.ticks.suggestedMin = maxMinValue.minValue;
    }
  }

  _adjustAxes(axes, maxTicks, maxMinValue) {
    if (!axes || !Array.isArray(axes) || !axes.length) {
      return;
    }

    for (let i = 0; i < axes.length; i++) {
      axes[i] = $.extend(true, {}, axes[i], {
        ticks: {
          maxTicksLimit: maxTicks,
          stepSize: (this.onlyIntegers ? 1 : undefined)
        }
      });
      if (maxMinValue) {
        axes[i].ticks.suggestedMax = maxMinValue.maxValue;
        axes[i].ticks.suggestedMin = maxMinValue.minValue;
      }
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
