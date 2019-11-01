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
import {CarouselLayout} from '../index';
import {HtmlComponent} from '../index';
import {SingleLayout} from '../index';
import {Widget} from '../index';
import {events} from '../index';
import {GridData} from '../index';
import {arrays} from '../index';

export default class Carousel extends Widget {

constructor() {
  super();
  this._addWidgetProperties(['widgets']);

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
}


_init(model) {
  super._init( model);
  this._setGridData(this.gridData);
  this.widgets = arrays.ensure(this.widgets);
}

_setGridData(gridData) {
  this._setProperty('gridData', new GridData(gridData));
}

_render() {
  // add container
  this.$container = this.$parent.appendDiv('carousel');

  this.htmlComp = HtmlComponent.install(this.$container, this.session);
  this.htmlComp.setLayout(new CarouselLayout(this));

  // add content filmstrip
  this.$carouselFilmstrip = this.$container.appendDiv('carousel-filmstrip');
  this._registerCarouselFilmstripEventListeners();
  this.htmlCompFilmstrip = HtmlComponent.install(this.$carouselFilmstrip, this.session);
}

_remove() {
  this._removeStatus();
  super._remove();
}

_renderProperties() {
  super._renderProperties();

  this._renderWidgets();
  this._renderCurrentItem(); // must be called after renderWidgets
  this._renderStatusEnabled();
}

setStatusEnabled(statusEnabled) {
  this.setProperty('statusEnabled', statusEnabled);
}

_renderStatusEnabled() {
  if (this.statusEnabled) {
    this._renderStatus();
    this._renderStatusItems();
    this._renderCurrentStatusItem();
  } else {
    this._removeStatus();
  }
  this.invalidateLayoutTree();
}

_renderStatus() {
  if (!this.$carouselStatus) {
    this.$carouselStatus = this.$container.appendDiv('carousel-status');
    this.htmlCompStatus = HtmlComponent.install(this.$carouselStatus, this.session);
  }
}

_removeStatus() {
  if (this.$carouselStatus) {
    this.$carouselStatus.remove();
    this.$carouselStatus = null;
  }
}

_renderStatusItems() {
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
}

_renderCurrentStatusItem() {
  if (!this.$carouselStatus) {
    return;
  }
  this.$carouselStatusItems.forEach(function(e, i) {
    e.toggleClass('current-item', i === this.currentItem);
  }.bind(this));
}

recalcTransformation() {
  this.positionX = this.currentItem * this.$container.width() * -1;
  this.$carouselFilmstrip.css({
    transform: 'translateX(' + this.positionX + 'px)'
  });
}

setCurrentItem(currentItem) {
  this.setProperty('currentItem', currentItem);
}

_renderCurrentItem() {
  this._renderItemsInternal(undefined, false);
  this._renderCurrentStatusItem();
  this.invalidateLayoutTree();
}

_renderItemsInternal(item, skipRemove) {
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
}

setWidgets(widgets) {
  this.setProperty('widgets', widgets);
}

_renderWidgets() {
  this.$carouselFilmstrip.empty();
  this.$carouselItems = this.widgets.map(function(widget) {
    var $carouselItem = this.$carouselFilmstrip.appendDiv('carousel-item');
    var htmlComp = HtmlComponent.install($carouselItem, this.session);
    htmlComp.setLayout(new SingleLayout());

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
}

_registerCarouselFilmstripEventListeners() {
  var $window = this.$carouselFilmstrip.window();
  this.$carouselFilmstrip.on('mousedown touchstart', function(event) {
    var origPageX = events.pageX(event);
    var origPosition = this.positionX;
    var minPositionX = this.$container.width() - this.$carouselFilmstrip.width();
    var containerWidth = this.$container.width();
    $window.on('mousemove.carouselDrag touchmove.carouselDrag', function(event) {
      var pageX = events.pageX(event);
      var moveX = pageX - origPageX;
      var positionX = origPosition + moveX;
      if (positionX !== this.positionX && positionX <= 0 && positionX >= minPositionX) {
        this.$carouselFilmstrip.css({
          transform: 'translateX(' + positionX + 'px)'
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
}
}
