// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

// TODO AWE: (scrollbar) discuss with C.GU, C.RU:
// - bugfix: Scrollbar impl. cannot deal with scrollTo()
// - bugfix: Scrollbar should not change insets (padding, margin) of scrollable container
// - rename initThumb to update
// - pass options object to Ctor instead of multiple arguments

scout.Scrollbar = function($parent, options) {

  this.defaultOptions = {
    axis:'y',
    invertColors:false
  };

  this.options = $.extend({}, this.defaultSettings, options);
  this._$parent = $parent;
  this._beginDefault = 0;
  this._thumbRange;
  this._scroll;
  this._offset;
  this._initThumbPending = false;

  // create scrollbar
  this._$scrollbar = $parent.beforeDiv('', 'scrollbar');
  this._$thumb = this._$scrollbar.appendDiv('', 'scrollbar-thumb');
  if (this.options.invertColors === true) {
    this._$thumb.css('background-color', 'rgba(0, 0, 0, 0.3)');
  }
  this._dim = this.options.axis === 'x' ? 'Width' : 'Height';
  this._dir = this.options.axis === 'x' ? 'left' : 'top';

  var begin = 0,
    that = this;

  //event handling
  $parent.parent().
    on('DOMMouseScroll mousewheel', '', scrollWheel).
    on('scroll', function() {
      $.log.debug('onScroll. _scroll=' + that._scroll + ' ._offset=' + that._offset + ' scrollTop=' + $parent.parent().scrollTop());
      this._setThumb($parent.parent().scrollTop() / (that._scroll / that._offset));
    }.bind(this));
  this._$scrollbar.on('mousedown', scrollEnd);
  this._$thumb.on('mousedown', '', scrollStart);
  $(window).on('load resize', this.initThumb.bind(this));

  function scrollWheel(event) {
    if (event.ctrlKey) {
      return true; // allow ctrl+mousewheel to zoom the page
    }
    event = event.originalEvent || window.event.originalEvent;
    var w = event.wheelDelta ? -event.wheelDelta / 2 : event.detail * 60,
      d = that._scroll / that._offset;
    that._setThumb(w / d);
    return false;
  }

  function scrollStart(event) {
    begin = (this.options.axis === 'x' ? event.pageX : event.pageY) - that._$thumb.offset()[that._dir];
    that._$thumb.addClass('scrollbar-thumb-move');
    $(document).
      on('mousemove', scrollEnd).
      one('mouseup', scrollExit);
    return false;
  }

  function scrollEnd(event) {
    begin = begin === 0 ? that._beginDefault : begin;
    var end = (this.options.axis === 'x' ? event.pageX : event.pageY) - that._$thumb.offset()[that._dir];
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
scout.Scrollbar.prototype.initThumb = function() {
  // Thumb is (re)initialized, but only after the current thread has finished.
  // Additionally, the call is scheduled at most once. This prevents unnecessary
  // executions of the same code while the UI is updated.
  if (this._initThumbPending) {
    return;
  }
  var that = this;
  setTimeout(function() {
    that._initThumbImpl();
    that._initThumbPending = false;
  }, 0);
  this._initThumbPending = true;
};


/**
 * do not use this internal method
 */
scout.Scrollbar.prototype._initThumbImpl = function() {
  this._offset = this._$parent[0]['offset' + this._dim];
  this._scroll = this._$parent[0]['scroll' + this._dim];

  var margin = parseFloat(this._$scrollbar.css('margin-top')),
    topContainer = parseFloat(this._$parent.css(this._dir));

  // when needed: move container to right position
  if (this._offset - topContainer >= this._scroll) {
    topContainer = Math.min(0, -this._scroll + this._offset);
    this._$parent.stop().animateAVCSD(this._dir, topContainer);
  }

  // calc size and range of thumb
  var thumbSize = Math.max(this._offset * this._offset / this._scroll - margin * 2, 30);
  this._thumbRange = this._offset - thumbSize - margin * 2;

  // set size of thumb
  this._$thumb.css(this._dim.toLowerCase(), thumbSize);
  this._beginDefault = thumbSize / 2;

  // set location of thumb
  $.log.debug('_initThumbImpl:  topContainer=' + topContainer + ' _offset=' + this._offset + ' _scroll=' + this._scroll + ' _thumbRange=' + this._thumbRange +
      ' jquery.scrollTop=' + this._$parent.parent().scrollTop());
  this._$thumb.css(this._dir, topContainer / (this._offset - this._scroll) * this._thumbRange);

  // show scrollbar
  if (this._offset >= this._scroll) {
    this._$scrollbar.css('visibility', 'hidden');
  } else {
    this._$scrollbar.css('visibility', 'visible');
  }
};

scout.Scrollbar.prototype._setThumb = function(posDiff) {
  var posOld = this._$thumb.offset()[this._dir] - this._$scrollbar.offset()[this._dir],
    posNew = Math.min(this._thumbRange, Math.max(0, posOld + posDiff));

  this._$parent.css(this._dir, (this._offset - this._scroll) / this._thumbRange * posNew);
  this._$thumb.css(this._dir, posNew);
};
