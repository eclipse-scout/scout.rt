/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {objects, strings} from '@eclipse-scout/core';
import {AbstractSvgChartRenderer} from '../index';
import $ from 'jquery';

export default class SalesfunnelChartRenderer extends AbstractSvgChartRenderer {

  constructor(chart) {
    super(chart);

    this.segmentSelectorForAnimation = '.salesfunnel-chart-bar';
    this.widthThresholdMedium = 400;
    this.widthThresholdSmall = 200;
    // Constants for "normalized mode"
    // Width of a bar is calculated by multiplying this value with the previous bar's width
    this.barDeltaPercentage = 0.95;
    // Factor to be multiplied with the last bar's width. This will create a funnel effect, because
    // the last bar is considerably smaller than the other bars.
    this.lastBarAdditionalPercentage = 0.7;
    // Percentage of the total width the last bar always has (delta might get smaller due to this constraint).
    this.lastBarMinWidthPercentage = 0.4;
    this.suppressLegendBox = true;

    let defaultConfig = {
      options: {
        salesfunnel: {
          normalized: undefined,
          calcConversionRate: undefined
        }
      }
    };
    chart.config = $.extend(true, {}, defaultConfig, chart.config);
  }

  _validate() {
    let chartData = this.chart.data;
    let chartConfig = this.chart.config;
    if (!chartData ||
      !chartConfig ||
      chartData.axes.length !== chartData.chartValueGroups.length ||
      chartData.chartValueGroups.length === 0 ||
      chartData.chartValueGroups[0].values.length === 0 ||
      chartConfig.options.salesfunnel.normalized === undefined ||
      chartConfig.options.salesfunnel.calcConversionRate === undefined) {
      return false;
    }
    return true;
  }

  _renderInternal() {
    let chartData = this.chart.data,
      bars = chartData.chartValueGroups.length;

    this.conversionRateWidth = this._dynamicConversionRateWidth();
    this.dataAnalyzeResult = this._analyzeData(chartData.chartValueGroups);
    this.paddingBetweenLabel = 20;
    this.barHeight = this.chartBox.height / bars;
    this.barAreaHeight = this.barHeight * bars;
    this.barAreaWidth = this.chartBox.width -
      this.dataAnalyzeResult.maxLengthFirstValueRow -
      (this.paddingBetweenLabel * this.dataAnalyzeResult.labelCount) -
      this.dataAnalyzeResult.maxLengthSecondValueRow -
      this.conversionRateWidth;
    this.centerX = this.barAreaWidth / 2;

    if (this.chart.config.options.salesfunnel.normalized) {
      this._renderBarsNormalized(chartData.chartValueGroups);
    } else {
      this._renderBarsAccordingToValues(chartData.chartValueGroups);
    }

    this._addClipping('salesfunnel-chart-bar');
  }

  _renderBarsNormalized(chartValueGroups) {
    let barCount = chartValueGroups.length;
    let startPointX = this.barAreaWidth +
      this.dataAnalyzeResult.maxLengthFirstValueRow +
      this.dataAnalyzeResult.maxLengthSecondValueRow +
      (this.paddingBetweenLabel * (this.dataAnalyzeResult.labelCount + 1));

    let delta = this.barAreaWidth * (1 - this.barDeltaPercentage);
    let minLastWidth = this.barAreaWidth * this.lastBarMinWidthPercentage;
    let secondLastWidth = minLastWidth / this.lastBarAdditionalPercentage;
    delta = Math.min(delta, (this.barAreaWidth - secondLastWidth) / (barCount - 1));

    for (let i = 0; i < barCount; i++) {
      let width = this.barAreaWidth - (i * delta),
        barLabel = chartValueGroups[i].groupName,
        widthBottom = this.barAreaWidth - ((i + 1) * delta),
        yCoord = i * this.barHeight;

      let renderPolyOptions = {
        xStart: this.centerX,
        yStart: yCoord,
        rect: true,
        width: width,
        widthBottom: widthBottom,
        cssClass: 'salesfunnel-chart-bar',
        fill: chartValueGroups[i].colorHexValue,
        label: chartValueGroups[i].groupName,
        clickObject: this._createClickObject(null, i)
      };

      if (this.chart.config.options.autoColor) {
        renderPolyOptions.cssClass += ' auto-color color0';
      } else if (this.chart.data.chartValueGroups[i].cssClass) {
        renderPolyOptions.cssClass += ' ' + this.chart.data.chartValueGroups[i].cssClass;
      }

      // Adjust last widths to look like funnel.
      if (i === barCount - 1) {
        renderPolyOptions.width = renderPolyOptions.width * this.lastBarAdditionalPercentage;
        renderPolyOptions.widthBottom = renderPolyOptions.widthBottom * this.lastBarAdditionalPercentage;
      }

      this._renderPolygon(renderPolyOptions);
      this._renderLabel(chartValueGroups[i].values[0], false, i);
      this._renderBarLabel(barLabel, i, renderPolyOptions.widthBottom);
      let labelLineWidth = this.dataAnalyzeResult.maxLengthFirstValueRow + this.paddingBetweenLabel;
      if (chartValueGroups[i].values.length > 1) {
        this._renderLabel(chartValueGroups[i].values[1], true, i);
        labelLineWidth += this.dataAnalyzeResult.maxLengthSecondValueRow + this.paddingBetweenLabel;
      }
      if (i > 0) {
        this._renderLabelSeparatorLine(yCoord, labelLineWidth);
        if (this.chart.config.options.salesfunnel.calcConversionRate) {
          this._renderConversionRate(i, startPointX, this._calcConversionRate(chartValueGroups[i - 1].values[0], chartValueGroups[i].values[0]));
        }
      }
    }
  }

