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
import {AbstractLayout} from '../index';
import {HtmlComponent} from '../index';
import {Dimension} from '../index';

export default class CarouselLayout extends AbstractLayout {

constructor(carousel) {
  super();

  this.carousel = carousel;
}


layout($container) {
  // recalculate style transformation after layout
  this.carousel.recalcTransformation();

  var filmstripSize = this.carousel.htmlComp.availableSize()
    .subtract(this.carousel.htmlComp.insets())
    .subtract(this.carousel.htmlCompFilmstrip.margins());
  var itemSize = this.carousel.htmlComp.availableSize()
    .subtract(this.carousel.htmlComp.insets())
    .subtract(this.carousel.htmlCompFilmstrip.margins());

  if (this.carousel.statusEnabled && this.carousel.htmlCompStatus) {
    var carouselStatusSize = this.carousel.htmlCompStatus.size().add(this.carousel.htmlCompStatus.margins());

    filmstripSize.height -= carouselStatusSize.height;
    itemSize.height -= carouselStatusSize.height;
  }

  var $carouselItems = this.carousel.$carouselItems;
  filmstripSize.width = $carouselItems.length * filmstripSize.width;
  this.carousel.htmlCompFilmstrip.setSize(filmstripSize);

  $carouselItems.forEach(function(e) {
    var htmlCarouselItem = HtmlComponent.get(e);
    htmlCarouselItem.setSize(itemSize);
  });
}

preferredLayoutSize($container) {
  var currentIndex = this.carousel.currentItem;
  var dim = new Dimension(1, 1);
  if (currentIndex < this.carousel.$carouselItems.length && currentIndex >= 0) {
    dim = HtmlComponent.get(this.carousel.$carouselItems[currentIndex]).prefSize();
  }
  dim.height += this.carousel.htmlCompStatus.size().height;
  return dim;
}
}
