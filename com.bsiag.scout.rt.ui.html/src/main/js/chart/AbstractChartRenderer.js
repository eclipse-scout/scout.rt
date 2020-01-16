/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSI CRM Software License v1.0
 * which accompanies this distribution as bsi-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {ObjectFactory, scout, strings, tooltips} from '@eclipse-scout/core';
import * as $ from 'jquery';
import {Chart} from '../index';

export default class AbstractChartRenderer {

  constructor(chart) {
    this.chart = chart;
    this.session = chart.session;
    this.rendering = false; // true while this.render() is executing
    this.rendered = false;
    this.animated = false; // set by render() and remove(), makes it unnecessary to carry an argument through all method calls

    this.chartBox = {};
    this.labelBox = {};

    // Viewbox settings
    this.viewBoxHeight = 1000;
    this.viewBoxWidth = 1000;

    // Clipping and masking
    this.clipId = 'Clip-' + ObjectFactory.get().createUniqueId();
    this.maskId = 'Mask-' + ObjectFactory.get().createUniqueId();

    // Padding constants
    this.viewBoxLegendBubblePadding = 20;
    this.legendBubblePadding = 5;
    this.viewBoxLegendPadding = 100;
    this.horizontalLegendPaddingLeft = 0;
    this.horizontalLegendEntriesPerLine = 3;

    this.verticalLegendPaddingLeft = 20;

    this.legendLabelClass = 'legend-label';

    this.suppressLegendBox = false;
  }

  static FONT_SIZE_SMALLEST = 'smallestFont';
  static FONT_SIZE_SMALL = 'smallFont';
  static FONT_SIZE_MIDDLE = 'middleFont';
  static FONT_SIZE_BIG = 'bigFont';

  validate() {
    var chartData = this.chart && this.chart.chartData;
    if (!chartData || !chartData.chartValueGroups || chartData.chartValueGroups.length === 0) {
      return false;
    }

    // check lengths
    var i, length = 0;
    for (i = 0; i < chartData.chartValueGroups.length; i++) {
      var chartValueGroup = chartData.chartValueGroups[i];
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
      if (!this.chart.autoColor && !chartValueGroup.colorHexValue && !chartValueGroup.cssClass) {
        return false;
      }
    }
    for (i = 0; i < chartData.axes.length; i++) {
      if (chartData.axes[i].length !== length) {
        return false;
      }
    }

    return this._validate();
  }

  _validate() {
    // Override in subclasses
    return true;
  }

  /**
   * @param requestAnimation
   *          Whether animations should be used while rendering the chart. Note that his
   *          property is ignored when chart.animated is <code>false</code>!
   */
  render(requestAnimation) {
    this.animated = requestAnimation && this.chart.animated;
    if (!this.validate() || !this.chart.rendered) {
      return;
    }
    this.rendering = true;
    if (!this.$svg) {
      this.$svg = this.chart.$container.appendSVG('svg', 'chart-svg');
    }
    this.svgHeight = this.$svg.height();
    this.svgWidth = this.$svg.width();
    // When chart is smaller than 300px only show two elements per line in legend
    if (this.svgWidth < 300) {
      this.horizontalLegendEntriesPerLine = 2;
    }
    var boxHeight = this.svgHeight,
      boxWidth = this.svgWidth;
    // This works, because CSS specifies 100% width/height
    this.height = boxHeight;
    this.width = boxWidth;
    this._initLegendTextHeights();
    this._initChartBox();
    this._initLabelBox();
    if (this._useFontSizeBig()) {
      this.$svg.addClass(AbstractChartRenderer.FONT_SIZE_BIG);
    } else if (this._useFontSizeMiddle()) {
      this.$svg.addClass(AbstractChartRenderer.FONT_SIZE_MIDDLE);
    } else if (this._useFontSizeSmall()) {
      this.$svg.addClass(AbstractChartRenderer.FONT_SIZE_SMALL);
    } else if (this._useFontSizeSmallest()) {
      this.$svg.addClass(AbstractChartRenderer.FONT_SIZE_SMALLEST);
    }
    if (!this.$svg.isAttached()) {
      // user navigated away. do not try to render->error
      return;
    }
    this._render();
    this.rendering = false;
    this.rendered = true;
  }

  _render() {
    // Override in subclasses
  }

  checkCompletlyRendered() {
    if (this.rendered || !this.chart.chartData) {
      return;
    }
    this.render();
  }

