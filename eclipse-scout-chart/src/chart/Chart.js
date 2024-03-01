/*
 * Copyright (c) 2010-2024 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {ChartJsRenderer, ChartLayout, FulfillmentChartRenderer, SalesfunnelChartRenderer, SpeedoChartRenderer, VennChartRenderer} from '../index';
import {arrays, colorSchemes, HtmlComponent, objects, Widget} from '@eclipse-scout/core';
import $ from 'jquery';

/**
 * @typedef ChartValueGroup
 * @property {*[]} values
 * @property {string} type
 * @property {string | string[]} colorHexValue
 */

/**
 * @typedef ClickObject
 * @property {number} datasetIndex
 * @property {number} dataIndex
 * @property {number} [xIndex]
 * @property {number} [yIndex]
 */

export default class Chart extends Widget {

  constructor() {
    super();

    this.$container = null;
    this.chartRenderer = null;

    /**
     * @type {object}
     * @property {ChartValueGroup[]} chartValueGroups
     */
    this.data = null;
    this.config = null;
    /**
     *  @type {ClickObject[]}
     */
    this.checkedItems = [];

    this._updateChartTimeoutId = null;
    this._updateChartOpts = null;
    this._updateChartOptsWhileNotAttached = [];
    this.updatedOnce = false;
  }

  static Type = {
    PIE: 'pie',
    LINE: 'line',
    BAR: 'bar',
    BAR_HORIZONTAL: 'horizontalBar',
    COMBO_BAR_LINE: 'comboBarLine',
    FULFILLMENT: 'fulfillment',
    SPEEDO: 'speedo',
    SALESFUNNEL: 'salesfunnel',
    VENN: 'venn',
    DOUGHNUT: 'doughnut',
    POLAR_AREA: 'polarArea',
    RADAR: 'radar',
    BUBBLE: 'bubble',
    SCATTER: 'scatter'
  };

  static Position = {
    TOP: 'top',
    BOTTOM: 'bottom',
    LEFT: 'left',
    RIGHT: 'right',
    CENTER: 'center'
  };

  static DEFAULT_ANIMATION_DURATION = 600; // ms
  static DEFAULT_DEBOUNCE_TIMEOUT = 100; // ms

  _init(model) {
    super._init(model);
    this._setData(this.data);
    this.setConfig(this.config);
  }

  _render() {
    this.$container = this.$parent.appendDiv('chart');

    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.setLayout(new ChartLayout(this));

    // !!! Do _not_ update the chart here, because usually the container size
    // !!! is not correct anyway during the render phase. The ChartLayout
    // !!! will eventually call updateChart() when the layout is validated.
  }

  _renderProperties() {
    super._renderProperties();
    this._renderClickable();
    this._renderCheckable();
    this._renderChartType();
    this._renderColorScheme();
    this.updateChart({
      requestAnimation: true,
      debounce: Chart.DEFAULT_DEBOUNCE_TIMEOUT
    });
  }

  _renderOnAttach() {
    super._renderOnAttach();
    const updateChartOptsWhileNotAttached = this._updateChartOptsWhileNotAttached.splice(0);
    if (!this.chartRenderer?.isDetachSupported()) {
      // the chartRenderer does not support detach => recreate it
      this._updateChartRenderer();
      updateChartOptsWhileNotAttached.forEach(opts => delete opts.requestAnimation);
      updateChartOptsWhileNotAttached.push({requestAnimation: false});
    }
    updateChartOptsWhileNotAttached.forEach(opts => this.updateChart($.extend(true, {}, opts, {debounce: true})));
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
    this.setCheckedItems(this.checkedItems);
  }

  _setData(data) {
    if (data) {
      data = $.extend({axes: []}, data);
    }
    this._setProperty('data', data);
  }

  _renderData() {
    this.updateChart({
      requestAnimation: true,
      debounce: Chart.DEFAULT_DEBOUNCE_TIMEOUT,
      onlyUpdateData: true
    });
  }

