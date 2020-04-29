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
import {scout, ValueField} from '../../../index';
import $ from 'jquery';

export default class SliderField extends ValueField {

  constructor() {
    super();
    this.slider;
  }

  _init(model) {
    super._init(model);
    let sliderOptions = $.extend({
      parent: this
    }, model);
    this.slider = scout.create('Slider', sliderOptions);
  }

  _render() {
    this.addContainer(this.$parent, 'slider-field');
    this.addLabel();
    this.addMandatoryIndicator();
    this._renderSlider();
  }

  _renderSlider() {
    this.slider.render();
    this.addField(this.slider.$container);
  }

  _readDisplayText() {
    // Use the inner slider's value as display text, as the user cannot enter the value manually.
    // This value is already guaranteed to be a valid number (see Slider.js, _onValueChange). We
    // convert it to a string to match the expected data type for a display text.
    return String(this.slider.value);
  }

  _parseValue(displayText) {
    // Convert display text back to number
    return Number(displayText);
  }

  setValue(value) {
    this.slider.setValue(value);
  }

  getValue() {
    return this.slider.value;
  }
}
