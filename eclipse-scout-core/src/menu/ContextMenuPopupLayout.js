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
import {HtmlComponent, PopupWithHeadLayout} from '../index';
import $ from 'jquery';

export default class ContextMenuPopupLayout extends PopupWithHeadLayout {

  constructor(popup) {
    super(popup);
  }

  layout($container) {
    let $menuItems = this.popup.$visibleMenuItems();
    this._adjustTextAlignment($menuItems);
    this._resetMaxWidthFor($menuItems);
    super.layout($container);
    this._setMaxWidthFor($menuItems);
  }

  _adjustTextAlignment($menuItems) {
    // Calculate the text offset (= max icon width)
    let textOffset = 0;
    $menuItems.each((index, menuItem) => {
      let $menuItem = $(menuItem);
      let $icon = $menuItem.children('.icon');
      let iconWidth = 0;

      if ($icon.length > 0) {
        iconWidth = $icon.outerWidth(true);
      }
      textOffset = Math.max(textOffset, iconWidth);
    });

    // Update the padding of each text such that the sum of icon width and the padding
    // are the same for all items. This ensures that the texts are all aligned.
    $menuItems.each((index, menuItem) => {
      let $menuItem = $(menuItem);
      let $icon = $menuItem.children('.icon');
      let $text = $menuItem.children('.text');
      let iconWidth = 0;

      if ($icon.length > 0) {
        iconWidth = $icon.outerWidth(true);
      }
      $text.css('padding-left', textOffset - iconWidth);
      let htmlComp = HtmlComponent.optGet($menuItem);
      if (htmlComp) {
        htmlComp.invalidateLayout();
      }
    });
  }

  _resetMaxWidthFor($menuItems) {
    $menuItems.each((pos, item) => {
      let $menu = $(item),
        menu = $menu.data('widget');

      if (!menu) {
        // After closing a submenu the link to the widget gets lost
        return;
      }

      if (menu.$text) {
        menu.$text.css('max-width', '');
      }
    });
  }

  _setMaxWidthFor($menuItems) {
    $menuItems.each((pos, item) => {
      let $menu = $(item),
        menu = $menu.data('widget');

      if (!menu) {
        // After closing a submenu the link to the widget gets lost
        return;
      }

      if (menu.$text) {
        // Submenu icon is on the right side of the text.
        // If there is not enough space to show the whole menu item (icon, text and submenu icon), the text is truncated.
        // Icon and submenu icon are always shown.
        let textMaxWidth = this._calcTextMaxWidth(menu);
        menu.$text.cssPxValue('max-width', textMaxWidth);
      }
    });
  }

  _calcTextMaxWidth(menu) {
    let containerWidth = menu.$container.width(),
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
