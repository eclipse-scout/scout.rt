scout.Splitter = function(options) {
  options = options || {};
  this.splitHorizontal = options.splitHorizontal !== undefined ? options.splitHorizontal : true;
  this.$anchor = options.$anchor;
  this.events = new scout.EventSupport();
};

scout.Splitter.prototype.render = function($parent) {
  this.$parent = $parent;
  var anchor = scout.graphics.offsetBounds(this.$anchor);

  this.$splitter = $parent.appendDiv('splitter')
    .addClass(this.splitHorizontal ? 'x-axis' : 'y-axis')
    .on('mousedown', this._onMouseDown.bind(this));

  this.position();
};

scout.Splitter.prototype.on = function(type, func) {
  return this.events.on(type, func);
};

scout.Splitter.prototype.off = function(type, func) {
  this.events.off(type, func);
};

scout.Splitter.prototype.position = function() {
  var anchor = scout.graphics.offsetBounds(this.$anchor);
  if (this.splitHorizontal) {
    this.$splitter.cssLeft(anchor.x + anchor.width);
  }
};

scout.Splitter.prototype._onMouseDown = function(event) {
  // Add listeners (we add them to the window to make sure we get the mouseup event even when the cursor it outside the window)
  $(window)
    .on('mousemove.splitter', this._onMouseMove.bind(this))
    .one('mouseup', this._onMouseUp.bind(this));

  // Ensure the correct cursor is always shown while moving
  $('body').addClass(this.splitHorizontal ? 'col-resize' : 'row-resize');

  this.events.trigger('resizestart', event);
  return false;
};

scout.Splitter.prototype._onMouseMove = function(event) {
  this.events.trigger('resize', event);
  this.position();
};

scout.Splitter.prototype._onMouseUp = function(event) {
  // Remove listeners and reset cursor
  $(window).off('mousemove.splitter');
  $('body').removeClass((this.splitHorizontal ? 'col-resize' : 'row-resize'));

  this.events.trigger('resizeend', event);
};
