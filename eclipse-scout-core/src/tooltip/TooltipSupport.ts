/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {scout, Tooltip, tooltips} from '../index';
import $ from 'jquery';
import TooltipModel from './TooltipModel';
import MouseEnterEvent = JQuery.MouseEnterEvent;
import MouseLeaveEvent = JQuery.MouseLeaveEvent;

export interface TooltipSupportOptions extends TooltipModel {
  /**
   * Default is no selector
   */
  selector?: JQuery.Selector;
  /**
   * Default is {@link tooltips.DEFAULT_TOOLTIP_DELAY}
   */
  delay?: number;

  /**
   * Default is false.
   */
  nativeTooltip?: boolean;

  $anchor?: JQuery;
}

export default class TooltipSupport {
  protected _options: TooltipSupportOptions;
  protected _mouseEnterHandler: (event: MouseEnterEvent<HTMLElement>) => void;
  protected _mouseLeaveHandler: (event: MouseLeaveEvent<HTMLElement>) => void;
  protected _tooltip: Tooltip;
  protected _tooltipTimeoutId: number;

  constructor(options: TooltipSupportOptions) {
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

  install($comp: JQuery) {
    // prevent multiple installation of tooltip support
    if (!$comp.data('tooltipSupport')) {
      $comp
        .on('mouseenter', this._options.selector, this._mouseEnterHandler)
        .on('mouseleave', this._options.selector, this._mouseLeaveHandler)
        .data('tooltipSupport', this);
    }
  }

  uninstall($comp: JQuery) {
    $comp
      .removeData('tooltipSupport')
      .off('mouseleave', this._options.selector, this._mouseLeaveHandler)
      .off('mouseenter', this._options.selector, this._mouseEnterHandler);
    this._destroyTooltip();
  }

  update($comp: JQuery, options: TooltipSupportOptions) {
    $.extend(this._options, options);
    if (this._tooltip) {
      this._showTooltip($comp);
    }
  }

  cancel($comp: JQuery) {
    clearTimeout(this._tooltipTimeoutId);
  }

  close() {
    this._destroyTooltip();
  }

  protected _onMouseEnter(event: MouseEnterEvent<HTMLElement>) {
    let $comp = $(event.currentTarget);

    if (this._options.nativeTooltip) {
      let text = this._text($comp);
      $comp.attr('title', text);
    } else {
      clearTimeout(this._tooltipTimeoutId);
      this._tooltipTimeoutId = setTimeout(this._showTooltip.bind(this, $comp), this._options.delay);
    }
  }

  protected _onMouseLeave(event: MouseLeaveEvent<HTMLElement>) {
    this._destroyTooltip();
  }

  protected _destroyTooltip() {
    clearTimeout(this._tooltipTimeoutId);
    if (this._tooltip) {
      this._tooltip.destroy();
      this._tooltip = null;
    }
  }

  protected _text($comp: JQuery): string {
    let text = this._options.text || $comp.data('tooltipText');
    if ($.isFunction(text)) {
      text = text($comp);
    }
    return text;
  }

  protected _htmlEnabled($comp: JQuery): boolean {
    let htmlEnabled = this._options.htmlEnabled || $comp.data('htmlEnabled');
    if ($.isFunction(htmlEnabled)) {
      htmlEnabled = htmlEnabled($comp);
    }
    return scout.nvl(htmlEnabled, false);
  }

  protected _showTooltip($comp: JQuery) {
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
      this._tooltip = scout.create(Tooltip, options);
      this._tooltip.render(options.$parent);
    }
  }
}
