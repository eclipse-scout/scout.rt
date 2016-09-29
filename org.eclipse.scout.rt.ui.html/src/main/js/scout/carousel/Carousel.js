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

scout.Carousel = function() {
  scout.Carousel.parent.call(this);
  this._addAdapterProperties(['widgets']);

  // default values
  this.currentItem = 0; // current item
  this.moveThreshold = 0.25; // threshold
  this.widgets = []; // widgets

  this.$carouselFilmstrip; // carousel filmstrip
  this.$carouselItems = []; // carousel items
  this.positionX = 0; // last translation position
};
scout.inherits(scout.Carousel, scout.Widget);

scout.Carousel.prototype._init = function(model) {
  scout.Carousel.parent.prototype._init.call(this, model);
  this._syncGridData(this.gridData);
};

scout.Carousel.prototype._syncGridData = function(gridData) {
  this._setProperty('gridData', new scout.GridData(gridData));
};

scout.Carousel.prototype._render = function($parent) {
  // add container
  this.$container = $parent.appendDiv('carousel');

  this.htmlComp = scout.HtmlComponent.install(this.$container, this.session);
  this.htmlComp.setLayout(new scout.CarouselLayout(this));

  // add content filmstrip
  this.$carouselFilmstrip = this.$container.appendDiv('carousel-filmstrip');
  this._registerCarouselFilmstripEventListeners();
  this.htmlCompFilmstrip = scout.HtmlComponent.install(this.$carouselFilmstrip, this.session);
};

scout.Carousel.prototype._renderProperties = function() {
  scout.Carousel.parent.prototype._renderProperties.call(this);

  this._renderWidgets();
  this._renderCurrentItem(); // must be called after renderWidgets
};

scout.Carousel.prototype.recalcTransformation = function() {
  this.positionX = this.currentItem * this.$container.width() * -1;
  this.$carouselFilmstrip.css({
    "transform": " translateX(" + this.positionX + "px)"
  });
};

scout.Carousel.prototype._renderCurrentItem = function() {
  this._renderItemsInternal(undefined, false);
};

scout.Carousel.prototype._renderItemsInternal = function(item, skipRemove) {
  item = item || this.currentItem;
  if (!skipRemove) {
    this.widgets.forEach(function(w, j) {
      if (w.rendered && (j < item - 1 || j > item + 1)) {
        w.remove();
      }
    }, this);
  }
  for (var i = Math.max(item - 1, 0); i < Math.min(item + 2, this.widgets.length); i++) {
    if (!this.widgets[i].rendered) {
      this.widgets[i].render(this.$carouselItems[i]);
      this.widgets[i].htmlComp.revalidateLayout();
    }
  }
};

scout.Carousel.prototype._registerCarouselFilmstripEventListeners = function() {
  var $window = this.$carouselFilmstrip.window();
  this.$carouselFilmstrip.on('mousedown', function(event) {
    var origEvent = event;
    var origPosition = this.positionX;
    var minPositionX = this.$container.width() - this.$carouselFilmstrip.width();
    var containerWidth = this.$container.width();
    $window.on('mousemove.carouselDrag', function(event) {
      var moveX = event.pageX - origEvent.pageX;
      var positionX = origPosition + moveX;
      if (positionX !== this.positionX && positionX <= 0 && positionX >= minPositionX) {
        this.$carouselFilmstrip.css({
          "transform": " translateX(" + positionX + "px)"
        });
        this.positionX = positionX;
        // item
        var i = positionX / containerWidth * -1;
        this._renderItemsInternal(positionX < origPosition ? Math.floor(i) : Math.ceil(i), true);
      }
    }.bind(this));
    $window.on('mouseup.carouselDrag', function(e) {
      $window.off('.carouselDrag');
      // show only whole items
      var mod = this.positionX % containerWidth;
      var newCurrentItem = this.currentItem;
      if (this.positionX < origPosition && mod / containerWidth < (0 - this.moveThreshold)) { // next
        newCurrentItem = Math.ceil(this.positionX / containerWidth * -1);
      } else if (this.positionX > origPosition && mod / containerWidth >= this.moveThreshold - 1) { // prev
        newCurrentItem = Math.floor(this.positionX / containerWidth * -1);
      }
      this.setCurrentItem(newCurrentItem);
      if (mod !== 0) {
        this.positionX = newCurrentItem * containerWidth * -1;
        this.recalcTransformation();
      }
    }.bind(this));
  }.bind(this));
};

scout.Carousel.prototype._renderWidgets = function() {
  this.$carouselFilmstrip.empty();
  this.$carouselItems = [];

  this.widgets = scout.arrays.ensure(this.widgets);
  this.widgets.forEach(function(widget, i) {
    this.$carouselItems.push(this.$carouselFilmstrip.appendDiv('carousel-item'));
    scout.HtmlComponent.install(this.$carouselItems[i], this.session);
  }, this);
  this.setCurrentItem(0);
};

scout.Carousel.prototype.setCurrentItem = function(currentItem) {
  this.setProperty('currentItem', currentItem);
};

scout.Carousel.prototype.setWidgets = function(widgets) {
  this.setProperty('widgets', widgets);
};
