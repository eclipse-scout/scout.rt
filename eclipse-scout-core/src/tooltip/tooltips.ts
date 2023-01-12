/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Device, InitModelOf, Tooltip, TooltipSupport, TooltipSupportOptions} from '../index';
import $ from 'jquery';

export const tooltips = {
  /**
   * milliseconds
   */
  DEFAULT_TOOLTIP_DELAY: 600,

  /**
   * Quite long tooltip delay for cases where the normal delay would be annoying.
   * Value in milliseconds
   */
  LONG_TOOLTIP_DELAY: 1000,

  install($comp: JQuery, options: InitModelOf<TooltipSupport>) {
    let support = $comp.data('tooltipSupport') as TooltipSupport;
    if (!support) {
      support = new TooltipSupport(options);
      support.install($comp);
    } else {
      support.update($comp, options);
    }
  },

  uninstall($comp: JQuery) {
    let support = $comp.data('tooltipSupport') as TooltipSupport;
    if (support) {
      support.uninstall($comp);
    }
  },

  /**
   * If the tooltip is currently showing, its contents are updated immediately.
   * Otherwise, nothing happens.
   */
  update($comp: JQuery, options?: Partial<TooltipSupportOptions>) {
    let support = $comp.data('tooltipSupport') as TooltipSupport;
    if (support) {
      support.update($comp, options);
    }
  },

  close($comp: JQuery) {
    let support = $comp.data('tooltipSupport') as TooltipSupport;
    if (support) {
      support.close();
    }
  },

  /**
   * Cancels the scheduled task to show the tooltip.
   */
  cancel($comp: JQuery) {
    let support = $comp.data('tooltipSupport') as TooltipSupport;
    if (support) {
      support.cancel($comp);
    }
  },

  /**
   * Convenient function to install tooltip support for ellipsis only.
   */
  installForEllipsis($comp: JQuery, options: InitModelOf<TooltipSupport>) {
    let defaultOptions = {
      text: $label => {
        if ($label.isContentTruncated()) {
          return $label.text();
        }
      },
      nativeTooltip: !Device.get().isCustomEllipsisTooltipPossible()
    };
    options = $.extend({}, defaultOptions, options);
    tooltips.install($comp, options);
  },

  /**
   * Finds every tooltip whose $anchor belongs to $context.
   */
  find($context: JQuery): Tooltip[] {
    let $tooltips, i, tooltip, tooltipArr = [];
    $tooltips = $('.tooltip', $context.document(true));

    for (i = 0; i < $tooltips.length; i++) {
      tooltip = $tooltips.eq(i).data('tooltip');
      if ($context.has(tooltip.$anchor).length > 0) {
        tooltipArr.push(tooltip);
      }
    }
    return tooltipArr;
  }
};
