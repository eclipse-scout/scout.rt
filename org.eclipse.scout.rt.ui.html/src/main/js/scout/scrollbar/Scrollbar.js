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
scout.Scrollbar = function() {
  scout.Scrollbar.parent.call(this);

  this._beginDefault = 0;
  this._thumbRange;
  this._scrollSize;
  this._offsetSize;
  this._addEventSupport();

  this._onScrollWheelHandler = this._onScrollWheel.bind(this);
  this._onScrollHandler = this._onScroll.bind(this);
  this._onScrollbarMousedownHandler = this._onScrollbarMousedown.bind(this);
  this._onThumbMousedownHandler = this._onThumbMousedown.bind(this);
  this._onDocumentMousemoveHandler = this._onDocumentMousemove.bind(this);
  this._onDocumentMouseupHandler = this._onDocumentMouseup.bind(this);
  this._fixScrollbarHandler = this._fixScrollbar.bind(this);
  this._unfixScrollbarHandler = this._unfixScrollbar.bind(this);
};
scout.inherits(scout.Scrollbar, scout.Widget);

scout.Scrollbar.prototype._init = function(options) {
  scout.Scrollbar.parent.prototype._init.call(this, options);
  var defaults = {
    axis: 'y',
    borderless: false,
    mouseWheelNeedsShift: false
  };

  $.extend(this, defaults, options);
};

scout.Scrollbar.prototype._render = function($parent) {
  this._begin = 0;
  this.$container = $parent.appendDiv('scrollbar')
    .addClass(this.axis + '-axis');
  this._$thumb = this.$container.appendDiv('scrollbar-thumb')
    .addClass(this.axis + '-axis');
  if (this.borderless) {
    this.$container.addClass('borderless');
  }

  this._dim = this.axis === 'x' ? 'Width' : 'Height';
  this._dir = this.axis === 'x' ? 'left' : 'top';
  this._dirReverse = this.axis === 'x' ? 'right' : 'bottom';
  this._scrollDir = this.axis === 'x' ? 'scrollLeft' : 'scrollTop';

  // Install listeners
  $parent
    .on('DOMMouseScroll mousewheel', this._onScrollWheelHandler)
    .on('scroll', this._onScrollHandler)
    .data('scrollbars').forEach(function(scrollbar) {
      scrollbar.on('scrollstart', this._fixScrollbarHandler);
      scrollbar.on('scrollend', this._unfixScrollbarHandler);
    }.bind(this));
  this.$container.on('mousedown', this._onScrollbarMousedownHandler);
  this._$thumb.on('mousedown', this._onThumbMousedownHandler);
};

scout.Scrollbar.prototype._onScrollWheel = function(event) {
  var w, d;
  if (!this.$container.isVisible()) {
    return true; // ignore scroll wheel event if there is no scroll bar visible
  }
  if (event.ctrlKey) {
    return true; // allow ctrl + mousewheel to zoom the page
  }
  if (this.mouseWheelNeedsShift !== event.shiftKey) {
    // only scroll if shift modifier matches
    return true;
  }
  event = event.originalEvent || this.$container.window(true).event.originalEvent;
  w = event.wheelDelta ? -event.wheelDelta / 2 : event.detail * 20;
  d = this._scrollSize / this._offsetSize;

  this.trigger('scrollstart');
  this.scroll(w / d);
  this.trigger('scrollend');
  return false;
};

scout.Scrollbar.prototype._onScroll = function(event) {
  this.update();
};

scout.Scrollbar.prototype._onScrollbarMousedown = function(event) {
  this.trigger('scrollstart');
  this._scrollTo(event);
  this.trigger('scrollend');
};

scout.Scrollbar.prototype._onThumbMousedown = function(event) {
  this.trigger('scrollstart');
  this._begin = (this.axis === 'x' ? event.pageX : event.pageY) - this._$thumb.offset()[this._dir];
  this._$thumb.addClass('scrollbar-thumb-move');
  this._$thumb.document()
    .on('mousemove', this._onDocumentMousemoveHandler)
    .one('mouseup', this._onDocumentMouseupHandler);
  return false;
};

scout.Scrollbar.prototype._onDocumentMousemove = function(event) {
  if (!this.rendered) {
    // Scrollbar may be removed in the meantime
    return;
  }
  this._scrollTo(event);
};

