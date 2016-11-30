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
scout.CarouselField = function() {
  scout.CarouselField.parent.call(this);
  this.gridDataHints.weightY = 1.0;

  this._addAdapterProperties(['carousel']);
};
scout.inherits(scout.CarouselField, scout.FormField);

scout.CarouselField.prototype._render = function($parent) {
  this.addContainer($parent, 'carousel-field');
  this.addLabel();
  this.addMandatoryIndicator();
  this.addStatus();
  if (this.carousel) {
    this._renderCarousel();
  }
};

scout.CarouselField.prototype._renderCarousel = function() {
  this.carousel.render(this.$container);
  this.addField(this.carousel.$container);
};

scout.CarouselField.prototype._removeCarousel = function() {
  this.carousel.remove();
  this._removeField();
};
