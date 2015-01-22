// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG
scout.Scrollbar = function($parent, options) {
  var begin = 0,
    that = this;

  this.defaultOptions = {
    axis: 'y',
    invertColors: false,
    borderless: false
  };

  this.options = $.extend({}, this.defaultOptions, options);
  this._$parent = $parent;
  this._beginDefault = 0;
  this._thumbRange;
  this._scrollSize;
  this._offsetSize;
  this._updateThumbPending = false;

  // create scrollbar
  this._$scrollbar = $parent.beforeDiv('scrollbar');
  this._$thumb = this._$scrollbar.appendDiv('scrollbar-thumb');
  if (this.options.invertColors) {
    this._$thumb.addClass('inverted');
  }
  if (this.options.borderless) {
    this._$scrollbar.addClass('borderless');
  }

  this._dim = this.options.axis === 'x' ? 'Width' : 'Height';
  this._dir = this.options.axis === 'x' ? 'left' : 'top';
  this._scrollDir = this.options.axis === 'x' ? 'scrollLeft' : 'scrollTop';

  // event handling
  $parent.parent().on('DOMMouseScroll mousewheel', '', scrollWheel);
  this._$scrollbar.on('mousedown', scrollEnd);
  this._$thumb.on('mousedown', '', scrollStart);

  function scrollWheel(event) {
    var w, d;
    if (event.ctrlKey) {
      return true; // allow ctrl+mousewheel to zoom the page
    }
    event = event.originalEvent || window.event.originalEvent;
    w = event.wheelDelta ? -event.wheelDelta / 2 : event.detail * 60;
    d = that._scrollSize / that._offsetSize;
    that._setThumb(w / d);
    return false;
  }

  function scrollStart(event) {
    begin = (that.options.axis === 'x' ? event.pageX : event.pageY) - that._$thumb.offset()[that._dir];
    that._$thumb.addClass('scrollbar-thumb-move');
    $(document).
      on('mousemove', scrollEnd).
      one('mouseup', scrollExit);
    return false;
  }

  function scrollEnd(event) {
    begin = begin === 0 ? that._beginDefault : begin;
    var end = (that.options.axis === 'x' ? event.pageX : event.pageY) - that._$thumb.offset()[that._dir];
    that._setThumb(end - begin);
  }

  function scrollExit() {
    that._$thumb.removeClass('scrollbar-thumb-move');
    $(document).off('mousemove');
    return false;
  }
};

/**
 * Use this function (from outside) if size of tree content changes
 */
scout.Scrollbar.prototype.updateThumb = function() {
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
  this._offsetSize = this._$parent[0]['offset' + this._dim];
  this._scrollSize = this._$parent[0]['scroll' + this._dim];

  var margin = parseFloat(this._$scrollbar.css('margin-top')),
    scrollPos = this._$parent.parent()[this._scrollDir]();

  // when needed: move container to right position
  if (this._offsetSize + scrollPos >= this._scrollSize) {
    scrollPos = Math.max(0, this._scrollSize - this._offsetSize);
    this._$parent.parent()[this._scrollDir](scrollPos);
  }

  // calc size and range of thumb
  var thumbSize = Math.max(this._offsetSize * this._offsetSize / this._scrollSize - margin * 2, 30);
  this._thumbRange = this._offsetSize - thumbSize - margin * 2;

  // set size of thumb
  this._$thumb.css(this._dim.toLowerCase(), thumbSize);
  this._beginDefault = thumbSize / 2;

  // set location of thumb
  var posNew = -scrollPos / (this._offsetSize - this._scrollSize) * this._thumbRange;
  this._$thumb.css(this._dir, posNew);

  // show scrollbar
  if (this._offsetSize >= this._scrollSize) {
    this._$scrollbar.css('visibility', 'hidden');
  } else {
    this._$scrollbar.css('visibility', 'visible');
  }
  this._$scrollbar.css(this._dir, scrollPos);
};

scout.Scrollbar.prototype.scrollTop = function(scrollTop) {
  if (scrollTop === undefined) {
    return this._$parent.parent().scrollTop();
  }
  var posNew = -scrollTop / (this._offsetSize - this._scrollSize) * this._thumbRange;
  this._$parent.parent().scrollTop(scrollTop);
  this._$scrollbar.css(this._dir, scrollTop);
  this._$thumb.css(this._dir, posNew);
};

scout.Scrollbar.prototype._setThumb = function(posDiff) {
  var posOld = this._$thumb.offset()[this._dir] - this._$scrollbar.offset()[this._dir],
    posNew = Math.min(this._thumbRange, Math.max(0, posOld + posDiff)),
    scrollPos = -(this._offsetSize - this._scrollSize) / this._thumbRange * posNew;

  this._$parent.parent()[this._scrollDir](scrollPos);
  this._$scrollbar.css(this._dir, scrollPos);
  this._$thumb.css(this._dir, posNew);
};
