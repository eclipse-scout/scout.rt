/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {scout, tooltips} from '../index';
import $ from 'jquery';

export default class TooltipSupport {

  constructor(options) {
    let defaultOptions = {
      selector: null,
      delay: tooltips.DEFAULT_TOOLTIP_DELAY,
      text: undefined,
      nativeTooltip: false
    };
    options = $.extend({}, defaultOptions, options);
    this._options = options;
    this._mouseEnterHandler = this._onMouseEnter.bind(this);
    this._mouseLeaveHandler = this._onMouseLeave.bind(this);
    this._tooltip = null;
    this._tooltipTimeoutId = null;
  }

  install($comp) {
    // prevent multiple installation of tooltip support
    if (!$comp.data('tooltipSupport')) {
      $comp
        .on('mouseenter', this._options.selector, this._mouseEnterHandler)
        .on('mouseleave', this._options.selector, this._mouseLeaveHandler)
        .data('tooltipSupport', this);
    }
  }

  uninstall($comp) {
    $comp
      .removeData('tooltipSupport')
      .off('mouseleave', this._options.selector, this._mouseLeaveHandler)
      .off('mouseenter', this._options.selector, this._mouseEnterHandler);
    this._destroyTooltip();
  }

  update($comp, options) {
    $.extend(this._options, options);
    if (this._tooltip) {
      this._showTooltip($comp);
    }
  }

  cancel($comp) {
    clearTimeout(this._tooltipTimeoutId);
  }

  close() {
    this._destroyTooltip();
  }

  _onMouseEnter(event) {
    let $comp = $(event.currentTarget);

    if (this._options.nativeTooltip) {
      let text = this._text($comp);
      $comp.attr('title', text);
    } else {
      clearTimeout(this._tooltipTimeoutId);
      this._tooltipTimeoutId = setTimeout(this._showTooltip.bind(this, $comp), this._options.delay);
    }
  }

  _onMouseLeave(event) {
    this._destroyTooltip();
  }

  _destroyTooltip() {
    clearTimeout(this._tooltipTimeoutId);
    if (this._tooltip) {
      this._tooltip.destroy();
      this._tooltip = null;
    }
  }

  _text($comp) {
    let text = this._options.text || $comp.data('tooltipText');
    if ($.isFunction(text)) {
      text = text($comp);
    }
    return text;
  }

  _htmlEnabled($comp) {
    let htmlEnabled = this._options.htmlEnabled || $comp.data('htmlEnabled');
    if ($.isFunction(htmlEnabled)) {
      htmlEnabled = htmlEnabled($comp);
    }
    return scout.nvl(htmlEnabled, false);
  }

  _showTooltip($comp) {
    if (!$comp || !$comp.isAttached()) {
      return; // removed in the meantime (this method is called using setTimeout)
    }
    let text = this._text($comp);
    if (!text) {
      return; // treat undefined and no text as no tooltip
    }

    let htmlEnabled = this._htmlEnabled($comp);

    if (this._tooltip && this._tooltip.rendered) {
      // update existing tooltip
      this._tooltip.setText(text);
      this._tooltip.setSeverity(this._options.severity);
      this._tooltip.setMenus(this._options.menus);
    } else {
      // create new tooltip
      let options = $.extend({}, this._options, {
        $anchor: this._options.$anchor || $comp,
        text: text,
        htmlEnabled: htmlEnabled
      });
      this._tooltip = scout.create('Tooltip', options);
      this._tooltip.render(options.$parent);
    }
  }
}
