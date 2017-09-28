/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.SliderLayout = function(slider) {
  scout.SliderLayout.parent.call(this);
  this.slider = slider;
};
scout.inherits(scout.SliderLayout, scout.AbstractLayout);

scout.SliderLayout.prototype.layout = function($container) {
  var size = scout.graphics.size($container);
  this.slider.$sliderInput.css('height', size.height);
  this.slider.$sliderValue.css('height', size.height);
};
