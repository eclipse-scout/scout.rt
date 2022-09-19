/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {AbstractLayout, Dimension} from '../../index';

export default class MenuBarBoxLayout extends AbstractLayout {

  constructor(menubox) {
    super();
    this.menubox = menubox;
  }

  layout($container) {
    // void since the menu items are floated inline block.
  }

  preferredLayoutSize($container, options) {
    let menuItemSize = null;

    return this.menubox.menuItems.filter(menuItem => {
      return !menuItem.overflown && menuItem.isVisible();
    }).reduce((prefSize, menuItem) => {
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
