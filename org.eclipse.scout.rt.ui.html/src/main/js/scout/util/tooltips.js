/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.tooltips = {
  install: function($comp, options) {
    var support = new scout.TooltipSupport(options);
    support.install($comp);
  },

  uninstall: function($comp) {
    var support = $comp.data('tooltipSupport');
    if (support) {
      support.uninstall($comp);
    }
  },

  /**
   * Finds every tooltip whose $anchor belongs to $context.
   *
   */
  find: function($context) {
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
  }
};

scout.TooltipSupport = function(options) {
  var defaultOptions = {
    selector: null,
    delay: 350,
    tooltipText: undefined
  };
  options = $.extend({}, defaultOptions, options);
  this._options = options;
  this._mouseEnterHandler = this._onMouseEnter.bind(this);
  this._mouseLeaveHandler = this._onMouseLeave.bind(this);
  this._tooltip;
  this._tooltipTimeoutId;
};

scout.TooltipSupport.prototype.install = function($comp) {
  // prevent multiple installation of tooltip support
  if (!$comp.data('tooltipSupport')) {
    $comp
      .on('mouseenter', this._options.selector, this._mouseEnterHandler)
      .on('mouseleave', this._options.selector, this._mouseLeaveHandler)
      .data('tooltipSupport', this);
  }
};

scout.TooltipSupport.prototype.uninstall = function($comp) {
  $comp
    .removeData('tooltipSupport')
    .off('mouseleave', this._options.selector, this._mouseLeaveHandler)
    .off('mouseenter', this._options.selector, this._onMouseEnterHandler);
  this._removeTooltip();
};

scout.TooltipSupport.prototype._onMouseEnter = function(event) {
  var $comp = $(event.currentTarget);
  clearTimeout(this._tooltipTimeoutId);
  this._tooltipTimeoutId = setTimeout(this._showTooltip.bind(this, $comp), this._options.delay);
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
  var text, tooltipTextData = this._options.tooltipText ||
    $comp.data('tooltipText') ||
    this._options.text;
  if ($.isFunction(tooltipTextData)) {
    text = tooltipTextData($comp);
  } else if (tooltipTextData) {
    text = tooltipTextData;
  }

  if (!text) {
    return; // treat undefined and no text as no tooltip
  }

  if (this._tooltip && this._tooltip.rendered) {
    // update existing tooltip
    this._tooltip.setText(text);
  } else {
    // create new tooltip
    var options = $.extend({
      $anchor: $comp,
      text: text
    }, this._options);
    this._tooltip = scout.create(scout.Tooltip, options);
    this._tooltip.render();
  }
};
