/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.DatePickerPopupLayout = function(popup) {
  scout.DatePickerPopupLayout.parent.call(this);
  this.popup = popup;
};
scout.inherits(scout.DatePickerPopupLayout, scout.PopupLayout);

scout.DatePickerPopupLayout.prototype.layout = function($container) {
  var size,
    htmlComp = this.popup.htmlComp,
    htmlPicker = this.popup.picker.htmlComp;

  scout.DatePickerPopupLayout.parent.prototype.layout.call(this, $container);

  size = htmlComp.size()
    .subtract(htmlComp.insets())
    .subtract(htmlPicker.margins());

  htmlPicker.setSize(size);

  // Reposition because opening direction may have to be switched if popup gets bigger
  // Don't do it the first time (will be done by popup.open), only if the popup is already open and gets layouted again
  if (this.popup.htmlComp.layouted) {
    this.popup.position();
  }
};

scout.DatePickerPopupLayout.prototype.preferredLayoutSize = function($container) {
  var prefSize,
    htmlComp = this.popup.htmlComp,
    htmlPicker = this.popup.picker.htmlComp;

  prefSize = htmlPicker.prefSize()
    .add(htmlComp.insets())
    .add(htmlPicker.margins());

  return prefSize;
};
