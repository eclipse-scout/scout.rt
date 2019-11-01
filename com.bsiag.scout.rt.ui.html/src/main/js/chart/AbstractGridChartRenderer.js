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
import {AbstractChartRenderer} from '../index';
import * as $ from 'jquery';
import {Chart} from '../index';

export default class AbstractGridChartRenderer extends AbstractChartRenderer {

  constructor(chart) {
    super(chart);
    this.animationTriggered = false;
    this.spaceBetweenYValues = 40;
    this.spaceBetweenXValues = 20;
    this.paddingLeft = 40;
    this.paddingTop = 20;
    this.paddingBottom = 40;
    this.paddingRight = 40;
    this.chartAreaXPadding = 20;
    this.horizontalLegendPaddingLeft = this.paddingLeft;
    this.verticalLegendPaddingLeft = 25;
    this.minLegendWidth = 80;
    this.maxWidthLabel = 0;
    this.legendLabelClass = 'legend-label-grid';
    this.toBigLabelHoverFunc = undefined;
    this.toBigLabelHoverOffFunc = undefined;
    this.xAxisLabels = [];
  }


  _validate() {
    if (this.chart.chartData.axes.length === 0 ||
      this.chart.chartData.chartValueGroups.length === 0) {
      return false;
    }
    return true;
  }

  _render() {
    this.chartDataAreaHeight = this.chartBox.height - this.paddingTop - this.paddingBottom;
    this.chartDataAreaWidth = this.chartBox.width - this.paddingLeft - this.paddingRight;
    this.maxMinValue = this._findMaxMinValue();
    this.xItems = this.chart.chartData.axes[0].length;
    this.possibleYLines = Math.floor(this.chartDataAreaHeight / this.spaceBetweenYValues);
    this.spaceBetweenXValues = (this.chartDataAreaWidth - 2 * this.chartAreaXPadding) / (this.xItems - 1);
  }

  _findMaxMinValue() {
    var maxValue = 0,
      minValue = 0,
      i = 0,
      j = 0,
      f;
    var chartGroups = this.chart.chartData.chartValueGroups;
    for (i = 0; i < chartGroups.length; i++) {
      for (j = 0; j < chartGroups[i].values.length; j++) {
        maxValue = Math.max(chartGroups[i].values[j], maxValue);
        minValue = Math.min(chartGroups[i].values[j], minValue);
      }
    }

    if (maxValue > 0) {
      f = Math.ceil(Math.log(maxValue) / Math.LN10) - 1;
      maxValue = Math.ceil(maxValue / Math.pow(10, f)) * Math.pow(10, f);
      maxValue = Math.ceil(maxValue / 4) * 4;
    }

    if (minValue < 0) {
      minValue = minValue * (-1);
      f = Math.ceil(Math.log(minValue) / Math.LN10) - 1;
      minValue = Math.ceil(minValue / Math.pow(10, f)) * Math.pow(10, f);
      minValue = Math.ceil(minValue / 4) * 4;
      minValue = minValue * (-1);
    }

    return {
      maxValue: Math.ceil(maxValue),
      minValue: Math.floor(minValue),
      range: Math.ceil(maxValue) - Math.floor(minValue)
    };
  }

  _initLegendTextHeights() {
    super._initLegendTextHeights();

    this.legendTextHeights.textGap = this.legendTextHeights.textHeight;
  }

  _initLabelBox() {
    super._initLabelBox();

    // adjust to labels to center line
    this.labelBox.y = this.labelBox.y + this.paddingTop * 0.5 - this.paddingBottom * 0.5;

    // adjust label position for grid charts to have more space for the label text
    if (this.chart.legendPosition === Chart.LEGEND_POSITION_RIGHT) {
      this.labelBox.x = this.chartBox.width;
    } else if (this.chart.legendPosition === Chart.LEGEND_POSITION_LEFT) {
      this.labelBox.x = this.verticalLegendPaddingLeft;
    }
  }

  _renderGridLine(x1, y1, x2, y2) {
    return this._renderLine(x1, y1, x2, y2, 'chart-axis');
  }

  _renderGridLineLabel(x, y, label, xAxis) {
    return this._renderLineLabel(x, y, label, xAxis ? 'chart-x-axis-label' : 'chart-y-axis-label');
  }

