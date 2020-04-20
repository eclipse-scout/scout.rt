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
import $ from 'jquery';

export default class LineChartRenderer extends AbstractGridChartRenderer {

  constructor(chart) {
    super(chart);
    this.glowClass = 'glow' + this.chart.id;
    this.numSupportedColors = 6;
    this.segmentSelectorForAnimation = 'path.line-chart-line';
  }

  _validate() {
    if (!super._validate()) {
      return false;
    }
    if (this.chart.chartData.axes[0].length < 2 ||
      this.chart.chartData.chartValueGroups[0].values.length < 2) {
      return false;
    }
    return true;
  }

  _render() {
    super._render();
    var i,
      j,
      chartValueGroups = this.chart.chartData.chartValueGroups,
      widthPerX = this.getWidthPerX(),
      width = this.getWidth(chartValueGroups);

    // Grid
    var yLabels = this._createYLabelsAndAdjustDimensions(this.possibleYLines);

    this.renderYGrid(yLabels);
    this._renderAxisLabels();

    // Lines
    var chartValueGroup,
      that = this,
      moveUpFunc = function(now, fx) {
        var $this = $(this),
          pointValues = $this.data('pointValues'),
          d = that._calcDForValuesString(pointValues, fx.pos, false),
          $highlightPath = $this.data('$highlightPath');

        $this.attr('d', d);
        $highlightPath.attr('d', d);

      },
      lineColors = [];

    var handleHover = function(event) {
        var $this = $(this);
        var $path = $this.data('$path');
        $path.addClass('hover-style');
        $path.data('$bubbles').forEach(function($elem) {
          $elem.attr('opacity', 1);
        });
      },
      hoverOff = function(event) {
        var $this = $(this);
        var $path = $this.data('$path');
        $path.removeClass('hover-style');
        $path.data('$bubbles').forEach(function($elem) {
          $elem.attr('opacity', 0);
        });
      };
    this.$paths = [];
    for (i = 0; i < chartValueGroups.length; i++) {
      chartValueGroup = chartValueGroups[i];
      var lineClass = 'line-chart-line',
        legendClass = 'line-chart-line',
        pointValues = [],
        d = '';
      if (this.chart.autoColor) {
        lineClass += ' stroke-color' + (i % this.numSupportedColors);
        legendClass += ' color' + (i % this.numSupportedColors);
        lineColors[i] = null;
      } else if (chartValueGroup.cssClass) {
        lineClass += ' ' + chartValueGroup.cssClass;
        legendClass += ' ' + chartValueGroup.cssClass;
        lineColors[i] = null;
      } else {
        lineColors[i] = chartValueGroup.colorHexValue;
      }
      this._renderLegendEntry(chartValueGroup.groupName, lineColors[i], legendClass, i);

      // Loop over each pair of points
      for (j = 0; j < chartValueGroup.values.length; j++) {
        pointValues.push(chartValueGroup.values[j]);
        var yCoord = this.animated ? this._calculateYCoordinate(0) : this._calculateYCoordinate(chartValueGroup.values[j]);
        if (j === 0) {
          d += this._addFirstPoint(this._calculateXCoordinate(j), yCoord);
        } else {
          d += this._addLinePoint(this._calculateXCoordinate(j), yCoord);
        }

        if (i === 0) {
          this.renderXGridLabel(chartValueGroups, j, width, widthPerX, true);
        }
      }
      // Draw a line from "j" to "j + 1"
      var $path = this.$svg.appendSVG('path', lineClass)
        .attr('d', d)
        .attr('fill', 'none')
        .data('pointValues', pointValues);

      var $highlightPath = this.$svg.appendSVG('path', 'highlight path')
        .attr('d', d)
        .attr('opacity', '0')
        .attr('fill', 'none')
        .attr('stroke-width', '14px')
        .attr('stroke', '#ccc')
        .data('$path', $path);

      $path.data('$highlightPath', $highlightPath);
      $highlightPath
        .mouseenter(handleHover)
        .mouseleave(hoverOff);

      if (this.animated) {
        $path
          .delay(200)
          .animate({
            tabIndex: 0
          }, this._createAnimationObjectWithTabindexRemoval(moveUpFunc));

      }
      if (lineColors[i]) {
        $path.attr('stroke', lineColors[i]);
      }
      $path.data('$bubbles', []);
      this.$paths.push($path);
    }

    // Data points (not inside "Lines" loop to draw them over the lines)
    for (i = 0; i < chartValueGroups.length; i++) {
      chartValueGroup = chartValueGroups[i];
      for (j = 0; j < chartValueGroup.values.length; j++) {
        this._renderValueBubble(j, chartValueGroup.values[j], 5, lineColors[i], i);
      }
    }
    this.handleTooBigLabels(widthPerX);
  }

  remove(animated, afterRemoveFunc) {
    super.remove(animated, afterRemoveFunc);
  }

