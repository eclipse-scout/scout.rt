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
scout.ContextMenuPopupLayout = function(popup) {
  scout.ContextMenuPopupLayout.parent.call(this, popup);
};
scout.inherits(scout.ContextMenuPopupLayout, scout.PopupWithHeadLayout);

scout.ContextMenuPopupLayout.prototype.layout = function($container) {
  var $menuItems = this.popup.$menuItems();
  this._resetMaxWidthFor($menuItems);
  scout.ContextMenuPopupLayout.parent.prototype.layout.call(this, $container);
  this._setMaxWidthFor($menuItems);
};

scout.ContextMenuPopupLayout.prototype._resetMaxWidthFor = function($menuItems) {
  $menuItems.each(function(pos, item) {
    var $menu = $(item),
      menu = $menu.data('widget');

    if (!menu) {
      // After closing a submenu the link to the widget gets lost
      return;
    }

    if (menu.$text) {
      menu.$text.css('max-width', '');
    }
  }.bind(this));
};

scout.ContextMenuPopupLayout.prototype._setMaxWidthFor = function($menuItems) {
  $menuItems.each(function(pos, item) {
    var $menu = $(item),
      menu = $menu.data('widget');

    if (!menu) {
      // After closing a submenu the link to the widget gets lost
      return;
    }

    if (menu.$text) {
      // Submenu icon is on the right side of the text.
      // If there is not enough space to show the whole menu item (icon, text and submenu icon), the text is truncated.
      // Icon and submenu icon are always shown.
      var textMaxWidth = this._calcTextMaxWidth(menu);
      menu.$text.cssPxValue('max-width', textMaxWidth);
    }
  }.bind(this));
};

scout.ContextMenuPopupLayout.prototype._calcTextMaxWidth = function(menu) {
  var containerWidth = menu.$container.width(),
    $icon = menu.$container.data('$icon'),
    $text = menu.$text,
    $submenuIcon = menu.$submenuIcon,
    textWidth = containerWidth + 1; // add 1px to make it work even if containerWidth is a float

  if ($text && $text.isVisible()) {
    textWidth -= $text.cssMarginX();
  }
  if ($icon && $icon.isVisible()) {
    textWidth -= $icon.outerWidth(true);
  }
  if ($submenuIcon && $submenuIcon.isVisible()) {
    textWidth -= $submenuIcon.outerWidth(true);
  }
  return textWidth;
};