scout.Scrollbar.prototype._onDocumentMouseup = function(event) {
  var $document = $(event.currentTarget);
  $document.off('mousemove', this._onDocumentMousemoveHandler);
  if (this.rendered) {
    this._$thumb.removeClass('scrollbar-thumb-move');
  }
  this.trigger('scrollend');
  return false;
};

scout.Scrollbar.prototype._scrollTo = function(event) {
  var begin = this._begin === 0 ? this._beginDefault : this._begin;
  var end = (this.axis === 'x' ? event.pageX : event.pageY) - this._$thumb.offset()[this._dir];
  this.scroll(end - begin);
};

scout.Scrollbar.prototype._remove = function() {
  // Uninstall listeners
  this._$parent
    .off('DOMMouseScroll mousewheel', this._onScrollWheelHandler)
    .off('scroll', this._onScrollHandler)
    .data('scrollbars').forEach(function(scrollbar) {
      scrollbar.off('scrollstart', this._fixScrollbarHandler);
      scrollbar.off('scrollend', this._unfixScrollbarHandler);
    }.bind(this));
  this.$container.off('mousedown', this._onScrollbarMousedownHandler);
  this._$thumb.off('mousedown', '', this._onThumbMousedownHandler);

  scout.Scrollbar.parent.prototype._remove.call(this);
};

/**
 * do not use this internal method
 */
scout.Scrollbar.prototype.update = function() {
  var margin = this.$container['cssMargin' + this.axis.toUpperCase()](),
    scrollPos = this._$parent[this._scrollDir](),
    scrollLeft = this._$parent.scrollLeft(),
    scrollTop = this._$parent.scrollTop();

  this.reset();
  this._offsetSize = this._$parent[0]['offset' + this._dim];
  this._scrollSize = this._$parent[0]['scroll' + this._dim];

  // calc size and range of thumb
  var thumbSize = Math.max(this._offsetSize * this._offsetSize / this._scrollSize - margin, 30);
  this._thumbRange = this._offsetSize - thumbSize - margin;

  // set size of thumb
  this._$thumb.css(this._dim.toLowerCase(), thumbSize);
  this._beginDefault = thumbSize / 2;

  // set location of thumb
  var posNew = scrollPos / (this._scrollSize - this._offsetSize) * this._thumbRange;
  this._$thumb.css(this._dir, posNew);

  // show scrollbar
  if (this._offsetSize >= this._scrollSize) {
    this.$container.css('display', 'none');
  } else {
    this.$container.css('display', '');
  }

  // Position the scrollbar(s)
  // Always update both to make sure every scrollbar (x and y) is positioned correctly
  this.$container.cssRight(-1 * scrollLeft);
  this.$container.cssBottom(-1 * scrollTop);
};

/*
 * Resets thumb size and scrollbar position to make sure it does not extend the scrollSize
 */
scout.Scrollbar.prototype.reset = function() {
  this._$thumb.css(this._dim.toLowerCase(), 0);
  this.$container.cssRight(0);
  this.$container.cssBottom(0);
};

scout.Scrollbar.prototype.scroll = function(posDiff) {
  var scrollbarOffset = this.$container.offset(),
    thumbOffset = this._$thumb.offset(),
    posOld = thumbOffset[this._dir] - scrollbarOffset[this._dir],
    posNew = Math.min(this._thumbRange, Math.max(0, posOld + posDiff)),
    scrollPos = (this._scrollSize - this._offsetSize) / this._thumbRange * posNew;

  this._$parent[this._scrollDir](scrollPos);

  // Thumb and scrollbar would be updated by the scroll handler. To make it more fluent it is done here as well
  this._$thumb.css(this._dir, posNew);
  this.$container.css(this._dirReverse, -1 * scrollPos);
};

/**
 * Sets the position to fixed and updates left and top position.
 * This is necessary to prevent flickering in IE.
 */
scout.Scrollbar.prototype._fixScrollbar = function() {
  scout.scrollbars.fix(this.$container);
};

/**
 * Reverts the changes made by fixScrollbar.
 */
scout.Scrollbar.prototype._unfixScrollbar = function() {
  this._unfixTimeoutId = scout.scrollbars.unfix(this.$container, this._unfixTimeoutId);
};

scout.Scrollbar.prototype.notifyBeforeScroll = function() {
  this.trigger('scrollstart');
};

scout.Scrollbar.prototype.notifyAfterScroll = function() {
  this.trigger('scrollend');
};