  _calcDForValuesString(valuesArr, fxPos, negativeDirection) {
    if (!valuesArr) {
      return '';
    }
    var d = '';
    if (fxPos === undefined) {
      fxPos = 1;
    }
    if (negativeDirection) {
      fxPos = 1 - fxPos;
    }
    for (var i = 0; i < valuesArr.length; i++) {
      if (i === 0) {
        d += this._addFirstPoint(this._calculateXCoordinate(i), this._calculateYCoordinate(valuesArr[i] * fxPos));
      } else {
        d += this._addLinePoint(this._calculateXCoordinate(i), this._calculateYCoordinate(valuesArr[i] * fxPos));
      }
    }
    return d;
  }

  _addLinePoint(x, y) {
    return 'L' + x + ',' + y + ' ';
  }

  _addFirstPoint(x, y) {
    return 'M' + x + ',' + y + ' ';
  }

  _removeAnimated(afterRemoveFunc) {
    if (this.animationTriggered) {
      return;
    }
    var yCoord = 0,
      that = this,
      moveDownFunc = function(now, fx) {
        var $this = $(this),
          $highlightPath = $this.data('$highlightPath'),
          pointValues = $this.data('pointValues'),
          d = that._calcDForValuesString(pointValues, fx.pos, true);
        $this.attr('d', d);
        $highlightPath.attr('d', d);
      };
    if (this.$svg.children('.line-chart-line').length > 0) {
      yCoord = this._calculateYCoordinate(0);
    }

    this.animationTriggered = true;
    this.$svg.children(this.segmentSelectorForAnimation)
      .animate({
        tabIndex: 0
      }, this._createAnimationObjectWithTabindexRemoval(moveDownFunc))
      .promise()
      .done(function() {
        this._remove(afterRemoveFunc);
        this.animationTriggered = false;
      }.bind(this));
  }

  _renderValueBubble(index, value, radius, color, groupIndex) {
    var x = this._calculateXCoordinate(index),
      y = this.animated ? this._calculateYCoordinate(0) : this._calculateYCoordinate(value),
      endY = this._calculateYCoordinate(value);

    var colorClass;
    if (this.chart.autoColor) {
      colorClass = 'stroke-color' + (groupIndex % this.numSupportedColors);
    } else {
      colorClass = this.chart.chartData.chartValueGroups[groupIndex].cssClass || '';
    }

    var $bubble = this.$svg.appendSVG('circle', 'line-chart-value-bubble' + strings.box(' ', colorClass, ''))
      .attr('cx', x)
      .attr('cy', y)
      .attr('r', radius)
      .attr('opacity', 0);
    if (color) {
      $bubble.attr('stroke', color);
    }
    if (this.animated) {
      $bubble.animateSVG('cy', endY, 600, null, true);
    }

    var legendPositions = {
      x1: x,
      x2: x + 2 * radius,
      y1: this._calculateYCoordinate(value) - radius,
      y2: this._calculateYCoordinate(value) - 4 * radius,
      v: -1,
      h: 1
    };
    // calculate opening direction
    var labelPositionFunc = function(labelWidth, labelHeight) {
      if (value <= 0 || legendPositions.y2 - labelHeight < 0) {
        legendPositions.v = 1;
        if (0 > x - 2 * radius - labelWidth) {
          legendPositions.h = 1;
        } else {
          legendPositions.h = -1;
          legendPositions.x2 = x - 2 * radius;
        }
        legendPositions.y1 = this._calculateYCoordinate(value) + radius;
        legendPositions.y2 = this._calculateYCoordinate(value) + 4 * radius;
      }
      // check if left is enough space
      if (this.chartDataAreaWidth < legendPositions.x2 + labelWidth) {
        legendPositions.h = -1;
        legendPositions.x2 = x - 2 * radius;
      }
      return legendPositions;
    };

    legendPositions.autoPosition = true;
    legendPositions.posFunc = labelPositionFunc;

    var groupName = this.chart.chartData.chartValueGroups[groupIndex].groupName;
    var legend = this._renderWireLegend(
      strings.join(': ', groupName, this.session.locale.decimalFormat.format(value)),
      legendPositions, 'line-chart-wire-label', false);
    legend.detachFunc();

    var that = this,
      mouseIn = function() {
        legend.attachFunc();
        $(this).data('$path').addClass('hover-style');
        if (that.toBigLabelHoverFunc) {
          that.toBigLabelHoverFunc(that.xAxisLabels[index]);
        }
      },
      mouseOut = function() {
        legend.detachFunc();
        $(this).data('$path').removeClass('hover-style');
        if (that.toBigLabelHoverFunc) {
          that.toBigLabelHoverOffFunc(that.xAxisLabels[index]);
        }
      };
    this.$paths[groupIndex].data('$bubbles').push($bubble);

    $bubble.on('click', this._createClickObject(0, index, groupIndex), this.chart._onValueClick.bind(this.chart));

    $bubble
      .mouseenter(mouseIn)
      .mouseleave(mouseOut)
      .data('$path', this.$paths[groupIndex])
      .data('legend', legend);
  }
}