  _renderLabel(label, secondLabel, barIndexFromTop) {
    if (label === null) {
      return;
    }
    let y = (barIndexFromTop * this.barHeight) + (this.barHeight / 2),
      labelOffset = this.dataAnalyzeResult.maxLengthFirstValueRow + (secondLabel ? this.dataAnalyzeResult.maxLengthSecondValueRow : 0),
      labelIndex = secondLabel ? 2 : 1,
      x = this.barAreaWidth + labelOffset + (labelIndex * this.paddingBetweenLabel),
      labelClass = this._dynamicCssClass('salesfunnel-label');

    let $label = this.$svg.appendSVG('text', labelClass)
      .attr('x', x)
      .attr('y', y)
      .text(this.session.locale.decimalFormat.format(label));
    if (this.animationDuration) {
      $label
        .attr('opacity', 0)
        .animateSVG('opacity', 1, this.animationDuration, null, true);
    }
    if (this.chart.config.options.plugins.tooltip.enabled && this.chart.data.axes.length > 0) {
      let desc = this.chart.data.axes[barIndexFromTop][secondLabel ? 1 : 0].label,
        textBounds = this._measureText(label, labelClass);
      this._renderWireLabels(desc, $label, x - textBounds.width / 2, y - textBounds.height);
    }
  }

  _renderWireLabels(label, $text, x1, y1) {
    let legendPositions = {
      x1: x1,
      x2: x1 - 10,
      y1: y1,
      y2: y1 - 10,
      v: -1,
      h: -1
    };
    // calculate opening direction
    let labelPositionFunc = (labelWidth, labelHeight) => {
      if (legendPositions.y2 - labelHeight < 0) {
        legendPositions.v = 1;
        legendPositions.y1 = legendPositions.y1 + labelHeight * 1.5;
        legendPositions.y2 = legendPositions.y1 + 10;
      }
      return legendPositions;
    };

    legendPositions.autoPosition = true;
    legendPositions.posFunc = labelPositionFunc;
    let
      legend = this._renderWireLegend(label, legendPositions, 'line-chart-wire-label', 'wire-label-line-white', false),
      mouseIn = legend.attachFunc.bind(legend),
      mouseOut = legend.detachFunc.bind(legend);
    legend.detachFunc();
    $text.mouseenter(mouseIn)
      .mouseleave(mouseOut);
  }

  _renderBarLabel(label, barIndexFromTop, barWidth) {
    let y = (barIndexFromTop * this.barHeight) + (this.barHeight / 2),
      x = this.centerX,
      labelClass = this._dynamicCssClass('salesfunnel-bar-label');

    this._renderLineLabel(x, y, label, labelClass, true);
  }

  _renderConversionRate(barIndexFromTop, startPointX, conversionRate) {
    if (conversionRate === undefined) {
      return;
    }
    let ctrlY = barIndexFromTop * this.barHeight,
      labelClass = this._dynamicCssClass('salesfunnel-conversionrate-label');

    let $label = this.$svg.appendSVG('text', labelClass)
      .attr('x', startPointX)
      .attr('y', ctrlY)
      .text('↓ ' + conversionRate + '%');
    if (this.animationDuration) {
      $label
        .attr('opacity', 0)
        .animateSVG('opacity', 1, this.animationDuration, null, true);
    }
  }

