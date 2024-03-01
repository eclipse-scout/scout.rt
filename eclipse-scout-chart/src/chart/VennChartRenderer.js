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
import {AbstractSvgChartRenderer, VennAsync3Calculator, VennCircle, VennCircleHelper} from '../index';
import $ from 'jquery';
import {arrays} from '@eclipse-scout/core';

export default class VennChartRenderer extends AbstractSvgChartRenderer {

  constructor(chart) {
    super(chart);
    this.animationTriggered = false;
    this.suppressLegendBox = true;

    let defaultConfig = {
      options: {
        venn: {
          numberOfCircles: undefined
        }
      }
    };
    chart.config = $.extend(true, {}, defaultConfig, chart.config);
  }

  _validate() {
    let chartData = this.chart.data;
    if (!chartData ||
      chartData.axes.length !== 0 ||
      chartData.chartValueGroups.length === 0 ||
      chartData.chartValueGroups[0].values.length === 0) {
      return false;
    }
    return true;
  }

  _renderInternal() {
    this.centerX = this.width / 2;
    this.centerY = this.height / 2;

    if (this.centerX === 0 || this.centerY === 0) {
      return false;
    }

    // basic values
    this.data = this.chart.data.chartValueGroups;
    this.numberOfCircles = this.chart.config.options.venn.numberOfCircles;

    // render parameter
    let distR = 10,
      maxR = Math.min(this.centerX, this.centerY),
      minR = maxR / 15,
      total = this.data.reduce((s, e) => {
        return s + parseFloat(e.values[0]);
      }, 1);

    this.vennCircleHelper = new VennCircleHelper(distR, maxR, minR, total);

    // create svg elements and venns
    if (this.numberOfCircles > 0) {
      this.$v1 = this._createCircle(0, arrays.ensure(this.data[0].colorHexValue)[0], this.data[0].cssClass);
      this.vennNumber1 = new VennCircle(this.$v1);
      this.vennReal1 = new VennCircle(this.$v1);
    }
    if (this.numberOfCircles > 1) {
      this.$v2 = this._createCircle(1, arrays.ensure(this.data[1].colorHexValue)[0], this.data[1].cssClass);
      this.vennNumber2 = new VennCircle(this.$v2);
      this.vennReal2 = new VennCircle(this.$v2);
    }
    if (this.numberOfCircles > 2) {
      this.$v3 = this._createCircle(2, arrays.ensure(this.data[2].colorHexValue)[0], this.data[2].cssClass);
      this.vennNumber3 = new VennCircle(this.$v3);
      this.vennReal3 = new VennCircle(this.$v3);
    }

    // Final callback
    // In the case of 3 circles, draw will be called async after render is completed. Therefore, the current animationDuration needs to be set again when _draw is finally called.
    const animationDurationRender = this.animationDuration;
    let draw = function() {
      this.readyToDraw = true;
      if (!this.$svg.isAttached()) {
        // user navigated away. do not try to render-> error
        return;
      }
      this.readyToDraw = false;
      const animationDuration = this.animationDuration;
      this.setAnimationDuration(animationDurationRender);
      this._draw(true, true);
      this.setAnimationDuration(animationDuration);
    }.bind(this);

    // save callback if user navigated away while calculating and _draw is not executed.
    this.readyToDraw = false;

    // calc venns and set legend
    if (this.numberOfCircles === 1) {
      this._calc1(this.vennNumber1);
      this._calc1(this.vennReal1);
      draw();

    } else if (this.numberOfCircles === 2) {
      this._calc2(this.vennNumber1, this.vennNumber2, false);
      this._calc2(this.vennReal1, this.vennReal2, true);
      draw();

    } else if (this.numberOfCircles === 3) {
      this._calc3(this.vennNumber1, this.vennNumber2, this.vennNumber3, false, () => {
        if (this.rendering || this.rendered) {
          this._calc3(this.vennReal1, this.vennReal2, this.vennReal3, true, draw);
        }
      });
    }
  }

  remove(requestAnimation, afterRemoveFunc) {
    this._cancelAsync3Calculator();
    super.remove(requestAnimation, afterRemoveFunc);
  }

  // calculation

  _calc1(v1) {
    // set basic data
    let a = this.data[0].values[0];

    // calc sizes
    if (a > 0) {
      v1.r = this.vennCircleHelper.calcR(a, 0.8);
    } else {
      v1.r = this.vennCircleHelper.calcR(a, 0);
    }

    v1.x = 0;
    v1.y = 0;

    // place legend and label
    v1.setLegend(this.data[0].groupName, 1, -1);
    v1.addLabel(a, v1.x, v1.y);
  }

