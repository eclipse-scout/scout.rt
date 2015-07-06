scout.tooltips = {

  defaultOptions: {
    arrowPosition: 25,
    arrowPositionUnit: '%',
    htmlEnabled: false,
    tooltipDelay: 350
  },

  install: function($comp, options) {
    options = $.extend({}, this.defaultOptions, options);
    var support = new scout.TooltipSupport(options);
    support.install($comp);
  },

  uninstall: function($comp) {
    var support = $comp.data('tooltipSupport');
    if (support) {
      support.uninstall($comp);
    }
  }
};

scout.TooltipSupport = function(options) {
  this._options = options;
  this._tooltip;
  this._tooltipTimeoutId;
};

scout.TooltipSupport.prototype.install = function($comp) {
  // prevent multiple installation of tooltip support
  if (!$comp.data('tooltipSupport')) {
    $comp
      .on('mouseenter', this._onMouseEnter.bind(this))
      .on('mouseleave', this._onMouseLeave.bind(this))
      .data('tooltipSupport', this);
  }
};

scout.TooltipSupport.prototype.uninstall = function($comp) {
  $comp
    .removeData('tooltipSupport')
    .off('mouseleave', this._mouseLeaveHandler)
    .off('mouseenter', this._onMouseEnterHandler);
  this._removeTooltip();
};

scout.TooltipSupport.prototype._onMouseEnter = function(event) {
  var $comp = $(event.currentTarget);
  clearTimeout(this._tooltipTimeoutId);
  this._tooltipTimeoutId = setTimeout(this._showTooltip.bind(this, $comp), this._options.tooltipDelay);
};

scout.TooltipSupport.prototype._onMouseLeave = function(event) {
  this._removeTooltip();
};

scout.TooltipSupport.prototype._removeTooltip = function() {
  clearTimeout(this._tooltipTimeoutId);
  if (this._tooltip) {
    this._tooltip.remove();
    this._tooltip = null;
  }
};

scout.TooltipSupport.prototype._showTooltip = function($comp) {
  var text = this._options.text,
    tooltipTextData = $comp.data('tooltipText');
  if ($.isFunction(tooltipTextData)) {
    text = tooltipTextData($comp);
  } else if (tooltipTextData) {
    text = tooltipTextData;
  }

  if (this._tooltip && this._tooltip.rendered) {
    // update existing tooltip
    this.tooltip.renderText(text);
  }
  else {
    // create new tooltip
    var options = $.extend({
      $anchor: $comp,
      text: text
    }, this._options);
    this._tooltip = new scout.Tooltip(options);
    this._tooltip.render();
  }
};
