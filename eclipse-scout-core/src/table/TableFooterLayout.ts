/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {AbstractLayout, graphics} from '../index';
import $ from 'jquery';

export default class TableFooterLayout extends AbstractLayout {

  constructor(tableFooter) {
    super();
    this._tableFooter = tableFooter;
  }

  /**
   * @override
   */
  layout($container) {
    let contentFits,
      $controls = this._tableFooter._$controls,
      $info = this._tableFooter._$info,
      $infoItems = $info.find('.table-info-item'),
      containerWidth = graphics.size($container).width;

    let controlsWidth = graphics.size($controls, true).width;
    let infoWidth = graphics.size($info, true).width;

    // Remove width to make sure elements are as width as they want to be
    $infoItems.each(function() {
      let $item = $(this);
      // Do not touch items which are being hidden to make sure they can properly animate width to 0
      if ($item.isVisible() && !$item.hasClass('hiding')) {
        $item.data('oldWidth', $item.outerWidth());
        $item.css('width', 'auto');
      }
    });
    $info.css('max-width', '');

    // Always try to use max space first
    if (this._tableFooter._compactStyle) {
      this._tableFooter._compactStyle = false;
      this._tableFooter._renderInfo();
    }
    infoWidth = graphics.size($info, true).width;
    if (controlsWidth + infoWidth <= containerWidth) {
      // Make sure table info tooltip is not shown anymore (only available in compact style)
      if (this._tableFooter._tableInfoTooltip) {
        this._tableFooter._tableInfoTooltip.destroy();
      }
      contentFits = true;
    }

    if (!contentFits) {
      // If elements don't fit, try to minimize table-info
      this._tableFooter._compactStyle = true;
      this._tableFooter._renderInfo();

      infoWidth = graphics.size($info, true).width;
      if (controlsWidth + infoWidth <= containerWidth) {
        contentFits = true;
      }
      // Make sure info section does not overlap controls
      $info.css('max-width', Math.max(containerWidth - controlsWidth - $info.cssMarginX(), 0));
    }

    // don't animate on the first layouting -> only animate on user interactions
    let animated = this._tableFooter.htmlComp.layouted;
    this._setInfoItemsSize($infoItems, animated);

    if (this._tableFooter._tableStatusTooltip && this._tableFooter._tableStatusTooltip.rendered) {
      this._tableFooter._tableStatusTooltip.position();
    }
    if (this._tableFooter._tableInfoTooltip && this._tableFooter._tableInfoTooltip.rendered) {
      this._tableFooter._tableInfoTooltip.position();
    }

    // Let table controls update their content according to the new footer size
    this._tableFooter.table.tableControls.forEach(control => {
      control.revalidateLayout();
    });
  }

  _setInfoItemsSize($infoItems, animated) {
    $infoItems.each(function() {
      let $item = $(this);
      if ($item.isVisible() && !$item.hasClass('hiding')) {
        // Make sure complete function of already scheduled animation will be executed
        let existingComplete = $item.data('animationComplete');
        if (animated) {
          $item.stop().cssWidthAnimated($item.data('oldWidth'), $item.outerWidth(), {
            complete: existingComplete
          });
          $item.removeData('oldWidth');
        } else {
          $item.cssWidth($item.outerWidth());
        }
      }
    });
  }
}
