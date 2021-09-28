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
import {App, Device} from '@eclipse-scout/core';
import {Chart} from 'chart.js';

Chart.defaults.plugins.tooltipDelay = {
  showTooltipDelay: 700,
  resetTooltipDelay: 200
};

let pluginId = 'tooltipDelay';

/**
 * copied from chart.js PluginService._notify
 */
let _notifyOthers = (chart, hook, args) => {
  // <customized>
  let descriptors = chart._plugins._descriptors(chart);
  // </customized>
  let ilen = descriptors.length;
  let i, descriptor, plugin, params, method;

  for (i = 0; i < ilen; ++i) {
    descriptor = descriptors[i];
    plugin = descriptor.plugin;
    // <customized>
    if (plugin.id === pluginId) {
      continue;
    }
    // </customized>
    method = plugin[hook];
    if (typeof method === 'function') {
      params = [chart, args, descriptor.options];
      if (method.apply(plugin, params) === false && args.cancelable) {
        return false;
      }
    }
  }
  return true;
};

/**
 * copied from chart.js _drawTooltip
 */
let _drawTooltip = (chart, args) => {
  // <customized>
  if (!chart.ctx) {
    return;
  }
  let tooltip = chart.tooltip;

  if (_notifyOthers(chart, 'beforeTooltipDraw', [args]) === false) {
    // </customized>
    return;
  }

  tooltip.draw(chart.ctx);

  // <customized>
  _notifyOthers(chart, 'afterTooltipDraw', [args]);
  // </customized>
};

/**
 * @typedef IPlugin
 */

/**
 * @type IPlugin
 */
export default class ChartJsTooltipDelay {
  constructor() {
    this.id = pluginId;
    this._chartTooltipVisible = false;
    this._chartTooltipShowTimeoutIds = []; // timeoutIds
    this._chartTooltipHideTimeoutIds = []; // timeoutIds
  }

  beforeTooltipDraw(chart, args, options) {
    let tooltip = chart.tooltip,
      showTooltipDelay = options.showTooltipDelay,
      resetTooltipDelay = options.resetTooltipDelay;

    if ((tooltip._active || []).length) {
      if (!this._chartTooltipVisible) {
        this._chartTooltipShowTimeoutIds.push(setTimeout(() => {
          this._chartTooltipShowTimeoutIds = [];
          this._chartTooltipVisible = true;
          _drawTooltip(chart, args);
        }, showTooltipDelay));
      } else {
        this._chartTooltipHideTimeoutIds.forEach(tid => clearTimeout(tid));
        this._chartTooltipHideTimeoutIds = [];
        _drawTooltip(chart, args);
      }
    } else {
      if (this._chartTooltipVisible) {
        this._chartTooltipHideTimeoutIds.push(setTimeout(() => {
          this._chartTooltipHideTimeoutIds = [];
          this._chartTooltipVisible = false;
        }, resetTooltipDelay));
        _drawTooltip(chart, args);
      } else {
        this._chartTooltipShowTimeoutIds.forEach(tid => clearTimeout(tid));
        this._chartTooltipShowTimeoutIds = [];
      }
    }

    return false;
  }
}

App.addListener('init', () => {
  if (!Device.get().supportsOnlyTouch()) {
    Chart.register(new ChartJsTooltipDelay());
  }
});
