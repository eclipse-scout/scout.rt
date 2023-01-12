/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {InitModelOf, scout, Slider, SliderFieldModel, ValueField} from '../../../index';
import $ from 'jquery';

export class SliderField extends ValueField<number> {
  declare model: SliderFieldModel;

  slider: Slider;

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    let sliderOptions: InitModelOf<Slider> = $.extend({
      parent: this
    }, model);
    this.slider = scout.create(Slider, sliderOptions);
  }

  protected override _render() {
    this.addContainer(this.$parent, 'slider-field');
    this.addLabel();
    this.addMandatoryIndicator();
    this._renderSlider();
  }

  protected _renderSlider() {
    this.slider.render();
    this.addField(this.slider.$container);
  }

  protected override _readDisplayText(): string {
    // Use the inner slider's value as display text, as the user cannot enter the value manually.
    // This value is already guaranteed to be a valid number (see Slider.js, _onValueChange). We
    // convert it to a string to match the expected data type for a display text.
    return String(this.slider.value);
  }

  protected override _parseValue(displayText: string): number {
    // Convert display text back to number
    return Number(displayText);
  }

  override setValue(value: number) {
    this.slider.setValue(value);
  }

  getValue(): number {
    return this.slider.value;
  }
}
