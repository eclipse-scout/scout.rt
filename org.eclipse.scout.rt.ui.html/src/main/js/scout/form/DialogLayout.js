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
    htmlComp = this._form.htmlComp,
    dialogMargins = htmlComp.getMargins(),
    windowSize = $container.windowSize(),
    cacheBounds = this._form.readCacheBounds();

  if (cacheBounds) {
    dialogSize = cacheBounds.dimension();
    currentBounds = cacheBounds;
  } else {
    dialogSize = this.preferredLayoutSize($container);
    currentBounds = htmlComp.getBounds();
  }

  dialogSize = scout.DialogLayout.fitContainerInWindow(windowSize, currentBounds, dialogSize, dialogMargins);

  // Add markers to be able to style the dialog in a different way when it uses the full width or height
  $container
    .toggleClass('full-width', (currentBounds.x === 0 && dialogMargins.horizontal() === 0 && windowSize.width === dialogSize.width))
    .toggleClass('full-height', (currentBounds.y === 0 && dialogMargins.vertical() === 0 && windowSize.height === dialogSize.height));

  // Ensure the dialog can only get larger, not smaller.
  //  This prevents 'snapping' the dialog back to the calculated size when a field changes its visibility, but
  //  the user previously enlarged the dialog.
  //  This must not happen when the dialog is laid out the first time (-> when it is opened, because it has not the right size yet and may get too big)
  if (htmlComp.layouted) {
    dialogSize.width = Math.max(dialogSize.width, currentBounds.width);
    dialogSize.height = Math.max(dialogSize.height, currentBounds.height);
  }

  scout.graphics.setSize($container, dialogSize);
  scout.DialogLayout.parent.prototype.layout.call(this, $container);
};

/**
 * Returns the new container size, if the given containerSize is larger then the windowSize, the size will be adjusted.
 * This function has a side effect, as it modifies the given currentBounds by subtracting the container margins.
 *
 * @param $container
 * @returns {scout.Dimension} new, adjusted container size
 * @static
 */
scout.DialogLayout.fitContainerInWindow = function(windowSize, currentBounds, containerSize, containerMargins) {

  // Because prefSize does not include the dialog margins, we have to subtract them from the current size as well.
  // Because currentBounds.subtract() would also alter the x/y values, we subtract the dimensions manually.
  currentBounds.width -= containerMargins.horizontal();
  currentBounds.height -= containerMargins.vertical();

  // class .dialog may specify a margin
  // currentBounds.y and x are 0 initially, but if size changes while dialog is open they are greater than 0
  // This guarantees the dialog size may not exceed the document size
  var maxWidth = (windowSize.width - containerMargins.horizontal() - currentBounds.x);
  var maxHeight = (windowSize.height - containerMargins.vertical() - currentBounds.y);

  // Calculate new dialog size:
  // 1. Ensure the dialog is not larger than viewport
  containerSize.width = Math.min(maxWidth, containerSize.width);
  containerSize.height = Math.min(maxHeight, containerSize.height);

  return containerSize;
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
    containerSize = scout.HtmlComponent.get($container).getSize(),
    left = (windowSize.width - containerSize.width) / 2,
    top = (windowSize.height - containerSize.height) / 2,
    opticalMiddleOffset = Math.min(top / 5, 10);

  top -= opticalMiddleOffset;

  return new scout.Point(left, top);
};