  setConfig(config) {
    let defaultConfig = {
      type: Chart.Type.PIE,
      options: {
        autoColor: true,
        colorScheme: colorSchemes.ColorSchemeId.DEFAULT,
        transparent: false,
        maxSegments: 5,
        adjustGridMaxMin: true,
        clickable: false,
        checkable: false,
        animation: {
          duration: Chart.DEFAULT_ANIMATION_DURATION
        },
        plugins: {
          datalabels: {
            display: false
          },
          tooltip: {
            enabled: true
          },
          legend: {
            display: true,
            clickable: false,
            position: Chart.Position.RIGHT
          }
        }
      }
    };
    config = $.extend(true, {}, defaultConfig, config);
    config.options.colorScheme = colorSchemes.ensureColorScheme(config.options.colorScheme);
    if (objects.equalsRecursive(this.config, config)) {
      return;
    }

    // check if only data has changed
    let oldConfigWithNewData = $.extend(true, {}, this.config);
    if (config.data) {
      oldConfigWithNewData.data = config.data;
    } else {
      delete oldConfigWithNewData.data;
    }

    // the label map is technically part of the config, but it is handled as data. Therefore, it is excluded from this check.
    let transferLabelMap = (source, target, identifier) => {
      if (!source || !target || !identifier) {
        return;
      }
      // Property not set on source -> remove from target
      if (!source.options || !source.options[identifier]) {
        if (target.options) {
          delete target.options[identifier];
        }
        if (target.options && objects.isEmpty(target.options.scales) && !(source.options && source.options.scales)) {
          delete target.options.scales;
        }
        if (objects.isEmpty(target.options) && !source.options) {
          delete target.options;
        }
        return;
      }
      target.options[identifier] = source.options[identifier];
    };
    transferLabelMap(config, oldConfigWithNewData, 'xLabelMap');
    transferLabelMap(config, oldConfigWithNewData, 'yLabelMap');

    if (objects.equalsRecursive(oldConfigWithNewData, config)) {
      this._setProperty('config', config);
      if (this.rendered) {
        this._renderConfig(true);
      }
      this.setCheckedItems(this.checkedItems);
      return;
    }

    if (this.rendered && this.config && this.config.type) {
      this.$container.removeClass(this.config.type + '-chart');
    }
    this.setProperty('config', config);
    this.setCheckedItems(this.checkedItems);
    this._updateChartRenderer();
  }

  _renderConfig(onlyUpdateData) {
    this._renderClickable();
    this._renderCheckable();
    this._renderChartType();
    this._renderColorScheme();
    this.updateChart({
      requestAnimation: true,
      debounce: Chart.DEFAULT_DEBOUNCE_TIMEOUT,
      onlyUpdateData: onlyUpdateData
    });
  }

  setCheckedItems(checkedItems) {
    this.setProperty('checkedItems', arrays.ensure(this._filterCheckedItems(checkedItems)));
  }

  _filterCheckedItems(checkedItems) {
    if (!Array.isArray(checkedItems)) {
      return checkedItems;
    }
    let datasetLengths = [];
    if (this.data && this.data.chartValueGroups) {
      this.data.chartValueGroups.forEach(chartValueGroup => datasetLengths.push(chartValueGroup.values.length));
    } else if (this.config && this.config.data) {
      this.config.data.datasets.forEach(dataset => datasetLengths.push(dataset.data.length));
    }
    let filteredCheckedItems = checkedItems.filter(item => datasetLengths[item.datasetIndex] && item.dataIndex < datasetLengths[item.datasetIndex]);
    if (filteredCheckedItems.length < checkedItems.length) {
      return filteredCheckedItems;
    }
    return checkedItems;
  }

