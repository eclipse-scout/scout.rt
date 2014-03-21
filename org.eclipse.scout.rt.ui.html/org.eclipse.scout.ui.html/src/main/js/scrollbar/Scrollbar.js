// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

Scout.Scrollbar = function($parent, axis) {
  this._$parent = $parent;
  this._beginDefault = 0;
  this._thumbRange;
  this._scroll;
  this._offset;
  // create scrollbar
  this._$scrollbar = $parent.beforeDiv('', 'scrollbar');
  this._$thumb = this._$scrollbar.appendDiv('', 'scrollbar-thumb');
  this._dim = (axis === "x" ? "Width" : "Height");
  this._dir = (axis === "x" ? "left" : "top");

  var begin = 0,
    that = this;

  //event handling
  $parent.parent().on('DOMMouseScroll mousewheel', '', scrollWheel);
  this._$scrollbar.on('mousedown', scrollEnd);
  this._$thumb.on('mousedown', '', scrollStart);
  $(window).on('load resize', this.initThumb.bind(this));

  function scrollWheel(event) {
    event = event.originalEvent || window.event.originalEvent;
    var w = event.wheelDelta ? -event.wheelDelta / 8 : event.detail * 15;
    that._setThumb(w);
    return false;
  }

  function scrollStart(event) {
    begin = (axis === "x" ? event.pageX : event.pageY) - that._$thumb.offset()[that._dir];
    that._$thumb.addClass('scrollbar-thumb-move');
    $(document).on('mousemove', scrollEnd)
      .one('mouseup', scrollExit);
    return false;
  }

  function scrollEnd(event) {
    begin = begin === 0 ? that._beginDefault : begin;
    var end = (axis === "x" ? event.pageX : event.pageY) - that._$thumb.offset()[that._dir];
    that._setThumb(end - begin);
  }

  function scrollExit() {
    that._$thumb.removeClass('scrollbar-thumb-move');
    $(document).off("mousemove");
    return false;
  }
};

/**
 * Use this function (from outside) if size of tree content changes
 */
Scout.Scrollbar.prototype.initThumb = function() {
  this._offset = this._$parent[0]["offset" + this._dim];
  this._scroll = this._$parent[0]["scroll" + this._dim];

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
  this._$thumb.css(this._dir, topContainer / (this._offset - this._scroll) * this._thumbRange);

  // show scrollbar
  if (this._offset >= this._scroll) {
    this._$scrollbar.css('visibility', 'hidden');
  } else {
    this._$scrollbar.css('visibility', 'visible');
  }
};

Scout.Scrollbar.prototype._setThumb = function(posDiff) {
  var posOld = this._$thumb.offset()[this._dir] - this._$scrollbar.offset()[this._dir],
    posNew = Math.min(this._thumbRange, Math.max(0, posOld + posDiff));

  this._$parent.css(this._dir, (this._offset - this._scroll) / this._thumbRange * posNew);
  this._$thumb.css(this._dir, posNew);
};
