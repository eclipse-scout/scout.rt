/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {aria, arrays, objects, scout} from '@eclipse-scout/core';
import {AbstractSvgChartRenderer, Chart} from '../index';
import $ from 'jquery';
import {UpdateChartOptions} from './Chart';

export class FulfillmentChartRenderer extends AbstractSvgChartRenderer {
  animationTriggered: boolean;
  segmentSelectorForAnimation: string;
  r: number;
  fullR: number;

  constructor(chart: Chart) {
    super(chart);

    this.animationTriggered = false;
    this.segmentSelectorForAnimation = '.fulfillment-chart';
    this.suppressLegendBox = true;

    let defaultConfig = {
      options: {
        fulfillment: {
          startValue: undefined
        }
      }
    };
    chart.config = $.extend(true, {}, defaultConfig, chart.config);
  }

  protected override _validate(): boolean {
    let chartValueGroups = this.chart.data.chartValueGroups;
    if (chartValueGroups.length !== 2 ||
      chartValueGroups[0].values.length !== 1 ||
      chartValueGroups[1].values.length !== 1) {
      return false;
    }
    return true;
  }

  protected override _renderInternal() {
    // Calculate percentage
    let chartData = this.chart.data;
    let value = chartData.chartValueGroups[0].values[0] as number;
    let total = chartData.chartValueGroups[1].values[0] as number;

    this.fullR = (Math.min(this.chartBox.height, this.chartBox.width) / 2) - 2;

    this._renderInnerCircle();
    this._renderPercentage(value, total);
  }

  protected _renderPercentage(value: number, total: number) {
    // arc segment
    let arcClass = 'fulfillment-chart',
      color = arrays.ensure(this.chart.data.chartValueGroups[0].colorHexValue)[0],
      chartGroupCss = this.chart.data.chartValueGroups[0].cssClass;

    if (this.chart.config.options.autoColor) {
      arcClass += ' auto-color';
    } else if (chartGroupCss) {
      arcClass += ' ' + chartGroupCss;
    }

    let startValue = scout.nvl(this.chart.config.options.fulfillment.startValue, 0);
    let end = 0;
    let lastEnd = 0;
    if (total) {
      // use slightly less than 1.0 as max value because otherwise, the SVG element would not be drawn correctly.
      end = Math.min(value / total, 0.999999);
      lastEnd = Math.min(startValue / total, 0.999999);
    }
    this.r = this.fullR;

    let that = this;
    let tweenInFunc = function(now, fx) {
      let $this = $(this);
      let start = $this.data('animation-start'),
        end = $this.data('animation-end');
      $this.attr('d', that.pathSegment(start * fx.pos, lastEnd + (end - lastEnd) * fx.pos));
    };

    let $arc = this.$svg.appendSVG('path', arcClass)
      .data('animation-start', 0)
      .data('animation-end', end);

    let radius2 = (this.fullR / 8) * 6.7;
    let $transparentCircle = this._renderCirclePath('fulfillment-chart-inner-circle-transparent', 'InnerCircle3', radius2);
    $transparentCircle.css('fill', this.firstOpaqueBackgroundColor);

    // Label
    let percentage = (total ? Math.round((value / total) * 100) : 0);
    let $label = this.$svg.appendSVG('text', 'fulfillment-chart-label ')
      .attr('x', this.chartBox.mX())
      .attr('y', this.chartBox.mY())
      .css('font-size', (this.fullR / 2) + 'px') // font of label in center relative to circle radius
      .attr('dy', '0.3em') // workaround for 'dominant-baseline: central' which is not supported in IE
      .attrXLINK('href', '#InnerCircle')
      .text(percentage + '%');

    if (this.chart.config.options.clickable) {
      $arc.on('click', this._createClickObject(null, null), e => this.chart.handleValueClick(e.data));
    }
    if (!this.chart.config.options.autoColor && !chartGroupCss) {
      $arc.attr('fill', color);
    }
    if (this.animationDuration) {
      $arc
        .attr('d', this.pathSegment(0, lastEnd))
        .animate({
          tabIndex: 0
        }, this._createAnimationObjectWithTabIndexRemoval(tweenInFunc, this.animationDuration));

      $label
        .attr('opacity', 0)
        .animateSVG('opacity', 1, this.animationDuration, null, true);
    } else {
      $arc.attr('d', this.pathSegment(0, end));
    }

    aria.description(this.$svg, this.session.text('ui.FulfillmentChartAriaDescription', percentage));
  }

