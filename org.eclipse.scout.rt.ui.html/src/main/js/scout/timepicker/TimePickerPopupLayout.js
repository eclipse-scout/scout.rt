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
scout.TimePickerPopupLayout = function(popup) {
  scout.TimePickerPopupLayout.parent.call(this);
  this.popup = popup;
};
scout.inherits(scout.TimePickerPopupLayout, scout.PopupLayout);

scout.TimePickerPopupLayout.prototype.layout = function($container) {
  var size,
    htmlComp = this.popup.htmlComp,
    htmlPicker = this.popup.picker.htmlComp;

  scout.TimePickerPopupLayout.parent.prototype.layout.call(this, $container);

  size = htmlComp.getSize()
    .subtract(htmlComp.getInsets())
    .subtract(htmlPicker.getMargins());

  htmlPicker.setSize(size);

  // Reposition because opening direction may have to be switched if popup gets bigger
  // Don't do it the first time (will be done by popup.open), only if the popup is already open and gets layouted again
  if (this.popup.htmlComp.layouted) {
    this.popup.position();
  }
};

scout.TimePickerPopupLayout.prototype.preferredLayoutSize = function($container) {
  var prefSize,
    htmlComp = this.popup.htmlComp,
    htmlPicker = this.popup.picker.htmlComp;

  prefSize = htmlPicker.getPreferredSize()
    .add(htmlComp.getInsets())
    .add(htmlPicker.getMargins());

  prefSize.height = Math.max(15, Math.min(350, prefSize.height)); // at least some pixels height in case there is no data, no status, no active filter
  return prefSize;
};
