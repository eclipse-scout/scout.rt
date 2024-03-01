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

import {arrays, objects} from '@eclipse-scout/core';

export default class AbstractChartRenderer {

  /**
   * @param {Chart} chart
   */
  constructor(chart) {

    /**
     * @property {Chart} chart
     */
    this.chart = chart;
    this.session = chart.session;
    this.rendering = false; // true while this.render() is executing
    this.rendered = false;
    this.animationDuration = 0; // set by render() and remove(), makes it unnecessary to carry an argument through all method calls

    this.firstOpaqueBackgroundColor = '';
  }

  validate() {
    if (!this._validateChartData()) {
      return false;
    }

    return this._validate();
  }

  _validateChartData() {
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

  _validate() {
    // Override in subclasses
    return true;
  }

  /**
   * @param requestAnimation
   *          Whether animations should be used while rendering the chart. Note that his
   *          property is ignored when chart.config.options.animation.duration is <code>0</code>!
   */
  render(requestAnimation) {
    if (!this.validate() || !this.chart.rendered) {
      return;
    }
    const configAnimationDuration = this.chart.config.options.animation.duration;
    this.setAnimationDuration(requestAnimation ? configAnimationDuration : 0);
    this.rendering = true;
    this._render();
    this.rendering = false;
    this.rendered = true;
    this.setAnimationDuration(configAnimationDuration);
  }

  _render() {
    // Override in subclasses
  }

  renderCheckedItems() {
    if (this.rendered) {
      this._renderCheckedItems();
    }
  }

  _renderCheckedItems() {
    // nop
  }

  /**
   * @param requestAnimation
   *          Whether animations should be used while updating the chart. Note that his
   *          property is ignored when chart.config.options.animation.duration is <code>0</code>!
   */
  updateData(requestAnimation) {
    if (!this.rendered) {
      this.render(requestAnimation);
      return;
    }
    if (!this.validate() || !this.isDataUpdatable()) {
      return;
    }
    const configAnimationDuration = this.chart.config.options.animation.duration;
    this.setAnimationDuration(requestAnimation ? configAnimationDuration : 0);
    this._updateData();
    this.setAnimationDuration(configAnimationDuration);
  }

  _updateData() {
    // Override in subclasses
  }

  isDataUpdatable() {
    return false;
  }

  isDetachSupported() {
    return true;
  }

  refresh() {
    if (this.rendered) {
      this.remove(false);
    }
    this.render(false);
  }

  setAnimationDuration(animationDuration) {
    if (objects.equals(this.animationDuration, animationDuration)) {
      return;
    }

    this._setAnimationDuration(animationDuration);
    if (this.rendered) {
      this._renderAnimationDuration();
    }
  }

  _setAnimationDuration(animationDuration) {
    this.animationDuration = animationDuration;
  }

  _renderAnimationDuration() {
    // nop
  }

  /**
   * @param requestAnimation
   *          Whether animations should be used while removing the chart. Note that his
   *          property is ignored when chart.config.options.animation.duration is <code>0</code>!
   */
  remove(requestAnimation, afterRemoveFunc) {
    const configAnimationDuration = this.chart.config.options.animation.duration;
    this.setAnimationDuration(requestAnimation && configAnimationDuration);
    if (this.animationDuration && this.rendered) {
      this._removeAnimated(afterRemoveFunc);
    } else {
      this._remove(afterRemoveFunc);
    }
    this.setAnimationDuration(configAnimationDuration);
  }

  _remove(afterRemoveFunc) {
    this.rendered = false;
    afterRemoveFunc && afterRemoveFunc();
  }

  _removeAnimated(afterRemoveFunc) {
    // Override in subclasses
    this._remove(afterRemoveFunc);
  }

  /**
   * Controls if the animation of the chart is shown when chart data has been updated.
   */
  shouldAnimateRemoveOnUpdate(opts) {
    return opts.requestAnimation;
  }
}
