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
import {numbers, RoundingMode} from '@eclipse-scout/core';
import {AbstractSvgChartRenderer} from '../index';

export default class SpeedoChartRenderer extends AbstractSvgChartRenderer {

  constructor(chart) {
    super(chart);
    this.segmentSelectorForAnimation = '.pointer';
    this.suppressLegendBox = true;

    let defaultConfig = {
      options: {
        speedo: {
          greenAreaPosition: undefined
        }
      }
    };
    chart.config = $.extend(true, {}, defaultConfig, chart.config);
  }

  static Position = {
    LEFT: 'left',
    CENTER: 'center',
    RIGHT: 'right'
  };

  static NUM_PARTS_GREEN_CENTER = 7;
  static NUM_PARTS_GREEN_EDGE = 4;

  static ONE_THOUSAND = 1000;
  static TEN_THOUSAND = 10000;
  static ONE_MILLION = 1000000;

  static ARC_MIN = -0.25;
  static ARC_MAX = 0.25;
  static ARC_RANGE = SpeedoChartRenderer.ARC_MAX - SpeedoChartRenderer.ARC_MIN;

  static SEGMENT_GAP = 0.0103; // space between two segments (lines)

  _validate() {
    let chartData = this.chart.data;
    let chartConfig = this.chart.config;
    if (!chartData ||
      !chartConfig ||
      chartData.axes.length > 0 ||
      chartData.chartValueGroups.length !== 1 ||
      chartData.chartValueGroups[0].values.length !== 3 ||
      chartConfig.options.speedo.greenAreaPosition === undefined) {
      return false;
    }
    return true;
  }

  _renderInternal() {
    let chartData = this.chart.data,
      minValue = chartData.chartValueGroups[0].values[0],
      maxValue = chartData.chartValueGroups[0].values[2],
      value = chartData.chartValueGroups[0].values[1];

    // radius of the scale
    this.r = Math.min(this.chartBox.height, this.chartBox.width / 2) * 0.7;

    // width (thickness) of the scale
    this.scaleWeight = this.r * 0.27;

    this.my = this.chartBox.yOffset + this.chartBox.height - (this.chartBox.height - this.r * 1.12) / 2;

    // number of parts in the scale
    this.parts = this.chart.config.options.speedo.greenAreaPosition === SpeedoChartRenderer.Position.CENTER ?
      SpeedoChartRenderer.NUM_PARTS_GREEN_CENTER : SpeedoChartRenderer.NUM_PARTS_GREEN_EDGE;

    // number of lines per part
    this.numSegmentsPerPart = this.parts === SpeedoChartRenderer.NUM_PARTS_GREEN_CENTER ? 5 : 8;

    // to remember 'filled' parts
    this.$filledParts = [];

    let numTotalSegments = this.parts * this.numSegmentsPerPart; // total number of lines in the whole chart (all colors)
    let numTotalGaps = numTotalSegments - 1;

    // width of one segment (line)
    this.segmentWidth = (SpeedoChartRenderer.ARC_RANGE - (numTotalGaps * SpeedoChartRenderer.SEGMENT_GAP)) / numTotalSegments;

    // width of one segment including the gap to the next segment.
    this.widthOfSegmentWithGap = this.segmentWidth + SpeedoChartRenderer.SEGMENT_GAP;

    // pointer value in range [0,1]
    let valuePercentage = this._limitValue((value - minValue) / (maxValue - minValue), 1);

    // value in the range [0,1] rounded to one segment
    let segmentToPointAt = Math.round(valuePercentage * numTotalGaps);

    // value rounded to the closest segment so that the pointer never stays "in between" two segments but always on a segment
    let valuePercentageRounded = this._getPercentageValueOfSegment(segmentToPointAt % this.numSegmentsPerPart,
      this._getPartForValue(valuePercentage));

    for (let i = 0; i < this.parts; i++) {
      this._renderCirclePart(i);
    }

    this._renderPointer(valuePercentageRounded);
    this._renderLegend(minValue, value, maxValue, chartData.chartValueGroups[0].groupName);

    this.$svg.addClass('speedo-chart-svg');
    if (this.chart.config.options.clickable) {
      this.$svg.on('click', this._createClickObject(null, null), this.chart._onValueClick.bind(this.chart));
    }
  }