  _renderPolygon(renderPolyOptions) {
    let that = this,
      points = this._calcPolygonPoints(true, this.animationDuration ? 0 : 1, renderPolyOptions.xStart, renderPolyOptions.yStart, renderPolyOptions.width, renderPolyOptions.widthBottom, this.barHeight - 1);

    let $poly = this.$svg.appendSVG('polygon', renderPolyOptions.cssClass, '', renderPolyOptions.id)
      .attr('points', points)
      .attr('opacity', renderPolyOptions.opacity)
      .data('xStart', renderPolyOptions.xStart)
      .data('yStart', renderPolyOptions.yStart)
      .data('widthBar', renderPolyOptions.width)
      .data('widthBottom', renderPolyOptions.widthBottom)
      .data('heightBar', this.barHeight);
    if (!this.chart.config.options.autoColor && renderPolyOptions.fill) {
      $poly.attr('fill', renderPolyOptions.fill);
    }
    $poly.css('stroke', this.firstOpaqueBackgroundColor);

    let expandFunc = function(now, fx) {
      let $this = $(this);
      let xStart = $this.data('xStart'),
        yStart = $this.data('yStart'),
        width = $this.data('widthBar'),
        height = $this.data('heightBar'),
        widthBottom = $this.data('widthBottom');
      $this.attr('points', that._calcPolygonPoints(true, fx.pos, xStart, yStart, width, widthBottom, height));
    };

    if (this.animationDuration) {
      $poly.animate({
        tabIndex: 0
      }, this._createAnimationObjectWithTabIndexRemoval(expandFunc, this.animationDuration));
    }
    if (this.chart.config.options.clickable) {
      $poly.on('click', renderPolyOptions.clickObject, this.chart._onValueClick.bind(this.chart));
    }
    if (renderPolyOptions.fill) {
      $poly.attr('fill', renderPolyOptions.fill);
    }

    return $poly;
  }

  _calcPolygonPoints(expand, fxPos, xStart, yStart, width, widthBottom, height) {
    let xOffsetTop = 0,
      xOffsetBottom = 0;
    if (expand) {
      xOffsetTop = width / 2 * fxPos;
      xOffsetBottom = widthBottom / 2 * fxPos;
    } else {
      xOffsetTop = (width / 2) - (width / 2 * fxPos);
      xOffsetBottom = (widthBottom / 2) - (widthBottom / 2 * fxPos);
    }
    let x1 = xStart - xOffsetTop,
      y1 = yStart,
      x2 = xStart + xOffsetTop,
      y2 = y1,
      x3 = xStart + xOffsetBottom,
      y3 = yStart + height,
      x4 = xStart - xOffsetBottom,
      y4 = y3;
    return x1 + ',' + y1 + ' ' + x2 + ',' + y2 + ' ' + x3 + ',' + y3 + ' ' + x4 + ',' + y4 + ' ';
  }

  _renderBarsAccordingToValues(chartValueGroups) {
    let widthPerN = (this.dataAnalyzeResult.maxValue ? this.barAreaWidth * 0.8 / this.dataAnalyzeResult.maxValue : 0),
      startPointX = this.barAreaWidth + this.dataAnalyzeResult.maxLengthFirstValueRow + this.dataAnalyzeResult.maxLengthSecondValueRow + this.paddingBetweenLabel * this.dataAnalyzeResult.labelCount + 2 * this.paddingBetweenLabel,
      barCount = chartValueGroups.length;

    for (let i = 0; i < barCount; i++) {
      let width = chartValueGroups[i].values[0] * widthPerN + this.barAreaWidth * 0.2,
        barLabel = chartValueGroups[i].groupName,
        yCoord = i * this.barHeight;

      let renderPolyOptions = {
        xStart: this.centerX,
        yStart: yCoord,
        rect: true,
        width: width,
        widthBottom: width,
        cssClass: 'salesfunnel-chart-bar',
        fill: chartValueGroups[i].colorHexValue,
        label: chartValueGroups[i].groupName,
        clickObject: this._createClickObject(null, i)
      };

      if (this.chart.config.options.autoColor) {
        renderPolyOptions.cssClass += ' auto-color color0';
      } else if (this.chart.data.chartValueGroups[i].cssClass) {
        renderPolyOptions.cssClass += ' ' + this.chart.data.chartValueGroups[i].cssClass;
      }

      this._renderPolygon(renderPolyOptions);
      this._renderLabel(chartValueGroups[i].values[0], false, i);
      this._renderBarLabel(barLabel, i, renderPolyOptions.widthBottom);
      let labelLineWidth = this.dataAnalyzeResult.maxLengthFirstValueRow + this.paddingBetweenLabel;
      if (chartValueGroups[i].values.length > 1) {
        this._renderLabel(chartValueGroups[i].values[1], true, i);
        labelLineWidth += this.dataAnalyzeResult.maxLengthSecondValueRow + this.paddingBetweenLabel;
      }
      if (i > 0) {
        this._renderLabelSeparatorLine(yCoord, labelLineWidth);
        if (this.chart.config.options.salesfunnel.calcConversionRate) {
          this._renderConversionRate(i, startPointX, this._calcConversionRate(chartValueGroups[i - 1].values[0], chartValueGroups[i].values[0]));
        }
      }
    }
  }

