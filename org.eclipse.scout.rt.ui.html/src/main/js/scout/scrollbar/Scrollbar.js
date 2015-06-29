// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG
scout.Scrollbar = function(options) {
  scout.Scrollbar.parent.call(this);
  var defaults = {
    axis: 'y',
    invertColors: false,
    borderless: false,
    updateScrollbarPos: true,
    mouseWheelNeedsShift: false
  };

  $.extend(this, defaults, options);

  this._beginDefault = 0;
  this._thumbRange;
  this._scrollSize;
  this._offsetSize;
  this._addEventSupport();
};
scout.inherits(scout.Scrollbar, scout.Widget);

scout.Scrollbar.prototype._render = function($parent) {
  var begin = 0;

  this._$parent = $parent;
  this._$scrollbar = $parent.appendDiv('scrollbar')
    .addClass(this.axis + '-axis');
  this._$thumb = this._$scrollbar.appendDiv('scrollbar-thumb')
    .addClass(this.axis + '-axis');
  if (this.invertColors) {
    this._$thumb.addClass('inverted');
  }
  if (this.borderless) {
    this._$scrollbar.addClass('borderless');
  }

  this._dim = this.axis === 'x' ? 'Width' : 'Height';
  this._dir = this.axis === 'x' ? 'left' : 'top';
  this._dirReverse = this.axis === 'x' ? 'right' : 'bottom';
  this._scrollDir = this.axis === 'x' ? 'scrollLeft' : 'scrollTop';

  // event handling
  $parent.on('DOMMouseScroll mousewheel', '', scrollWheel.bind(this))
    .on('scroll', this.update.bind(this));
  $parent.data('scrollbars').forEach(function(scrollbar) {
    scrollbar.on('scrollstart', this._fixScrollbar.bind(this));
    scrollbar.on('scrollend', this._unfixScrollbar.bind(this));
  }, this);
  this._$scrollbar.on('mousedown', onScrollbarMousedown.bind(this));
  this._$thumb.on('mousedown', '', onThumbMousedown.bind(this));

  function scrollWheel(event) {
    var w, d;
    if (event.ctrlKey) {
      return true; // allow ctrl + mousewheel to zoom the page
    }
    if (this.mouseWheelNeedsShift !== event.shiftKey) {
      // only scroll if shift modifier matches
      return true;
    }
    event = event.originalEvent || window.event.originalEvent;
    w = event.wheelDelta ? -event.wheelDelta / 2 : event.detail * 20;
    d = this._scrollSize / this._offsetSize;

    this.trigger('scrollstart');
    this.scroll(w / d);
    this.trigger('scrollend');
    return false;
  }

  function onScrollbarMousedown(event) {
    this.trigger('scrollstart');
    scrollTo.call(this, event);
    this.trigger('scrollend');
  }

  function onThumbMousedown(event) {
    this.trigger('scrollstart');
    begin = (this.axis === 'x' ? event.pageX : event.pageY) - this._$thumb.offset()[this._dir];
    this._$thumb.addClass('scrollbar-thumb-move');
    this._mousemoveHandler = scrollTo.bind(this);
    $(document)
      .on('mousemove', this._mousemoveHandler)
      .one('mouseup', onThumbMouseup.bind(this));
    return false;
  }

  function onThumbMouseup() {
    this._$thumb.removeClass('scrollbar-thumb-move');
    $(document).off('mousemove', this._mousemoveHandler);
    this.trigger('scrollend');
    return false;
  }

  function scrollTo(event) {
    begin = begin === 0 ? this._beginDefault : begin;
    var end = (this.axis === 'x' ? event.pageX : event.pageY) - this._$thumb.offset()[this._dir];
    this.scroll(end - begin);
  }
};

/**
 * do not use this internal method
 */
scout.Scrollbar.prototype.update = function() {
  var margin = this._$scrollbar['cssMargin' + this.axis.toUpperCase()](),
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
    this._$scrollbar.css('visibility', 'hidden');
  } else {
    this._$scrollbar.css('visibility', 'visible');
  }

  // Position the scrollbar(s)
  if (this.updateScrollbarPos) {
    // Always update both to make sure every scrollbar (x and y) is positioned correctly
    this._$scrollbar.cssRight(-1 * scrollLeft);
    this._$scrollbar.cssBottom(-1 * scrollTop);
  }
};

/*
 * Resets thumb size and scrollbar position to make sure it does not extend the scrollSize
 */
scout.Scrollbar.prototype.reset = function() {
  this._$thumb.css(this._dim.toLowerCase(), 0);
  if (this.updateScrollbarPos) {
    this._$scrollbar.cssRight(0);
    this._$scrollbar.cssBottom(0);
  }
};

scout.Scrollbar.prototype.scroll = function(posDiff) {
  var scrollbarOffset = this._$scrollbar.offset(),
    thumbOffset = this._$thumb.offset(),
    posOld = thumbOffset[this._dir] - scrollbarOffset[this._dir],
    posNew = Math.min(this._thumbRange, Math.max(0, posOld + posDiff)),
    scrollPos = (this._scrollSize - this._offsetSize) / this._thumbRange * posNew;

  this._$parent[this._scrollDir](scrollPos);

  // Thumb and scrollbar would be updated by the scroll handler. To make it more fluent it is done here as well
  this._$thumb.css(this._dir, posNew);
  if (this.updateScrollbarPos) {
    this._$scrollbar.css(this._dirReverse, -1 * scrollPos);
  }
};

/**
 * Sets the position to fixed and updates left and top position.
 * This is necessary to prevent flickering in IE.
 */
scout.Scrollbar.prototype._fixScrollbar = function() {
  scout.scrollbars.fix(this._$scrollbar);
};

/**
 * Reverts the changes made by fixScrollbar.
 */
scout.Scrollbar.prototype._unfixScrollbar = function() {
  this._unfixTimeoutId = scout.scrollbars.unfix(this._$scrollbar, this._unfixTimeoutId);
};

scout.Scrollbar.prototype.notifyBeforeScroll = function() {
  this.trigger('scrollstart');
};

scout.Scrollbar.prototype.notifyAfterScroll = function() {
  this.trigger('scrollend');
};
