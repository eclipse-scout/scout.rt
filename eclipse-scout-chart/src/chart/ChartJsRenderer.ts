/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AbstractChartRenderer, CartesianChartScale, Chart, chartJsDateAdapter, RadialChartScale} from '../index';
import {
  _adapters as chartJsAdapters, ActiveElement, ArcElement, BarElement, BubbleDataPoint, CartesianScaleOptions, Chart as ChartJs, ChartArea, ChartConfiguration, ChartDataset, ChartEvent, ChartType as ChartJsType, Color, DefaultDataPoint,
  FontSpec, LegendElement, LegendItem, LegendOptions, LinearScaleOptions, PointElement, PointHoverOptions, RadialLinearScaleOptions, Scale, ScatterDataPoint, Scriptable, ScriptableContext, TooltipCallbacks, TooltipItem, TooltipLabelStyle,
  TooltipModel, TooltipOptions
} from 'chart.js';
import 'chart.js/auto'; // Import from auto to register charts
import {aria, arrays, colorSchemes, graphics, numbers, objects, Point, scout, strings, styles, Tooltip, tooltips} from '@eclipse-scout/core';
import ChartDataLabels, {Context} from 'chartjs-plugin-datalabels';
import $ from 'jquery';
import {ChartAxis, ChartConfig, ChartData, ChartType, ClickObject, NumberFormatter} from './Chart';

ChartJs.register(ChartDataLabels);

$.extend(true, ChartJs.defaults, {
  maintainAspectRatio: false,
  elements: {
    line: {
      borderWidth: 2
    },
    point: {
      radius: 0,
      hitRadius: 10,
      hoverRadius: 7,
      hoverBorderWidth: 2
    },
    arc: {
      borderWidth: 1
    },
    bar: {
      borderWidth: 1,
      borderSkipped: ''
    }
  },
  plugins: {
    legend: {
      labels: {
        usePointStyle: true,
        boxHeight: 7
      }
    }
  }
});
$.extend(true, ChartJs.overrides, {
  line: {
    elements: {
      point: {
        borderWidth: 2
      }
    }
  },
  scatter: {
    elements: {
      point: {
        radius: 3
      }
    }
  }
});

let chartJsGlobalsInitialized = false;
const PHI = (1 + Math.sqrt(5)) / 2; // golden ratio

export class ChartJsRenderer extends AbstractChartRenderer {
  static ARROW_LEFT_RIGHT = '\u2194';
  static ARROW_UP_DOWN = '\u2195';

  chartJs: ChartJsChart;
  onlyIntegers: boolean;
  maxXAxesTicksHeight: number;
  numSupportedColors: number;
  colorSchemeCssClass: string;
  minRadialChartDatalabelSpace: number;
  resetDatasetAfterHover: boolean;
  legendHoverDatasets: number[];
  removing: boolean;

  $canvas: JQuery<HTMLCanvasElement>;

  protected _labelFormatter: LabelFormatter;
  protected _xLabelFormatter: LabelFormatter;
  protected _yLabelFormatter: LabelFormatter;
  protected _xAxisFitter: AxisFitter;
  protected _yAxisFitter: AxisFitter;
  protected _radialChartDatalabelsDisplayHandler: DatalabelsDisplayHandler;
  protected _radialChartDatalabelsFormatter: RadialChartDatalabelsFormatter;
  protected _datalabelsFormatter: DatalabelsFormatter;
  protected _datalabelBackgroundColorHandler: DatalabelBackgroundColorHandler;
  protected _legendLabelGenerator: LegendLabelGenerator;
  protected _clickHandler: ChartEventHandler;
  protected _hoverHandler: ChartEventHandler;
  protected _pointerHoverHandler: ChartEventHandler;
  protected _legendClickHandler: LegendEventHandler;
  protected _legendHoverHandler: LegendEventHandler;
  protected _legendPointerHoverHandler: LegendEventHandler;
  protected _legendLeaveHandler: LegendEventHandler;
  protected _legendPointerLeaveHandler: LegendEventHandler;
  protected _resizeHandler: ResizeHandler;
  protected _tooltipTitleGenerator: TooltipTitleGenerator;
  protected _tooltipItemsGenerator: TooltipItemsGenerator;
  protected _tooltipLabelGenerator: TooltipLabelGenerator;
  protected _tooltipLabelValueGenerator: TooltipLabelValueGenerator;
  protected _tooltipLabelColorGenerator: TooltipLabelColorGenerator;
  protected _tooltipRenderer: TooltipRenderer;
  protected _tooltip: Tooltip;
  protected _tooltipTimeoutId: number;
  protected _updatingDatalabels: boolean;

  constructor(chart: Chart) {
    super(chart);
    this.chartJs = null;
    this.onlyIntegers = true;
    this.maxXAxesTicksHeight = 75;
    this.numSupportedColors = 6;
    this.colorSchemeCssClass = '';
    this.minRadialChartDatalabelSpace = 25;

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

    this._tooltipTitleGenerator = this._generateTooltipTitle.bind(this);
    this._tooltipItemsGenerator = this._generateTooltipItems.bind(this);
    this._tooltipLabelGenerator = this._generateTooltipLabel.bind(this);
    this._tooltipLabelValueGenerator = this._generateTooltipLabelValue.bind(this);
    this._tooltipLabelColorGenerator = this._generateTooltipLabelColor.bind(this);
    this._tooltipRenderer = this._renderTooltip.bind(this);
    this._tooltip = null;
    this._tooltipTimeoutId = null;
  }

