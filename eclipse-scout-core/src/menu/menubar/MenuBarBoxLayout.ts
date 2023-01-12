/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AbstractLayout, Dimension, HtmlCompPrefSizeOptions, MenuBarBox} from '../../index';

export class MenuBarBoxLayout extends AbstractLayout {
  menubox: MenuBarBox;

  constructor(menubox: MenuBarBox) {
    super();
    this.menubox = menubox;
  }

  override layout($container: JQuery) {
    // void since the menu items are floated inline block.
  }

  override preferredLayoutSize($container: JQuery, options?: HtmlCompPrefSizeOptions): Dimension {
    let menuItemSize = null;

    return this.menubox.menuItems
      .filter(menuItem => !menuItem.overflown && menuItem.isVisible())
      .reduce((prefSize, menuItem) => {
        menuItemSize = menuItem.htmlComp.prefSize({
          useCssSize: true,
          includeMargin: true
        });
        prefSize.height = Math.max(prefSize.height, menuItemSize.height);
        prefSize.width = Math.max(prefSize.width, menuItemSize.width);
        return prefSize;
      }, new Dimension());
  }
}