  _useFontSizeBig() {
    return false;
  }

  _useFontSizeMiddle() {
    return false;
  }

  _useFontSizeSmall() {
    return false;
  }

  _useFontSizeSmallest() {
    return false;
  }

  /**
   * @param requestAnimation
   *          Whether animations should be used while removing the chart. Note that his
   *          property is ignored when chart.animated is <code>false</code>!
   */
  remove(requestAnimation, afterRemoveFunc) {
    if (this.rendered && !this.chartAnimationStopping) {
      this.chartAnimationStopping = true;
      this.$svg.children().stop(true, false);
      this.chartAnimationStopping = false;
    }
    this.animated = requestAnimation && this.chart.animated;
    if (this.animated && this.rendered) {
      this._removeAnimated(afterRemoveFunc);
    } else {
      this._remove(afterRemoveFunc);
    }
  }

  _remove(afterRemoveFunc) {
    // this function is called directly from renderers after all removal animations are done
    // however, other animations may have been queued in the meantime (e.g. in case the chart was removed, then added (+animation queued), and then removed again)
    if (this.rendered) {
      this.$svg.children().stop(true, false);
      // need to check again, as stop() may have triggered a chart removal and this may not be rendered anymore
      if (this.rendered) {
        this.$svg.remove();
        this.$svg = null;
      }
    }
    this.rendered = false;
    afterRemoveFunc && afterRemoveFunc(this.chartAnimationStopping);
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

  _initRectRenderOptions(renderRectOptions) {
    // init all values for animation with -1

    // Default options (end value of "null" means "don't set this attribute")
    var options = {
      xStart: 0,
      xEnd: null,
      yStart: 0,
      yEnd: null,
      widthStart: 0,
      widthEnd: null,
      heightStart: 0,
      heightEnd: null,
      delay: 200,
      animate: this.animated,
      animationDuration: 600,
      clickObject: null,
      fill: null,
      opacity: 1,
      id: '',
      cssClass: '',
      customAttributes: [] // add custom attributes like this-> [[0]=>attributeName [1]=>attributeValue]
    };
    $.extend(options, renderRectOptions || {});
    return options;
  }

  /**
   * For all parameters: use -1 when parameter is not used or set by a chart type.
   *
   * @param axisIndex number
   * @param valueIndex number
   * @param groupIndex number
   */
  _createClickObject(axisIndex, valueIndex, groupIndex) {
    return {
      axisIndex: axisIndex,
      valueIndex: valueIndex,
      groupIndex: groupIndex
    };
  }

  _initLabelBox() {
    if (!this.chart.legendVisible || this.suppressLegendBox) {
      return;
    }
    var labelCount = Math.min(this.chart.chartData.chartValueGroups.length, this.chart.maxSegments),
      startY,
      startX,
      legendPadding = this.useViewBox ? this.viewBoxLegendPadding : this.horizontalLegendPaddingLeft,
      bubblePadding = this.useViewBox ? this.viewBoxLegendBubblePadding : this.legendBubblePadding,
      widthPerLabel;

    if (this.chart.legendPosition === Chart.LEGEND_POSITION_RIGHT || this.chart.legendPosition === Chart.LEGEND_POSITION_LEFT) {
      if (labelCount % 2 === 0) {
        startY = this.chartBox.mY() - ((labelCount / 2 - 1) * this.legendTextHeights.textHeight) - ((labelCount / 2) * this.legendTextHeights.textGap);
      } else {
        startY = this.chartBox.mY() - ((labelCount / 2 - 0.75) * this.legendTextHeights.textHeight) - (Math.floor(labelCount / 2) * this.legendTextHeights.textGap);
      }
      widthPerLabel = 0; // not used in vertical rendering
      startX = this.chart.legendPosition === Chart.LEGEND_POSITION_RIGHT ? this.chartBox.width : 0;
      startX = this.verticalLegendPaddingLeft + bubblePadding + this.legendTextHeights.bubbleR * 2 + startX;
    } else {
      startY = this.chart.legendPosition === Chart.LEGEND_POSITION_BOTTOM ? this.chartBox.height + this.legendTextHeights.textHeight : this.legendTextHeights.textGap + this.legendTextHeights.textHeight;
      startY += this.legendTextHeights.legendBoxPaddingTopBottom;
      widthPerLabel = (this.chartBox.width - 2 * legendPadding) / Math.min(labelCount, this.horizontalLegendEntriesPerLine);
      startX = legendPadding + bubblePadding + this.legendTextHeights.bubbleR * 2;
    }

    this.labelBox = {
      y: startY,
      x: startX,
      textGap: this.legendTextHeights.textGap,
      textHeight: this.legendTextHeights.textHeight,
      bubbleR: this.legendTextHeights.bubbleR,
      bubblePadding: bubblePadding,
      widthPerLabel: widthPerLabel,
      textWidth: widthPerLabel - this.legendTextHeights.bubbleR * 2 - bubblePadding * 2
    };
  }

  _initLegendTextHeights() {
    var textBoundingBox = this._measureText('MeasureHeight', this.legendLabelClass),
      textHeight = textBoundingBox.height,
      textGap = textHeight / 5,
      paddingTopBottom = textHeight / 2,
      bubbleR = textHeight / 8 * 3;

    this.legendTextHeights = {
      textHeight: textHeight,
      textGap: textGap,
      legendBoxPaddingTopBottom: paddingTopBottom,
      bubbleR: bubbleR
    };
  }

  _measureText(text, legendLabelClass) {
    var $label = this.$svg.appendSVG('text', legendLabelClass)
      .attr('x', 0)
      .attr('y', 0)
      .attr('visibility', 'hidden')
      .text(text);
    var textBoundingBox;
    try {
      // Firefox throws error when node is not in dom(already removed by navigating away). all other browser returns a boundingbox with 0
      textBoundingBox = $label[0].getBBox();
    } catch (e) {
      return {
        height: 0,
        width: 0
      };
    }
    $label.remove();

    return textBoundingBox;
  }

  _renderLegendEntry(label, color, colorClass, position) {
    if (!this.chart.legendVisible || this.suppressLegendBox) {
      return;
    }
    if (this.chart.legendPosition === Chart.LEGEND_POSITION_RIGHT ||
      this.chart.legendPosition === Chart.LEGEND_POSITION_LEFT) {
      this._renderVerticalLegendEntry(label, color, colorClass, position);
    }
    if (this.chart.legendPosition === Chart.LEGEND_POSITION_BOTTOM ||
      this.chart.legendPosition === Chart.LEGEND_POSITION_TOP) {
      this._renderHorizontalLegendEntry(label, color, colorClass, position);
    }

  }

  _renderHorizontalLegendEntry(label, color, colorClass, position) {
    var line = Math.floor(position / this.horizontalLegendEntriesPerLine);
    var posInLine = position % this.horizontalLegendEntriesPerLine;
    var offsetTop = this.labelBox.y + line * this.labelBox.textGap + line * this.labelBox.textHeight;
    var offsetLeft = this.labelBox.x + posInLine * this.labelBox.widthPerLabel;

    var shorterLabel = label;
    var realTextWidth = this._measureText(shorterLabel).width;
    if (realTextWidth > this.labelBox.textWidth) {
      var i = 1; // number of deleted characters
      while (realTextWidth > this.labelBox.textWidth && i < label.length) {
        shorterLabel = label.substr(0, label.length - i) + '...';
        realTextWidth = this._measureText(shorterLabel).width;
        i++;
      }
      if (i === label.length) {
        shorterLabel = '';
      }
    }

    var $label = this.$svg.appendSVG('text', this.legendLabelClass)
      .attr('x', offsetLeft)
      .attr('y', offsetTop)
      .text(shorterLabel);
    if (this.animated) {
      $label
        .attr('opacity', 0)
        .delay(400)
        .animateSVG('opacity', 1, 400, null, true);
    }

    addTooltipIfShortLabel.call(this, $label);

    if (color || colorClass) {
      var $bubble = this._renderLegendBubble(color, colorClass, offsetLeft, offsetTop);
      addTooltipIfShortLabel.call(this, $bubble);
    }

    return $label;

    // ----- Helper function -----

    function addTooltipIfShortLabel($element) {
      if (shorterLabel !== label) {
        tooltips.install($element, {
          parent: this.chart,
          text: label,
          delay: 0
        });
      }
    }
  }

  _renderVerticalLegendEntry(label, color, colorClass, position) {
    var offsetTop = this.labelBox.y + position * this.labelBox.textGap + position * this.labelBox.textHeight;
    var $label = this.$svg.appendSVG('text', this.legendLabelClass)
      .attr('x', this.labelBox.x)
      .attr('y', offsetTop)
      .text(label);
    if (this.animated) {
      $label
        .attr('opacity', 0)
        .delay(400)
        .animateSVG('opacity', 1, 400, null, true);
    }

    if (color || colorClass) {
      this._renderLegendBubble(color, colorClass, this.labelBox.x, offsetTop);
    }

    return $label;
  }

  _renderLegendBubble(color, colorClass, x, y) {
    var $bubble = this.$svg.appendSVG('circle', 'legend-bubble' + strings.box(' ', colorClass, ''))
      .attr('cx', x - this.labelBox.bubblePadding - this.labelBox.bubbleR)
      .attr('cy', y - this.labelBox.bubbleR + 1)
      .attr('r', this.labelBox.bubbleR);

    if (color) {
      $bubble.attr('fill', color);
    }
    if (this.animated) {
      $bubble
        .attr('opacity', 0)
        .delay(400)
        .animateSVG('opacity', 1, 400, null, true);
    }

    return $bubble;
  }

  _renderRect(renderRectOptions) {
    var $rect = this.$svg.appendSVG('rect', renderRectOptions.cssClass, '', renderRectOptions.id)
      .attr('x', renderRectOptions.xStart)
      .attr('y', renderRectOptions.yStart)
      .attr('width', renderRectOptions.widthStart)
      .attr('height', renderRectOptions.heightStart)
      .attr('opacity', renderRectOptions.opacity)
      .delay(renderRectOptions.delay);

    if (this.chart.clickable) {
      $rect.on('click', renderRectOptions.clickObject, this.chart._onValueClick.bind(this.chart));
    }

    for (var i = 0; i < renderRectOptions.customAttributes.length; i++) {
      var customAttribute = renderRectOptions.customAttributes[i];
      if (customAttribute.length === 2) {
        $rect.attr(customAttribute[0], customAttribute[1]);
      }
    }

    applyAttribute('x', renderRectOptions.xEnd);
    applyAttribute('y', renderRectOptions.yEnd);
    applyAttribute('height', renderRectOptions.heightEnd);
    applyAttribute('width', renderRectOptions.widthEnd);

    if (renderRectOptions.fill) {
      $rect.attr('fill', renderRectOptions.fill);
    }

    return $rect;

    // ----- Helper functions -----

    function applyAttribute(attribute, value) {
      if (scout.nvl(value, -1) < 0) {
        return;
      }
      if (renderRectOptions.animate) {
        $rect.animateSVG(attribute, value, renderRectOptions.animationDuration, null, true);
      } else {
        $rect.attr(attribute, value);
      }
    }
  }

  _renderLine(x1, y1, x2, y2, lineClass) {
    var $line = this.$svg.appendSVG('line', lineClass)
      .attr('x1', x1).attr('y1', y1)
      .attr('x2', x2).attr('y2', y2);
    if (this.animated) {
      $line
        .attr('opacity', 0)
        .delay(200)
        .animateSVG('opacity', 1, 600, null, true);
    }
    return $line;
  }

  _renderLineLabel(x, y, label, labelClass, drawBackground) {
    var $label = this.$svg.appendSVG('text', labelClass ? labelClass : 'line-label')
      .attr('x', x).attr('y', y)
      .text(label);

    if (drawBackground) {
      $label.attr('mask', 'url(#' + this.maskId + ')');

      var $background = this.$svg.appendSVG('text', labelClass ? labelClass + ' background' : 'line-label-background')
        .attr('x', x).attr('y', y)
        .attr('clip-path', 'url(#' + this.clipId + ')')
        .text(label);

      $label.data('$background', $background);

      if (this.animated) {
        $background
          .attr('opacity', 0)
          .delay(200)
          .animateSVG('opacity', 1, 600, null, true);
      }
    }

    if (this.animated) {
      $label
        .attr('opacity', 0)
        .delay(200)
        .animateSVG('opacity', 1, 600, null, true);
    }
    return $label;
  }

  _initChartBox() {
    this.chartBox = {
      width: this._calcChartBoxWidth(),
      height: this._calcChartBoxHeight(),
      xOffset: this._calcChartBoxXOffset(),
      yOffset: this._calcChartBoxYOffset(),
      mX: function() {
        return this.xOffset + (this.width / 2);
      },
      mY: function() {
        return this.yOffset + (this.height / 2);
      }
    };
  }

  _calcChartBoxWidth() {
    if (this.chart.legendVisible && !this.suppressLegendBox &&
      (this.chart.legendPosition === Chart.LEGEND_POSITION_RIGHT ||
        this.chart.legendPosition === Chart.LEGEND_POSITION_LEFT)) {
      return this.width / 2;
    }
    return this.width;
  }

  _calcChartBoxHeight() {
    if (this.chart.legendVisible && !this.suppressLegendBox) {
      var lines = Math.ceil(this.chart.chartData.chartValueGroups.length / this.horizontalLegendEntriesPerLine);
      if (this.chart.legendPosition === Chart.LEGEND_POSITION_BOTTOM) {
        return this.height - this.legendTextHeights.legendBoxPaddingTopBottom - (this.legendTextHeights.textHeight + this.legendTextHeights.textGap) * lines + this.legendTextHeights.textGap;
      } else if (this.chart.legendPosition === Chart.LEGEND_POSITION_TOP) {
        return this.height - this.legendTextHeights.legendBoxPaddingTopBottom - (this.legendTextHeights.textHeight + this.legendTextHeights.textGap) * lines;
      }
    }
    return this.height;
  }

  _calcChartBoxXOffset() {
    if (this.chart.legendVisible && !this.suppressLegendBox && this.chart.legendPosition === Chart.LEGEND_POSITION_LEFT) {
      return this.width / 2;
    }
    return 0;
  }

  _calcChartBoxYOffset() {
    if (this.chart.legendVisible && !this.suppressLegendBox && this.chart.legendPosition === Chart.LEGEND_POSITION_TOP) {
      var lines = Math.ceil(this.chart.chartData.chartValueGroups.length / this.horizontalLegendEntriesPerLine);
      return (this.legendTextHeights.textHeight + this.legendTextHeights.textGap) * lines + this.legendTextHeights.textGap + this.legendTextHeights.legendBoxPaddingTopBottom;
    }
    return 0;
  }

  _createAnimationObjectWithTabindexRemoval(animationFunc, duration) {
    return {
      step: function(now, fx) {
        try {
          animationFunc.bind(this)(now, fx);
        } catch (e) {
          // prevent logging thousands of exceptions (1 per animation step) by stopping and clearing the queue
          $(this).stop(true, false);
          throw e;
        }
      },
      duration: duration ? duration : 600,
      complete: function() {
        $(this).removeAttr('tabindex');
      }
    };
  }

  _addClipping(cssClass) {
    // add clip and mask paths for all relevant objects
    var $clip = this.$svg
      .appendSVG('clipPath');
    $clip[0].id = this.clipId;

    var $mask = this.$svg.appendSVG('mask');
    $mask.appendSVG('rect')
      .attr('x', 0)
      .attr('y', 0)
      .attr('width', '100%')
      .attr('height', '100%')
      .attr('fill', 'white');
    $mask[0].id = this.maskId;

    this.chart.$container.find('.' + cssClass).each(function(i) {
      this.id = 'ClipMask-' + ObjectFactory.get().createUniqueId();
      $clip.appendSVG('use').attrXLINK('href', '#' + this.id);
      $mask.appendSVG('use').attrXLINK('href', '#' + this.id);
    });
  }

  _renderWireLegend(text, legendPositions, className, drawBackgroundBox) {
    var legend = {
      detachFunc: function() {
      },
      attachFunc: function() {
      },
      removeFunc: function() {
      }
    };
    if (!this.chart.interactiveLegendVisible) {
      return legend;
    }
    var padding = 5,
      $background,
      backgroundWidth = 0,
      lineHeight = 17,
      backgroundHeight = lineHeight;

    if (drawBackgroundBox) {
      $background = this.$svg.appendSVG('rect', 'wire-legend-background-box')
        .attr('opacity', '1');
    }

    var positions = legendPositions;

    // draw and measure label

    var $legend,
      lengthLegend = 0;

    if (Array.isArray(text)) {
      for (var i = 0; i < text.length; i++) {
        var posIndex = text.length - i - 1;
        var yPos = positions.y2 + positions.v * padding - lineHeight * posIndex - padding * posIndex;
        var $line = this._renderLineLabel(positions.x2 + padding, yPos, text[i], '', drawBackgroundBox);
        $line.addClass(className);
        lengthLegend = Math.max(lengthLegend, $line[0].getComputedTextLength());
        if (i === 0) {
          $legend = $line;
        } else {
          if ($legend.data('lines')) {
            $legend.data('lines').push($line);
          } else {
            $legend.data('lines', [$line]);
          }
        }
      }
    } else {
      $legend = this._renderLineLabel(positions.x2 + padding, positions.y2 + positions.v * padding, text, '', drawBackgroundBox);
      $legend.addClass(className);
      lengthLegend = $legend[0].getComputedTextLength();
    }
    backgroundWidth = lengthLegend + 2 * padding;

    if (legendPositions.autoPosition) {
      positions = legendPositions.posFunc.call(this, backgroundWidth, backgroundHeight);
      // adjust legend
      $legend.attr('x', positions.x2 + padding);
      $legend.attr('y', positions.y2 + positions.v * padding);
    }

    // fix layout depending on orientation of legend
    if (positions.h === -1) {
      $legend.attr('x', positions.x2 - padding - lengthLegend);
      $legend.css('text-anchor', 'left');
      if ($legend.data('lines')) {
        $legend.data('lines').forEach(function($line) {
          $line.attr('x', positions.x2 - padding - lengthLegend);
          $line.css('text-anchor', 'left');
        });
      }
    } else {
      $legend.attr('x', positions.x2 + padding);
      $legend.css('text-anchor', 'right');
      if ($legend.data('lines')) {
        $legend.data('lines').forEach(function($line) {
          $line.attr('x', positions.x2 + padding);
          $line.css('text-anchor', 'right');
        });
      }
    }
    if (positions.v === 1) {
      if ($legend.data('lines')) {
        $legend.data('lines').forEach(function($line, i) {
          $line.attr('y', positions.x2 - padding - lengthLegend);
          var index = 1 + i;
          $line.attr('y', positions.y2 + positions.v * padding + lineHeight * index + padding * (index + 1));
        });
      }
      $legend.attr('dy', '0.7em');
    } else {
      if ($legend.data('lines')) {
        var index = $legend.data('lines').length;
        $legend.attr('y', positions.y2 + positions.v * padding - lineHeight * index - padding * index);
        $legend.data('lines').forEach(function($line, i) {
          index = $legend.data('lines').length - 1 - i;
          $line.attr('y', positions.y2 + positions.v * padding - lineHeight * index - padding * index);
        });
      }
    }

    // align background text
    $legend.add($legend.data('lines')).each(function(i, line) {
      var $line = $(line),
        $background = $line.data('$background');
      if ($background) {
        $background.attr('x', $line.attr('x'));
        $background.attr('y', $line.attr('y'));
        $background.css('text-anchor', $line.css('text-anchor'));
        $background.attr('dy', $line.attr('dy'));
      }
    });

    // draw lines, if wished
    var wires = [];
    if (positions.x1 > 0 && positions.y1 > 0) {
      wires.push(this._renderLine(positions.x1, positions.y1, positions.x2, positions.y2, 'label-line'));
      wires.push(this._renderLine(positions.x2, positions.y2, positions.x2 + positions.h * (lengthLegend + 2 * padding), positions.y2, 'label-line'));
    }
    $legend.data('wires', wires);

    var $svg = this.$svg;
    legend.detachFunc = function() {
      $legend.data('wires').forEach(function($wire) {
        $wire.detach();
      });
      if ($legend.data('lines')) {
        $legend.data('lines').forEach(function($line) {
          if ($line.data('$background')) {
            $svg.append($line.data('$background'));
          }
          $line.detach();
        });
      }
      if ($legend.data('$background')) {
        $legend.data('$background').remove();
      }
      $legend.detach();
    };

    legend.attachFunc = function() {
      $svg.append($legend);
      if ($legend.data('$background')) {
        $svg.append($legend.data('$background'));
      }
      $svg.append($legend.data('wires'));
      if ($legend.data('lines')) {
        $legend.data('lines').forEach(function($line) {
          $svg.append($line);
          if ($line.data('$background')) {
            $svg.append($line.data('$background'));
          }
        });
      }
    };

    legend.removeFunc = function() {
      $legend.data('wires').forEach(function($wire) {
        $wire.remove();
      });
      if ($legend.data('lines')) {
        $legend.data('lines').forEach(function($line) {
          if ($line.data('$background')) {
            $line.data('$background').remove();
          }
          $line().remove();
        });
      }
      if ($legend.data('$background')) {
        $legend.data('$background').remove();
      }
      $legend.remove();
    };
    legend.$field = $legend;
    return legend;
  }
}
