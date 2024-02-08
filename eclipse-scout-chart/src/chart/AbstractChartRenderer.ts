/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {Chart} from '../index';
import {arrays, Session} from '@eclipse-scout/core';
import {UpdateChartOptions} from './Chart';

export class AbstractChartRenderer {
  chart: Chart;
  session: Session;

  /** true while this.render() is executing */
  rendering: boolean;
  rendered: boolean;

  /** set by render() and remove(), makes it unnecessary to carry an argument through all method calls */
  animationDuration: number;
  firstOpaqueBackgroundColor: string;

  constructor(chart: Chart) {
    this.chart = chart;
    this.session = chart.session;
    this.rendering = false;
    this.rendered = false;
    this.animationDuration = 0;
    this.firstOpaqueBackgroundColor = '';
  }

  validate(): boolean {
    if (!this._validateChartData()) {
      return false;
    }

    return this._validate();
  }

  protected _validateChartData(): boolean {
    let chartData = this.chart && this.chart.data;
    if (!chartData || !chartData.chartValueGroups || chartData.chartValueGroups.length === 0) {
      return false;
    }

    // check lengths
    let i, length = 0;
    for (i = 0; i < chartData.chartValueGroups.length; i++) {
      let chartValueGroup = chartData.chartValueGroups[i];
      if (!chartValueGroup.values) {
        return false;
      }
      // Length of all "values" arrays have to be equal
      if (i === 0) {
        length = chartValueGroup.values.length;
      } else {
        if (chartValueGroup.values.length !== length) {
          return false;
        }
      }
      // color should have been set.
      if (!this.chart.config.options.autoColor && !arrays.ensure(chartValueGroup.colorHexValue).length && !chartValueGroup.cssClass) {
        return false;
      }
    }
    for (i = 0; i < chartData.axes.length; i++) {
      if (chartData.axes[i].length !== length) {
        return false;
      }
    }

    return true;
  }

  protected _validate(): boolean {
    // Override in subclasses
    return true;
  }

  /**
   * @param requestAnimation
   *          Whether animations should be used while rendering the chart. Note that his
   *          property is ignored when chart.config.options.animation.duration is <code>0</code>!
   */
  render(requestAnimation: boolean) {
    this.animationDuration = requestAnimation ? this.chart.config.options.animation.duration : 0;
    if (!this.validate() || !this.chart.rendered) {
      return;
    }
    this.rendering = true;
    this._render();
    this.rendering = false;
    this.rendered = true;
  }

  protected _render() {
    // Override in subclasses
  }

  renderCheckedItems() {
    if (this.rendered) {
      this._renderCheckedItems();
    }
  }

  protected _renderCheckedItems() {
    // nop
  }

  /**
   * @param requestAnimation
   *          Whether animations should be used while updating the chart. Note that his
   *          property is ignored when chart.config.options.animation.duration is <code>0</code>!
   */
  updateData(requestAnimation: boolean) {
    if (!this.rendered) {
      this.render(requestAnimation);
      return;
    }
    this.animationDuration = requestAnimation ? this.chart.config.options.animation.duration : 0;
    if (!this.validate() || !this.isDataUpdatable()) {
      return;
    }
    this._updateData();
  }

  protected _updateData() {
    // Override in subclasses
  }

  isDataUpdatable(): boolean {
    return false;
  }

  isDetachSupported(): boolean {
    return true;
  }

  refresh() {
    if (this.rendered) {
      this.remove(false);
    }
    this.render(false);
  }

  /**
   * @param requestAnimation
   *          Whether animations should be used while removing the chart. Note that his
   *          property is ignored when chart.config.options.animation.duration is <code>0</code>!
   */
  remove(requestAnimation = false, afterRemoveFunc?: (chartAnimationStopping?: boolean) => void) {
    this.animationDuration = requestAnimation && this.chart.config.options.animation.duration;
    if (this.animationDuration && this.rendered) {
      this._removeAnimated(afterRemoveFunc);
    } else {
      this._remove(afterRemoveFunc);
    }
  }

  protected _remove(afterRemoveFunc: (chartAnimationStopping?: boolean) => void) {
    this.rendered = false;
    afterRemoveFunc && afterRemoveFunc();
  }

  protected _removeAnimated(afterRemoveFunc: (chartAnimationStopping?: boolean) => void) {
    // Override in subclasses
    this._remove(afterRemoveFunc);
  }

  /**
   * Controls if the animation of the chart is shown when chart data has been updated.
   */
  shouldAnimateRemoveOnUpdate(opts: UpdateChartOptions): boolean {
    return opts.requestAnimation;
  }
}
