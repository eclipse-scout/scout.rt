scout.FormToolPopupLayout = function(popup) {
  scout.FormToolPopupLayout.parent.call(this, popup);
  this.popup = popup;
};
scout.inherits(scout.FormToolPopupLayout, scout.AbstractLayout);

scout.FormToolPopupLayout.prototype.layout = function($container) {
  var maxWidthLeft, maxWidthRight, maxHeightDown, maxHeightUp,
    htmlComp = this.popup.htmlComp,
    htmlForm = this.popup.form.htmlComp,
    windowPaddingX = this.popup.windowPaddingX,
    windowPaddingY = this.popup.windowPaddingY,
    popupMargins = htmlComp.getMargins(),
    popupBounds = htmlComp.getBounds(),
    popupSize = new scout.Dimension(),
    prefSize = this.preferredLayoutSize($container),
    $window = $(window),
    windowSize = new scout.Dimension($window.width(), $window.height());

  maxWidthRight = (windowSize.width - popupBounds.x - popupMargins.left - popupMargins.right - windowPaddingX);
  maxWidthLeft = (popupBounds.x + popupBounds.width - popupMargins.left - popupMargins.right - windowPaddingX);
  maxHeightDown = (windowSize.height - popupBounds.y - popupMargins.top - popupMargins.bottom - windowPaddingY);
  maxHeightUp = (popupBounds.y + popupBounds.height - popupMargins.top - popupMargins.bottom - windowPaddingY);

  // Decide whether the prefSize can be used or the popup needs to be shrinked so that it fits into the viewport
  // The decision is based on the preferred opening direction
  // Example: The popup would like to be opened right and down
  // If there is enough space on the right and on the bottom -> pref size is used
  // If there is not enough space on the right it checks whether there is enough space on the left
  // If there is enough space on the left -> use preferred width -> The opening direction will be switched using position() at the end
  // If there is not enough space on the left as well, the greater width is used -> Position() will either switch the direction or not, depending on the size of the popup
  // The same happens for y direction if there is not enough space on the bottom
  if (this.popup.openingDirectionX === 'right' &&
    prefSize.width > maxWidthRight && prefSize.width > maxWidthLeft) {
    popupSize.width = Math.max(maxWidthRight, maxWidthLeft);
  } else if (this.popup.openingDirectionX === 'left' &&
    prefSize.width > maxWidthLeft && prefSize.width > maxWidthRight) {
    popupSize.width = Math.max(maxWidthRight, maxWidthLeft);
  } else {
    popupSize.width = prefSize.width;
  }
  if (this.popup.openingDirectionY === 'down' &&
    prefSize.height > maxHeightDown && prefSize.height > maxHeightUp) {
    popupSize.height = Math.max(maxHeightDown, maxHeightUp);
  } else if (this.popup.openingDirectionY === 'up' &&
    prefSize.height > maxHeightUp && prefSize.height > maxHeightDown) {
    popupSize.height = Math.max(maxHeightDown, maxHeightUp);
  } else {
    popupSize.height = prefSize.height;
  }

  // Set size of container
  scout.graphics.setSize(htmlComp.$comp, popupSize);

  // Set size of body
  popupSize = popupSize.subtract(htmlComp.getInsets());
  scout.graphics.setSize(this.popup.$body, popupSize);

  // set size of form
  popupSize = popupSize.subtract(scout.graphics.getInsets(this.popup.$body));
  htmlForm.setSize(popupSize);

  // Reposition because opening direction may have to be switched if popup gets bigger
  this.popup.position();
};

scout.FormToolPopupLayout.prototype.preferredLayoutSize = function($container) {
  var htmlComp = this.popup.htmlComp,
    htmlForm = this.popup.form.htmlComp,
    prefSize;

  prefSize = htmlForm.getPreferredSize()
    .add(htmlComp.getInsets())
    .add(scout.graphics.getInsets(this.popup.$body, {includeMargin: true}))
    .add(htmlForm.getMargins());

  return prefSize;
};
