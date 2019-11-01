/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSI CRM Software License v1.0
 * which accompanies this distribution as bsi-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {strings} from '@eclipse-scout/core';
import {AbstractGridChartRenderer} from '../index';

export default class BarChartRenderer extends AbstractGridChartRenderer {

  constructor(chart) {
    super(chart);
  }


  _render() {
    super._render();

    var chartGroups = this.chart.chartData.chartValueGroups,
      widthPerX = this.getWidthPerX(),
      width = this.getWidth(chartGroups),
      yLabels = this._createYLabelsAndAjustDimensions(this.possibleYLines);
    this.spaceBetweenXValues = widthPerX;

    // data-axis
    this.renderYGrid(yLabels);

    // draw data
    for (var cg = 0; cg < chartGroups.length; cg++) {
      var barClass = 'bar-chart-bar' + (this.chart.autoColor ? ' color' + cg : '');
      this._renderLegendEntry(chartGroups[cg].groupName, !this.chart.autoColor ? this.chart.chartData.chartValueGroups[cg].colorHexValue : null, barClass, cg);

      for (var i = 0; i < chartGroups[cg].values.length; i++) {
        var key = this.chart.chartData.axes[0][i].label,
          value = chartGroups[cg].values[i];
        this._renderSingleBar(key, value, i, cg, width, widthPerX, chartGroups);
      }
    }

    this.handleTooBigLabels(widthPerX);
    this._addClipping('bar-chart-bar');
  }

  _renderSingleBar(key, value, valueIndex, chartGroupIndex, width, widthPerX) {
    var yEnd = 0,
      heightEnd = 0,
      $text,
      chartGroups = this.chart.chartData.chartValueGroups,
      xCoord = this._calculateXCoordinate(valueIndex) + chartGroupIndex * (2 + width),
      chartGroupCss = this.chart.chartData.chartValueGroups[chartGroupIndex].cssClass,
      barClass = 'bar-chart-bar' + (this.chart.autoColor ? ' color' + chartGroupIndex : strings.box(' ', chartGroupCss, ''));

    // draw label only once
    if (chartGroupIndex === 0) {
      $text = this.renderXGridLabel(chartGroups, valueIndex, width, widthPerX);
    }

    if (value >= 0) {
      yEnd = this._calculateYCoordinate(value);
      heightEnd = this._calculateYCoordinate(0) - this._calculateYCoordinate(value);
    } else {
      yEnd = this._calculateYCoordinate(0);
      heightEnd = this._calculateYCoordinate(value) - this._calculateYCoordinate(0);
    }

    var renderRectOptions = this._initRectRenderOptions({
      xStart: xCoord,
      yStart: this._calculateYCoordinate(0),
      yEnd: yEnd,
      widthStart: width,
      heightStart: 0,
      heightEnd: heightEnd,
      cssClass: barClass,
      clickObject: this._createClickObject(0, valueIndex, chartGroupIndex)
    });
    if (!this.chart.autoColor && !chartGroupCss) {
      renderRectOptions.fill = this.chart.chartData.chartValueGroups[chartGroupIndex].colorHexValue;
    }
    renderRectOptions.customAttributes.push(['negValue', value < 0]);
    renderRectOptions.customAttributes.push(['data-xAxis', key]);

    var $rect = this._renderRect(renderRectOptions);

    // rect legend
    var legendPositions = {
      x1: xCoord + width / 2,
      x2: xCoord + width / 2 + 10,
      y1: yEnd,
      y2: yEnd - 20,
      v: -1,
      h: 1
    };

    // calculate opening direction
    var labelPositionFunc = function(labelWidth, labelHeight) {
      if (value <= 0 || legendPositions.y2 - labelHeight < 0) {
        legendPositions.v = 1;
        if (0 > xCoord - 10 - labelWidth) {
          legendPositions.h = 1;
        } else {
          legendPositions.h = -1;
          legendPositions.x2 = xCoord - 10;
        }
        legendPositions.y1 = yEnd + heightEnd;
        legendPositions.y2 = yEnd + heightEnd + 20;
      }
      // check if left is enough space
      if (this.chartDataAreaWidth < legendPositions.x2 + labelWidth) {
        legendPositions.h = -1;
        legendPositions.x2 = xCoord - 10;
      }
      return legendPositions;
    };

    legendPositions.autoPosition = true;
    legendPositions.posFunc = labelPositionFunc;

    var
      that = this,
      legend = this._renderWireLegend(strings.join(
        ': ', this.chart.chartData.chartValueGroups[chartGroupIndex].groupName, this.session.locale.decimalFormat.format(value)),
        legendPositions, 'line-chart-wire-label', true),
      mouseIn = function() {
        legend.attachFunc();
        if (that.toBigLabelHoverFunc) {
          that.toBigLabelHoverFunc(that.xAxisLabels[valueIndex]);
        }
      },
      mouseOut = function() {
        legend.detachFunc();
        if (that.toBigLabelHoverOffFunc) {
          that.toBigLabelHoverOffFunc(that.xAxisLabels[valueIndex]);
        }
      };

    $rect.mouseenter(mouseIn).mouseleave(mouseOut);
    legend.detachFunc();

    $rect.data('legend', legend);
    if ($text) {
      $rect.data('data-text', $text);
    }
  }

  _removeAnimated(afterRemoveFunc) {
    if (this.animationTriggered) {
      return;
    }
    var yCoord = 0;
    if (this.$svg.children('.bar-chart-bar').length > 0) {
      yCoord = this._calculateYCoordinate(0);
    }

    this.animationTriggered = true;
    this.$svg.children('.bar-chart-bar')
      .animateSVG('y', yCoord, 200, null, true)
      .animateSVG('height', 0, 200, null, true)
      .promise().done(function() {
      this._remove(afterRemoveFunc);
      this.animationTriggered = false;
    }.bind(this));
  }
}
