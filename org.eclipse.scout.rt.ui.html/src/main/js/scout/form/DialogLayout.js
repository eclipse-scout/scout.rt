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

  var htmlComp = this._form.htmlComp,
    $window = this._form.$container.window(),
    dialogMargins = htmlComp.getMargins(),
    windowSize = new scout.Dimension($window.width(), $window.height()),
    dialogSize = new scout.Dimension(),
    currentBounds = htmlComp.getBounds(),
    prefSize = this.preferredLayoutSize($container);

  // Because prefSize does not include the dialog margins, we have to subtract them from the current size as well.
  // Because currentBounds.subtract() would also alter the x/y values, we subtract the dimensions manually.
  currentBounds.width -= dialogMargins.horizontal();
  currentBounds.height -= dialogMargins.vertical();

  // class .dialog may specify a margin
  // currentBounds.y and x are 0 initially, but if size changes while dialog is open they are greater than 0
  // This guarantees the dialog size may not exceed the document size
  var maxWidth = (windowSize.width - dialogMargins.horizontal() - currentBounds.x);
  var maxHeight = (windowSize.height - dialogMargins.vertical() - currentBounds.y);

  // Calculate new dialog size:
  // 1. Ensure the dialog is not larger than viewport
  dialogSize.width = Math.min(maxWidth, prefSize.width);
  dialogSize.height = Math.min(maxHeight, prefSize.height);

  // Add markers to be able to style the dialog in a different way when it uses the full width or height
  htmlComp.$comp.toggleClass('full-width', (currentBounds.x === 0 && dialogMargins.horizontal() === 0 && windowSize.width === dialogSize.width));
  htmlComp.$comp.toggleClass('full-height', (currentBounds.y === 0 && dialogMargins.vertical() === 0 && windowSize.height === dialogSize.height));

  // 2. Ensure the dialog can only get larger, not smaller.
  //    This prevents 'snapping' the dialog back to the calculated size when a field changes its visibility, but
  //    the user previously enlarged the dialog.
  //    This must not happen when the dialog is layouted the first time (-> when it is opened, because it has not the right size yet and may get too big)
  if (htmlComp.layouted) {
    dialogSize.width = Math.max(dialogSize.width, currentBounds.width);
    dialogSize.height = Math.max(dialogSize.height, currentBounds.height);
  }

  scout.graphics.setSize(htmlComp.$comp, dialogSize);
  scout.DialogLayout.parent.prototype.layout.call(this, $container);
};
