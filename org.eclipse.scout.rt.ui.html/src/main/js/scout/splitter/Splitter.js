scout.Splitter = function(options) {
  scout.Splitter.parent.call(this);
  options = options || {};
  this.splitHorizontal = options.splitHorizontal !== undefined ? options.splitHorizontal : true;
  this.$anchor = options.$anchor;
  this._addEventSupport();
};
scout.inherits(scout.Splitter, scout.Widget);

scout.Splitter.prototype._render = function($parent) {
  this.$container = $parent.appendDiv('splitter')
    .addClass(this.splitHorizontal ? 'x-axis' : 'y-axis')
    .on('mousedown', this._onMouseDown.bind(this));

  this.position();
};

scout.Splitter.prototype.position = function() {
  var anchor = scout.graphics.offsetBounds(this.$anchor);
  if (this.splitHorizontal) {
    this.$container.cssLeft(anchor.x + anchor.width);
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

scout.Splitter.prototype._onMouseMove = function(event) {
  this.trigger('resize', event);
  this.position();
};

scout.Splitter.prototype._onMouseUp = function(event) {
  // Remove listeners and reset cursor
  $(window).off('mousemove.splitter');
  $('body').removeClass((this.splitHorizontal ? 'col-resize' : 'row-resize'));

  this.trigger('resizeend', event);
};
