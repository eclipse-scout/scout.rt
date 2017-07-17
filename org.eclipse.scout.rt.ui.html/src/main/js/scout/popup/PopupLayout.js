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
scout.PopupLayout = function(popup) {
  scout.PopupLayout.parent.call(this);
  this.popup = popup;
};
scout.inherits(scout.PopupLayout, scout.AbstractLayout);

scout.PopupLayout.prototype.layout = function($container) {
  var popupSize,
    htmlComp = this.popup.htmlComp,
    prefSize = this.preferredLayoutSize($container);

  if (this.popup.boundToAnchor) {
    popupSize = this._adjustSizeWithAnchor(prefSize);
  } else {
    popupSize = this._adjustSize(prefSize);
  }

  scout.graphics.setSize(htmlComp.$comp, popupSize);
};

scout.PopupLayout.prototype._adjustSize = function(prefSize) {
  var popupSize = new scout.Dimension(),
    maxSize = this._calcMaxSize();

  // Ensure the popup is not larger than max size
  popupSize.width = Math.min(maxSize.width, prefSize.width);
  popupSize.height = Math.min(maxSize.height, prefSize.height);

  return popupSize;
};

/**
 * Considers window boundaries.
 *
 * @returns {scout.Dimension}
 */
scout.PopupLayout.prototype._calcMaxSize = function() {
  // Position the popup at the desired location before doing any calculations to consider the preferred bounds
  this.popup.position(false);

  var maxWidth, maxHeight,
    htmlComp = this.popup.htmlComp,
    windowPaddingX = this.popup.windowPaddingX,
    windowPaddingY = this.popup.windowPaddingY,
    popupMargins = htmlComp.margins(),
    popupPosition = htmlComp.location(),
    $window = this.popup.$container.window(),
    windowSize = new scout.Dimension($window.width(), $window.height());

  maxWidth = (windowSize.width - popupMargins.horizontal() - popupPosition.x - windowPaddingX);
  maxHeight = (windowSize.height - popupMargins.vertical() - popupPosition.y - windowPaddingY);

  return new scout.Dimension(maxWidth, maxHeight);
};

scout.PopupLayout.prototype._adjustSizeWithAnchor = function(prefSize) {
  var popupSize = new scout.Dimension(),
    maxSize = this._calcMaxSizeAroundAnchor();

  // Compared to $comp.height() and width(), $comp.offset() may return fractional values. This means the maxSizes may be fractional as well.
  // The popup sizes must be integers, otherwise reading the height/width later on might result in wrong calculations.
  // This is especially important for the position calculation.
  // Popup.position() uses popup.overlap(), if the popup height is lets say 90.5, overlapY would be 0.5 because height returned 91
  // -> the popup switches its direction unnecessarily
  maxSize = maxSize.floor();

  // Decide whether the prefSize can be used or the popup needs to be shrinked so that it fits into the viewport
  // The decision is based on the preferred opening direction
  // Example: The popup would like to be opened right and down
  // If there is enough space on the right and on the bottom -> pref size is used
  // If there is not enough space on the right it checks whether there is enough space on the left
  // If there is enough space on the left -> use preferred width -> The opening direction will be switched using position() at the end
  // If there is not enough space on the left as well, the greater width is used -> Position() will either switch the direction or not, depending on the size of the popup
  // The same happens for y direction if there is not enough space on the bottom
  popupSize.width = prefSize.width;
  if (this.popup.trimWidth) {
    if (prefSize.width > maxSize.right && prefSize.width > maxSize.left) {
      popupSize.width = Math.max(maxSize.right, maxSize.left);
    }
  }
  popupSize.height = prefSize.height;
  if (this.popup.trimHeight) {
    if (prefSize.height > maxSize.bottom && prefSize.height > maxSize.top) {
      popupSize.height = Math.max(maxSize.bottom, maxSize.top);
    }
  }
  return popupSize;
};

/**
 * Considers window boundaries.
 *
 * @returns {scout.Dimension}
 */
scout.PopupLayout.prototype._calcMaxSizeAroundAnchor = function() {
  var maxWidthLeft, maxWidthRight, maxHeightDown, maxHeightUp,
    htmlComp = this.popup.htmlComp,
    windowPaddingX = this.popup.windowPaddingX,
    windowPaddingY = this.popup.windowPaddingY,
    popupMargins = htmlComp.margins(),
    anchorBounds = this.popup.getAnchorBounds(),
    $window = this.popup.$container.window(),
    windowSize = new scout.Dimension($window.width(), $window.height());

  maxWidthRight = (windowSize.width - (anchorBounds.x + anchorBounds.width) - popupMargins.horizontal() - windowPaddingX);
  maxWidthLeft = (anchorBounds.x - popupMargins.horizontal() - windowPaddingX);
  maxHeightDown = (windowSize.height - (anchorBounds.y + anchorBounds.height) - popupMargins.vertical() - windowPaddingY);
  maxHeightUp = (anchorBounds.y - popupMargins.vertical() - windowPaddingY);

  return new scout.Insets(maxHeightUp, maxWidthRight, maxHeightDown, maxWidthLeft);
};
