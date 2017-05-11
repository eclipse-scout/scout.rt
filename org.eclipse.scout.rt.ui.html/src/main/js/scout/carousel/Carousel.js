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
  this.statusEnabled = true;
  this.statusItemHtml = '&bull;';
  this.currentItem = 0; // current item
  this.moveThreshold = 0.25; // threshold
  this.widgets = []; // widgets

  this.$carouselFilmstrip; // carousel filmstrip
  this.$carouselItems = []; // carousel items
  this.$carouselStatus; // carousel status bar (containing current position)
  this.$carouselStatusItems = []; // carousel status items

  this.positionX = 0; // last translation position
};
scout.inherits(scout.Carousel, scout.Widget);

scout.Carousel.prototype._init = function(model) {
  scout.Carousel.parent.prototype._init.call(this, model);
  this._setGridData(this.gridData);
  this.widgets = scout.arrays.ensure(this.widgets);
};

scout.Carousel.prototype._setGridData = function(gridData) {
  this._setProperty('gridData', new scout.GridData(gridData));
};

scout.Carousel.prototype._render = function() {
  // add container
  this.$container = this.$parent.appendDiv('carousel');

  this.htmlComp = scout.HtmlComponent.install(this.$container, this.session);
  this.htmlComp.setLayout(new scout.CarouselLayout(this));

  // add content filmstrip
  this.$carouselFilmstrip = this.$container.appendDiv('carousel-filmstrip');
  this._registerCarouselFilmstripEventListeners();
  this.htmlCompFilmstrip = scout.HtmlComponent.install(this.$carouselFilmstrip, this.session);
};

scout.Carousel.prototype._remove = function() {
  this._removeStatus();
  scout.Carousel.parent.prototype._remove.call(this);
};

scout.Carousel.prototype._renderProperties = function() {
  scout.Carousel.parent.prototype._renderProperties.call(this);

  this._renderWidgets();
  this._renderCurrentItem(); // must be called after renderWidgets
  this._renderStatusEnabled();
};

scout.Carousel.prototype.setStatusEnabled = function(statusEnabled) {
  this.setProperty('statusEnabled', statusEnabled);
};

scout.Carousel.prototype._renderStatusEnabled = function() {
  if (this.statusEnabled) {
    this._renderStatus();
    this._renderStatusItems();
    this._renderCurrentStatusItem();
  } else {
    this._removeStatus();
  }
  this.invalidateLayoutTree();
};

scout.Carousel.prototype._renderStatus = function() {
  if (!this.$carouselStatus) {
    this.$carouselStatus = this.$container.appendDiv('carousel-status');
    this.htmlCompStatus = scout.HtmlComponent.install(this.$carouselStatus, this.session);
  }
};

scout.Carousel.prototype._removeStatus = function() {
  if (this.$carouselStatus) {
    this.$carouselStatus.remove();
    this.$carouselStatus = null;
  }
};

scout.Carousel.prototype._renderStatusItems = function() {
  if (!this.$carouselStatus) {
    return;
  }
  this.$carouselStatusItems = [];
  this.$carouselStatus.empty();
  this.$carouselItems.forEach(function() {
    var $statusItem = this.$carouselStatus.appendDiv('status-item');
    $statusItem.html(this.statusItemHtml);
    this.$carouselStatusItems.push($statusItem);
  }.bind(this));
};

scout.Carousel.prototype._renderCurrentStatusItem = function() {
  if (!this.$carouselStatus) {
    return;
  }
  this.$carouselStatusItems.forEach(function(e, i) {
    e.toggleClass('current-item', i === this.currentItem);
  }.bind(this));
};

scout.Carousel.prototype.recalcTransformation = function() {
  this.positionX = this.currentItem * this.$container.width() * -1;
  this.$carouselFilmstrip.css({
    "transform": " translateX(" + this.positionX + "px)"
  });
};

scout.Carousel.prototype.setCurrentItem = function(currentItem) {
  this.setProperty('currentItem', currentItem);
};

scout.Carousel.prototype._renderCurrentItem = function() {
  this._renderItemsInternal(undefined, false);
  this._renderCurrentStatusItem();
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
    var widget = this.widgets[i];
    if (!widget.rendered) {
      widget.render(this.$carouselItems[i]);
      widget.htmlComp.revalidateLayout();
    }
  }
};

scout.Carousel.prototype.setWidgets = function(widgets) {
  this.setProperty('widgets', widgets);
};

scout.Carousel.prototype._renderWidgets = function() {
  this.$carouselFilmstrip.empty();
  this.$carouselItems = this.widgets.map(function(widget) {
    var $carouselItem = this.$carouselFilmstrip.appendDiv('carousel-item');
    var htmlComp = scout.HtmlComponent.install($carouselItem, this.session);
    htmlComp.setLayout(new scout.SingleLayout());

    // Add the CSS classes of the widget to be able to style the carousel items.
    // Use a suffix to prevent conflicts
    var cssClasses = widget.cssClassAsArray();
    cssClasses.forEach(function(cssClass) {
      $carouselItem.addClass(cssClass + '-carousel-item');
    });

    return $carouselItem;
  }, this);

  this._renderStatusItems();

  // reset current item
  this.setCurrentItem(0);
};

scout.Carousel.prototype._registerCarouselFilmstripEventListeners = function() {
  var $window = this.$carouselFilmstrip.window();
  this.$carouselFilmstrip.on('mousedown touchstart', function(event) {
    var origPageX = scout.events.pageX(event);
    var origPosition = this.positionX;
    var minPositionX = this.$container.width() - this.$carouselFilmstrip.width();
    var containerWidth = this.$container.width();
    $window.on('mousemove.carouselDrag touchmove.carouselDrag', function(event) {
      var pageX = scout.events.pageX(event);
      var moveX = pageX - origPageX;
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
    $window.on('mouseup.carouselDrag touchend.carouselDrag touchcancel.carouselDrag', function(e) {
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