  _limitValue(value, maxValue) {
    value = Math.max(value, 0); // cannot be < 0
    value = Math.min(value, maxValue); // cannot be > maxValue
    return value;
  }

  /**
   * Gets the percentage value in range [0,1] of the specified segment.
   */
  _getPercentageValueOfSegment(segmentIndexInPart, part) {
    // get the segment position
    let pointerRange = this._calcSegmentPos(segmentIndexInPart, part);

    // calculate the center position in the Arc range [0, 0.5] of the segment
    let pointerPos = pointerRange.from - SpeedoChartRenderer.ARC_MIN + ((pointerRange.to - pointerRange.from) / 2);

    // calculate the percentage value of the center of the segment in range [0,1]
    return this._limitValue(pointerPos / SpeedoChartRenderer.ARC_RANGE, 1);
  }

  /**
   * Renders the pointer line and registers animation to move the pointer and the corresponding filling of the segments
   */
  _renderPointer(valuePercentage) {
    this.$pointer = this.$svg.appendSVG('path', 'pointer')
      .attr('d', this._pathPointer(0))
      .attr('data-end', valuePercentage)
      .attr('stroke-width', (this.scaleWeight / 6) + 'px') // width of the pointer bar depends on size of chart
      .attr('fill', 'none');

    if (this.animationDuration) {
      let that = this;
      let tweenIn = function(now, fx) {
        let val = this.getAttribute('data-end') * fx.pos;
        that._updatePointer(val);
        that._updatePartsFill(val);
      };

      this.$pointer
        .animate({
          tabIndex: 0
        }, this._createAnimationObjectWithTabIndexRemoval(tweenIn, this.animationDuration));
    } else {
      this._updatePointer(valuePercentage);
      this._updatePartsFill(valuePercentage);
    }
  }

  /**
   * renders a single segment line.
   */
  _renderSegment(from, to, colorClass) {
    return this.$svg.appendSVG('path', 'speedo-chart-arc ' + colorClass)
      .attr('id', 'ArcAxisWide' + this.chart.id)
      .attr('fill', 'none')
      .attr('stroke-width', this.scaleWeight + 'px')
      .attr('d', this._pathSegment(from, to));
  }

  _renderCirclePart(part) {
    let colorClass = this._getColorForPart(part);
    // render 'empty' segments
    for (let i = 0; i < this.numSegmentsPerPart; i++) {
      let segPos = this._calcSegmentPos(i, part);
      this._renderSegment(segPos.from, segPos.to, colorClass);
    }

    // render and remember 'filled' segments (not visible by default).
    this.$filledParts.push(this._renderSegment(SpeedoChartRenderer.ARC_MIN, SpeedoChartRenderer.ARC_MIN, colorClass)); // 'filled' segments. invisible by default
  }

