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
scout.ViewMenuPopupLayout = function(popup) {
  scout.ViewMenuPopupLayout.parent.call(this, popup);
};
scout.inherits(scout.ViewMenuPopupLayout, scout.PopupWithHeadLayout);

scout.ViewMenuPopupLayout.prototype.preferredLayoutSize = function($container) {
  var prefSize = scout.ViewMenuPopupLayout.parent.prototype.preferredLayoutSize.call(this, $container);

  // The actual width may be fractional, but the width returned by prefSize is always an int
  // Add 1 px to prevent ellipsis
  // TODO CGU 6.1 consider using getBoundingClientRect which returns float values in graphics.getSize
  // This is not true for IE 9, but it seems that IE uses ceil() compared to chrome which uses floor() (needs verification if really true).
  // prefSize also uses scrollWidth. Unfortunately there is no float alternative for that, at least as far as I know.
  // Maybe we could remove scrollWidth from prefSize, it was introduced for context menus which don't need it anymore
  prefSize.width += 1;

  // Always use pref size if it is larger than view button box so that the menu items are fully readable
  if (prefSize.width >= this.popup.viewButtonBoxBounds.width) {
    return prefSize;
  }

  // Otherwise make popup as width as the view button box or MAX_MENU_WIDTH at max
  prefSize.width = Math.min(scout.ViewMenuPopup.MAX_MENU_WIDTH, this.popup.viewButtonBoxBounds.width);
  return prefSize;
};
