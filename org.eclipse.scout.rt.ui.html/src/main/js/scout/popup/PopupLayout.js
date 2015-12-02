/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.PopupLayout = function(popup) {
  scout.PopupLayout.parent.call(this, popup);
  this.popup = popup;
};
scout.inherits(scout.PopupLayout, scout.AbstractLayout);

scout.PopupLayout.prototype.layout = function($container) {
  var htmlComp = this.popup.htmlComp,
  popupSize = new scout.Dimension(),
  prefSize = this.preferredLayoutSize($container),
  maxSizes = this._calcMaxSizes();
  // Decide whether the prefSize can be used or the popup needs to be shrinked so that it fits into the viewport
  // The decision is based on the preferred opening direction
  // Example: The popup would like to be opened right and down
  // If there is enough space on the right and on the bottom -> pref size is used
  // If there is not enough space on the right it checks whether there is enough space on the left
  // If there is enough space on the left -> use preferred width -> The opening direction will be switched using position() at the end
  // If there is not enough space on the left as well, the greater width is used -> Position() will either switch the direction or not, depending on the size of the popup
  // The same happens for y direction if there is not enough space on the bottom
    if (this.popup.openingDirectionX === 'right' &&
      prefSize.width > maxSizes.right && prefSize.width > maxSizes.left) {
      popupSize.width = Math.max(maxSizes.right, maxSizes.left);
    } else if (this.popup.openingDirectionX === 'left' &&
      prefSize.width > maxSizes.left && prefSize.width > maxSizes.right) {
      popupSize.width = Math.max(maxSizes.right, maxSizes.left);
    } else {
      popupSize.width = prefSize.width;
    }
    if (this.popup.openingDirectionY === 'down' &&
      prefSize.height > maxSizes.bottom && prefSize.height > maxSizes.top) {
      popupSize.height = Math.max(maxSizes.bottom, maxSizes.top);
    } else if (this.popup.openingDirectionY === 'up' &&
      prefSize.height > maxSizes.top && prefSize.height > maxSizes.bottom) {
      popupSize.height = Math.max(maxSizes.bottom, maxSizes.top);
    } else {
      popupSize.height = prefSize.height;
    }

    scout.graphics.setSize(htmlComp.$comp, popupSize);
};

/**
 * Calculates the available space around the anchor.
 *
 * @returns {scout.Insets}
 */
scout.PopupLayout.prototype._calcMaxSizes = function() {
  var maxWidthLeft, maxWidthRight, maxHeightDown, maxHeightUp,
    htmlComp = this.popup.htmlComp,
    windowPaddingX = this.popup.windowPaddingX,
    windowPaddingY = this.popup.windowPaddingY,
    popupMargins = htmlComp.getMargins(),
    anchorBounds = this.popup.getAnchorBounds(),
    $window = this.popup.$container.window(),
    windowSize = new scout.Dimension($window.width(), $window.height());

  maxWidthRight = (windowSize.width - (anchorBounds.x + anchorBounds.width) - popupMargins.horizontal() - windowPaddingX);
  maxWidthLeft = (anchorBounds.x - popupMargins.horizontal() - windowPaddingX);
  maxHeightDown = (windowSize.height - (anchorBounds.y + anchorBounds.height) - popupMargins.vertical() - windowPaddingY);
  maxHeightUp = (anchorBounds.y - popupMargins.vertical() - windowPaddingY);

  return new scout.Insets(maxHeightUp, maxWidthRight, maxHeightDown, maxWidthLeft);
};
