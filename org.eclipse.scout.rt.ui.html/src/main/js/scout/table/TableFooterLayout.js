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
scout.TableFooterLayout = function(tableFooter) {
  scout.TableFooterLayout.parent.call(this);

  this._tableFooter = tableFooter;
};
scout.inherits(scout.TableFooterLayout, scout.AbstractLayout);

/**
 * @override
 */
scout.TableFooterLayout.prototype.layout = function($container) {
  var contentFits, controlsWidth, infoWidth,
    $controls = this._tableFooter._$controls,
    $info = this._tableFooter._$info,
    $infoItems = $info.find('.table-info-item'),
    containerWidth = scout.graphics.getSize($container).width;

  controlsWidth = scout.graphics.getSize($controls).width;
  infoWidth = scout.graphics.getSize($info).width;

  // Remove width to make sure elements are as width as they want to be
  $infoItems.each(function() {
    var $item = $(this);
    // Do not touch items which are being hidden to make sure they can properly animate width to 0
    if ($item.isVisible() && !$item.data('hiding')) {
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
  infoWidth = scout.graphics.getSize($info).width;
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

    infoWidth = scout.graphics.getSize($info).width;
    if (controlsWidth + infoWidth <= containerWidth) {
      contentFits = true;
    }
    // Make sure info section does not overlap controls
    $info.css('max-width', Math.max(containerWidth - controlsWidth, 0));
  }

  // don't animate on the first layouting -> only animate on user interactions
  var animated = this._tableFooter.htmlComp.layouted;
  this._setInfoItemsSize($infoItems, animated);

  if (this._tableFooter._tableStatusTooltip && this._tableFooter._tableStatusTooltip.rendered) {
    this._tableFooter._tableStatusTooltip.position();
  }
  if (this._tableFooter._tableInfoTooltip && this._tableFooter._tableInfoTooltip.rendered) {
    this._tableFooter._tableInfoTooltip.position();
  }

  // Let table controls update their content according to the new footer size
  this._tableFooter.table.tableControls.forEach(function(control) {
    control.revalidateLayout();
  });
};

scout.TableFooterLayout.prototype._setInfoItemsSize = function($infoItems, animated) {
  $infoItems.each(function() {
    var $item = $(this);
    if ($item.isVisible() && !$item.data('hiding')) {
      // Make sure complete function of already scheduled animation will be executed
      var existingComplete = $item.data('animationComplete');
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
};
