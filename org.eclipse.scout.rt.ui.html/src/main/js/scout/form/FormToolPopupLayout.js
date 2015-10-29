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
scout.FormToolPopupLayout = function(popup) {
  scout.FormToolPopupLayout.parent.call(this, popup);
  this.popup = popup;
};
scout.inherits(scout.FormToolPopupLayout, scout.PopupWithHeadLayout);

scout.FormToolPopupLayout.prototype.layout = function($container) {
  var popupSize,
    htmlForm = this.popup.form.htmlComp;

  scout.FormToolPopupLayout.parent.prototype.layout.call(this, $container);

  popupSize = scout.graphics.getSize(this.popup.$body);

  // set size of form
  popupSize = popupSize.subtract(scout.graphics.getInsets(this.popup.$body));
  htmlForm.setSize(popupSize);
};

scout.FormToolPopupLayout.prototype.preferredLayoutSize = function($container) {
  var htmlComp = this.popup.htmlComp,
    htmlForm = this.popup.form.htmlComp,
    prefSize;

  prefSize = htmlForm.getPreferredSize()
    .add(htmlComp.getInsets())
    .add(scout.graphics.getInsets(this.popup.$body, {
      includeMargin: true
    }))
    .add(htmlForm.getMargins());

  return prefSize;
};
