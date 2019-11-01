/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {PopupWithHeadLayout} from '../index';
import {HtmlComponent} from '../index';
import * as $ from 'jquery';

export default class ContextMenuPopupLayout extends PopupWithHeadLayout {

constructor(popup) {
  super( popup);
}


layout($container) {
  var $menuItems = this.popup.$visibleMenuItems();
  this._adjustTextAlignment($menuItems);
  this._resetMaxWidthFor($menuItems);
  super.layout( $container);
  this._setMaxWidthFor($menuItems);
}

_adjustTextAlignment($menuItems) {
  // Calculate the text offset (= max icon width)
  var textOffset = 0;
  $menuItems.each(function(index, menuItem) {
    var $menuItem = $(menuItem);
    var $icon = $menuItem.children('.icon');
    var iconWidth = 0;

    if ($icon.length > 0) {
      iconWidth = $icon.outerWidth(true);
    }
    textOffset = Math.max(textOffset, iconWidth);
  });

  // Update the padding of each text such that the sum of icon width and the padding
  // are the same for all items. This ensures that the texts are all aligned.
  $menuItems.each(function(index, menuItem) {
    var $menuItem = $(menuItem);
    var $icon = $menuItem.children('.icon');
    var $text = $menuItem.children('.text');
    var iconWidth = 0;

    if ($icon.length > 0) {
      iconWidth = $icon.outerWidth(true);
    }
    $text.css('padding-left', textOffset - iconWidth);
    var htmlComp = HtmlComponent.optGet($menuItem);
    if (htmlComp) {
      htmlComp.invalidateLayout();
    }
  });
}

_resetMaxWidthFor($menuItems) {
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
}

_setMaxWidthFor($menuItems) {
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
}

_calcTextMaxWidth(menu) {
  var containerWidth = menu.$container.width(),
    $icon = menu.get$Icon(),
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
}
}
