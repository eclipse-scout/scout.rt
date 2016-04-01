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
scout.SliderField = function() {
  scout.SliderField.parent.call(this);
  this.slider;
};
scout.inherits(scout.SliderField, scout.ValueField);

scout.SliderField.prototype._init = function(model) {
  scout.SliderField.parent.prototype._init.call(this, model);
  var sliderOptions = $.extend({parent: this}, model);
  this.slider = scout.create('Slider', sliderOptions);
};

scout.SliderField.prototype._render = function($parent) {
  this.addContainer($parent, 'slider-field');
  this.addLabel();
  this.addMandatoryIndicator();
  this._renderSlider();
};

scout.SliderField.prototype._renderSlider = function() {
  this.slider.render(this.$container);
  this.addField(this.slider.$container);
};
