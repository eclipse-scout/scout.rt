// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG
scout.Scrollbar = function($parent, options) {
  var begin = 0,
    that = this;

  var defaults = {
    axis: 'y',
    invertColors: false,
    borderless: false,
    updateScrollbarPos: true,
    mouseWheelNeedsShift: false
  };

  $.extend(this, defaults, options);

  this._$parent = $parent;
  this._beginDefault = 0;
  this._thumbRange;
  this._scrollSize;
  this._offsetSize;
  this._updateThumbPending = false;

  // create scrollbar
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
  $parent.on('DOMMouseScroll mousewheel', '', scrollWheel)
    .on('scroll', this._updateThumbImpl.bind(this));
  this._$scrollbar.on('mousedown', scrollEnd);
  this._$thumb.on('mousedown', '', scrollStart);

  this.updateThumb();

  function scrollWheel(event) {
    var w, d;
    if (event.ctrlKey) {
      return true; // allow ctrl+mousewheel to zoom the page
    }
    if (that.mouseWheelNeedsShift !== event.shiftKey) {
      // only scroll if shift modifier matches
      return true;
    }
    event = event.originalEvent || window.event.originalEvent;
    w = event.wheelDelta ? -event.wheelDelta / 2 : event.detail * 60;
    d = that._scrollSize / that._offsetSize;
    that.scroll(w / d);
    return false;
  }

  function scrollStart(event) {
    begin = (that.axis === 'x' ? event.pageX : event.pageY) - that._$thumb.offset()[that._dir];
    that._$thumb.addClass('scrollbar-thumb-move');
    $(document)
      .on('mousemove', scrollEnd)
      .one('mouseup', scrollExit);
    return false;
  }

  function scrollEnd(event) {
    begin = begin === 0 ? that._beginDefault : begin;
    var end = (that.axis === 'x' ? event.pageX : event.pageY) - that._$thumb.offset()[that._dir];
    that.scroll(end - begin);
  }

  function scrollExit() {
    that._$thumb.removeClass('scrollbar-thumb-move');
    $(document).off('mousemove');
    return false;
  }
};

/**
 * Use this function (from outside) if size of tree content changes
 * @force set to true to immediately update the scrollbar, If set to false, it will be queued in order to prevent unnecessary updates.
 */
scout.Scrollbar.prototype.updateThumb = function(force) {
  if (force) {
    this._updateThumbImpl();
    return;
  }
  // Thumb is (re)initialized, but only after the current thread has finished.
  // Additionally, the call is scheduled at most once. This prevents unnecessary
  // executions of the same code while the UI is updated.
  if (this._updateThumbPending) {
    return;
  }
  setTimeout(function() {
    this._updateThumbImpl();
    this._updateThumbPending = false;
  }.bind(this), 0);
  this._updateThumbPending = true;
};

/**
 * do not use this internal method
 */
scout.Scrollbar.prototype._updateThumbImpl = function() {
  var margin = this._$scrollbar['cssMargin' + this.axis.toUpperCase()](),
    scrollPos = this._$parent[this._scrollDir](),
    scrollLeft = this._$parent.scrollLeft(),
    scrollTop = this._$parent.scrollTop();

  // Reset thumb size and scrollbar position to make sure it does not extend the scrollSize
  this._$thumb.css(this._dim.toLowerCase(), 0);
  if (this.updateScrollbarPos) {
    this._$scrollbar.cssRight(0);
    this._$scrollbar.cssBottom(0);
  }

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

scout.Scrollbar.prototype.scroll = function(posDiff) {
  var posOld = this._$thumb.offset()[this._dir] - this._$scrollbar.offset()[this._dir],
    posNew = Math.min(this._thumbRange, Math.max(0, posOld + posDiff)),
    scrollPos = (this._scrollSize - this._offsetSize) / this._thumbRange * posNew;

  this._$parent[this._scrollDir](scrollPos);

  // Thumb and scrollbar would be updated by the scroll handler. To make it more fluent it is done here as well
  this._$thumb.css(this._dir, posNew);
  if (this.updateScrollbarPos) {
    this._$scrollbar.css(this._dirReverse, -1 * scrollPos);
  }
};
