/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {HtmlComponent, objects, scout, SliderLayout, Widget} from '../index';
import Device from '../util/Device';

export default class Slider extends Widget {

  constructor() {
    super();

    this.value = null;
    this.minValue = null;
    this.maxValue = null;
    this.step = null;

    this.$sliderInput = null;
    this.$sliderValue = null;
  }

  _init(options) {
    super._init(options);
    this.value = options.value;
    this.minValue = options.minValue;
    this.maxValue = options.maxValue;
    this.step = options.step;
  }

  _render() {
    this.$container = this.$parent.appendDiv('slider');
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.setLayout(new SliderLayout(this));
    this.$sliderInput = this.$container.appendElement('<input>', 'slider-input')
      .attr('type', 'range')
      .on('change', this._onValueChange.bind(this))
      .addClass(Device.get().cssClassForEdge());

    this.$sliderValue = this.$container
      .appendSpan('slider-value', this.value);
  }

  _renderProperties() {
    super._renderProperties();
    this._renderValue();
    this._renderMinValue();
    this._renderMaxValue();
    this._renderStep();
  }

  _remove() {
    super._remove();
    this.$sliderInput = null;
    this.$sliderValue = null;
  }

  _renderValue() {
    let value = scout.nvl(this.value, 0);
    this.$sliderInput.val(value);
    this.$sliderValue.text(value);
  }

  _renderMinValue() {
    if (this.minValue) {
      this.$sliderInput.attr('min', this.minValue);
    } else {
      this.$sliderInput.removeAttr('min');
    }
  }

  _renderMaxValue() {
    if (this.maxValue) {
      this.$sliderInput.attr('max', this.maxValue);
    } else {
      this.$sliderInput.removeAttr('max');
    }
  }

  _renderStep() {
    if (this.step) {
      this.$sliderInput.attr('step', this.step);
    } else {
      this.$sliderInput.removeAttr('step');
    }
  }

  _onValueChange(event) {
    let n = Number(this.$sliderInput.val());
    // Ensure valid number
    if (!objects.isNumber(n)) {
      n = scout.nvl(this.maxValue, this.minValue, 0);
    }
    this.setValue(n);
  }

  setValue(value) {
    this.setProperty('value', value);
  }
}
