/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.Slider = function() {
  scout.Slider.parent.call(this);

  this.value;
  this.minValue;
  this.maxValue;
  this.step;

  this._addEventSupport();
};
scout.inherits(scout.Slider, scout.Widget);

scout.Slider.prototype._init = function(options) {
  scout.Slider.parent.prototype._init.call(this, options);
  this.value = options.value;
  this.minValue = options.minValue;
  this.maxValue = options.maxValue;
  this.step = options.step;
};

scout.Slider.prototype._render = function($parent) {
  this.$container = $parent.appendDiv('slider');
  this.$sliderInput = this.$container.appendElement('<input>', 'slider-input')
    .attr('type', 'range')
    .on('change', this._onValueChange.bind(this));

  this.$sliderValue = this.$container
    .appendSpan('slider-value', this.value);
};

scout.Slider.prototype._renderProperties = function() {
  this._renderValue();
  this._renderMinValue();
  this._renderMaxValue();
  this._renderStep();
};

scout.Slider.prototype._renderValue = function() {
  var value = scout.nvl(this.value, 0);
  this.$sliderInput.val(value);
  this.$sliderValue.text(value);
};

scout.Slider.prototype._renderMinValue = function() {
  if (this.minValue) {
    this.$sliderInput.attr('min', this.minValue);
  } else {
    this.$sliderInput.removeAttr('min');
  }
};

scout.Slider.prototype._renderMaxValue = function() {
  if (this.maxValue) {
    this.$sliderInput.attr('max', this.maxValue);
  } else {
    this.$sliderInput.removeAttr('max');
  }
};

scout.Slider.prototype._renderStep = function() {
  if (this.step) {
    this.$sliderInput.attr('step', this.step);
  } else {
    this.$sliderInput.removeAttr('step');
  }
};

scout.Slider.prototype._onValueChange = function(event) {
  this.setValue(this.$sliderInput.val());
};

scout.Slider.prototype.setValue = function(value) {
  if (value === this.value) {
    return;
  }
  this._setProperty('value', value);
  if (this.rendered) {
    this._renderValue();
  }
};

