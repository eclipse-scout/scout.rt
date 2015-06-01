scout.Splitter = function(options) {
  scout.Splitter.parent.call(this);
  options = options || {};
  this.splitHorizontal = options.splitHorizontal !== undefined ? options.splitHorizontal : true;
  this.$anchor = options.$anchor;
  this._$root = options.$root;
  this._maxRatio = options.maxRatio;
  this._oldRatio;
  this._addEventSupport();
};
scout.inherits(scout.Splitter, scout.Widget);

scout.Splitter.prototype._render = function($parent) {
  this.$container = $parent.appendDiv('splitter')
    .addClass(this.splitHorizontal ? 'x-axis' : 'y-axis')
    .on('mousedown', this._onMouseDown.bind(this));
  this.position();
};

/**
 * This method is also called by the Desktop while the bread-crumb animation is running.
 * In that case an event object is passed to this function, that's why the isNumeric check
 * is required.
 */
scout.Splitter.prototype.position = function(newSize) {
  if (newSize && $.isNumeric(newSize)) {
    if (this.splitHorizontal) {
      this.$container.cssLeft(newSize);
    } else {
      this.$container.cssTop(newSize);
    }
  } else {
    var anchorBounds = scout.graphics.offsetBounds(this.$anchor);
    if (this.splitHorizontal) {
      this.$container.cssLeft(anchorBounds.x + anchorBounds.width);
    } else {
      this.$container.cssTop(anchorBounds.y + anchorBounds.height);
    }
  }
};

scout.Splitter.prototype._onMouseDown = function(event) {
  // Add listeners (we add them to the window to make sure we get the mouseup event even when the cursor it outside the window)
  $(window)
    .on('mousemove.splitter', this._onMouseMove.bind(this))
    .one('mouseup', this._onMouseUp.bind(this));
  // Ensure the correct cursor is always shown while moving
  $('body').addClass(this.splitHorizontal ? 'col-resize' : 'row-resize');
  this.trigger('resizestart', event);
  // Prevent text selection in a form
  event.preventDefault();
};

scout.Splitter.prototype._ratio = function(event) {
  var splitterBounds = scout.graphics.offsetBounds(this.$container),
    rootBounds = scout.graphics.offsetBounds(this._$root);
  var ratio, rootSize;
  if (this.splitHorizontal) {
    rootSize = rootBounds.width;
    ratio = (event ? event.pageX : splitterBounds.x) / rootBounds.width;
  } else {
    rootSize = rootBounds.height;
    ratio = (event ? event.pageY : splitterBounds.y) / rootBounds.height;
  }
  return {
    ratio: ratio,
    rootSize: rootSize
  };
};

scout.Splitter.prototype._onMouseMove = function(event) {
  var obj = this._ratio(event),
    ratio = obj.ratio;
  if (ratio >= this._maxRatio) {
    ratio = this._maxRatio;
  }
  if (ratio != this._oldRatio) {
    var newSize = ratio * obj.rootSize;
    event.data = newSize;
    this.trigger('resize', event);
    this.position(newSize);
  }
  this._oldRatio = ratio;
};

scout.Splitter.prototype._onMouseUp = function(event) {
  var obj = this._ratio(event);
  event.data = obj.ratio * obj.rootSize;

  // Remove listeners and reset cursor
  $(window).off('mousemove.splitter');
  $('body').removeClass((this.splitHorizontal ? 'col-resize' : 'row-resize'));
  this.trigger('resizeend', event);
};
