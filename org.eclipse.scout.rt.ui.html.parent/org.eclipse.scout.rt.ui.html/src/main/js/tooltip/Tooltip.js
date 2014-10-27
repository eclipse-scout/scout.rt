scout.Tooltip = function(options) {
  this.text = options.text || '';
  this.arrowPosition = options.arrowPosition || 50;
  this.arrowPositionUnit = options.arrowPositionUnit || 'px';
  this.windowPaddingX = options.windowPaddingX !== undefined ? options.windowPaddingX : 10;
  this.windowPaddingY = options.windowPaddingY !== undefined ? options.windowPaddingY : 5;
  this.origin = options.origin;
  this.$origin = options.$origin;
  this.autoRemove = options.autoRemove !== undefined ? options.autoRemove : true;
  this.$context = options.$context;
};

scout.Tooltip.prototype.render = function($parent) {
  if (!$parent) {
    $parent = $('body');
  }

  this.$container = $.makeDIV('tooltip')
    .hide()
    .data('tooltip', this)
    .data('tooltipContext', this.$context)
    .appendTo($parent);

  this.$arrow = $.makeDIV('tooltip-arrow').appendTo(this.$container);
  this.$container.appendDIV('tooltip-content', this.text);
  this.position();
  this.$container.show();

  if (this.autoRemove) {
    //every user action will close menu
    $(document).on('mousedown.tooltip', this._onTooltipClicked.bind(this));
    $(document).on('keydown.tooltip', this.remove.bind(this));
  }
  this.rendered = true;
};

scout.Tooltip.prototype.position = function() {
  var top, left, arrowHeight, overlapX, overlapY, x, y, origin,
    tooltipWidth, tooltipHeight, arrowDivWidth;

  origin = this.origin || this.$origin && scout.graphics.offsetBounds(this.$origin);
  x = origin.x + origin.width / 2;
  y = origin.y;

  arrowDivWidth = this.$arrow.outerWidth();
  //Arrow is a div rotated by 45 deg -> visible height is half the div
  arrowHeight = scout.Tooltip.computeHypotenuse(arrowDivWidth) / 2;

  tooltipHeight = this.$container.outerHeight();
  tooltipWidth = this.$container.outerWidth();

  //Compute actual arrow position if position is provided in percentage
  if (this.arrowPositionUnit === '%') {
    this.arrowPosition = tooltipWidth * this.arrowPosition / 100;
  }

  top = y - tooltipHeight - arrowHeight;
  left = x - this.arrowPosition;
  overlapX = left + tooltipWidth + this.windowPaddingX - $(window).width();
  overlapY = top - this.windowPaddingY;

  //Move tooltip to the left until it gets fully visible
  if (overlapX > 0) {
    left -= overlapX;
    this.arrowPosition = x - left;
  }

  //Move tooltip to the bottom, arrow on top
  if (overlapY < 0) {
    this.$arrow.addClass('arrow-top');
    top = y + origin.height + arrowHeight;
  } else {
    this.$arrow.addClass('arrow-bottom');
  }

  this.$arrow.cssLeft(this.arrowPosition);
  this.$container.
    cssLeft(left).
    cssTop(top);
};

scout.Tooltip.computeHypotenuse = function(x) {
  return Math.sqrt(Math.pow(x, 2) + Math.pow(x, 2));
};

scout.Tooltip.prototype.remove = function() {
  this.rendered = false;
  $(document).off('mousedown.tooltip keydown.tooltip');
  this.$container.remove();
};

scout.Tooltip.prototype._onTooltipClicked = function() {
  //Only remove the tooltip if the click is outside of the container
  if (!this.$container.children().is($(event.target))) {
    this.remove();
  }
};

/**
 * Removes every tooltip which belongs to one of the given $contexts
 */
scout.Tooltip.removeTooltips = function($contexts, $parent) {
  var $context,  $tooltips;
  if (!$parent) {
    $parent = $('body');
  }
  $tooltips = $parent.find('.tooltip');

  for (var i=0; i < $tooltips.length; i++) {
    for (var j=0; j < $contexts.length; j++) {
      $context = $tooltips.eq(i).data('tooltipContext');

      if ($context[0] === $contexts.eq(j)[0]) {
        $tooltips[i].data('tooltip').remove();
      }
    }
  }
};
