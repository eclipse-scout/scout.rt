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
import {scout, Slider, SliderFieldModel, SliderModel, ValueField} from '../../../index';
import $ from 'jquery';

export default class SliderField extends ValueField<number> {
  declare model: SliderFieldModel;

  slider: Slider;

  protected override _init(model: SliderFieldModel) {
    super._init(model);
    let sliderOptions: SliderModel = $.extend({
      parent: this
    }, model);
    this.slider = scout.create(Slider, sliderOptions);
  }

  protected _render() {
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