  renderYGrid(yLabels) {
    var formatOptions = this._calculateLabelFormatOptions(yLabels);
    var xBase = this.chartBox.xOffset + this.paddingLeft;
    var xCoord = xBase - 5;
    this._renderGridLineLabel(xCoord, this.paddingTop / 2 - 4, formatOptions.shortcut, false);

    for (var l = 0; l < yLabels.length; l++) {
      var label = yLabels[l],
        yCoord = this._calculateYCoordinate(label);
      this._renderGridLine(xBase, yCoord, xBase + this.chartDataAreaWidth, yCoord);
      this._renderGridLineLabel(xCoord, yCoord, this._formatLabel(formatOptions, yLabels[l]), false);
    }
  }

  _formatLabel(formatOptions, numberLabel, withoutDecimalFormat) {
    var label = numberLabel.toString(),
      decimalSep = '.', // not locale dependent here!
      decimalSepPos = label.indexOf(decimalSep),
      labelLength = decimalSepPos === -1 ? label.length : decimalSepPos,
      tmpLabel;

    if (labelLength > formatOptions.separatorPosition && formatOptions.separatorPosition > 0) {
      var decimalInsertPosition = label.length - formatOptions.separatorPosition;
      tmpLabel = strings.insertAt(label, decimalSep, decimalInsertPosition);
    } else if (label.length === formatOptions.separatorPosition) {
      if (numberLabel < 0) {
        tmpLabel = '-0' + decimalSep + label.substring(1);
      } else {
        tmpLabel = '0' + decimalSep + label;
      }
    } else if (formatOptions.separatorPosition > 0) {
      tmpLabel = this._formatLabel(formatOptions, '0' + label, true);
    } else {
      tmpLabel = label;
    }
    if (withoutDecimalFormat) {
      return tmpLabel;
    }
    return this.session.locale.decimalFormat.format(Number(tmpLabel));
  }

  renderXGridLabel(chartGroups, xIndex, width, widthPerX, drawOnValuePoint) {
    // draw label only once
    var key = this.chart.chartData.axes[0][xIndex].label,
      paintedWidth = drawOnValuePoint ? 0 : width * chartGroups.length + 2 * Math.max(chartGroups.length - 1, 0),
      $text = this._renderGridLineLabel(this._calculateXCoordinate(xIndex) + paintedWidth / 2, this._calculateYCoordinate(this.maxMinValue.minValue) + 20, key, true);

    try {
      // Firefox throws error when $text is not in dom(already removed by navigating away). all other browser returns a boundingbox with 0
      // use first group to calculate w.
      var w = chartGroups[0].values.length * chartGroups.length > 50 ? widthPerX : $text[0].getBBox().width;
      this.maxWidthLabel = (w > this.maxWidthLabel) ? w : this.maxWidthLabel;
    } catch (e) {
      // NOP
    }
    this.xAxisLabels[xIndex] = $text;
    return $text;
  }

  handleTooBigLabels(widthPerX) {
    // in case of to many elements, hide or rotate label
    this.toBigLabelHoverFunc = undefined;
    this.toBigLabelHoverOffFunc = undefined;
    if (this.maxWidthLabel > (widthPerX - 10) * 3) {
      this.toBigLabelHoverOffFunc = function($label) {
        $label.attr('fill-opacity', 0);
      };
      this.toBigLabelHoverFunc = function($label) {
        $label.attr('fill-opacity', 1);
      };
      this.$svg.children('.chart-x-axis-label').attr('fill-opacity', 0);
    } else if (this.maxWidthLabel > widthPerX) {
      this.$svg.children('.chart-x-axis-label').each(function() {
        $(this)
          .css('text-anchor', 'end')
          .attr('y', parseFloat($(this).attr('y')) - 4)
          .attr('x', parseFloat($(this).attr('x')) + 4)
          .attr('transform', 'rotate(-25 ' + $(this).attr('x') + ', ' + $(this).attr('y') + ')');
      });
    }
  }

  getWidth(chartGroups) {
    var width = this.getWidthPerX() / chartGroups.length - 2 * Math.max(chartGroups.length - 1, 0) - 4;
    width = Math.max(1, width);
    return width;
  }

  getWidthPerX() {
    return (this.chartDataAreaWidth - 2 * this.chartAreaXPadding) / this.xItems;
  }

  _calculateYCoordinate(yPoint) {
    var coord = this.chartDataAreaHeight - (yPoint - this.maxMinValue.minValue) / this.maxMinValue.range * this.chartDataAreaHeight;
    if (isNaN(coord)) {
      coord = this.chartDataAreaHeight;
    }
    return coord + this.paddingTop + this.chartBox.yOffset;
  }

  _calculateXCoordinate(xPoint) {
    return this.chartBox.xOffset + this.paddingLeft + this.chartAreaXPadding + xPoint * this.spaceBetweenXValues;
  }

