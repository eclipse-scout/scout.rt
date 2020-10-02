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
import {
  BarChartRenderer,
  ChartJsRenderer,
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

    this.data = null;
    this.config = null;

    this._updateChartTimeoutId = null;
    this._updateChartOpts = null;
    this.updatedOnce = false;
  }

  static Type = {
    PIE: 'pie',
    PIE_OLD: 'pie_old',
    LINE: 'line',
    LINE_OLD: 'line_old',
    BAR: 'bar',
    BAR_HORIZONTAL: 'horizontalBar',
    BAR_VERTICAL_OLD: 'barVertical_old',
    BAR_HORIZONTAL_OLD: 'barHorizontal_old',
    COMBO_BAR_LINE: 'combo_bar_line',
    SCATTER: 'scatter',
    FULFILLMENT: 'fulfillment',
    SPEEDO: 'speedo',
    SALESFUNNEL: 'salesfunnel',
    VENN: 'venn',
    DOUGHNUT: 'doughnut',
    DONUT_OLD: 'donut_old',
    POLAR_AREA: 'polarArea',
    RADAR: 'radar',
    BUBBLE: 'bubble'
  };

  static Position = {
    TOP: 'top',
    BOTTOM: 'bottom',
    LEFT: 'left',
    RIGHT: 'right'
  };

  static DEFAULT_ANIMATION_DURATION = 600; // ms
  static DEFAULT_DEBOUNCE_TIMEOUT = 100; // ms

  _init(model) {
    super._init(model);
    this.setConfig(this.config);
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

  setData(data) {
    this.setProperty('data', data);
  }

  _renderData() {
    this.updateChart({
      requestAnimation: true,
      debounce: Chart.DEFAULT_DEBOUNCE_TIMEOUT
    });
  }

  setConfig(config) {
    let defaultConfig = {
      type: Chart.Type.PIE_OLD,
      options: {
        autoColor: true,
        maxSegments: 5,
        clickable: false,
        animation: {
          duration: Chart.DEFAULT_ANIMATION_DURATION
        },
        tooltips: {
          enabled: true
        },
        legend: {
          display: true,
          clickable: false,
          position: Chart.Position.RIGHT
        },
        plugins: {
          datalabels: {
            display: false
          }
        }
      }
    };
    this.setProperty('config', $.extend(true, {}, defaultConfig, config));
    this._updateChartRenderer();
  }

  _renderConfig() {
    this._renderClickable();
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

  _renderClickable() {
    this.$container.toggleClass('clickable', this.config.options.clickable);
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
    switch (this.config.type) {
      case Chart.Type.PIE_OLD:
        return new PieChartRenderer(this);
      case Chart.Type.LINE_OLD:
        return new LineChartRenderer(this);
      case Chart.Type.BAR_VERTICAL_OLD:
        return new BarChartRenderer(this);
      case Chart.Type.BAR_HORIZONTAL_OLD:
        return new BarHorizontalChartRenderer(this);
      case Chart.Type.SCATTER:
        return new ScatterChartRenderer(this);
      case Chart.Type.FULFILLMENT:
        return new FulfillmentChartRenderer(this);
      case Chart.Type.SPEEDO:
        return new SpeedoChartRenderer(this);
      case Chart.Type.SALESFUNNEL:
        return new SalesfunnelChartRenderer(this);
      case Chart.Type.VENN:
        return new VennChartRenderer(this);
      case Chart.Type.DONUT_OLD:
        return new DonutChartRenderer(this);
      case Chart.Type.BAR:
      case Chart.Type.BAR_HORIZONTAL:
      case Chart.Type.LINE:
      case Chart.Type.COMBO_BAR_LINE:
      case Chart.Type.PIE:
      case Chart.Type.DOUGHNUT:
      case Chart.Type.POLAR_AREA:
      case Chart.Type.RADAR:
      case Chart.Type.BUBBLE:
        return new ChartJsRenderer(this);
    }
    return null;
  }

  _updateChartRenderer() {
    this.chartRenderer && this.chartRenderer.remove();
    this.setProperty('chartRenderer', this._resolveChartRenderer());
  }

  _onValueClick(event) {
    this.trigger('valueClick', event);
  }
}
