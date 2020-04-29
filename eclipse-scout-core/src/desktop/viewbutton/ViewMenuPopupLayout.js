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
import {PopupWithHeadLayout, ViewMenuPopup} from '../../index';

export default class ViewMenuPopupLayout extends PopupWithHeadLayout {

  constructor(popup) {
    super(popup);
  }

  preferredLayoutSize($container) {
    let prefSize = super.preferredLayoutSize($container);

    // Always use pref size if it is larger than view button box so that the menu items are fully readable
    if (prefSize.width >= this.popup.viewButtonBoxBounds.width) {
      return prefSize;
    }

    // Otherwise make popup as width as the view button box or MAX_MENU_WIDTH at max
    prefSize.width = Math.min(ViewMenuPopup.MAX_MENU_WIDTH, this.popup.viewButtonBoxBounds.width);
    return prefSize;
  }
}