  _calc2(v1, v2, real) {
    // set basic data
    let a = this.data[0].values[0];
    let b = this.data[1].values[0];
    let ab = this.data[2].values[0];
    let d12;

    if (real) {
      // basics calculation
      v1.r = this.vennCircleHelper.calcR(a + ab, 0.8);
      v2.r = this.vennCircleHelper.calcR(b + ab, 0.8);
      d12 = this.vennCircleHelper.calcD(v1, v2, a, b, ab, true);

      // calc x
      v1.x = 0;
      v2.x = d12;
    } else {
      // eslint-disable-next-line no-multi-assign
      v1.r = v2.r = this.vennCircleHelper.calcR(-1, 0.7);
      v1.x = -v1.r * 0.6;
      v2.x = v2.r * 0.6;
    }

    // calc y ;)
    v1.y = 0;
    v2.y = 0;

    // balance circles
    this.vennCircleHelper.findBalance2(v1, v2);

    // prepare legend
    v1.setLegend(this.data[0].groupName, -1, -1);
    v2.setLegend(this.data[1].groupName, 1, -1);

    // draw labels, and fix legend
    if (real) {
      if (ab === 0) {
        v1.addLabel(a, v1.x, v1.y);
        v2.addLabel(b, v2.x, v2.y);
      } else if (a === 0 && b === 0) {
        v1.addLabel(ab, v1.x, v1.y);
      } else if (a === 0) {
        v2.addLabel(ab, v1.x, v1.y);
        v2.addLabel(b, v2.x - (d12 - v2.r - v1.r) / 2, v2.y);
        v1.legendR = v2.r - d12;
      } else if (b === 0) {
        v1.addLabel(ab, v2.x, v2.y);
        v1.addLabel(a, v1.x + (d12 - v2.r - v1.r) / 2, v1.y);
        v2.legendR = v1.r - d12;
      } else {
        v1.addLabel(a, v1.x + (d12 - v2.r - v1.r) / 2, v1.y);
        v1.addLabel(b, v2.x - (d12 - v2.r - v1.r) / 2, v2.y);
        v2.addLabel(ab, v1.x + v1.r + (d12 - v2.r - v1.r) / 2, v1.y);
      }
    } else {
      v1.addLabel(a, -v1.r * 0.9, 0);
      v1.addLabel(b, v1.r * 0.9, 0);
      v2.addLabel(ab, 0, 0);
    }
  }

  _calc3(v1, v2, v3, real, callback) {
    // set basic data
    let a = this.data[0].values[0];
    let b = this.data[1].values[0];
    let c = this.data[2].values[0];
    let ab = this.data[3].values[0];
    let ac = this.data[4].values[0];
    let bc = this.data[5].values[0];
    let abc = this.data[6].values[0];

    let d12, d13, d23;

    // calc sizes
    if (real) {
      // basics calculation
      v1.r = this.vennCircleHelper.calcR(a + ab + ac + abc, 0.55);
      v2.r = this.vennCircleHelper.calcR(b + ab + bc + abc, 0.55);
      v3.r = this.vennCircleHelper.calcR(c + ac + bc + abc, 0.55);

      // find distance between a; may reduce r
      d12 = this.vennCircleHelper.calcD(v1, v2, a + ac, b + bc, ab + abc, true);
      d13 = this.vennCircleHelper.calcD(v1, v3, a + ab, c + bc, ab + abc, false);
      d23 = this.vennCircleHelper.calcD(v2, v3, b + ab, c + ac, ab + abc, false);

      // find coordinates of a and b
      v1.x = 0;
      v2.x = d12;
      v3.x = d13;

      v1.y = 0;
      v2.y = 0;
      v3.y = 0;

      // c is much more difficult..., only changes v3
      this._cancelAsync3Calculator();
      this.async3Calculator = new VennAsync3Calculator(this.vennCircleHelper, v1, v2, v3, a, b, c, ab, ac, bc, abc, d12, d13, d23);
      this.async3Calculator.start(() => {
        this.async3Calculator = null;

        // balance circles
        this.vennCircleHelper.findBalance3(v1, v2, v3);

        // prepare legend
        v1.setLegend(this.data[0].groupName, -1, 1);
        v2.setLegend(this.data[1].groupName, 1, 1);
        v3.setLegend(this.data[2].groupName, 1, -1);

        callback();
      });

    } else {
      // draw label
      // eslint-disable-next-line no-multi-assign
      v1.r = v2.r = v3.r = this.vennCircleHelper.calcR(-1, 0.55);

      v1.x = -v1.r * 0.73;
      v2.x = v2.r * 0.73;
      v3.x = 0;

      v1.y = v1.r * 0.58;
      v2.y = v2.r * 0.58;
      v3.y = -v3.r * 0.58;

      // prepare legend
      v1.setLegend(this.data[0].groupName, -1, 1);
      v2.setLegend(this.data[1].groupName, 1, 1);
      v3.setLegend(this.data[2].groupName, 1, -1);

      v1.addLabel(a, -v1.r, v1.r * 0.76);
      v2.addLabel(b, v1.r, v1.r * 0.76);
      v3.addLabel(c, 0, -v1.r * 0.82);
      v1.addLabel(ab, 0, v1.r * 0.76);
      v1.addLabel(ac, -v1.r * 0.49, -v1.r * 0.05);
      v2.addLabel(bc, v1.r * 0.49, -v1.r * 0.05);
      v1.addLabel(abc, 0, v1.r * 0.22);

      callback();
    }
  }

