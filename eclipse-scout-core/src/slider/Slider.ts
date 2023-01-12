/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Device, HtmlComponent, InitModelOf, objects, scout, SliderEventMap, SliderLayout, SliderModel, Widget} from '../index';

export class Slider extends Widget implements SliderModel {
  declare model: SliderModel;
  declare eventMap: SliderEventMap;
  declare self: Slider;

  value: number;
  minValue: number;
  maxValue: number;
  step: number;
  $sliderInput: JQuery<HTMLInputElement>;
  $sliderValue: JQuery<HTMLSpanElement>;

  constructor() {
    super();

    this.value = null;
    this.minValue = null;
    this.maxValue = null;
    this.step = null;

    this.$sliderInput = null;
    this.$sliderValue = null;
  }

  protected override _init(options: InitModelOf<this>) {
    super._init(options);
    this.value = options.value;
    this.minValue = options.minValue;
    this.maxValue = options.maxValue;
    this.step = options.step;
  }

  protected override _render() {
    this.$container = this.$parent.appendDiv('slider');
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.setLayout(new SliderLayout(this));
    this.$sliderInput = this.$container.appendElement('<input>', 'slider-input')
      .attr('type', 'range')
      .on('change', this._onValueChange.bind(this))
      .addClass(Device.get().cssClassForEdge()) as JQuery<HTMLInputElement>;

    this.$sliderValue = this.$container
      .appendSpan('slider-value', this.value + '');
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderValue();
    this._renderMinValue();
    this._renderMaxValue();
    this._renderStep();
  }

  protected override _remove() {
    super._remove();
    this.$sliderInput = null;
    this.$sliderValue = null;
  }

  protected _renderValue() {
    let value = scout.nvl(this.value, 0);
    this.$sliderInput.val(value);
    this.$sliderValue.text(value);
  }

  protected _renderMinValue() {
    if (this.minValue) {
      this.$sliderInput.attr('min', this.minValue);
    } else {
      this.$sliderInput.removeAttr('min');
    }
  }

  protected _renderMaxValue() {
    if (this.maxValue) {
      this.$sliderInput.attr('max', this.maxValue);
    } else {
      this.$sliderInput.removeAttr('max');
    }
  }

  protected _renderStep() {
    if (this.step) {
      this.$sliderInput.attr('step', this.step);
    } else {
      this.$sliderInput.removeAttr('step');
    }
  }

  protected _onValueChange(event: JQuery.ChangeEvent) {
    let n = Number(this.$sliderInput.val());
    // Ensure valid number
    if (!objects.isNumber(n)) {
      n = scout.nvl(this.maxValue, this.minValue, 0);
    }
    this.setValue(n);
  }

  setValue(value: number) {
    this.setProperty('value', value);
  }
}
