/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
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
   * Cancels the scheduled task to show the tooltip.
   */
  cancel: function($comp) {
    var support = $comp.data('tooltipSupport');
    if (support) {
      support.cancel($comp);
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