  protected override _validateChartData(): boolean {
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

    if (chartConfigDataValid && scout.isOneOf(config.type, Chart.Type.POLAR_AREA, Chart.Type.RADAR)) {
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

  protected override _render() {
    if (!this.$canvas) {
      this.$canvas = this.chart.$container.appendElement('<canvas>') as JQuery<HTMLCanvasElement>;
      aria.hidden(this.$canvas, true); // aria not supported yet
    }
    this.firstOpaqueBackgroundColor = styles.getFirstOpaqueBackgroundColor(this.$canvas);
    if (!chartJsGlobalsInitialized) {
      ChartJs.defaults.font.family = this.$canvas.css('font-family');
      chartJsAdapters._date.override(chartJsDateAdapter.getAdapter(this.chart.session));
      chartJsGlobalsInitialized = true;
    }
    let config = $.extend(true, {}, this.chart.config);
    this._adjustConfig(config);
    this._renderChart(config);
  }

  protected _renderChart(config: ChartConfig) {
    if (this.chartJs) {
      this.chartJs.destroy();
    }
    config = $.extend(true, {}, config, {
      options: {
        animation: {}
      }
    }, config);
    config.options.animation.duration = this.animationDuration;

    this.chartJs = new ChartJs(this.$canvas[0].getContext('2d'), config as ChartConfiguration) as ChartJsChart;
    this._adjustSize(this.chartJs.config, this.chartJs.chartArea);
    this.refresh();
  }

  protected override _updateData() {
    if (!this.chartJs) {
      return;
    }
    let config = $.extend(true, {}, this.chart.config);
    this._adjustConfig(config);

    let targetData = this.chartJs.config.data,
      sourceData = config.data;

    // Transfer property from source object to target object:
    // 1. If the property is not set on the target object, copy it from source.
    // 2. If the property is not set on the source object, set it to undefined if setToUndefined = true. Otherwise, empty the array if it is an array property or set it to undefined.
    // 3. If the property is not an array on the source or the target object, copy the property from the source to the target object.
    // 4. If the property is an array on both objects, do not update the array, but transfer the elements (update elements directly, use pop(), push() or splice() if one array is longer than the other).
    let transferProperty = (source: object, target: object, property: string, setToUndefined?) => {
      if (!source || !target || !property) {
        return;
      }
      // 1. Property not set on target
      if (!target[property]) {
        let src = source[property];
        if (src || setToUndefined) {
          target[property] = src;
        }
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
          target[property].splice(0, target[property].length);
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
        target[property].splice(sourceLength, targetLength - sourceLength);
      } else if (targetLength < sourceLength) {
        // Source array is longer than target array
        target[property].push(...source[property].splice(targetLength));
      }
    };

    let findDataset = (datasets: ChartDataset[], datasetId) => arrays.find(datasets, dataset => dataset.datasetId === datasetId);
    let findDatasetIndex = (datasets: ChartDataset[], datasetId) => arrays.findIndex(datasets, dataset => dataset.datasetId === datasetId);

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
          transferProperty(sourceDataset, targetDataset, 'pointBackgroundColor', true);
          transferProperty(sourceDataset, targetDataset, 'pointHoverBackgroundColor', true);
          transferProperty(sourceDataset, targetDataset, 'pointBorderColor', true);
          transferProperty(sourceDataset, targetDataset, 'pointHoverBorderColor', true);

          transferProperty(sourceDataset, targetDataset, 'uncheckedBackgroundColor', true);
          transferProperty(sourceDataset, targetDataset, 'uncheckedHoverBackgroundColor', true);
          transferProperty(sourceDataset, targetDataset, 'checkedBackgroundColor', true);
          transferProperty(sourceDataset, targetDataset, 'checkedHoverBackgroundColor', true);
          transferProperty(sourceDataset, targetDataset, 'uncheckedPointBackgroundColor', true);
          transferProperty(sourceDataset, targetDataset, 'uncheckedPointHoverBackgroundColor', true);
          transferProperty(sourceDataset, targetDataset, 'checkedPointBackgroundColor', true);
          transferProperty(sourceDataset, targetDataset, 'checkedPointHoverBackgroundColor', true);

          transferProperty(sourceDataset, targetDataset, 'lineTension', true);

          transferProperty(sourceDataset, targetDataset, 'pointRadius', true);
          transferProperty(sourceDataset, targetDataset, 'uncheckedPointRadius', true);
          transferProperty(sourceDataset, targetDataset, 'checkedPointRadius', true);

          this._adjustDatasetBorderWidths(targetDataset);
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
    transferProperty(config.options, this.chartJs.config.options, 'xLabelMap', true);
    transferProperty(config.options, this.chartJs.config.options, 'yLabelMap', true);

    $.extend(true, this.chartJs.config, {
      options: {
        animation: {
          duration: this.animationDuration
        }
      }
    });
    let scales = this.chartJs.config.options.scales || {},
      axes = [scales.x || {}, scales.y || {}, scales.yDiffType || {}, scales.r || {}];
    axes.forEach((axis: LinearScaleOptions | RadialLinearScaleOptions) => {
      (axis.ticks || {} as (LinearScaleOptions | RadialLinearScaleOptions)['ticks']).stepSize = undefined;
    });

    this.refresh();

    this._adjustSize(this.chartJs.config, this.chartJs.chartArea);
    this.refresh();
  }

  override isDataUpdatable(): boolean {
    return true;
  }

  override isDetachSupported(): boolean {
    // chart.js removes the animation-listeners onProgress and onComplete on detach and does not add them again on attach
    // these listeners are needed for the datalabels => this renderer does not support detach
    return false;
  }

  override refresh() {
    if (this.chartJs) {
      this.chartJs.update();
    } else {
      super.refresh();
    }
  }

  protected override _renderAnimationDuration() {
    if (!this.chartJs) {
      return;
    }
    $.extend(true, this.chartJs.config, {
      options: {
        animation: {
          duration: this.animationDuration
        }
      }
    });
    this.refresh();
  }

  protected override _renderCheckedItems() {
    if (this.chartJs && this._checkItems(this.chartJs.config)) {
      this.refresh();
    }
  }

  protected _checkItems(config: ChartConfig): boolean {
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
    config.data.datasets.forEach((dataset: ChartDataset, datasetIndex) => {
      let checkedIndices = this.chart.checkedItems.filter(item => item.datasetIndex === datasetIndex)
          .map(item => item.dataIndex),
        uncheckedIndices = arrays.init(dataset.data.length, null).map((elem, idx) => idx);
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

      this._adjustDatasetBorderWidths(dataset);
    });

    return 0 < changed;
  }

  stopAnimations() {
    if (this.chartJs) {
      this.chartJs.stop();
    }
  }

  protected _adjustConfig(config: ChartConfig) {
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

  protected _adjustType(config: ChartConfig) {
    if (config.type === Chart.Type.COMBO_BAR_LINE) {
      config.type = Chart.Type.BAR;

      let scaleLabelByTypeMap = (config.options || {}).scaleLabelByTypeMap;
      if (scaleLabelByTypeMap) {
        scaleLabelByTypeMap[Chart.Type.BAR] = scaleLabelByTypeMap[Chart.Type.COMBO_BAR_LINE];
      }
    } else if (this._isHorizontalBar(config)) {
      config.type = Chart.Type.BAR;
      config.options = $.extend(true, {}, config.options, {
        indexAxis: 'y'
      });
    }
  }

  protected _computeDatasets(chartData: ChartData, config: ChartConfig) {
    let labels = [],
      datasets = [];

    let setLabelMap = (identifier, labelMap) => {
      if (!$.isEmptyObject(labelMap)) {
        config.options[identifier] = labelMap;
      }
    };

    (chartData.axes[0] || []).forEach(elem => labels.push(elem.label));

    let isHorizontalBar = this._isHorizontalBar(config);
    setLabelMap(isHorizontalBar ? 'yLabelMap' : 'xLabelMap', this._computeLabelMap(chartData.axes[0]));
    setLabelMap(isHorizontalBar ? 'xLabelMap' : 'yLabelMap', this._computeLabelMap(chartData.axes[1]));

    chartData.chartValueGroups.forEach(elem => datasets.push({
      type: elem.type,
      label: elem.groupName,
      data: $.extend(true, [], elem.values)
    }));

    config.data = {
      labels: labels,
      datasets: datasets
    };
  }

  protected _isHorizontalBar(config: ChartConfig): boolean {
    return config && (config.type === Chart.Type.BAR_HORIZONTAL
      || (config.type === Chart.Type.BAR && config.options && config.options.indexAxis === 'y'));
  }

  protected _computeLabelMap(axis: ChartAxis[]): Record<number, string> {
    let labelMap = {};
    (axis || []).forEach((elem, idx) => {
      labelMap[idx] = elem.label;
    });
    return labelMap;
  }

  protected _adjustData(config: ChartConfig) {
    if (!config || !config.data || !config.type) {
      return;
    }

    this._adjustBarBorderWidth(config);
    this._adjustMaxSegments(config);
    this._adjustBubbleRadii(config);
    this._adjustOnlyIntegers(config);
  }

  protected _adjustBarBorderWidth(config: ChartConfig) {
    if (!config || !config.data || !config.type || !scout.isOneOf(config.type, Chart.Type.BAR)) {
      return;
    }

    config.data.datasets.forEach(dataset => {
      if ((dataset.type || Chart.Type.BAR) === Chart.Type.BAR) {
        dataset.borderWidth = dataset.borderWidth || 1;
        dataset.hoverBorderWidth = dataset.hoverBorderWidth || 2;
        this._adjustDatasetBorderWidths(dataset);
      }
    });
  }

  protected _adjustDatasetBorderWidths(dataset: ChartDataset) {
    this._adjustDatasetBorderWidth(dataset);
    this._adjustDatasetBorderWidth(dataset, true);
  }

  /**
   * Sets the borderWidth to 0 if the backgroundColor and the borderColor are identical and backups the original value.
   * This method is idempotent as it restores the original state first and then applies its logic.
   *
   * @param hover whether hoverBorderWidth, hoverBackgroundColor and hoverBorderColor should be considered instead of borderWidth, backgroundColor and borderColor
   */
  protected _adjustDatasetBorderWidth(dataset: ChartDataset, hover?: boolean) {
    if (!dataset) {
      return;
    }
    let borderWidthIdentifier = hover ? 'hoverBorderWidth' : 'borderWidth',
      borderWidthBackupIdentifier = hover ? 'hoverBorderWidthBackup' : 'borderWidthBackup',
      backgroundColorIdentifier = hover ? 'hoverBackgroundColor' : 'backgroundColor',
      borderColorIdentifier = hover ? 'hoverBorderColor' : 'borderColor';
    // restore original state if there is a backup
    if (dataset[borderWidthBackupIdentifier]) {
      dataset[borderWidthIdentifier] = dataset[borderWidthBackupIdentifier];
      delete dataset[borderWidthBackupIdentifier];
    }
    // do nothing if there is no borderWidth set on the dataset or the borderWidth is a function
    if (!dataset[borderWidthIdentifier] || objects.isFunction(dataset[borderWidthIdentifier])) {
      return;
    }
    let isBorderWidthArray = Array.isArray(dataset[borderWidthIdentifier]),
      isBackgroundColorArray = Array.isArray(dataset[backgroundColorIdentifier]),
      isBorderColorArray = Array.isArray(dataset[borderColorIdentifier]),
      isArray = isBorderWidthArray || isBackgroundColorArray || isBorderColorArray;
    // if none of the properties is an array, simply backup the borderWidth and set it to 0
    if (!isArray && dataset[backgroundColorIdentifier] === dataset[borderColorIdentifier]) {
      dataset[borderWidthBackupIdentifier] = dataset[borderWidthIdentifier];
      dataset[borderWidthIdentifier] = 0;
      return;
    }
    // at least one of the properties is an array, therefore the borderWidth needs to be an array from now on
    let dataLength = (dataset.data || []).length;
    if (!isBorderWidthArray) {
      dataset[borderWidthIdentifier] = arrays.init(dataLength, dataset[borderWidthIdentifier]);
    } else if (dataset[borderWidthIdentifier].length < dataLength) {
      dataset[borderWidthIdentifier].push(...arrays.init(dataLength - dataset[borderWidthIdentifier].length, dataset[borderWidthIdentifier][0]));
    }
    let borderWidth = dataset[borderWidthIdentifier],
      length = borderWidth.length,
      borderWidthBackup = arrays.init(length, null);
    for (let i = 0; i < length; i++) {
      // it makes no difference if the backgroundColor/borderColor is not an array as a not-array-value is applied to every element by chart.js
      let backgroundColor = isBackgroundColorArray ? dataset[backgroundColorIdentifier][i] : dataset[backgroundColorIdentifier],
        borderColor = isBorderColorArray ? dataset[borderColorIdentifier][i] : dataset[borderColorIdentifier];
      borderWidthBackup[i] = borderWidth[i];
      if (backgroundColor === borderColor) {
        borderWidth[i] = 0;
      }
    }
    // only set the backup if at least one of the borderWidths changed
    if (!arrays.equals(borderWidth, borderWidthBackup)) {
      dataset[borderWidthBackupIdentifier] = borderWidthBackup;
    }
  }

  protected _adjustMaxSegments(config: ChartConfig) {
    if (!config || !config.data || !config.type || !scout.isOneOf(config.type, Chart.Type.PIE, Chart.Type.DOUGHNUT, Chart.Type.POLAR_AREA, Chart.Type.RADAR)) {
      return;
    }

    let maxSegments = config.options.maxSegments;
    if (!(maxSegments && config.data.datasets.length && maxSegments < config.data.datasets[0].data.length)) {
      return;
    }
    config.data.datasets.forEach(elem => {
      let newData = elem.data.slice(0, maxSegments);
      newData[maxSegments - 1] = elem.data.slice(maxSegments - 1, elem.data.length).reduce((x: number, y: number) => {
        return x + y;
      }, 0);
      elem.data = newData;
    });

    let newLabels = config.data.labels.slice(0, maxSegments);
    newLabels[maxSegments - 1] = this.chart.session.text('ui.OtherValues');
    config.data.labels = newLabels;
    config.data.maxSegmentsExceeded = true;
  }

  protected _isMaxSegmentsExceeded(config: ChartConfig, index: number): boolean {
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

  /**
   *  Fill temporary variable z for every bubble, if not yet set, and set bubble radius temporary to 1.
   *  This allows the chart to render itself with correct dimensions and with no interfering from bubbles (very large bubbles make the chart grid itself smaller).
   *  Later on in {@link _adjustBubbleSizes}, the bubble sizes will be calculated relative to the chart dimensions and the configured min/max sizes.
   */
  protected _adjustBubbleRadii(config: ChartConfig) {
    if (!config || !config.data || !config.type || config.type !== Chart.Type.BUBBLE) {
      return;
    }

    config.data.datasets.forEach(dataset => dataset.data.forEach((data: BubbleDataPoint) => {
      if (!isNaN(data.r)) {
        data.z = Math.pow(data.r, 2);
      }
      data.r = 1;
    }));
  }

  protected _adjustOnlyIntegers(config: ChartConfig) {
    this.onlyIntegers = true;

    if (!config || !config.data || !config.type) {
      return;
    }

    if (scout.isOneOf(config.type, Chart.Type.BUBBLE, Chart.Type.SCATTER)) {
      this.onlyIntegers = config.data.datasets.every(dataset => dataset.data.every((data: ScatterDataPoint | BubbleDataPoint) => numbers.isInteger(data.x) && numbers.isInteger(data.y)));
    } else {
      this.onlyIntegers = config.data.datasets.every(dataset => dataset.data.every(data => numbers.isInteger(data)));
    }
  }

  protected _adjustTooltip(config: ChartConfig) {
    if (!config) {
      return;
    }

    config.options = $.extend(true, {}, {
      plugins: {
        tooltip: {
          callbacks: {
            title: this._tooltipTitleGenerator,
            items: this._tooltipItemsGenerator,
            label: this._tooltipLabelGenerator,
            labelValue: this._tooltipLabelValueGenerator,
            labelColor: this._tooltipLabelColorGenerator
          }
        }
      }
    }, config.options);

    let tooltip = config.options.plugins.tooltip;

    if (!tooltip.enabled) {
      return;
    }

    tooltip.enabled = false;
    tooltip.external = this._tooltipRenderer;
  }

  protected _generateTooltipTitle(tooltipItems: TooltipItem<any>[]): string | string[] {
    if (!tooltipItems || !tooltipItems.length) {
      return '';
    }
    let tooltipItem = tooltipItems[0],
      chart = tooltipItem.chart as ChartJsChart,
      config = chart.config,
      dataset = tooltipItem.dataset,
      title = [];
    if (scout.isOneOf(config.type, Chart.Type.BUBBLE)) {
      let xAxis = config.options.scales.x,
        yAxis = config.options.scales.y,
        axisLabels = this._getAxisLabels(config);
      // @ts-expect-error
      let xTickLabel = xAxis.ticks.callback(dataset.data[tooltipItem.dataIndex].x, null, null) as string;
      if (xTickLabel) {
        title.push(this._createTooltipAttribute(axisLabels.x, strings.encode(xTickLabel), true));
      }
      // @ts-expect-error
      let yTickLabel = yAxis.ticks.callback(dataset.data[tooltipItem.dataIndex].y, null, null) as string;
      if (yTickLabel) {
        title.push(this._createTooltipAttribute(axisLabels.y, strings.encode(yTickLabel), true));
      }
    } else if (scout.isOneOf(config.type, Chart.Type.SCATTER)) {
      // nop, scatter has the title in the items table
    } else {
      let label = chart.data.labels[tooltipItem.dataIndex] as string;
      title.push(this._createTooltipAttribute(config.options.reformatLabels ? this._formatLabel(label) : label, '', true));
    }
    return title;
  }

  protected _getAxisLabels(config: ChartConfig): { x: string; y: string } {
    let xAxisLabel = config.options.scales.x.title.text,
      yAxisLabel = config.options.scales.y.title.text;
    xAxisLabel = this._resolveAxisLabel(xAxisLabel as string, ChartJsRenderer.ARROW_LEFT_RIGHT);
    yAxisLabel = this._resolveAxisLabel(yAxisLabel as string, '&nbsp;' + ChartJsRenderer.ARROW_UP_DOWN + '&nbsp;');

    return {x: xAxisLabel, y: yAxisLabel};
  }

  protected _resolveAxisLabel(axisLabel: string | (() => string), defaultLabel = ''): string {
    if (objects.isFunction(axisLabel)) {
      axisLabel = axisLabel();
      axisLabel = objects.isString(axisLabel) ? axisLabel : '';
    }
    return axisLabel ? strings.encode(axisLabel) : defaultLabel;
  }

  protected _generateTooltipItems(tooltipItems: TooltipItem<any>[], tooltipLabel: TooltipLabelGenerator, tooltipLabelValue: TooltipLabelValueGenerator, tooltipColor: TooltipLabelColorGenerator): string {
    if (!tooltipItems || !tooltipItems.length) {
      return '';
    }
    let tooltipItem = tooltipItems[0],
      chart = tooltipItem.chart as ChartJsChart,
      config = chart.config,
      xAxisValues = false,
      yAxisValues = false,
      itemsText = '';

    tooltipItems.forEach(tooltipItem => {
      let {label, labelValue, labelColor} = this._getItemDetails(tooltipItem, tooltipLabel, tooltipLabelValue, tooltipColor);
      if (scout.isOneOf(config.type, Chart.Type.SCATTER)) {
        let {x, y} = labelValue as { x: string; y: string };
        xAxisValues ||= objects.isString(x);
        yAxisValues ||= objects.isString(y);
        itemsText += this._createTooltipScatterAttribute(label, x, y, false, labelColor);
      } else {
        itemsText += this._createTooltipAttribute(label, labelValue as string, false, labelColor);
      }
    });

    // tabular representation for scatter tooltip needs an additional header and footer
    if (scout.isOneOf(config.type, Chart.Type.SCATTER)) {
      let tableHeader = '<table><tbody>';
      let axisLabels = this._getAxisLabels(config);
      tableHeader += this._createTooltipScatterAttribute('',
        xAxisValues ? axisLabels.x : '', // do not show axis label if no values are shown
        yAxisValues ? axisLabels.y : '', // do not show axis label if no values are shown
        true);
      let tableFooter = '</tbody></table>';
      itemsText = strings.box(tableHeader, itemsText, tableFooter);
    }

    return itemsText;
  }

  protected _getItemDetails(tooltipItem: TooltipItem<any>, tooltipLabel: TooltipLabelGenerator, tooltipLabelValue: TooltipLabelValueGenerator, tooltipColor: TooltipLabelColorGenerator)
    : { label: string; labelValue: string | { x: string; y: string }; labelColor: string } {
    let label, labelValue, labelColor;
    if (objects.isFunction(tooltipLabel)) {
      label = tooltipLabel(tooltipItem);
      label = objects.isString(label) ? label : '';
    }
    if (objects.isFunction(tooltipLabelValue)) {
      labelValue = tooltipLabelValue(tooltipItem);
      labelValue = objects.isString(labelValue) || objects.isPlainObject(labelValue) ? labelValue : '';
    }
    if (objects.isFunction(tooltipColor)) {
      labelColor = tooltipColor(tooltipItem);
      labelColor = objects.isPlainObject(labelColor) ? (labelColor.backgroundColor || '') : '';
    }
    return {label, labelValue, labelColor};
  }

  protected _generateTooltipLabel(tooltipItem: TooltipItem<any>): string {
    return strings.encode(tooltipItem.dataset.label);
  }

  protected _generateTooltipLabelValue(tooltipItem: TooltipItem<any>): string | { x: string; y: string } {
    let config = tooltipItem.chart.config as ChartConfiguration,
      dataset = tooltipItem.dataset;
    if (config.type === Chart.Type.BUBBLE) {
      return strings.encode(this._formatLabel(dataset.data[tooltipItem.dataIndex].z));
    } else if (config.type === Chart.Type.SCATTER) {
      return {
        x: strings.encode(this._formatLabel(dataset.data[tooltipItem.dataIndex].x)),
        y: strings.encode(this._formatLabel(dataset.data[tooltipItem.dataIndex].y))
      };
    }
    return strings.encode(this._formatLabel(dataset.data[tooltipItem.dataIndex]));
  }

  protected _generateTooltipLabelColor(tooltipItem: TooltipItem<any>): TooltipLabelStyle {
    let config = tooltipItem.chart.config as ChartConfiguration,
      dataset = tooltipItem.dataset,
      legendColor, backgroundColor, borderColor, index;
    if (scout.isOneOf((dataset.type || config.type), Chart.Type.LINE, Chart.Type.BAR, Chart.Type.BAR_HORIZONTAL, Chart.Type.RADAR, Chart.Type.BUBBLE, Chart.Type.SCATTER)) {
      borderColor = dataset.borderColor;
      legendColor = Array.isArray(dataset.legendColor) ? dataset.legendColor[tooltipItem.dataIndex] : dataset.legendColor;
      index = tooltipItem.datasetIndex;
    }
    if (scout.isOneOf(config.type, Chart.Type.PIE, Chart.Type.DOUGHNUT, Chart.Type.POLAR_AREA)) {
      legendColor = Array.isArray(dataset.legendColor) ? dataset.legendColor[tooltipItem.dataIndex] : dataset.legendColor;
      backgroundColor = Array.isArray(dataset.backgroundColor) ? dataset.backgroundColor[tooltipItem.dataIndex] : dataset.backgroundColor;
      backgroundColor = this._adjustColorOpacity(backgroundColor, 1);
      index = tooltipItem.dataIndex;
    }
    if (objects.isFunction(legendColor)) {
      legendColor = legendColor.call(tooltipItem.chart, index);
    }
    let tooltipLabelColor = legendColor || backgroundColor || borderColor;
    if (!tooltipLabelColor || objects.isFunction(tooltipLabelColor)) {
      let defaultTypeTooltipLabelColor;
      if (ChartJs.overrides[config.type] && ChartJs.overrides[config.type].plugins && ChartJs.overrides[config.type].plugins.tooltip && ChartJs.overrides[config.type].plugins.tooltip.callbacks) {
        defaultTypeTooltipLabelColor = ChartJs.overrides[config.type].plugins.tooltip.callbacks.labelColor;
      }
      let defaultTooltipLabelColor = defaultTypeTooltipLabelColor || ChartJs.defaults.plugins.tooltip.callbacks.labelColor;
      tooltipLabelColor = defaultTooltipLabelColor.call(tooltipItem.chart, tooltipItem).backgroundColor;
    }
    return {
      backgroundColor: tooltipLabelColor
    } as TooltipLabelStyle;
  }

  protected _createTooltipAttribute(label: string, value: string, isTitle: boolean, color?: string): string {
    let cssClass = isTitle ? 'attribute title' : 'attribute';
    return '<div class="' + cssClass + '">' +
      (color ? '<div class="color" style="background-color:' + color + '"></div>' : '') +
      (label ? '<label>' + label + '</label>' : '') +
      (value ? '<div class="value">' + value + '</div>' : '') +
      '</div>';
  }

  protected _createTooltipScatterAttribute(label: string, xValue: string, yValue: string, isTitle: boolean, color?: string): string {
    let cssClass = isTitle ? 'attribute title' : 'attribute';
    return '<tr class="' + cssClass + '">' +
      '<td class="color-cell">' +
      (color ? '<div class="color" style="background-color:' + color + '"></div>' : '') +
      '</td>' +
      '<td class="label">' + label + '</td>' +
      (xValue ? '<td class="value">' + xValue + '</td>' : '') +
      (yValue ? '<td class="value">' + yValue + '</td>' : '') +
      '</tr>';
  }

  protected _renderTooltip(context: { chart: ChartJs; tooltip: TooltipModel<any> }) {
    let isHideTooltip = context.tooltip.opacity === 0 || context.tooltip.dataPoints.length < 1;
    if (isHideTooltip) {
      if (this._tooltipTimeoutId) {
        clearTimeout(this._tooltipTimeoutId);
        this._tooltipTimeoutId = undefined;
      }
      if (this._tooltip) {
        this._tooltip.destroy();
        this._tooltip = null;
      }
      return;
    }

    let isTooltipShowing = !!this._tooltip;
    if (isTooltipShowing) {
      this._renderTooltipLater(context);
    } else {
      // clear timeout before creating a new handler.
      // Otherwise, changing the context within the tooltip delay time creates a second handler
      // and the first one will always be executed, since the tooltipTimoutId reference to it is lost
      clearTimeout(this._tooltipTimeoutId);
      this._tooltipTimeoutId = setTimeout(() => this._renderTooltipLater(context), tooltips.DEFAULT_TOOLTIP_DELAY);
    }
  }

  protected _renderTooltipLater(context: { chart: ChartJs; tooltip: TooltipModel<any> }) {
    if (!this.rendered || this.removing) {
      return;
    }
    let tooltip = context.tooltip,
      dataPoints = tooltip.dataPoints;
    if (dataPoints.length < 1) {
      return;
    }
    let firstDataPoint = dataPoints[0],
      chart = firstDataPoint.chart;
    if (!chart.getDatasetMeta(firstDataPoint.datasetIndex).data[firstDataPoint.dataIndex]) {
      return;
    }
    if (this._tooltip) {
      this._tooltip.destroy();
      this._tooltip = null;
    }

    let tooltipOptions = tooltip.options || {} as TooltipOptions,
      tooltipCallbacks = tooltipOptions.callbacks || {} as TooltipCallbacks<any>,
      tooltipTitle = tooltipCallbacks.title as TooltipTitleGenerator,
      tooltipItems = tooltipCallbacks.items,
      tooltipLabel = tooltipCallbacks.label,
      tooltipLabelValue = tooltipCallbacks.labelValue,
      tooltipColor = tooltipCallbacks.labelColor,
      tooltipText = '';

    if (objects.isFunction(tooltipTitle)) {
      tooltipText += arrays.ensure(tooltipTitle(dataPoints)).join('');
    }
    if (objects.isFunction(tooltipItems)) {
      tooltipText += arrays.ensure(tooltipItems(dataPoints, tooltipLabel, tooltipLabelValue, tooltipColor)).join('');
    }

    let positionAndOffset = this._computeTooltipPositionAndOffset(firstDataPoint);
    let offset = new Point(tooltip.caretX + positionAndOffset.offsetX, tooltip.caretY + positionAndOffset.offsetY);

    this._tooltip = scout.create({
      objectType: Tooltip,
      parent: this.chart,
      $anchor: this.$canvas,
      text: tooltipText,
      htmlEnabled: true,
      cssClass: strings.join(' ', 'chart-tooltip', tooltipOptions.cssClass),
      tooltipPosition: positionAndOffset.tooltipPosition,
      tooltipDirection: positionAndOffset.tooltipDirection,
      originProducer: $anchor => {
        const origin = graphics.offsetBounds($anchor);
        origin.height = positionAndOffset.height;
        return origin;
      },
      offsetProducer: origin => offset
    });
    this._tooltip.render();

    this._tooltip.$container
      .css('pointer-events', 'none');

    let reposition = false,
      fontFamily = ((tooltipOptions.titleFont || {}) as FontSpec).family;
    if (fontFamily) {
      this._tooltip.$container
        .css('--chart-tooltip-font-family', fontFamily);
      reposition = true;
    }

    let maxLabelPrefSize = 0;
    this._tooltip.$container.find('label').each((idx, elem) => {
      maxLabelPrefSize = Math.max(maxLabelPrefSize, graphics.prefSize($(elem)).width);
    });
    if (maxLabelPrefSize > 0) {
      this._tooltip.$container
        .css('--chart-tooltip-label-width', Math.min(maxLabelPrefSize, 120) + 'px');
      reposition = true;
    }
    if (reposition) {
      this._tooltip.position();
    }
  }

  protected _computeTooltipPositionAndOffset(tooltipItem: TooltipItem<any>): { tooltipPosition: 'top' | 'bottom'; tooltipDirection: 'left' | 'right'; offsetX: number; offsetY: number; height: number } {
    let tooltipPosition: 'top' | 'bottom' = 'top',
      tooltipDirection: 'left' | 'right' = 'right',
      offsetX = 0,
      offsetY = 0,
      height = 0;

    let chart = tooltipItem.chart as ChartJsChart,
      datasetIndex = tooltipItem.datasetIndex,
      dataIndex = tooltipItem.dataIndex,
      config = chart.config,
      datasets = config.data.datasets,
      dataset = datasets[datasetIndex],
      value = dataset.data[dataIndex];

    if (this._isHorizontalBar(config)) {
      if (objects.isPlainObject(value) && objects.isArray(value.x) && value.x.length === 2) {
        let avg = (value.x[0] + value.x[1]) / 2;
        tooltipDirection = avg < 0 ? 'left' : 'right';
      } else {
        tooltipDirection = (value as number) < 0 ? 'left' : 'right';
      }
    } else if ((dataset.type || config.type) === Chart.Type.BAR) {
      tooltipPosition = (value as number) < 0 ? 'bottom' : 'top';
    } else if (scout.isOneOf(config.type, Chart.Type.PIE, Chart.Type.DOUGHNUT, Chart.Type.POLAR_AREA)) {
      let element = (chart.getDatasetMeta(datasetIndex).data[dataIndex] as unknown as ArcElement).getProps(['startAngle', 'endAngle']);
      let startAngle = element.startAngle,
        endAngle = element.endAngle,
        angle = (startAngle + endAngle) / 2;
      tooltipPosition = (0 <= angle && angle < Math.PI) ? 'bottom' : 'top';
      tooltipDirection = (-Math.PI / 2 <= angle && angle < Math.PI / 2) ? 'right' : 'left';
    } else if (config.type === Chart.Type.RADAR) {
      let element = (chart.getDatasetMeta(datasetIndex).data[dataIndex] as unknown as PointElement).getProps(['angle']);
      let angle = element.angle as number;
      tooltipPosition = (0 <= angle && angle < Math.PI) ? 'bottom' : 'top';
      tooltipDirection = (-Math.PI / 2 <= angle && angle < Math.PI / 2) ? 'right' : 'left';
    } else if (scout.isOneOf(config.type, Chart.Type.BUBBLE, Chart.Type.SCATTER)) {
      let element = chart.getDatasetMeta(datasetIndex).data[dataIndex];
      let chartArea = chart.chartArea,
        mid = chartArea.left + (chartArea.width / 2);
      tooltipDirection = element.x < mid ? 'left' : 'right';
    }

    if (this._isHorizontalBar(config)) {
      let element = (chart.getDatasetMeta(datasetIndex).data[dataIndex] as unknown as BarElement).getProps(['height', 'width']);
      height = element.height;
      let width = element.width,
        // golden ratio: (a + b) / a = a / b = PHI
        // and a + b = width
        // -> b = width / (PHI + 1)
        b = width / (PHI + 1);

      offsetY = -height / 2;
      offsetX = tooltipDirection === 'left' ? b : -b;
    } else if (scout.isOneOf(config.type, Chart.Type.LINE, Chart.Type.BUBBLE, Chart.Type.SCATTER, Chart.Type.RADAR) || dataset.type === Chart.Type.LINE) {
      let element = chart.getDatasetMeta(datasetIndex).data[dataIndex] as unknown as PointElement;
      let options = element.options as unknown as PointHoverOptions,
        offset = options.hoverRadius + options.hoverBorderWidth;
      if (config.type === Chart.Type.BUBBLE) {
        offset += (value as BubbleDataPoint).r;
      }

      height = 2 * offset;
      offsetY = -offset;
    } else if (scout.isOneOf(config.type, Chart.Type.PIE, Chart.Type.DOUGHNUT, Chart.Type.POLAR_AREA)) {
      let element = (chart.getDatasetMeta(datasetIndex).data[dataIndex] as unknown as ArcElement).getProps(['startAngle', 'endAngle', 'innerRadius', 'outerRadius']);
      let startAngle = element.startAngle,
        endAngle = element.endAngle,
        angle = (startAngle + endAngle) / 2,
        innerRadius = element.innerRadius,
        outerRadius = element.outerRadius,
        offset = (outerRadius - innerRadius) / 2;
      offsetX = offset * Math.cos(angle);
      offsetY = offset * Math.sin(angle);
    }

    return {tooltipPosition, tooltipDirection, offsetX, offsetY, height};
  }

  protected _adjustGrid(config: ChartConfig) {
    if (!config) {
      return;
    }

    config.options = $.extend(true, {}, config.options);

    this._adjustScalesR(config);
    this._adjustScalesXY(config);
  }

  protected _adjustScalesR(config: ChartConfig) {
    if (!config || !config.type || !config.options) {
      return;
    }

    if (scout.isOneOf(config.type, Chart.Type.POLAR_AREA, Chart.Type.RADAR)) {
      config.options = $.extend(true, {}, {
        scales: {
          r: {}
        }
      }, config.options);
    }

    let options = config.options,
      scales = options ? options.scales : {};
    if (scales && scales.r) {
      scales.r = $.extend(true, {}, {
        minSpaceBetweenTicks: 35,
        beginAtZero: true,
        angleLines: {
          display: false
        },
        ticks: {
          callback: this._labelFormatter
        },
        pointLabels: {
          font: {
            size: ChartJs.defaults.font.size
          }
        }
      }, scales.r);
    }
  }

  protected _adjustScalesXY(config: ChartConfig) {
    if (!config || !config.type || !config.options) {
      return;
    }

    if (scout.isOneOf(config.type, Chart.Type.BAR, Chart.Type.LINE, Chart.Type.BUBBLE)) {
      config.options = $.extend(true, {}, {
        scales: {
          x: {
            minSpaceBetweenTicks: 150
          },
          y: {
            minSpaceBetweenTicks: 35
          }
        }
      }, config.options);
    } else if (scout.isOneOf(config.type, Chart.Type.SCATTER)) {
      config.options = $.extend(true, {}, {
        scales: {
          x: {
            minSpaceBetweenTicks: 35,
            ticks: {
              padding: 10
            }
          },
          y: {
            minSpaceBetweenTicks: 35,
            ticks: {
              padding: 10
            }
          }
        }
      }, config.options);
    }

    this._adjustXAxis(config);
    this._adjustYAxis(config);
  }

  protected _adjustXAxis(config: ChartConfig) {
    if (!config || !config.type || !config.options || !config.options.scales || !config.options.scales.x) {
      return;
    }

    let type = config.type,
      scales = config.options.scales;

    if (this._isHorizontalBar(config) || scout.isOneOf(type, Chart.Type.BUBBLE, Chart.Type.SCATTER)) {
      scales.x = $.extend(true, {}, {
        beginAtZero: this._isHorizontalBar(config),
        offset: type === Chart.Type.BUBBLE,
        grid: {
          drawTicks: false
        },
        border: {
          display: false
        },
        ticks: {
          padding: 5
        }
      }, scales.x);
    } else {
      scales.x = $.extend(true, {}, {
        offset: true,
        grid: {
          display: false
        },
        border: {
          display: false
        }
      }, scales.x);
    }
    if (this._isHorizontalBar(config) || scout.isOneOf(type, Chart.Type.BUBBLE, Chart.Type.SCATTER) || config.options.reformatLabels) {
      scales.x = $.extend(true, {}, {
        ticks: {
          callback: this._xLabelFormatter
        }
      }, scales.x);
    }
    scales.x.afterCalculateLabelRotation = this._xAxisFitter;
  }

  protected _adjustYAxis(config: ChartConfig) {
    if (!config || !config.type || !config.options || !config.options.scales || !config.options.scales.y) {
      return;
    }

    let type = config.type,
      scales = config.options.scales;

    if (this._isHorizontalBar(config)) {
      scales.y = $.extend(true, {}, {
        grid: {
          display: false
        },
        border: {
          display: false
        }
      }, scales.y);
    } else {
      scales.y = $.extend(true, {}, {
        beginAtZero: !scout.isOneOf(type, Chart.Type.BUBBLE, Chart.Type.SCATTER),
        grid: {
          drawTicks: false
        },
        border: {
          display: false
        },
        ticks: {
          padding: 5
        }
      }, scales.y);
    }
    if (!this._isHorizontalBar(config) || config.options.reformatLabels) {
      scales.y = $.extend(true, {}, {
        ticks: {
          callback: this._yLabelFormatter
        }
      }, scales.y);
    }
    scales.y.afterFit = this._yAxisFitter;
  }

  protected _adjustPlugins(config: ChartConfig) {
    this._adjustPluginsDatalabels(config);
  }

  protected _adjustPluginsDatalabels(config: ChartConfig) {
    if (!config || !config.type || !config.options || !config.options.plugins || !config.options.plugins.datalabels || !config.options.plugins.datalabels.display) {
      return;
    }

    let plugins = config.options.plugins;
    if (scout.isOneOf(config.type, Chart.Type.PIE, Chart.Type.DOUGHNUT)) {
      plugins.datalabels = $.extend(true, {}, {
        formatter: this._radialChartDatalabelsFormatter
      }, plugins.datalabels);
      plugins.datalabels.display = this._radialChartDatalabelsDisplayHandler;
      // since the this._radialChartDatalabelsDisplayHandler depends on values that are animated, we need to update the labels during the animation
      this._updatingDatalabels = false;
      let animation = config.options.animation || {},
        onProgress = animation.onProgress,
        onComplete = animation.onComplete,
        updateDatalabels = animation => {
          if (this._updatingDatalabels) {
            return;
          }
          this._updatingDatalabels = true;
          // invert the _actives of the datalabel plugin and call its afterEvent-hook
          // this hook will update its _actives to chart.getActiveElements() and recalculate the labels for all elements that changed
          // setting _actives to the complement of chart.getActiveElements() guarantees that all labels are updated
          let chart = animation.chart,
            metas = chart.getSortedVisibleDatasetMetas(),
            activeElements = [...chart.getActiveElements()],
            inactiveElements = [];

          metas.forEach((meta, datasetIndex) => {
            meta.data.forEach((element, index) => {
              let activeIndex = arrays.findIndex(activeElements, activeElement => activeElement.datasetIndex === datasetIndex && activeElement.index === index);
              if (activeIndex > 0) {
                activeElements.splice(activeIndex, 1);
              } else {
                inactiveElements.push({
                  datasetIndex: datasetIndex,
                  index: index,
                  element: element
                });
              }
            });
          });

          // the datalabels plugin stores its data on the chart in $datalabels (see EXPANDO_KEY in chartjs-plugin-datalabels)
          chart['$' + ChartDataLabels.id]._actives = inactiveElements;
          ChartDataLabels.afterEvent(chart, null, null);
          this._updatingDatalabels = false;
        },
        updateDatalabelsAndDefaultCallback = (animation, defaultCallback) => {
          updateDatalabels(animation);
          if (defaultCallback) {
            defaultCallback(animation);
          }
        };

      config.options.animation = $.extend(true, {}, config.options.animation, {
        onProgress: animation => updateDatalabelsAndDefaultCallback(animation, onProgress),
        onComplete: animation => updateDatalabelsAndDefaultCallback(animation, onComplete)
      });

    } else if (scout.isOneOf(config.type, Chart.Type.BAR, Chart.Type.LINE, Chart.Type.POLAR_AREA, Chart.Type.RADAR, Chart.Type.BUBBLE)) {
      plugins.datalabels = $.extend(true, {}, {
        backgroundColor: this._datalabelBackgroundColorHandler,
        borderRadius: 3
      }, plugins.datalabels);
      plugins.datalabels.display = 'auto';
    } else if (scout.isOneOf(config.type, Chart.Type.SCATTER)) {
      plugins.datalabels = $.extend(true, {}, {
        backgroundColor: this._datalabelBackgroundColorHandler,
        borderRadius: 3,
        anchor: 'end',
        align: 'top',
        offset: 3
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

  protected _formatLabel(label: number | string): string {
    return this._formatLabelMap(label, null, this._getNumberFormatter());
  }

  protected _getNumberFormatter(): NumberFormatter {
    if (this.chartJs && this.chartJs.config && this.chartJs.config.options) {
      return this.chartJs.config.options.numberFormatter;
    }
  }

  protected _formatXLabel(label: number | string): string {
    return this._formatLabelMap(label, this._getXLabelMap(), this._getNumberFormatter());
  }

  protected _formatYLabel(label: number | string): string {
    return this._formatLabelMap(label, this._getYLabelMap(), this._getNumberFormatter());
  }

  protected _getXLabelMap(): Record<number | string, string> {
    return this._getLabelMap('xLabelMap');
  }

  protected _getYLabelMap(): Record<number | string, string> {
    return this._getLabelMap('yLabelMap');
  }

  protected _getLabelMap(identifier): Record<number | string, string> {
    if (this.chartJs && this.chartJs.config && this.chartJs.config.options) {
      return this.chartJs.config.options[identifier];
    }
  }

  protected _formatLabelMap(label: number | string, labelMap: Record<string, string>, numberFormatter: NumberFormatter): string {
    if (labelMap) {
      return labelMap[label];
    }
    // @ts-expect-error
    if (isNaN(label) || typeof label === 'string') {
      return '' + label;
    }
    if (numberFormatter) {
      return numberFormatter(label, this._formatNumberLabel.bind(this));
    }
    return this._formatNumberLabel(label);
  }

  protected _formatNumberLabel(label: number | string): string {
    // @ts-expect-error
    if (isNaN(label)) {
      return '' + label;
    }
    // @ts-expect-error
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
        if (abs >= 1000) {
          abs = abs / 1000;
          abbreviation = ' ' + abbreviations[i];
        } else {
          break;
        }
      }
    }
    // @ts-expect-error
    return this.session.locale.decimalFormat.format(Math.sign(label) * abs) + abbreviation;
  }

  protected _fitXAxis(xAxis: Scale<CartesianScaleOptions>) {
    if (!xAxis || xAxis.labelRotation === 0) {
      return;
    }
    let maxHeight = this.maxXAxesTicksHeight,
      fontDefaults = ChartJs.defaults.font,
      ticksDefaults = (ChartJs.defaults.scale as CartesianScaleOptions).ticks,
      ticksFontDefaults = (ticksDefaults.font || {}) as FontSpec,
      fontSize,
      maxRotation;
    if (this.chartJs) {
      let chartArea = this.chartJs.chartArea,
        chartAreaHeight = Math.abs(chartArea.top - chartArea.bottom);
      maxHeight = Math.min(maxHeight, chartAreaHeight / 3);
    }
    if (xAxis.options && xAxis.options.ticks) {
      maxRotation = xAxis.options.ticks.maxRotation;
      let ticksFont = (xAxis.options.ticks.font || {}) as FontSpec;
      fontSize = ticksFont.size;
    }
    maxRotation = maxRotation || ticksDefaults.maxRotation;
    fontSize = fontSize || ticksFontDefaults.size || fontDefaults.size;
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
    // @ts-expect-error
    let labelSizes = xAxis._labelSizes || {},
      widest = labelSizes.widest || {};
    if (widest.width > maxLabelLength) {
      let measureText = xAxis.ctx.measureText.bind(xAxis.ctx);
      xAxis.ticks.forEach(tick => {
        tick.label = strings.truncateText(tick.label as string, maxLabelLength, measureText);
      });
      // reset label sizes, chart.js will recalculate them using the new truncated labels
      // @ts-expect-error
      xAxis._labelSizes = undefined;
    }
  }

  protected _fitYAxis(yAxis: Scale<CartesianScaleOptions>) {
    if (!yAxis) {
      return;
    }
    let padding = 0,
      tickLength = 0;
    if (yAxis.options && yAxis.options.ticks) {
      padding = yAxis.options.ticks.padding || 0;
    }
    if (yAxis.options && yAxis.options.grid) {
      tickLength = yAxis.options.grid.tickLength || 0;
    }
    // @ts-expect-error
    let labelSizes = yAxis._labelSizes || {},
      widest = labelSizes.widest || {};
    if (widest.width > yAxis.maxWidth - padding) {
      let horizontalSpace = yAxis.maxWidth - padding - tickLength,
        measureText = yAxis.ctx.measureText.bind(yAxis.ctx);
      yAxis.ticks.forEach(tick => {
        tick.label = strings.truncateText(tick.label as string, horizontalSpace, measureText);
      });
    }
  }

  protected _displayDatalabelsOnRadialChart(context: Context): boolean {
    let element = (context.chart.getDatasetMeta(context.datasetIndex).data[context.dataIndex] as unknown as ArcElement).getProps(['startAngle', 'endAngle', 'innerRadius', 'outerRadius']);
    // Compute the biggest circle that fits inside sector/arc with center in the middle between inner and outer radius.
    // First compute a circle C1 that touches the straight boundaries of the sector/arc. Then compute a circle C2 that touches the inner and the outer radius.
    // The smaller one of these two circles is the biggest possible circle that fits inside sector/arc with center in the middle between inner and outer radius.
    // circle C1:
    let midRadius = (element.outerRadius + element.innerRadius) / 2,
      // If the difference between the angles is greater than pi, it is no longer possible for a circle to be inside the sector/arc and touch both straight boundaries.
      angle = Math.min((element.endAngle - element.startAngle), Math.PI) / 2,
      radius1 = Math.abs(Math.sin(angle)) * midRadius,
      diameter1 = radius1 * 2,
      // circle C2:
      diameter2 = element.outerRadius - element.innerRadius;
    return Math.min(diameter1, diameter2) > this.minRadialChartDatalabelSpace;
  }

  protected _formatDatalabelsOnRadialChart(value: number, context: Context): string {
    let sum = this._computeSumOfVisibleElements(context),
      dataset = context.dataset,
      roundingError = 0,
      roundedResults = [];
    for (let i = 0; i < context.dataIndex + 1; i++) {
      let result = (dataset.data[i] as number) / sum * 100 - roundingError,
        roundedResult = Math.round(result);
      roundingError = roundedResult - result;
      roundedResults.push(roundedResult + '%');
    }
    return roundedResults[context.dataIndex];
  }

  protected _computeSumOfVisibleElements(context: Context): number {
    let dataset = context.dataset,
      chart = context.chart,
      sum = 0;
    for (let i = 0; i < dataset.data.length; i++) {
      if (chart.getDataVisibility(i)) {
        sum += dataset.data[i] as number;
      }
    }
    return sum;
  }

  protected _formatDatalabels(value: number | ScatterDataPoint | BubbleDataPoint, context: Context): string {
    let config = context.chart.config as ChartConfiguration;
    if (config.type === Chart.Type.BUBBLE) {
      return this._formatLabel((value as BubbleDataPoint).z);
    } else if (config.type === Chart.Type.SCATTER) {
      return strings.join(' / ', this._formatLabel((value as ScatterDataPoint).x), this._formatLabel((value as ScatterDataPoint).y));
    }
    return this._formatLabel(value as number);
  }

  protected _getBackgroundColorOfDataset(context: Context): Color {
    return context.dataset.backgroundColor as Color;
  }

  protected _adjustColors(config: ChartConfig) {
    this._adjustColorSchemeCssClass(config);
    this._adjustDatasetColors(config);
    this._adjustLegendColors(config);
    this._adjustScalesRColors(config);
    this._adjustScalesXYColors(config);
    this._adjustPluginColors(config);
  }

  protected _adjustColorSchemeCssClass(config: ChartConfig) {
    if (!config || !config.options) {
      return;
    }
    this.colorSchemeCssClass = colorSchemes.getCssClasses(config.options.colorScheme).join(' ');
  }

  protected _adjustDatasetColors(config: ChartConfig) {
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
      const getColor = colorsArray => {
        let candidate = colorsArray[idx];
        if (multipleColorsPerDataset && !Array.isArray(candidate)) {
          // we want multiple colors -> get the parent array
          candidate = colorsArray;
        }
        return candidate;
      };

      let backgroundColor = getColor(colors.backgroundColors),
        borderColor = getColor(colors.borderColors),
        hoverBackgroundColor = getColor(colors.hoverBackgroundColors),
        hoverBorderColor = getColor(colors.hoverBorderColors),
        legendColor = getColor(colors.legendColors),
        pointHoverBackgroundColor = getColor(colors.pointHoverColors);

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
      if (scout.isOneOf(type, Chart.Type.LINE, Chart.Type.RADAR) || (type === Chart.Type.BAR && elem.type === Chart.Type.LINE)) {
        setProperty('pointHoverBackgroundColor', pointHoverBackgroundColor);
        setProperty('pointBorderColor', this.firstOpaqueBackgroundColor);
        setProperty('pointHoverBorderColor', this.firstOpaqueBackgroundColor);
      }
      if (checkable) {
        const datasetLength = elem.data.length,
          ensureColorArray = color => {
            if (Array.isArray(color)) {
              return color;
            }
            return arrays.init(datasetLength, color);
          };
        if (scout.isOneOf(type, Chart.Type.PIE, Chart.Type.DOUGHNUT, Chart.Type.POLAR_AREA, Chart.Type.BUBBLE, Chart.Type.SCATTER) || (type === Chart.Type.BAR && (elem.type || Chart.Type.BAR) === Chart.Type.BAR)) {
          let uncheckedBackgroundColor = ensureColorArray(backgroundColor),
            uncheckedHoverBackgroundColor = ensureColorArray(hoverBackgroundColor),

            checkedBackgroundColor = ensureColorArray(getColor(colors.checkedBackgroundColors)),
            checkedHoverBackgroundColor = ensureColorArray(getColor(colors.checkedHoverBackgroundColors));

          setProperty('uncheckedBackgroundColor', uncheckedBackgroundColor);
          setProperty('uncheckedHoverBackgroundColor', uncheckedHoverBackgroundColor);
          setProperty('checkedBackgroundColor', checkedBackgroundColor);
          setProperty('checkedHoverBackgroundColor', checkedHoverBackgroundColor);

          setProperty('backgroundColor', elem.uncheckedBackgroundColor);
          setProperty('hoverBackgroundColor', elem.uncheckedHoverBackgroundColor);
        } else if (scout.isOneOf(type, Chart.Type.LINE, Chart.Type.RADAR) || (type === Chart.Type.BAR && elem.type === Chart.Type.LINE)) {
          let uncheckedPointBackgroundColor = ensureColorArray(pointHoverBackgroundColor),
            uncheckedPointHoverBackgroundColor = ensureColorArray(pointHoverBackgroundColor),

            checkedPointBackgroundColor = ensureColorArray(borderColor),
            checkedPointHoverBackgroundColor = ensureColorArray(hoverBorderColor || borderColor);

          setProperty('uncheckedPointBackgroundColor', uncheckedPointBackgroundColor);
          setProperty('uncheckedPointHoverBackgroundColor', uncheckedPointHoverBackgroundColor);
          setProperty('checkedPointBackgroundColor', checkedPointBackgroundColor);
          setProperty('checkedPointHoverBackgroundColor', checkedPointHoverBackgroundColor);

          setProperty('pointBackgroundColor', elem.uncheckedPointBackgroundColor);
          setProperty('pointHoverBackgroundColor', elem.uncheckedPointHoverBackgroundColor);

          let uncheckedPointRadius = arrays.init(datasetLength, ((config.options.elements || {}).point || {}).radius || ChartJs.defaults.elements.point.radius),
            checkedPointRadius = arrays.init(datasetLength, (((config.options.elements || {}).point || {}).hoverRadius || ChartJs.defaults.elements.point.hoverRadius) as number - 1);
          setProperty('uncheckedPointRadius', uncheckedPointRadius);
          setProperty('checkedPointRadius', checkedPointRadius);

          setProperty('pointRadius', elem.uncheckedPointRadius);
        }
      }
      this._adjustDatasetBorderWidths(elem);
    });
    if (checkable) {
      this._checkItems(config);
    }
  }

  protected _computeDatasetColors(config: ChartConfig, multipleColorsPerDataset: boolean): DatasetColors {
    if (!config || !config.data || !config.type) {
      return {};
    }

    let data = config.data,
      type = config.type,
      colors: DatasetColors = {};

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

  protected _computeDatasetColorsAutoColor(config: ChartConfig, multipleColorsPerDataset: boolean): DatasetColors {
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

  protected _computeBackgroundColor(type: ChartType, index: number, checkable: boolean): string {
    return styles.get([this.colorSchemeCssClass, type + '-chart' + (checkable ? ' checkable' : ''), 'elements', 'color' + (index % this.numSupportedColors)], 'fill').fill;
  }

  protected _computeBorderColor(type: ChartType, index: number): string {
    let additionalProperties;
    if (scout.isOneOf(type, Chart.Type.PIE, Chart.Type.DOUGHNUT, Chart.Type.POLAR_AREA)) {
      additionalProperties = {stroke: this.firstOpaqueBackgroundColor};
    }
    return styles.get([this.colorSchemeCssClass, type + '-chart', 'elements', 'stroke-color' + (index % this.numSupportedColors)], 'stroke', additionalProperties).stroke;
  }

  protected _computeHoverBackgroundColor(type: ChartType, index: number, checkable: boolean): string {
    return styles.get([this.colorSchemeCssClass, type + '-chart' + (checkable ? ' checkable' : ''), 'elements', 'color' + (index % this.numSupportedColors) + ' hover'], 'fill').fill;
  }

  protected _computeHoverBorderColor(type: ChartType, index: number): string {
    let additionalProperties;
    if (scout.isOneOf(type, Chart.Type.PIE, Chart.Type.DOUGHNUT, Chart.Type.POLAR_AREA)) {
      additionalProperties = {stroke: this.firstOpaqueBackgroundColor};
    }
    return styles.get([this.colorSchemeCssClass, type + '-chart', 'elements', 'stroke-color' + (index % this.numSupportedColors) + ' hover'], 'stroke', additionalProperties).stroke;
  }

  protected _computeCheckedBackgroundColor(type: ChartType, index: number, checkable: boolean): string {
    return styles.get([this.colorSchemeCssClass, type + '-chart' + (checkable ? ' checkable' : ''), 'elements', 'color' + (index % this.numSupportedColors) + ' checked'], 'fill').fill;
  }

  protected _computeCheckedHoverBackgroundColor(type: ChartType, index: number, checkable: boolean): string {
    return styles.get([this.colorSchemeCssClass, type + '-chart' + (checkable ? ' checkable' : ''), 'elements', 'color' + (index % this.numSupportedColors) + ' hover checked'], 'fill').fill;
  }

  protected _computeLegendColor(type: ChartType, index: number): string {
    return styles.get([this.colorSchemeCssClass, type + '-chart', 'elements', 'color' + (index % this.numSupportedColors) + ' legend'], 'fill').fill;
  }

  protected _computePointHoverColor(type: ChartType, index: number): string {
    return styles.get([this.colorSchemeCssClass, type + '-chart', 'elements', 'color' + (index % this.numSupportedColors) + ' point hover'], 'fill').fill;
  }

  protected _computeDatasetColorsChartValueGroups(config: ChartConfig, multipleColorsPerDataset: boolean): DatasetColors {
    if (!config || !config.type || !this.chart.data) {
      return {};
    }

    let type = config.type,
      checkable = config.options && config.options.checkable,
      transparent = config.options && config.options.transparent,
      colors: DatasetColors = {
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
      } else if (scout.isOneOf(type, Chart.Type.BUBBLE, Chart.Type.SCATTER)) {
        backgroundOpacity = 0.2;
        hoverBackgroundOpacity = 0.35;
        hoverBackgroundDarker = 0;
      }

      const backgroundColors = [],
        borderColors = [],
        hoverBackgroundColors = [],
        hoverBorderColors = [],
        checkedBackgroundColors = [],
        checkedHoverBackgroundColors = [],
        legendColors = [],
        pointHoverColors = [];

      let colorHexValues = arrays.ensure(elem.colorHexValue);

      const datasetLength = arrays.length(elem.values as any[]);
      if (colorHexValues.length && colorHexValues.length < datasetLength) {
        // repeat colors for the whole dataset
        colorHexValues = arrays.init(datasetLength, null).map((elem, idx) => colorHexValues[idx % colorHexValues.length]);
      }

      colorHexValues.forEach(colorHexValue => {
        const rgbColor = styles.hexToRgb(colorHexValue),
          adjustColor = (opacity, darker) => this._adjustColorOpacity(styles.darkerColor(rgbColor, darker), opacity);

        backgroundColors.push(adjustColor((checkable || transparent) ? uncheckedBackgroundOpacity : backgroundOpacity, 0));
        borderColors.push(adjustColor(1, 0));
        hoverBackgroundColors.push(adjustColor((checkable || transparent) ? uncheckedHoverBackgroundOpacity : hoverBackgroundOpacity, (checkable || transparent) ? 0 : hoverBackgroundDarker));
        hoverBorderColors.push(adjustColor(1, hoverBorderDarker));
        checkedBackgroundColors.push(adjustColor(checkedBackgroundOpacity, checkedBackgroundDarker));
        checkedHoverBackgroundColors.push(adjustColor(checkedHoverBackgroundOpacity, checkedHoverBackgroundDarker));
        legendColors.push(adjustColor(1, 0));
        pointHoverColors.push(adjustColor(1, 0));
      });

      colors.backgroundColors.push(backgroundColors);
      colors.borderColors.push(borderColors);
      colors.hoverBackgroundColors.push(hoverBackgroundColors);
      colors.hoverBorderColors.push(hoverBorderColors);
      colors.checkedBackgroundColors.push(checkedBackgroundColors);
      colors.checkedHoverBackgroundColors.push(checkedHoverBackgroundColors);
      colors.legendColors.push(legendColors);
      colors.pointHoverColors.push(pointHoverColors);
    });
    colors.datalabelColor = this._computeDatalabelColor(type);

    return colors;
  }

  protected _adjustColorOpacity(color: string, opacity = 1): string {
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

  protected _adjustRgbColorOpacity(rgbColor: string, opacity = 1): string {
    if (!rgbColor || rgbColor.indexOf('rgb') !== 0) {
      return rgbColor;
    }
    let rgba = styles.rgb(rgbColor);
    rgba.alpha = opacity;
    return 'rgba(' + rgba.red + ', ' + rgba.green + ', ' + rgba.blue + ', ' + rgba.alpha + ')';
  }

  protected _adjustHexColorOpacity(hexColor: string, opacity = 1): string {
    if (!hexColor || hexColor.indexOf('#') !== 0 || !(hexColor.length === 4 || hexColor.length === 5 || hexColor.length === 7 || hexColor.length === 9)) {
      return hexColor;
    }
    return this._adjustRgbColorOpacity(styles.hexToRgb(hexColor), opacity);
  }

  protected _adjustLegendColors(config: ChartConfig) {
    if (!config || !config.type || !config.options) {
      return;
    }

    config.options = $.extend(true, {}, config.options, {
      plugins: {
        legend: {
          labels: {
            color: this._computeLabelColor(config.type),
            generateLabels: this._legendLabelGenerator
          }
        }
      }
    });
  }

  protected _computeLabelColor(type: ChartType): string {
    return styles.get([this.colorSchemeCssClass, type + '-chart', 'elements', 'label'], 'fill').fill;
  }

  protected _generateLegendLabels(chart: ChartJsChart): LegendItem[] {
    let config = chart.config,
      defaultTypeGenerateLabels;
    if (ChartJs.overrides[config.type] && ChartJs.overrides[config.type].plugins && ChartJs.overrides[config.type].plugins.legend && ChartJs.overrides[config.type].plugins.legend.labels) {
      defaultTypeGenerateLabels = ChartJs.overrides[config.type].plugins.legend.labels.generateLabels;
    }
    let defaultGenerateLabels = defaultTypeGenerateLabels || ChartJs.defaults.plugins.legend.labels.generateLabels;
    let labels = defaultGenerateLabels.call(chart, chart);
    if (this.removing) {
      return labels;
    }
    let data = config.data,
      measureText = chart.ctx.measureText.bind(chart.ctx),
      legend = chart.legend,
      legendProps = legend.getProps(['width', 'maxWidth']) as { width: number; maxWidth: number },
      legendLabelOptions = ((legend || {}).options || {}).labels || {} as LegendOptions<any>['labels'],
      boxWidth = legendLabelOptions.boxWidth || 0,
      padding = legendLabelOptions.padding || 0,
      horizontalSpace;
    if (scout.isOneOf(config.options.plugins.legend.position, Chart.Position.LEFT, Chart.Position.RIGHT)) {
      if (legendProps.maxWidth || legend.width) {
        horizontalSpace = Math.max((legendProps.maxWidth || legend.width) - boxWidth - 2 * padding, 0);
      }
      horizontalSpace = Math.min(250, horizontalSpace || 0, this.$canvas.cssWidth() / 3);

    } else {
      horizontalSpace = Math.min(250, this.$canvas.cssWidth() * 2 / 3);
    }
    labels.forEach((elem, idx) => {
      elem.text = strings.truncateText(elem.text, horizontalSpace, measureText);
      let dataset = data.datasets[idx],
        legendColor, borderColor, backgroundColor;
      if (dataset && scout.isOneOf((dataset.type || config.type), Chart.Type.LINE, Chart.Type.BAR, Chart.Type.RADAR, Chart.Type.BUBBLE, Chart.Type.SCATTER)) {
        legendColor = arrays.ensure(dataset.legendColor)[0];
        borderColor = this._adjustColorOpacity(dataset.borderColor as string, 1);
      } else if (data.datasets.length && scout.isOneOf(config.type, Chart.Type.PIE, Chart.Type.DOUGHNUT, Chart.Type.POLAR_AREA)) {
        dataset = data.datasets[0];
        legendColor = Array.isArray(dataset.legendColor) ? dataset.legendColor[idx] : dataset.legendColor;
        backgroundColor = Array.isArray(dataset.backgroundColor) ? dataset.backgroundColor[idx] : dataset.backgroundColor;
        backgroundColor = this._adjustColorOpacity(backgroundColor, 1);
      }
      if (objects.isFunction(legendColor)) {
        legendColor = legendColor.call(chart, idx);
      }
      let fillStyle = legendColor || backgroundColor || borderColor;
      if (!objects.isFunction(fillStyle)) {
        elem.fillStyle = fillStyle;
        elem.strokeStyle = fillStyle;
      }
    });
    return labels;
  }

  protected _adjustScalesRColors(config: ChartConfig) {
    if (!config || !config.type || !config.options || !config.options.scales || !config.options.scales.r) {
      return;
    }

    let labelColor = this._computeLabelColor(config.type),
      labelBackdropColor = this._computeLabelBackdropColor(config.type),
      gridColor = this._computeGridColor(config.type),
      scaleTicksColor = this._computeScaleTicksColor(config.type);

    config.options.scales.r.ticks = $.extend(true, {}, config.options.scales.r.ticks, {
      color: scaleTicksColor,
      backdropColor: labelBackdropColor
    });
    config.options.scales.r.pointLabels = $.extend(true, {}, config.options.scales.r.pointLabels, {
      color: labelColor
    });
    config.options.scales.r.grid = $.extend(true, {}, config.options.scales.r.grid, {
      color: gridColor
    });
  }

  protected _computeLabelBackdropColor(type: ChartType): string {
    return styles.get([this.colorSchemeCssClass, type + '-chart', 'elements', 'label-backdrop'], 'fill', {fill: this.firstOpaqueBackgroundColor}).fill;
  }

  protected _computeGridColor(type: ChartType): string {
    return styles.get([this.colorSchemeCssClass, type + '-chart', 'elements', 'grid'], 'fill').fill;
  }

  protected _computeScaleTicksColor(type: ChartType): string {
    return styles.get([this.colorSchemeCssClass, type + '-chart', 'elements', 'scale-ticks'], 'fill').fill;
  }

  protected _adjustScalesXYColors(config: ChartConfig) {
    if (!config || !config.type || !config.options || !config.options.scales) {
      return;
    }

    let axes = [];
    if (config.options.scales.x) {
      axes.push(config.options.scales.x);
    }
    if (config.options.scales.y) {
      axes.push(config.options.scales.y);
    }

    if (!axes.length) {
      return;
    }

    let labelColor = this._computeLabelColor(config.type),
      gridColor = this._computeGridColor(config.type),
      axisLabelColor = this._computeAxisLabelColor(config.type);

    axes.forEach(elem => {
      elem.grid = $.extend(true, {}, elem.grid, {
        color: gridColor
      });
      elem.ticks = $.extend(true, {}, elem.ticks, {
        color: labelColor
      });
      elem.title = $.extend(true, {}, elem.title, {
        color: axisLabelColor
      });
    });
  }

  protected _computeAxisLabelColor(type: ChartType): string {
    return styles.get([this.colorSchemeCssClass, type + '-chart', 'elements', 'axis-label'], 'fill').fill;
  }

  protected _adjustPluginColors(config: ChartConfig) {
    if (!config || !config.type || !config.options || !config.options.plugins) {
      return;
    }

    this._adjustPluginsDatalabelColors(config);
  }

  protected _adjustPluginsDatalabelColors(config: ChartConfig) {
    if (!config || !config.type || !config.options || !config.options.plugins || !config.options.plugins.datalabels) {
      return;
    }

    config.options.plugins.datalabels = $.extend(true, {}, config.options.plugins.datalabels, {
      color: this._computeDatalabelColor(config.type)
    });
  }

  protected _computeDatalabelColor(type: ChartType): string {
    return styles.get([this.colorSchemeCssClass, type + '-chart', 'elements', 'datalabel'], 'fill').fill;
  }

  protected _adjustClickHandler(config: ChartConfig) {
    if (!config || !config.options) {
      return;
    }

    if (config.options.clickable) {
      config.options.onClick = this._clickHandler;
      config.options.onHover = this._pointerHoverHandler;
    } else {
      config.options.onHover = this._hoverHandler;
    }

    if (!config.options.plugins || !config.options.plugins.legend) {
      return;
    }

    let legend = config.options.plugins.legend;
    if (legend.clickable) {
      legend.onClick = this._legendClickHandler;
      legend.onHover = this._legendPointerHoverHandler;
      legend.onLeave = this._legendPointerLeaveHandler;
    } else {
      legend.onClick = e => e.native.stopPropagation();
      legend.onHover = this._legendHoverHandler;
      legend.onLeave = this._legendLeaveHandler;
    }
  }

  protected _onClick(event: ChartEvent, items: ActiveElement[]) {
    if (!items.length) {
      return;
    }
    let relevantItem = this._selectRelevantItem(items);

    if (this._isMaxSegmentsExceeded(this.chartJs.config, relevantItem.index)) {
      return;
    }

    let itemIndex = relevantItem.index,
      datasetIndex = relevantItem.datasetIndex,
      clickObject: ClickObject = {
        datasetIndex: datasetIndex,
        dataIndex: itemIndex
      };
    if (scout.isOneOf(this.chartJs.config.type, Chart.Type.BUBBLE, Chart.Type.SCATTER)) {
      let data = this.chartJs.config.data.datasets[datasetIndex].data[itemIndex] as ScatterDataPoint | BubbleDataPoint;
      clickObject.xIndex = data.x;
      clickObject.yIndex = data.y;
    } else {
      clickObject.xIndex = itemIndex;
    }

    this.chart.handleValueClick(clickObject, event.native);
  }

  /**
   * Selects the most relevant item. Default is the first item.
   */
  protected _selectRelevantItem(items: ActiveElement[]): ActiveElement {
    let chartDatasets = this.chartJs.config.data.datasets;
    let relevantItem = items[0];

    if (this.chartJs.config.type === Chart.Type.BUBBLE) {
      // The smallest bubble, which is drawn in the foreground, is the most relevant item for the bubble chart.
      // If two bubbles are the same size, we choose the one which comes later in the array (bubble with array index n + 1 is draw in front of bubble with array index n)
      items.forEach(item => {
        if ((chartDatasets[item.datasetIndex].data[item.index] as BubbleDataPoint).z <= (chartDatasets[relevantItem.datasetIndex].data[relevantItem.index] as BubbleDataPoint).z) {
          relevantItem = item;
        }
      });
    }
    return relevantItem;
  }

  protected _onHover(event: ChartEvent, items: ActiveElement[]) {
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
      let dataset = config.data.datasets[item.datasetIndex];
      if (scout.isOneOf((dataset.type || type), Chart.Type.LINE, Chart.Type.RADAR)) {
        this._setHoverBackgroundColor(dataset);
        this.resetDatasetAfterHover = true;
        update = true;
      }
    });
    if (update) {
      this.refresh();
    }
  }

  protected _onHoverPointer(event: ChartEvent, items: ActiveElement[]) {
    this._onHover(event, items);
    if (!this.rendered || this.removing) {
      return;
    }
    if (items.length && !this._isMaxSegmentsExceeded(this.chartJs.config, items[0].index)) {
      this.$canvas.css('cursor', 'pointer');
    } else {
      this.$canvas.css('cursor', 'default');
    }
  }

  protected _onLegendClick(e: ChartEvent, legendItem: LegendItem, legend: LegendElement<any>) {
    if (!this.chartJs.config || !this.chartJs.config.type) {
      return;
    }

    let type = this.chartJs.config.type,
      defaultTypeLegendClick;
    if (ChartJs.overrides[type] && ChartJs.overrides[type].plugins && ChartJs.overrides[type].plugins.legend) {
      defaultTypeLegendClick = ChartJs.overrides[type].plugins.legend.onClick;
    }
    let defaultLegendClick = defaultTypeLegendClick || ChartJs.defaults.plugins.legend.onClick;
    defaultLegendClick.call(this.chartJs, e, legendItem, legend);
    this._onLegendLeave(e, legendItem, legend);
    this._onLegendHoverPointer(e, legendItem, legend);
  }

  protected _onLegendHover(e: ChartEvent, legendItem: LegendItem, legend: LegendElement<any>) {
    let index = legendItem.datasetIndex,
      config = this.chartJs.config,
      type = config.type;
    if (scout.isOneOf(type, Chart.Type.PIE, Chart.Type.DOUGHNUT, Chart.Type.POLAR_AREA)) {
      index = legendItem.index;
    }

    if (this.legendHoverDatasets.indexOf(index) > -1) {
      return;
    }

    let dataset = config.data.datasets[index],
      datasetType = dataset ? dataset.type : null;
    if ((datasetType || type) === Chart.Type.LINE) {
      this._setHoverBackgroundColor(dataset);
      this.refresh();
    }
    this._updateHoverStyle(index, true);
    this.chartJs.render();
    this.legendHoverDatasets.push(index);
  }

  protected _onLegendHoverPointer(e: ChartEvent, legendItem: LegendItem, legend: LegendElement<any>) {
    this._onLegendHover(e, legendItem, legend);
    if (!this.rendered || this.removing) {
      return;
    }
    this.$canvas.css('cursor', 'pointer');
  }

  protected _onLegendLeave(e: ChartEvent, legendItem: LegendItem, legend: LegendElement<any>) {
    let index = legendItem.datasetIndex,
      config = this.chartJs.config,
      type = config.type;
    if (scout.isOneOf(type, Chart.Type.PIE, Chart.Type.DOUGHNUT, Chart.Type.POLAR_AREA)) {
      index = legendItem.index;
    }

    if (this.legendHoverDatasets.indexOf(index) < 0) {
      return;
    }

    let dataset = config.data.datasets[index],
      datasetType = dataset ? dataset.type : null;
    if ((datasetType || type) === Chart.Type.LINE) {
      this._restoreBackgroundColor(dataset);
      this.refresh();
    }
    this._updateHoverStyle(index, false);
    this.chartJs.render();
    this.legendHoverDatasets.splice(this.legendHoverDatasets.indexOf(index), 1);
  }

  /**
   * Sets the hover background color as the datasets background color.
   * This little workaround is necessary for the line chart, which does not support a native hover effect.
   * The previous background color will be backed up on the dataset property "backgroundColorBackup"
   * and can be restored with {@link _restoreBackgroundColor}.
   */
  protected _setHoverBackgroundColor(dataset: ChartDataset) {
    if (!dataset) {
      return;
    }
    // backup the old background color first
    dataset.backgroundColorBackup = dataset.backgroundColor as Color;
    // overwrite the current background color with the hover color
    dataset.backgroundColor = dataset.hoverBackgroundColor;
    this._adjustDatasetBorderWidths(dataset);
  }

  /**
   * Restores the background color of a dataset or of all datasets,
   * if they were previously overwritten by {@link _setHoverBackgroundColor}.
   */
  protected _restoreBackgroundColor(dataset?: ChartDataset) {
    if (dataset) {
      dataset.backgroundColor = dataset.backgroundColorBackup || dataset.backgroundColor;
      delete dataset.backgroundColorBackup;
      this._adjustDatasetBorderWidths(dataset);
    } else {
      this.chartJs.config.data.datasets.forEach(dataset => this._restoreBackgroundColor(dataset));
    }
  }

  protected _onLegendLeavePointer(e: ChartEvent, legendItem: LegendItem, legend: LegendElement<any>) {
    this._onLegendLeave(e, legendItem, legend);
    if (!this.rendered || this.removing) {
      return;
    }
    this.$canvas.css('cursor', 'default');
  }

  protected _updateHoverStyle(datasetIndex: number, enabled: boolean) {
    let config = this.chartJs.config,
      type = config.type,
      mode,
      elements = [],
      datasets = config.data.datasets,
      dataset = datasets ? datasets[datasetIndex] : null,
      datasetType = dataset ? dataset.type : null;
    if (scout.isOneOf(type, Chart.Type.LINE, Chart.Type.PIE, Chart.Type.DOUGHNUT, Chart.Type.POLAR_AREA, Chart.Type.SCATTER) || datasetType === Chart.Type.LINE) {
      mode = 'point';
    } else {
      mode = 'dataset';
    }
    if (scout.isOneOf(type, Chart.Type.PIE, Chart.Type.DOUGHNUT, Chart.Type.POLAR_AREA)) {
      this.chartJs.getSortedVisibleDatasetMetas().forEach((meta, index) => elements.push({
        element: meta.data[datasetIndex],
        datasetIndex: index,
        index: datasetIndex
      }));
    } else {
      this.chartJs.getDatasetMeta(datasetIndex).data.forEach((element, index) => elements.push({
        element: element,
        datasetIndex: datasetIndex,
        index: index
      }));
    }
    if (elements && elements.length) {
      this.chartJs.updateHoverStyle(elements, mode, enabled);
    }
  }

  protected _adjustResizeHandler(config: ChartConfig) {
    if (!config || !config.options) {
      return;
    }

    if (config.options.handleResize) {
      config.options.onResize = this._resizeHandler;
    }
  }

  protected _onResize(chart: ChartJsChart, size: { width: number; height: number }) {
    chart.update();
    this._adjustSize(chart.config, chart.chartArea);
  }

  protected _adjustSize(config: ChartConfig, chartArea: ChartArea) {
    this._adjustBubbleSizes(config, chartArea);
    this._adjustGridMaxMin(config, chartArea);
  }

  protected _adjustBubbleSizes(config: ChartConfig, chartArea: ChartArea) {
    if (config.type !== Chart.Type.BUBBLE) {
      return;
    }

    let datasets = config.data.datasets;
    // Scale all bubbles so that the largest radius is equal to sizeOfLargestBubble and the smallest greater than or equal to minBubbleSize.
    // First reset all radii.
    datasets.forEach(dataset => dataset.data.forEach((data: BubbleDataPoint) => {
      if (!isNaN(data.z)) {
        data.r = Math.sqrt(data.z);
      }
    }));
    let maxMinR = this._computeMaxMinValue(config, datasets, 'r', true),
      maxR = maxMinR.maxValue,
      minR = maxMinR.minValue,
      // Compute a scalingFactor and an offset to get the new radius newR = r * scalingFactor + offset.
      bubbleScalingFactor = 1,
      bubbleRadiusOffset = 0,
      sizeOfLargestBubble = (config.options || {}).bubble ? config.options.bubble.sizeOfLargestBubble : 0,
      minBubbleSize = (config.options || {}).bubble ? config.options.bubble.minBubbleSize : 0;
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
    datasets.forEach(dataset => dataset.data.forEach((data: BubbleDataPoint) => {
      if (!objects.isNullOrUndefined(data.r)) {
        data.r = data.r * bubbleScalingFactor + bubbleRadiusOffset;
      }
    }));
  }

  protected _computeMaxMinValue(config: ChartConfig, datasets: ChartDataset[], identifier?: string, exact?: boolean, boundRange?: boolean, padding?: number, space?: number): Boundary {
    if (!datasets) {
      return;
    }

    let maxValue, minValue;
    if (config.type === Chart.Type.SCATTER && identifier === 'r') {
      // do not move the grid boundaries because of the radii of the points (would look weird)
      maxValue = 0;
      minValue = 0;
    } else {
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

  protected _calculateBoundary(value: number, roundingFunctionPositive: (number) => number, roundingFunctionNegative: (number) => number): number {
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

  protected _calculateBoundaryPositive(value: number, roundingFunction: (number) => number): number {
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

  protected _adjustGridMaxMin(config: ChartConfig, chartArea: ChartArea) {
    if (!config || !config.type || !config.options || !config.options.adjustGridMaxMin || !config.options.scales || !chartArea) {
      return;
    }

    let type = config.type;
    if (!scout.isOneOf(type, Chart.Type.BAR, Chart.Type.LINE, Chart.Type.POLAR_AREA, Chart.Type.RADAR, Chart.Type.BUBBLE, Chart.Type.SCATTER)) {
      return;
    }

    let scales = config.options.scales,
      xAxis = scales.x,
      yAxis = scales.y,
      rAxis = scales.r,
      minSpaceBetweenXTicks = xAxis ? xAxis.minSpaceBetweenTicks : 1,
      minSpaceBetweenYTicks = yAxis ? yAxis.minSpaceBetweenTicks : 1;

    if (rAxis) {
      minSpaceBetweenXTicks = rAxis.minSpaceBetweenTicks;
      minSpaceBetweenYTicks = rAxis.minSpaceBetweenTicks;
    }

    let width = Math.abs(chartArea.right - chartArea.left),
      height = Math.abs(chartArea.top - chartArea.bottom),
      maxXTicks = Math.max(Math.floor(width / minSpaceBetweenXTicks) + 1, 3),
      maxYTicks = Math.max(Math.floor(height / minSpaceBetweenYTicks) + 1, 3);

    let yBoundaries = this._computeYBoundaries(config, height),
      yBoundary = yBoundaries.yBoundary,
      yBoundaryDiffType = yBoundaries.yBoundaryDiffType;

    if (rAxis) {
      this._adjustAxisMaxMin(rAxis, Math.ceil(Math.min(maxXTicks, maxYTicks) / 2), yBoundary);
      return;
    }

    if (yBoundaryDiffType) {
      this._adjustAxisMaxMin(yAxis, maxYTicks, yBoundary);
      this._adjustAxisMaxMin(scales.yDiffType, maxYTicks, yBoundaryDiffType);
    } else if (this._isHorizontalBar(config)) {
      this._adjustAxisMaxMin(xAxis, maxXTicks, yBoundary);
    } else {
      this._adjustAxisMaxMin(yAxis, maxYTicks, yBoundary);
    }

    if (!scout.isOneOf(type, Chart.Type.BUBBLE, Chart.Type.SCATTER)) {
      return;
    }

    let xBoundary = this._computeXBoundaryPointElement(config, width);
    this._adjustAxisMaxMin(xAxis, maxXTicks, xBoundary);
  }

  protected _computeBoundaryPointElement(config: ChartConfig, identifier: string, space: number): Boundary {
    if (!config || !config.type || !scout.isOneOf(config.type, Chart.Type.BUBBLE, Chart.Type.SCATTER) || !config.data || !config.options || !config.options.scales || !(identifier === 'x' || identifier === 'y') || !space) {
      return;
    }

    let datasets = config.data.datasets,
      axis = config.options.scales[identifier],
      offset = axis && axis.offset,
      labelMap = config.options[identifier + 'LabelMap'],
      boundary;

    let maxR = this._computeMaxMinValue(config, datasets, 'r', true).maxValue,
      padding = maxR;
    if (config.options.elements && config.options.elements.point && config.options.elements.point.hoverRadius) {
      padding = padding + (config.options.elements.point.hoverRadius as number);
    }

    if (offset) {
      boundary = this._computeMaxMinValue(config, datasets, identifier, labelMap, true);
    } else {
      boundary = this._computeMaxMinValue(config, datasets, identifier, labelMap, true, padding, space);
    }
    if (labelMap) {
      boundary.maxValue = Math.ceil(boundary.maxValue);
      boundary.minValue = Math.floor(boundary.minValue);
    }
    return boundary;
  }

  protected _computeXBoundaryPointElement(config: ChartConfig, width: number): Boundary {
    return this._computeBoundaryPointElement(config, 'x', width);
  }

  protected _computeYBoundaryPointElement(config: ChartConfig, height: number): Boundary {
    return this._computeBoundaryPointElement(config, 'y', height);
  }

  protected _computeYBoundaries(config: ChartConfig, height: number): { yBoundary?: Boundary; yBoundaryDiffType?: Boundary } {
    if (!config || !config.type) {
      return {};
    }

    let type = config.type,
      yBoundary,
      yBoundaryDiffType;

    if (scout.isOneOf(type, Chart.Type.BUBBLE, Chart.Type.SCATTER)) {
      yBoundary = this._computeYBoundaryPointElement(config, height);
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

      yBoundary = this._computeMaxMinValue(config, datasets);

      if (datasets.length && datasetsDiffType.length) {
        yBoundaryDiffType = this._computeMaxMinValue(config, datasetsDiffType);
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

  protected _adjustYAxisDiffType(config: ChartConfig, datasets: ChartDataset[], datasetsDiffType: ChartDataset[]) {
    if (!config || !config.type || !datasets || !datasets.length || !datasetsDiffType || !datasetsDiffType.length) {
      return;
    }

    if (!config.options || !config.options.scales || !config.options.scales.y || config.options.scales.yDiffType) {
      return;
    }

    let type = config.type,
      options = config.options,
      scales = options.scales,
      yAxis = scales.y,
      yAxisDiffType = $.extend(true, {}, yAxis);
    scales.yDiffType = yAxisDiffType;

    if (config.data && config.data.datasets && config.data.datasets.length && config.data.datasets[0].type && config.data.datasets[0].type !== type) {
      yAxisDiffType.position = Chart.Position.LEFT;
      yAxis.position = Chart.Position.RIGHT;
      yAxis.grid.drawOnChartArea = false;
    } else {
      yAxis.position = Chart.Position.LEFT;
      yAxisDiffType.position = Chart.Position.RIGHT;
      yAxisDiffType.grid.drawOnChartArea = false;
    }

    yAxis.border.display = true;
    yAxis.grid.drawTicks = true;
    yAxisDiffType.border.display = true;
    yAxisDiffType.grid.drawTicks = true;

    let yAxisType = (datasets[0].type || type),
      yAxisDiffTypeType = (datasetsDiffType[0].type || type),
      yAxisTypeLabel = this.chart.session.text('ui.' + yAxisType),
      yAxisDiffTypeTypeLabel = this.chart.session.text('ui.' + yAxisDiffTypeType),
      yAxisScaleLabel = options.scaleLabelByTypeMap ? options.scaleLabelByTypeMap[yAxisType] : null,
      yAxisDiffTypeScaleLabel = options.scaleLabelByTypeMap ? options.scaleLabelByTypeMap[yAxisDiffTypeType] : null;

    yAxis.title.display = true;
    yAxis.title.text = yAxisScaleLabel ? yAxisScaleLabel + ' (' + yAxisTypeLabel + ')' : yAxisTypeLabel;
    yAxisDiffType.title.display = true;
    yAxisDiffType.title.text = yAxisDiffTypeScaleLabel ? yAxisDiffTypeScaleLabel + ' (' + yAxisDiffTypeTypeLabel + ')' : yAxisDiffTypeTypeLabel;

    datasets.forEach(dataset => {
      dataset.yAxisID = 'y';
    });
    datasetsDiffType.forEach(dataset => {
      dataset.yAxisID = 'yDiffType';
    });
  }

  protected _adjustAxisMaxMin(axis: AxisWithMaxMin, maxTicks: number, maxMinValue: Boundary) {
    if (!axis) {
      return;
    }

    let maxRangeBetweenTwoTicks = 1;

    axis.ticks = $.extend(true, {}, axis.ticks, {
      maxTicksLimit: maxTicks
    });
    if (maxMinValue) {
      axis.suggestedMax = maxMinValue.maxValue;
      axis.suggestedMin = maxMinValue.minValue;

      maxRangeBetweenTwoTicks = (maxMinValue.maxValue - maxMinValue.minValue) / (maxTicks - 1);
    }
    axis.ticks.stepSize = this.onlyIntegers && maxRangeBetweenTwoTicks < 1 ? 1 : undefined;
  }

  protected override _remove(afterRemoveFunc: (chartAnimationStopping?: boolean) => void) {
    if (this.rendered && !this.removing) {
      this.removing = true;
      this.chartJs.destroy();
      this.chartJs = null;
      this.$canvas.remove();
      this.$canvas = null;
    }
    super._remove(afterRemoveFunc);
    this.removing = false;
  }
}

export type LabelFormatter = (label: number | string) => string;
export type AxisFitter = (axis: Scale<CartesianScaleOptions>) => void;
export type DatalabelsDisplayHandler = (context: Context) => boolean;
export type DatalabelsFormatter = (value: number | ScatterDataPoint | BubbleDataPoint, context: Context) => string;
export type DatalabelBackgroundColorHandler = (context: Context) => Color;
export type RadialChartDatalabelsFormatter = (value: number, context: Context) => string;
export type LegendLabelGenerator = (chart: ChartJsChart) => LegendItem[];
export type ChartEventHandler = (event: ChartEvent, items: ActiveElement[]) => void;
export type LegendEventHandler = (e: ChartEvent, legendItem: LegendItem, legend: LegendElement<any>) => void;
export type ResizeHandler = (chart: ChartJsChart | ChartJs, size: { width: number; height: number }) => void;
export type TooltipTitleGenerator = (tooltipItems: TooltipItem<any>[]) => string | string[] | void;
export type TooltipItemsGenerator = (tooltipItems: TooltipItem<any>[], tooltipLabel: TooltipLabelGenerator, tooltipLabelValue: TooltipLabelValueGenerator, tooltipColor: TooltipLabelColorGenerator) => string;
export type TooltipLabelGenerator = (tooltipItem: TooltipItem<any>) => string | string[] | void;
export type TooltipLabelValueGenerator = (tooltipItem: TooltipItem<any>) => string | { x: string; y: string };
export type TooltipLabelColorGenerator = (tooltipItem: TooltipItem<any>) => TooltipLabelStyle | void;
export type TooltipRenderer = (context: { chart: ChartJs; tooltip: TooltipModel<any> }) => void;

export type DatasetColors = {
  backgroundColors?: (string | string[])[];
  borderColors?: (string | string[])[];
  hoverBackgroundColors?: (string | string[])[];
  hoverBorderColors?: (string | string[])[];
  checkedBackgroundColors?: (string | string[])[];
  checkedHoverBackgroundColors?: (string | string[])[];
  legendColors?: (string | string[])[];
  pointHoverColors?: (string | string[])[];
  datalabelColor?: string;
};

export type Boundary = { maxValue: number; minValue: number };

export type AxisWithMaxMin = (CartesianChartScale | RadialChartScale) & {
  ticks?: (CartesianChartScale | RadialChartScale)['ticks'] & {
    stepSize?: number;
  };
};

// extend chart.js

export type ChartJsChart = Omit<ChartJs, 'config'> & {
  config: ChartConfig;
  legend?: LegendElement<any>;
};

declare module 'chart.js' {
  interface ChartData<TType extends ChartJsType = ChartJsType, TData = DefaultDataPoint<TType>, TLabel = unknown> {
    maxSegmentsExceeded?: boolean;
  }

  interface ChartDatasetProperties<TType extends ChartJsType, TData> {
    datasetId?: string;
    yAxisID?: 'y' | 'yDiffType';

    pointBackgroundColor?: Scriptable<Color, ScriptableContext<TType>>;
    pointHoverBackgroundColor?: Scriptable<Color, ScriptableContext<TType>>;
    pointRadius?: Scriptable<number, ScriptableContext<TType>>;
    legendColor?: Scriptable<Color, number>;

    checkedBackgroundColor?: Color;
    checkedHoverBackgroundColor?: Color;
    checkedPointBackgroundColor?: Color;
    checkedPointHoverBackgroundColor?: Color;
    checkedPointRadius?: number;

    uncheckedBackgroundColor?: Color;
    uncheckedHoverBackgroundColor?: Color;
    uncheckedPointBackgroundColor?: Color;
    uncheckedPointHoverBackgroundColor?: Color;
    uncheckedPointRadius?: number;

    backgroundColorBackup?: Color;
  }

  interface BubbleDataPoint {
    z: number;
  }

  interface TooltipOptions<TType extends ChartJsType = ChartJsType> {
    cssClass?: string;
  }

  interface TooltipCallbacks<TType extends ChartJsType, Model = TooltipModel<TType>, Item = TooltipItem<TType>> {
    items: TooltipItemsGenerator;
    labelValue: TooltipLabelValueGenerator;
  }
}