  _createYLabelsAndAjustDimensions(possibleYLines) {
    var negNormalized = this.maxMinValue.minValue * (-1);
    var maxLabel = Math.max(negNormalized, this.maxMinValue.maxValue);
    var negValNormalizedBigger = maxLabel !== this.maxMinValue.maxValue;
    var bigger = (negValNormalizedBigger ? negNormalized : this.maxMinValue.maxValue);
    var smaller = (negValNormalizedBigger ? this.maxMinValue.maxValue : negNormalized);
    return this._calcNextLabelsAjustDimensions(bigger, smaller, 0, possibleYLines, negValNormalizedBigger);
  }

  _calculateLabelFormatOptions(labels) {
    var shortcut = '',
      maxNumberLen = 0,
      separatorPosition = 0,
      decimalSep = this.session.locale.decimalFormatSymbols.decimalSeparator;

    for (var i = 0; i < labels.length; i++) {
      var label = labels[i].toString(),
        decimalSepPos = label.indexOf(decimalSep);
      if (decimalSepPos > -1) {
        label = label.substr(0, decimalSepPos);
      }
      if (labels[i] < 0) {
        label = label.substr(1);
      }
      maxNumberLen = Math.max(maxNumberLen, label.length);
    }

    if (maxNumberLen > 21) {
      shortcut = this.chart.session.text('ui.Trd');
      separatorPosition = 21;
    } else if (maxNumberLen > 18) {
      shortcut = this.chart.session.text('ui.Tri');
      separatorPosition = 18;
    } else if (maxNumberLen > 15) {
      shortcut = this.chart.session.text('ui.Brd');
      separatorPosition = 15;
    } else if (maxNumberLen > 12) {
      shortcut = this.chart.session.text('ui.Bio');
      separatorPosition = 12;
    } else if (maxNumberLen > 19) {
      shortcut = this.chart.session.text('ui.Mrd');
      separatorPosition = 9;
    } else if (maxNumberLen > 6) {
      shortcut = this.chart.session.text('ui.Mio');
      separatorPosition = 6;
    }

    return {
      shortcut: shortcut,
      separatorPosition: separatorPosition
    };
  }

  _calcNextLabelsAjustDimensions(bigger, smaller, exp, possibleLines, negValNormalizedBigger) {
    var linesBigger = Math.pow(2, exp),
      step = bigger / linesBigger,
      smallerLines = [],
      biggerLines = [];

    // TODO [15.4] cgu: Fix case when all data points are zero
    if (step === 0) {
      // This prevents infinite loops below!
      return [];
    }

    for (var i = step; i <= bigger; i = i + step) {
      var negValue = i * (-1);
      if (smaller > i) {
        smallerLines.push(negValNormalizedBigger ? i : negValue);
      } else if (smaller <= i && smaller > i - step) {
        smallerLines.push(negValNormalizedBigger ? i : negValue);
      }
      biggerLines.push(negValNormalizedBigger ? negValue : i);
    }
    var spacesBetweenLines = smallerLines.length + biggerLines.length;
    if (possibleLines >= spacesBetweenLines) {
      var labels = biggerLines.concat([0], smallerLines),
        newLabels = this._calcNextLabelsAjustDimensions(bigger, smaller, exp + 1, possibleLines, negValNormalizedBigger);
      if (newLabels.length > 0) {
        return newLabels;
      } else {
        var biggerLinesValue = biggerLines.length ? biggerLines[biggerLines.length - 1] : 0,
          smallerLinesValue = smallerLines.length ? smallerLines[smallerLines.length - 1] : 0,
          maxValue = Math.ceil(negValNormalizedBigger ? smallerLinesValue : biggerLinesValue),
          minValue = Math.floor(negValNormalizedBigger ? biggerLinesValue : smallerLinesValue);
        this.maxMinValue = {
          maxValue: maxValue,
          minValue: minValue,
          range: maxValue - minValue
        };
        return labels;
      }
    } else {
      return [];
    }
  }

  _calcChartBoxWidth() {
    if (this.chart.legendVisible &&
      (this.chart.legendPosition === Chart.LEGEND_POSITION_RIGHT ||
        this.chart.legendPosition === Chart.LEGEND_POSITION_LEFT)) {
      return this.width / 6 > this.minLegendWidth ? this.width / 6 * 5 : this.width - this.minLegendWidth;
    }
    return this.width;
  }

  _calcChartBoxXOffset() {
    if (this.chart.legendVisible && this.chart.legendPosition === Chart.LEGEND_POSITION_LEFT) {
      return this.width / 6 > this.minLegendWidth ? this.width / 6 : this.minLegendWidth;
    }
    return 0;
  }
}
