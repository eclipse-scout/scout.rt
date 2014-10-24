scout.Tooltip = function(options) {
  this.text = options.text || '';
  this.arrowPosition = options.arrowPosition || 50;
  this.arrowPositionUnit = options.arrowPositionUnit || 'px';
  this.windowPaddingX = options.windowPaddingX || 10;
  this.windowPaddingY = options.windowPaddingY || 5;
  this.origin = options.origin || options.$origin && scout.graphics.offsetBounds(options.$origin);

  this.x = this.origin.x + this.origin.width / 2;
  this.y = this.origin.y;
};

scout.Tooltip.prototype.render = function($parent) {
  var top, left, arrowHeight, overlapX, overlapY,
    tooltipWidth, tooltipHeight, arrowDivWidth;

  if (!$parent) {
    $parent = $('body');
  }

  this.$container = $.makeDIV('tooltip')
    .hide()
    .appendTo($parent);

  this.$arrow = $.makeDIV('tooltip-arrow').appendTo(this.$container);
  this.$container.appendDIV('tooltip-content', this.text);

  arrowDivWidth = this.$arrow.outerWidth();
  //Arrow is a div rotated by 45 deg -> visible height is half the div
  arrowHeight = scout.Tooltip.computeHypotenuse(arrowDivWidth) / 2;

  tooltipHeight = this.$container.outerHeight();
  tooltipWidth = this.$container.outerWidth();

  //Compute actual arrow position if position is provided in percentage
  if (this.arrowPositionUnit === '%') {
    this.arrowPosition = tooltipWidth * this.arrowPosition / 100;
  }

  top = this.y - tooltipHeight - arrowHeight;
  left = this.x - this.arrowPosition;
  overlapX = left + tooltipWidth + this.windowPaddingX - $(window).width();
  overlapY = top - this.windowPaddingY;

  //Move tooltip to the left until it gets fully visible
  if (overlapX > 0) {
    left -= overlapX;
    this.arrowPosition = this.x - left;
  }

  //Move tooltip to the bottom, arrow on top
  if (overlapY < 0) {
    this.$arrow.addClass('arrow-top');
    top = this.y + this.origin.height + arrowHeight;
  } else {
    this.$arrow.addClass('arrow-bottom');
  }

  this.$arrow.cssLeft(this.arrowPosition);
  this.$container.
    cssLeft(left).
    cssTop(top).
    show();

  //every user action will close menu
  $(document).on('mousedown.tooltip', this._onTooltipClicked.bind(this));
  $(document).on('keydown.tooltip', this.remove.bind(this));
};

scout.Tooltip.computeHypotenuse = function(x) {
  return Math.sqrt(Math.pow(x, 2) + Math.pow(x, 2));
};

scout.Tooltip.prototype.remove = function() {
  $(document).off('mousedown.tooltip keydown.tooltip');
  this.$container.remove();
};

scout.Tooltip.prototype._onTooltipClicked = function() {
  //Only remove the tooltip if the click is outside of the container
  if (!this.$container.children().is($(event.target))) {
    this.remove();
  }
};
