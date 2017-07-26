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
scout.DialogLayout = function(form) {
  scout.DialogLayout.parent.call(this, form);
  this.autoSize = true;
};
scout.inherits(scout.DialogLayout, scout.FormLayout);

scout.DialogLayout.prototype.layout = function($container) {
  if (!this.autoSize) {
    scout.DialogLayout.parent.prototype.layout.call(this, $container);
    return;
  }

  var dialogSize, currentBounds,
    htmlComp = this.form.htmlComp,
    dialogMargins = htmlComp.margins(),
    windowSize = $container.windowSize(),
    cacheBounds = this.form.readCacheBounds();

  if (cacheBounds) {
    dialogSize = cacheBounds.dimension();
    currentBounds = cacheBounds;
  } else {
    dialogSize = this.preferredLayoutSize($container);
    currentBounds = htmlComp.offsetBounds(true);
  }

  dialogSize = scout.DialogLayout.fitContainerInWindow(windowSize, currentBounds.point(), dialogSize, dialogMargins);

  // Add markers to be able to style the dialog in a different way when it uses the full width or height
  $container
    .toggleClass('full-width', (currentBounds.x === 0 && dialogMargins.horizontal() === 0 && windowSize.width === dialogSize.width))
    .toggleClass('full-height', (currentBounds.y === 0 && dialogMargins.vertical() === 0 && windowSize.height === dialogSize.height));

  // Ensure the dialog can only get larger, not smaller.
  // This prevents 'snapping' the dialog back to the calculated size when a field changes its visibility, but the user previously enlarged the dialog.
  // This must not happen when the dialog is laid out the first time (-> when it is opened, because it has not the right size yet and may get too big)
  if (htmlComp.layouted) {
    dialogSize.width = Math.max(dialogSize.width, currentBounds.width - dialogMargins.horizontal());
    dialogSize.height = Math.max(dialogSize.height, currentBounds.height - dialogMargins.vertical());
  }

  scout.graphics.setSize($container, dialogSize);
  scout.DialogLayout.parent.prototype.layout.call(this, $container);
};

/**
 * Calculates the new container size and position. If the given containerSize is larger then the windowSize, the size will be adjusted.
 *
 * @param windowSize total size of the window
 * @param containerPosition {scout.Point} current position of the container
 * @param containerSize {scout.Dimension} preferred size of container (excluding margins)
 * @param containerMargins {scout.Insets} margins of the container
 * @returns {scout.Dimension} the new, adjusted container size (excluding margins)
 * @static
 */
scout.DialogLayout.fitContainerInWindow = function(windowSize, containerPosition, containerSize, containerMargins) {
  // class .dialog may specify a margin
  // currentBounds.y and x are 0 initially, but if size changes while dialog is open they are greater than 0
  // This guarantees the dialog size may not exceed the document size
  var maxWidth = (windowSize.width - containerMargins.horizontal() - containerPosition.x);
  var maxHeight = (windowSize.height - containerMargins.vertical() - containerPosition.y);

  // Calculate new dialog size, ensuring that the dialog is not larger than container
  var size = new scout.Dimension();
  size.width = Math.min(maxWidth, containerSize.width);
  size.height = Math.min(maxHeight, containerSize.height);

  return size;
};

/**
 * Returns the coordinates to place the given container in the optical middle of the window.
 *
 * @param $container
 * @returns {scout.Point} new X,Y position of the container
 * @static
 */
scout.DialogLayout.positionContainerInWindow = function($container) {
  var
    windowSize = $container.windowSize(),
    containerSize = scout.HtmlComponent.get($container).size(true),
    left = (windowSize.width - containerSize.width) / 2,
    top = (windowSize.height - containerSize.height) / 2;

  // optical middle (move up 20% of distance between window and dialog)
  var opticalMiddleOffset = (top / 5);
  top -= opticalMiddleOffset;

  // Ensure integer numbers
  left = Math.floor(left);
  top = Math.floor(top);

  return new scout.Point(left, top);
};