  _renderCheckedItems() {
    if (this.chartRenderer) {
      this.chartRenderer.renderCheckedItems();
    }
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

  _renderCheckable() {
    this.$container.toggleClass('checkable', this.config.options.checkable);
  }

  _renderChartType() {
    this.$container.addClass(this.config.type + '-chart');
  }

  _renderColorScheme() {
    colorSchemes.toggleColorSchemeClasses(this.$container, this.config.options.colorScheme);
  }

  /**
   * @param opts
   *   [requestAnimation] default false
   *   [debounce] default 0
   *   [onlyUpdateData] default false
   *   [onlyRefresh] default false
   */
  updateChart(opts) {
    opts = opts || {};
    opts.onlyUpdateData = opts.onlyUpdateData && this.chartRenderer && this.chartRenderer.isDataUpdatable();
    opts.enforceRerender = !opts.onlyUpdateData && !opts.onlyRefresh;

    // Cancel previously scheduled update and merge opts
    if (this._updateChartTimeoutId) {
      clearTimeout(this._updateChartTimeoutId);
      if (this._updateChartOpts) {
        // Inherit 'true' values from previously scheduled updates
        opts.requestAnimation = opts.requestAnimation || this._updateChartOpts.requestAnimation;
        opts.onlyUpdateData = opts.onlyUpdateData || this._updateChartOpts.onlyUpdateData;
        opts.onlyRefresh = opts.onlyRefresh || this._updateChartOpts.onlyRefresh;
        opts.enforceRerender = opts.enforceRerender || this._updateChartOpts.enforceRerender;
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

      if (!this.$container || !this.$container.isAttached()) {
        this._updateChartOptsWhileNotAttached.push(opts);
        return;
      }

      this.updatedOnce = true;
      if (!this.chartRenderer) {
        return; // nothing to render when there is no renderer.
      }
      if (opts.enforceRerender) {
        this.chartRenderer.remove(this.chartRenderer.shouldAnimateRemoveOnUpdate(opts), chartAnimationStopping => {
          if (this.removing || chartAnimationStopping) {
            // prevent exceptions trying to render after navigated away, and do not update/render while a running animation is being stopped
            return;
          }
          this.chartRenderer.render(opts.requestAnimation);
          this.trigger('chartRender');
        });
      } else if (opts.onlyUpdateData) {
        this.chartRenderer.updateData(opts.requestAnimation);
      } else if (opts.onlyRefresh) {
        this.chartRenderer.refresh();
      }
    }
  }

  _resolveChartRenderer() {
    switch (this.config.type) {
      case Chart.Type.FULFILLMENT:
        return new FulfillmentChartRenderer(this);
      case Chart.Type.SPEEDO:
        return new SpeedoChartRenderer(this);
      case Chart.Type.SALESFUNNEL:
        return new SalesfunnelChartRenderer(this);
      case Chart.Type.VENN:
        return new VennChartRenderer(this);
      case Chart.Type.BAR:
      case Chart.Type.BAR_HORIZONTAL:
      case Chart.Type.LINE:
      case Chart.Type.COMBO_BAR_LINE:
      case Chart.Type.PIE:
      case Chart.Type.DOUGHNUT:
      case Chart.Type.POLAR_AREA:
      case Chart.Type.RADAR:
      case Chart.Type.BUBBLE:
      case Chart.Type.SCATTER:
        return new ChartJsRenderer(this);
    }
    return null;
  }

  _updateChartRenderer() {
    this.chartRenderer && this.chartRenderer.remove();
    this.setProperty('chartRenderer', this._resolveChartRenderer());
  }

  _onValueClick(event) {
    if (this.config.options.checkable) {
      let checkedItems = [...this.checkedItems],
        clickedItem = event.data,
        checkedItem = checkedItems.filter(item => item.datasetIndex === clickedItem.datasetIndex && item.dataIndex === clickedItem.dataIndex)[0];
      if (checkedItem) {
        arrays.remove(checkedItems, checkedItem);
      } else {
        checkedItems.push(clickedItem);
      }
      this.setCheckedItems(checkedItems);
    }
    this.trigger('valueClick', event);
  }
}