  pathSegment(start: number, end: number): string {
    let s = start * 2 * Math.PI,
      e = end * 2 * Math.PI,
      pathString = '';

    pathString += 'M' + (this.chartBox.mX() + this.r * Math.sin(s)) + ',' + (this.chartBox.mY() - this.r * Math.cos(s));
    pathString += 'A' + this.r + ', ' + this.r;
    pathString += (end - start < 0.5) ? ' 0 0,1 ' : ' 0 1,1 ';
    pathString += (this.chartBox.mX() + this.r * Math.sin(e)) + ',' + (this.chartBox.mY() - this.r * Math.cos(e));
    pathString += 'L' + this.chartBox.mX() + ',' + this.chartBox.mY() + 'Z';

    return pathString;
  }

  protected _renderCirclePath(cssClass: string, id: string, radius: number): JQuery<SVGElement> {
    let chartGroupCss = this.chart.data.chartValueGroups[0].cssClass;
    let color = arrays.ensure(this.chart.data.chartValueGroups[1].colorHexValue)[0];

    if (this.chart.config.options.autoColor) {
      cssClass += ' auto-color';
    } else if (chartGroupCss) {
      cssClass += ' ' + chartGroupCss;
    }

    let radius2 = radius * 2;
    let $path = this.$svg.appendSVG('path', cssClass)
      .attr('id', id)
      .attr('d', 'M ' + this.chartBox.mX() + ' ' + this.chartBox.mY() +
        ' m 0, ' + (-radius) +
        ' a ' + radius + ',' + radius + ' 0 1, 1 0,' + radius2 +
        ' a ' + radius + ',' + radius + ' 0 1, 1 0,' + (-radius2));

    if (!this.chart.config.options.autoColor && !chartGroupCss) {
      $path
        .attr('fill', color)
        .attr('stroke', color);
    }

    return $path;
  }

  protected _renderInnerCircle() {
    let radius = (this.fullR / 8) * 7.5,
      radius2 = (this.fullR / 8) * 7.2;

    this._renderCirclePath('fulfillment-chart-inner-circle', 'InnerCircle', radius);
    let $transparentCircle = this._renderCirclePath('fulfillment-chart-inner-circle-transparent', 'InnerCircle2', radius2);
    $transparentCircle.css('fill', this.firstOpaqueBackgroundColor);
  }

  /**
   * Do not animate the removal of the chart if the chart data has been updated and the startValue option is set.
   * If startValue is not set use default implementation.
   */
  override shouldAnimateRemoveOnUpdate(opts: UpdateChartOptions): boolean {
    let startValue = objects.optProperty(this.chart, 'config', 'options', 'fulfillment', 'startValue');
    if (!objects.isNullOrUndefined(startValue)) {
      return false;
    }

    return super.shouldAnimateRemoveOnUpdate(opts);
  }

  protected override _removeAnimated(afterRemoveFunc: (chartAnimationStopping?: boolean) => void) {
    if (this.animationTriggered) {
      return;
    }
    let that = this;
    let tweenOut = function(now, fx) {
      let $this = $(this);
      let start = $this.data('animation-start'),
        end = $this.data('animation-end');
      $this.attr('d', that.pathSegment(start * (1 - fx.pos), end * (1 - fx.pos)));
    };

    this.animationTriggered = true;
    this.$svg.children(this.segmentSelectorForAnimation)
      .animate({
        tabIndex: 0
      }, this._createAnimationObjectWithTabIndexRemoval(tweenOut))
      .promise()
      .done(() => {
        this._remove(afterRemoveFunc);
        this.animationTriggered = false;
      });
  }
}
