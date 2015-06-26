scout.Tooltip = function(options) {
  scout.Tooltip.parent.call(this);
  options = options || {};
  this.text = options.text || '';
  this.arrowPosition = options.arrowPosition !== undefined ? options.arrowPosition : 50;
  this.arrowPositionUnit = options.arrowPositionUnit || 'px';
  this.windowPaddingX = options.windowPaddingX !== undefined ? options.windowPaddingX : 10;
  this.windowPaddingY = options.windowPaddingY !== undefined ? options.windowPaddingY : 5;
  this.origin = options.origin;
  this.$anchor = options.$anchor;
  this.autoRemove = options.autoRemove !== undefined ? options.autoRemove : true;
  this.cssClass = options.cssClass;
  this.tooltipPosition = options.position || 'top';
  this.scrollType = options.scrollType || 'remove';
  this.htmlEnabled = options.htmlEnabled !== undefined ? options.htmlEnabled : false;
  this.$content;
};
scout.inherits(scout.Tooltip, scout.Widget);

scout.Tooltip.prototype._render = function($parent) {
  // Auto-detect parent
  $parent = $parent || (this.$anchor && this.$anchor.closest('.desktop'));
  // Remember parent (necessary for detach helper)
  this.$parent = $parent;

  this.$container = $.makeDiv('tooltip')
    .hide()
    .data('tooltip', this)
    .appendTo($parent);

  if (this.cssClass) {
    this.$container.addClass(this.cssClass);
  }

  this.$arrow = $.makeDiv('tooltip-arrow').appendTo(this.$container);
  this.$content = $.makeDiv('tooltip-content').appendTo(this.$container);
  this.renderText(this.text);
  this.position();
  this.$container.show();

  if (this.autoRemove) {
    // Every user action will remove the tooltip
    $(document).on('mousedown.tooltip', this._onTooltipClicked.bind(this));
    $(document).on('keydown.tooltip', this.remove.bind(this));
  }

  if (this.$anchor) {
    if (this.scrollType === 'position') {
      this._scrollHandler = this.position.bind(this);
    } else if (this.scrollType === 'remove') {
      this._scrollHandler = this.remove.bind(this);
    }
    if (this._scrollHandler) {
      scout.scrollbars.onScroll(this.$anchor, this._scrollHandler);
    }
  }
};

scout.Tooltip.prototype._remove = function() {
  $(document).off('mousedown.tooltip keydown.tooltip');
  if (this._scrollHandler) {
    scout.scrollbars.offScroll(this._scrollHandler);
    this._scrollHandler = null;
  }
  scout.Tooltip.parent.prototype._remove.call(this);
};

scout.Tooltip.prototype.renderText = function(text) {
  if (this.htmlEnabled) {
    this.$content.html(text);
  } else {
    // use nl2br to allow tooltips with line breaks
    this.$content.html(scout.strings.nl2br(scout.strings.removeAmpersand(text)));
  }
};

scout.Tooltip.prototype.position = function() {
  var top, left, arrowHeight, overlapX, overlapY, x, y, origin,
    tooltipWidth, tooltipHeight, arrowDivWidth, inView;

  if (this.origin) {
    origin = this.origin;
    x = origin.x;
  } else {
    origin = this.$anchor && scout.graphics.offsetBounds(this.$anchor);
    x = origin.x + origin.width / 2;
  }
  y = origin.y;

  if (!this.autoRemove) {
    // Sticky tooltip must only be visible if the location where the tooltip points is in view
    inView = scout.scrollbars.isLocationInView(origin, this.$anchor.scrollParent());
    this.$container.setVisible(inView);
  }

  arrowDivWidth = this.$arrow.outerWidth();
  // Arrow is a div rotated by 45 deg -> visible height is half the div
  arrowHeight = scout.Tooltip.computeHypotenuse(arrowDivWidth) / 2;

  tooltipHeight = this.$container.outerHeight();
  tooltipWidth = this.$container.outerWidth();

  // Compute actual arrow position if position is provided in percentage
  if (this.arrowPositionUnit === '%') {
    this.arrowPosition = tooltipWidth * this.arrowPosition / 100;
  }

  top = y - tooltipHeight - arrowHeight;
  left = x - this.arrowPosition;
  overlapX = left + tooltipWidth + this.windowPaddingX - $(window).width();
  overlapY = top - this.windowPaddingY;

  // Move tooltip to the left until it gets fully visible
  if (overlapX > 0) {
    left -= overlapX;
    this.arrowPosition = x - left;
  }

  // FIXME AWE: (tooltip) discuss with C.GU/C.RU: also support left/right position for tooltip?
  // FIXME AWE: (tooltip) discuss with C.GU/C.RU: always use the same BG-color for tooltips?
  //   when custom BG-color is allowed, styling is a bit complicated because of arrow-'hack'

  // Move tooltip to the bottom, arrow on top
  if (this.tooltipPosition === 'bottom' || overlapY < 0) {
    this.$arrow.addClass('arrow-top');
    top = y + origin.height + arrowHeight;
  } else {
    this.$arrow.addClass('arrow-bottom');
  }

  this.$arrow.cssLeft(this.arrowPosition);
  this.$container
    .cssLeft(left)
    .cssTop(top);
};

scout.Tooltip.prototype._onTooltipClicked = function(event) {
  // Only remove the tooltip if the click is outside of the container or the $anchor (= status icon)
  var $target = $(event.target);
  if ($target[0] === this.$container[0] || this.$container.children().is($target) ||
    $target[0] === this.$anchor[0] || this.$anchor.children().is($target)) {
    return;
  }
  this.remove();
};

/* --- STATIC HELPERS ------------------------------------------------------------- */

/**
 * @memberOf scout.Tooltip
 */
scout.Tooltip.computeHypotenuse = function(x) {
  return Math.sqrt(Math.pow(x, 2) + Math.pow(x, 2));
};

/**
 * Finds every tooltip whose $anchor belongs to $context.
 *
 * @memberOf scout.Tooltip
 */
scout.Tooltip.findTooltips = function($context) {
  var $tooltips, i, tooltip,
    tooltips = [];
  $tooltips = $('.tooltip');

  for (i = 0; i < $tooltips.length; i++) {
    tooltip = $tooltips.eq(i).data('tooltip');
    if ($context.has(tooltip.$anchor).length > 0) {
      tooltips.push(tooltip);
    }
  }
  return tooltips;
};
