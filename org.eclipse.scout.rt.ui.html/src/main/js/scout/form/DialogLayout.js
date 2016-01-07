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

  // class .dialog may specify a margin
  // currentBounds.y and x are 0 initially, but if size changes while dialog is open they are greater than 0
  // This guarantees the dialog size may not exceed the document size
  var maxWidth = (windowSize.width - currentBounds.x - dialogMargins.horizontal());
  var maxHeight = (windowSize.height - currentBounds.y - dialogMargins.vertical());

  // Ensure the dialog is not larger than viewport
  dialogSize.width = Math.min(maxWidth, prefSize.width);
  dialogSize.height = Math.min(maxHeight, prefSize.height);

  // Only resize if height changes.
  // This makes sure the dialog won't change its size if a field changes its visibility AND if the user manually changed the width of the dialog.
  if (currentBounds.height === dialogSize.height) {
    scout.DialogLayout.parent.prototype.layout.call(this, $container);
    return;
  }

  scout.graphics.setSize(htmlComp.$comp, dialogSize);
  scout.DialogLayout.parent.prototype.layout.call(this, $container);
};