  _renderLegend(minValue, value, maxValue, groupName) {
    let minMaxLegendFontSize = this.scaleWeight * 0.8,
      padding = 5, // same as in AbstractChartRenderer#_renderWireLegend
      labelYPos = this.my + padding,
      labelMinMaxYPos = labelYPos + minMaxLegendFontSize * 0.8,
      positions = {
        x1: null,
        x2: this.chartBox.mX() - padding,
        y1: null,
        y2: labelYPos,
        v: -1,
        h: 1
      },
      minLegendValue = minValue ? this._formatValue(minValue) : 0,
      legendValue = value ? this._formatValue(value) : 0,
      maxLegendValue = maxValue ? this._formatValue(maxValue) : 0;

    // tooltip for min/max value
    if (this.chart.config.options.plugins.tooltip.enabled) {
      // min value
      let $minLegend = this.$svg.appendSVG('text', 'line-label line-chart-wire-label')
        .attr('x', this.chartBox.mX() - this.r)
        .attr('y', labelMinMaxYPos)
        .text(minLegendValue)
        .attr('style', 'font-size: ' + minMaxLegendFontSize + 'px; text-anchor: middle');

      // max value
      let $maxLegend = this.$svg.appendSVG('text', 'line-label line-chart-wire-label')
        .attr('x', this.chartBox.mX() + this.r)
        .attr('y', labelMinMaxYPos)
        .text(maxLegendValue)
        .attr('style', 'font-size: ' + minMaxLegendFontSize + 'px; text-anchor: middle');

      let mouseIn = function() {
        this.$svg.append($minLegend);
        this.$svg.append($maxLegend);
      }.bind(this);
      let mouseOut = () => {
        $minLegend.detach();
        $maxLegend.detach();
      };
      mouseOut();
      this.$svg
        .mouseenter(mouseIn)
        .mouseleave(mouseOut);
    }

    // actual value
    if (this.chart.config.options.plugins.legend.display) {
      this._renderLineLabel(positions.x2 + padding, positions.y2 + positions.v * padding, legendValue, '', false)
        .addClass('speedo-chart-label')
        .attr('style', 'font-size: ' + this.scaleWeight * 1.55 + 'px;');
    }
  }

  /**
   * returns the part index for the specified valuePercentage. The valuePercentage must be in the range [0,1].
   */
  _getPartForValue(valuePercentage) {
    let part = Math.floor(valuePercentage * this.parts);
    return this._limitValue(part, this.parts - 1);
  }

  /**
   * Formats the speedo-values in a compact way:
   * 0 - 999: as is
   * 1 to 9'999: as is, with grouping separators
   * 10 to 999 thousand: 123k
   * >= 1 million: millions with max. two fraction digits -> 1.23M
   */
  _formatValue(value) {
    if (value < SpeedoChartRenderer.TEN_THOUSAND) {
      return this.session.locale.decimalFormat.format(value);
    }
    if (value < SpeedoChartRenderer.ONE_MILLION) {
      return Math.floor(value / SpeedoChartRenderer.ONE_THOUSAND) + 'k';
    }
    let millions = value / SpeedoChartRenderer.ONE_MILLION;
    millions = numbers.round(millions, RoundingMode.HALF_UP, 2);
    return this.session.locale.decimalFormat.format(millions) + 'M';
  }

  /**
   * Updates the pointer position to point to the specified valuePercentage in range [0,1].
   */
  _updatePointer(valuePercentage) {
    this.$pointer
      .attr('d', this._pathPointer(valuePercentage))
      .removeClass('red yellow light-green dark-green')
      .addClass(this._getColorForPart(this._getPartForValue(valuePercentage)));
  }

  /**
   * Updates the filling of the 'filled' segments to be filled up to the specified valuePercentage in range [0,1].
   */
  _updatePartsFill(valuePercentage) {
    let from, to;
    for (let part = 0; part < this.$filledParts.length; part++) {
      from = this._calcSegmentPos(0, part, this.segmentWidth).from;
      if ((part + 1) / this.parts < valuePercentage) {
        // the current part is smaller than the value: completely filled part
        to = this._calcSegmentPos(this.numSegmentsPerPart - 1, part, this.segmentWidth).to;
      } else if (part / this.parts > valuePercentage) {
        // the current part is bigger than the speedo-value: hide element
        from = SpeedoChartRenderer.ARC_MIN;
        to = SpeedoChartRenderer.ARC_MIN;
      } else {
        // the value is within the current part
        to = (SpeedoChartRenderer.ARC_RANGE * valuePercentage) - SpeedoChartRenderer.ARC_MAX; // fill part exactly to the value
      }
      this.$filledParts[part].attr('d', this._pathSegment(from, to));
    }
  }

