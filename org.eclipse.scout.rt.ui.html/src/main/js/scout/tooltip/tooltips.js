/*******************************************************************************
 * Copyright (c) 2014-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.tooltips = {

  DEFAULT_TOOLTIP_DELAY: 600, // ms

  // Quite long tooltip delay for cases where the normal delay would be annoying
  LONG_TOOLTIP_DELAY: 2000, // ms

  install: function($comp, options) {
    var support = $comp.data('tooltipSupport');
    if (!support) {
      support = new scout.TooltipSupport(options);
      support.install($comp);
    } else {
      support.update($comp, options);
    }
  },

  uninstall: function($comp) {
    var support = $comp.data('tooltipSupport');
    if (support) {
      support.uninstall($comp);
    }
  },

  /**
   * If the tooltip is currently showing, its contents are updated immediately.
   * Otherwise, nothing happens.
   */
  update: function($comp, options) {
    var support = $comp.data('tooltipSupport');
    if (support) {
      support.update($comp, options);
    }
  },

  close: function($comp) {
    var support = $comp.data('tooltipSupport');
    if (support) {
      support.close();
    }
  },

  /**
   * Convenient function to install tooltip support for ellipsis only.
   */
  installForEllipsis: function($comp, options) {
    var defaultOptions = {
      text: function($label) {
        if ($label.isContentTruncated()) {
          return $label.text();
        }
      },
      nativeTooltip: !scout.device.isCustomEllipsisTooltipPossible()
    };
    options = $.extend({}, defaultOptions, options);
    this.install($comp, options);
  },

  /**
   * Finds every tooltip whose $anchor belongs to $context.
   */
  find: function($context) {
    var $tooltips, i, tooltip,
      tooltips = [];
    $tooltips = $('.tooltip', $context.document(true));

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
    delay: scout.tooltips.DEFAULT_TOOLTIP_DELAY,
    text: undefined,
    nativeTooltip: false
  };
  options = $.extend({}, defaultOptions, options);
  this._options = options;
  this._mouseEnterHandler = this._onMouseEnter.bind(this);
  this._mouseLeaveHandler = this._onMouseLeave.bind(this);
  this._tooltip = null;
  this._tooltipTimeoutId = null;
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
  this._destroyTooltip();
};

scout.TooltipSupport.prototype.update = function($comp, options) {
  $.extend(this._options, options);
  if (this._tooltip) {
    this._showTooltip($comp);
  }
};

scout.TooltipSupport.prototype.close = function() {
  this._destroyTooltip();
};

scout.TooltipSupport.prototype._onMouseEnter = function(event) {
  var $comp = $(event.currentTarget);

  if (this._options.nativeTooltip) {
    var text = this._text($comp);
    $comp.attr('title', text);
  } else {
    clearTimeout(this._tooltipTimeoutId);
    this._tooltipTimeoutId = setTimeout(this._showTooltip.bind(this, $comp), this._options.delay);
  }
};

scout.TooltipSupport.prototype._onMouseLeave = function(event) {
  this._destroyTooltip();
};

scout.TooltipSupport.prototype._destroyTooltip = function() {
  clearTimeout(this._tooltipTimeoutId);
  if (this._tooltip) {
    this._tooltip.destroy();
    this._tooltip = null;
  }
};

scout.TooltipSupport.prototype._text = function($comp) {
  var text = this._options.text || $comp.data('tooltipText');
  if ($.isFunction(text)) {
    text = text($comp);
  }
  return text;
};

scout.TooltipSupport.prototype._showTooltip = function($comp) {
  if (!$comp || !$comp.isAttached()) {
    return; // removed in the meantime (this method is called using setTimeout)
  }
  var text = this._text($comp);
  if (!text) {
    return; // treat undefined and no text as no tooltip
  }

  if (this._tooltip && this._tooltip.rendered) {
    // update existing tooltip
    this._tooltip.setText(text);
    this._tooltip.setSeverity(this._options.severity);
    this._tooltip.setMenus(this._options.menus);
  } else {
    // create new tooltip
    var options = $.extend({}, this._options, {
      $anchor: $comp,
      text: text
    });
    this._tooltip = scout.create('Tooltip', options);
    this._tooltip.render();
  }
};
