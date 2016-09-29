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

scout.CarouselLayout = function(carousel) {
  scout.CarouselLayout.parent.call(this);

  this.carousel = carousel;
};
scout.inherits(scout.CarouselLayout, scout.AbstractLayout);

scout.CarouselLayout.prototype.layout = function($container) {
  // recalculate style transformation after layout
  this.carousel.recalcTransformation();

  var filmstripSize = this.carousel.htmlComp.getAvailableSize()
    .subtract(this.carousel.htmlComp.getInsets())
    .subtract(this.carousel.htmlCompFilmstrip.getMargins());
  var itemSize = this.carousel.htmlComp.getAvailableSize()
    .subtract(this.carousel.htmlComp.getInsets())
    .subtract(this.carousel.htmlCompFilmstrip.getMargins());

  var $carouselItems = this.carousel.$carouselItems;
  filmstripSize.width = $carouselItems.length * filmstripSize.width;
  this.carousel.htmlCompFilmstrip.setSize(filmstripSize);

  $carouselItems.forEach(function(e) {
    var htmlCarouselItem = scout.HtmlComponent.get(e);
    htmlCarouselItem.setSize(itemSize);
  });
};

scout.CarouselLayout.prototype.preferredLayoutSize = function($container) {
  if (this.carousel.currentIndex < this.carousel.$carouselItems.length && this.carousel.currentIndex >= 0) {
    return scout.HtmlComponent.get(this.carousel.$carouselItems[this.carousel.currentIndex]).getPreferredSize();
  }
  return new scout.Dimension(1, 1);
};