  _renderLabelSeparatorLine(yCoord, labelLineWidth) {
    let $line = this.$svg.appendSVG('line', 'label-separator')
      .attr('x1', this.barAreaWidth + this.paddingBetweenLabel)
      .attr('y1', yCoord)
      .attr('x2', this.barAreaWidth + labelLineWidth)
      .attr('y2', yCoord);
    if (this.animationDuration) {
      $line
        .attr('opacity', 0)
        .animateSVG('opacity', 1, this.animationDuration, null, true);
    }
  }

  _calcConversionRate(valueBefore, value) {
    if (objects.isNullOrUndefined(valueBefore) || objects.isNullOrUndefined(value) || valueBefore === 0) {
      return undefined;
    }
    return Math.round(value / valueBefore * 100);
  }

  _analyzeData(valueGroups) {
    let result = {
        labelCount: 0,
        maxValue: null,
        maxLengthFirstValueRow: 0,
        maxLengthSecondValueRow: 0
      },
      labelClass = this._dynamicCssClass('salesfunnel-label');

    for (let i = 0; i < valueGroups.length; i++) {

      let valueGroup = valueGroups[i];
      result.labelCount = Math.max(result.labelCount, valueGroup.values.length);
      // only first value is relevant for bar
      if (valueGroup.values.length > 0 && valueGroup.values[0]) {
        if (result.maxValue === null) {
          result.maxValue = valueGroup.values[0];
        } else {
          result.maxValue = Math.max(result.maxValue, valueGroup.values[0]);
        }
        result.maxLengthFirstValueRow = Math.max(result.maxLengthFirstValueRow, this._measureText(this.session.locale.decimalFormat.format(valueGroup.values[0]), labelClass).width);
      }
      if (valueGroup.values.length > 1 && valueGroup.values[1]) {
        result.maxLengthSecondValueRow = Math.max(result.maxLengthSecondValueRow, this._measureText(this.session.locale.decimalFormat.format(valueGroup.values[1]), labelClass).width);
      }
    }
    return result;
  }

  _removeAnimated(afterRemoveFunc) {
    if (this.animationTriggered) {
      return;
    }
    this.animationTriggered = true;
    let that = this,
      shrink = function(now, fx) {
        let $this = $(this);
        let xStart = $this.data('xStart'),
          yStart = $this.data('yStart'),
          width = $this.data('widthBar'),
          height = $this.data('heightBar'),
          widthBottom = $this.data('widthBottom');
        $this.attr('points', that._calcPolygonPoints(false, fx.pos, xStart, yStart, width, widthBottom, height));
      };
    this.$svg.children(this.segmentSelectorForAnimation).animate({
      tabIndex: 0
    }, this._createAnimationObjectWithTabIndexRemoval(shrink))
      .promise()
      .done(() => {
        this._remove(afterRemoveFunc);
        this.animationTriggered = false;
      });
  }

  _dynamicCssClass(cssClass) {
    let small = '';
    if (this.chartBox.width <= this.widthThresholdSmall) {
      small = 'small';
    } else if (this.chartBox.width <= this.widthThresholdMedium) {
      small = 'medium';
    }
    return strings.join(' ', cssClass, small);
  }

  _dynamicConversionRateWidth() {
    if (!this.chart.config.options.salesfunnel.calcConversionRate) {
      return 0; // don't show conversion rate
    }
    if (this.chartBox.width <= this.widthThresholdSmall) {
      return 60;
    }
    if (this.chartBox.width <= this.widthThresholdMedium) {
      return 80;
    }
    return 100;
  }
}