  /**
   * Calculates the positions (from, to) of a single segment line.
   * @param segmentIndexInPart the index of the segment line within the part.
   * @param part the part index.
   */
  _calcSegmentPos(segmentIndexInPart, part) {
    let result = {
      from: 0,
      to: 0
    };
    let segmentNum = segmentIndexInPart + part * this.numSegmentsPerPart;
    result.from = segmentNum * this.widthOfSegmentWithGap - SpeedoChartRenderer.ARC_MAX;
    result.to = result.from + this.segmentWidth;
    return result;
  }

  _getColorForPart(part) {
    let position = this.chart.config.options.speedo.greenAreaPosition;
    switch (position) {
      case SpeedoChartRenderer.Position.LEFT:
        // only four parts
        if (part === 0) {
          return 'dark-green';
        } else if (part === 1) {
          return 'light-green';
        } else if (part === 2) {
          return 'yellow';
        } else if (part === 3) {
          return 'red';
        }
        break;
      case SpeedoChartRenderer.Position.RIGHT:
        // only four parts
        if (part === 0) {
          return 'red';
        } else if (part === 1) {
          return 'yellow';
        } else if (part === 2) {
          return 'light-green';
        } else if (part === 3) {
          return 'dark-green';
        }
        break;
      case SpeedoChartRenderer.Position.CENTER:
        if (part === 0) {
          return 'red';
        } else if (part === 1) {
          return 'yellow';
        } else if (part === 2) {
          return 'light-green';
        } else if (part === 3) {
          return 'dark-green';
        } else if (part === 4) {
          return 'light-green';
        } else if (part === 5) {
          return 'yellow';
        } else if (part === 6) {
          return 'red';
        }
        break;
      default:
        break;
    }
  }

  /**
   * calculates the path-values to be used in the 'd' attribute of the path tag for a segment.
   */
  _pathSegment(start, end) {
    let s = start * 2 * Math.PI,
      e = end * 2 * Math.PI,
      pathString = '';

    pathString += 'M ' + (this.chartBox.mX() + this.r * Math.sin(s)) + ' ' + (this.my - this.r * Math.cos(s)) + ' ';
    // http://www.w3.org/TR/SVG/paths.html#PathDataEllipticalArcCommands
    // A rx ry
    //   x-axis-rotation large-arc-flag sweep-flag
    //   x y
    pathString += 'A ' + this.r + ' ' + this.r + ' ';
    pathString += '0 ' + (end - start < 0.5 ? '0' : '1') + ' 1 ';
    pathString += (this.chartBox.mX() + this.r * Math.sin(e)) + ' ' + (this.my - this.r * Math.cos(e));

    return pathString;
  }

  /**
   * calculates the path-values to be used in the 'd' attribute of the path tag for the pointer
   */
  _pathPointer(valuePercentage) {
    let point = SpeedoChartRenderer.ARC_RANGE * valuePercentage - SpeedoChartRenderer.ARC_MAX;
    let s = point * 2 * Math.PI,
      pointerOuterR = this.r - (1.4 * this.scaleWeight),
      pointerInnerR = this.r + (1.37 * this.scaleWeight),
      pathString = '';

    pathString += 'M ' + (this.chartBox.mX() + pointerInnerR * Math.sin(s)) + ' ' + (this.my - pointerInnerR * Math.cos(s)) + ' ';
    pathString += 'L ' + (this.chartBox.mX() + pointerOuterR * Math.sin(s)) + ' ' + (this.my - pointerOuterR * Math.cos(s)) + ' ';
    pathString += 'Z';
    return pathString;
  }

  /**
   * @override
   */
  _removeAnimated(afterRemoveFunc) {
    if (this.animationTriggered) {
      return;
    }
    let that = this,
      tweenOut = function(now, fx) {
        let val = this.getAttribute('data-end') * (1 - fx.pos);
        that._updatePointer(val);
        that._updatePartsFill(val);
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