  _cancelAsync3Calculator() {
    if (this.async3Calculator) {
      this.async3Calculator.cancel();
      this.async3Calculator = null;
    }
  }

  // drawing

  _draw(animated, real) {
    if (!this.rendered && !this.rendering) { // additional check, because this method might be called from a setTimeout()
      return;
    }

    if (this.animationTriggered) {
      return;
    }
    this.animationTriggered = true;

    // remove labels and legends
    let that = this;
    this.$svg.children('.venn-legend, .venn-label, .venn-axis-white, .label-line')
      .stop()
      .animateSVG('opacity', 1, 0, null, true)
      .promise()
      .done(function() {
        this.remove();
        that.animationTriggered = false;
      });

    // find venns we will update
    let showVenn = [];

    if (this.numberOfCircles > 0) {
      showVenn.push(real ? this.vennReal1 : this.vennNumber1);
    }

    if (this.numberOfCircles > 1) {
      showVenn.push(real ? this.vennReal2 : this.vennNumber2);
    }

    if (this.numberOfCircles > 2) {
      showVenn.push(real ? this.vennReal3 : this.vennNumber3);
    }

    // update venn and draw labels
    for (let i = 0; i < showVenn.length; i++) {
      let venn = showVenn[i];
      this._updateVenn(venn, animated);

      for (let j = 0; j < venn.labels.length; j++) {
        let label = venn.labels[j];
        this._drawLabel(label.text, label.x, label.y, animated);
      }
    }
  }

  // handling of circles
  _createCircle(circleIndex, color, cssClass) {
    let $circle = this.$svg.appendSVG('circle', 'venn-circle')
      .attr('cx', this.centerX)
      .attr('cy', this.centerY)
      .attr('r', 0)
      .on('mouseenter', {
        showReal: false
      }, this._show.bind(this))
      .on('mouseleave', {
        showReal: true
      }, this._show.bind(this));

    if (this.chart.config.options.autoColor) {
      $circle.addClass('auto-color color0');
    } else if (cssClass) {
      $circle.addClass(cssClass);
    } else {
      $circle.attr('fill', color);
    }

    if (this.chart.config.options.clickable) {
      $circle.on('click', this._createClickObject(null, circleIndex), this.chart._onValueClick.bind(this.chart));
    }

    return $circle;
  }

  // handling of venn, label and legend
  _updateVenn(venn, animated) {
    // move circle
    venn.$circle
      .animateSVG('cx', this.centerX + venn.x, animated ? this.animationDuration : 0, null, true)
      .animateSVG('cy', this.centerY + venn.y, animated ? this.animationDuration : 0, null, true)
      .animateSVG('r', venn.r, animated ? this.animationDuration : 0, null, true);

    // set up position legend
    let minR = this.vennCircleHelper.minR,
      x1 = this.centerX + venn.x + venn.legendH * Math.sin(Math.PI / 5) * venn.r,
      y1 = this.centerY + venn.y + venn.legendV * Math.cos(Math.PI / 5) * venn.r,
      x2 = this.centerX + venn.x + venn.legendH * Math.sin(Math.PI / 5) * (venn.legendR + minR * 1.5),
      y2 = this.centerY + venn.y + venn.legendV * Math.cos(Math.PI / 5) * (venn.legendR + minR * 1.5);

    let legendPositions = {
      x1: x1,
      x2: x2,
      y1: y1,
      y2: y2,
      v: venn.legendV,
      h: venn.legendH
    };

    this._renderWireLegend(venn.legend, legendPositions, 'venn-legend');
  }

  _drawLabel(text, dx, dy, animated) {
    // draw label
    let $label = this.$svg.appendSVG('text', 'venn-label')
      .attr('x', this.centerX + dx)
      .attr('y', this.centerY + dy)
      .text(text);

    // animate if needed
    if (animated && this.animationDuration) {
      $label
        .attr('opacity', 0)
        .animateSVG('opacity', 1, this.animationDuration, null, true);
    }
  }

  // handling of show/hide numbers
  _show(event) {
    if (this.numberOfCircles === 1) {
      return; // Nothing to do for only one circle
    }

    // target contains element that is entered, relatedTarget contains element that is left
    let toElement = (event.type === 'mouseenter' ? event.target : event.relatedTarget);

    // check if true enter or just from one circle to another
    let isCircle = toElement && $(toElement).hasClass('venn-circle');
    if (this.wasCircle && isCircle) {
      return false;
    }
    this.wasCircle = isCircle;

    // draw animated in every case
    this._draw(true, event.data.showReal);
  }
}
