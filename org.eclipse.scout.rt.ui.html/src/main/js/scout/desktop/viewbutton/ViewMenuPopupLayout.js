/*******************************************************************************
 * Copyright (c) 2014-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.ViewMenuPopupLayout = function(popup) {
  scout.ViewMenuPopupLayout.parent.call(this, popup);
};
scout.inherits(scout.ViewMenuPopupLayout, scout.PopupWithHeadLayout);

scout.ViewMenuPopupLayout.prototype.preferredLayoutSize = function($container) {
  var prefSize = scout.ViewMenuPopupLayout.parent.prototype.preferredLayoutSize.call(this, $container);

  // Always use pref size if it is larger than view button box so that the menu items are fully readable
  if (prefSize.width >= this.popup.viewButtonBoxBounds.width) {
    return prefSize;
  }

  // Otherwise make popup as width as the view button box or MAX_MENU_WIDTH at max
  prefSize.width = Math.min(scout.ViewMenuPopup.MAX_MENU_WIDTH, this.popup.viewButtonBoxBounds.width);
  return prefSize;
};
