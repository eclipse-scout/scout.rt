/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSI CRM Software License v1.0
 * which accompanies this distribution as bsi-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {
  BarChartRenderer,
  BarHorizontalChartRenderer,
  ChartLayout,
  DonutChartRenderer,
  FulfillmentChartRenderer,
  LineChartRenderer,
  PieChartRenderer,
  SalesfunnelChartRenderer,
  ScatterChartRenderer,
  SpeedoChartRenderer,
  VennChartRenderer
} from '../index';
import {HtmlComponent, Widget} from '@eclipse-scout/core';

export default class Chart extends Widget {

  constructor() {
    super();

    this.$container = null;
    this.chartRenderer = null;

    /**
     * @type {object}
     * @property {object} [customProperties]
     * @property {string} [customProperties.gridXAxisLabel]
     * @property {string} [customProperties.gridYAxisLabel]
     */
    this.chartData = null;
    this._updateChartTimeoutId = null;
    this._updateChartOpts = null;
    this.updatedOnce = false;
    this.animated = true;
    this.autoColor = true;
    this.clickable = false;
    this.enabled = true;
    this.legendPosition = Chart.LEGEND_POSITION_RIGHT;
    this.legendVisible = true;
    this.maxSegments = 5;
    this.visible = true;
    this.interactiveLegendVisible = true;
  }

  static PIE = 1;
  static LINE = 2;
  static BAR_VERTICAL = 3;
  static BAR_HORIZONTAL = 4;
  static SCATTER = 5;
  static FULFILLMENT = 6;
  static SPEEDO = 7;
  static SALESFUNNEL = 8;
  static VENN = 9;
  static DONUT = 10;

  static LEGEND_POSITION_BOTTOM = 0;
  static LEGEND_POSITION_TOP = 2;
  static LEGEND_POSITION_RIGHT = 4;
  static LEGEND_POSITION_LEFT = 5;

  static DEFAULT_DEBOUNCE_TIMEOUT = 100; // ms

  _init(model) {
    super._init(model);
    this._updateChartRenderer();
  }

  _render() {
    this.$container = this.$parent.appendDiv('chart');

    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.setLayout(new ChartLayout(this));

    // !!! Do _not_ update the chart here, because usually the container size
    // !!! is not correct anyways during during the render phase. The ChartLayout
    // !!! will eventually call updateChart() when the layout is validated.
  }

  _renderProperties() {
    super._renderProperties();
    this._renderClickable();
  }

  _remove() {
    if (this.chartRenderer) {
      this.chartRenderer.remove(false);
    }
    this.$container.remove();
    this.$container = null;
  }

  _setChartType(chartType) {
    this._setProperty('chartType', chartType);
    this._updateChartRenderer();
  }

  _renderChartType() {
    this.updateChart({
      requestAnimation: true,
      debounce: Chart.DEFAULT_DEBOUNCE_TIMEOUT
    });
  }

  _renderAutoColor() {
    this.updateChart({
      requestAnimation: true,
      debounce: Chart.DEFAULT_DEBOUNCE_TIMEOUT
    });
  }

  _renderLegendVisible() {
    this.updateChart({
      requestAnimation: true,
      debounce: Chart.DEFAULT_DEBOUNCE_TIMEOUT
    });
  }

  _renderInteractiveLegendVisible() {
    this.updateChart({
      requestAnimation: true,
      debounce: Chart.DEFAULT_DEBOUNCE_TIMEOUT
    });
  }

  _renderLegendPosition() {
    this.updateChart({
      requestAnimation: true,
      debounce: Chart.DEFAULT_DEBOUNCE_TIMEOUT
    });
  }

  setChartData(chartData) {
    this.setProperty('chartData', chartData);
  }

  _renderChartData() {
    this.updateChart({
      requestAnimation: true,
      debounce: Chart.DEFAULT_DEBOUNCE_TIMEOUT
    });
  }

  /**
   * @override
   */
  _renderEnabled() {
    this.updateChart();
  }

  _renderMaxSegments() {
    this.updateChart();
  }

  _renderClickable() {
    this.$container.toggleClass('clickable', this.clickable);
    this.updateChart();
  }

  /**
   * @param opts
   *   [requestAnimation] default false
   *   [debounce] default 0
   */
  updateChart(opts) {
    opts = opts || {};

    // Cancel previously scheduled update and merge opts
    if (this._updateChartTimeoutId) {
      clearTimeout(this._updateChartTimeoutId);
      if (this._updateChartOpts) {
        // Inherit 'true' values from previously scheduled updates
        opts.requestAnimation = opts.requestAnimation || this._updateChartOpts.requestAnimation;
      }
      this._updateChartTimeoutId = null;
      this._updateChartOpts = null;
    }

    let updateChartImplFn = updateChartImpl.bind(this);
    let doDebounce = (opts.debounce === true || typeof opts.debounce === 'number');
    if (doDebounce) {
      this._updateChartOpts = opts;
      if (typeof opts.debounce === 'number') {
        this._updateChartTimeoutId = setTimeout(updateChartImplFn, opts.debounce);
      } else {
        this._updateChartTimeoutId = setTimeout(updateChartImplFn);
      }
    } else {
      updateChartImplFn();
    }

    // ---- Helper functions -----

    function updateChartImpl() {
      this._updateChartTimeoutId = null;
      this._updateChartOpts = null;
      if (this.chartRenderer) {
        this.chartRenderer.remove(this.chartRenderer.shouldAnimateRemoveOnUpdate(opts), chartAnimationStopping => {
          if (this.removing || chartAnimationStopping) {
            // prevent exceptions trying to render after navigated away, and do not update/render while a running animation is being stopped
            return;
          }
          this.chartRenderer.render(opts.requestAnimation);
          this.trigger('chartRender');
        });
      }
      this.updatedOnce = true;
    }
  }

  _resolveChartRenderer() {
    switch (this.chartType) {
      case Chart.PIE:
        return new PieChartRenderer(this);
      case Chart.LINE:
        return new LineChartRenderer(this);
      case Chart.BAR_VERTICAL:
        return new BarChartRenderer(this);
      case Chart.BAR_HORIZONTAL:
        return new BarHorizontalChartRenderer(this);
      case Chart.SCATTER:
        return new ScatterChartRenderer(this);
      case Chart.FULFILLMENT:
        return new FulfillmentChartRenderer(this);
      case Chart.SPEEDO:
        return new SpeedoChartRenderer(this);
      case Chart.SALESFUNNEL:
        return new SalesfunnelChartRenderer(this);
      case Chart.VENN:
        return new VennChartRenderer(this);
      case Chart.DONUT:
        return new DonutChartRenderer(this);
    }
    return null;
  }

  _updateChartRenderer() {
    this.chartRenderer && this.chartRenderer.remove();
    this.chartRenderer = this._resolveChartRenderer();
  }

  _onValueClick(event) {
    this.trigger('valueClick', event);
  }
}
