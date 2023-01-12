/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Dimension, graphics, HtmlCompPrefSizeOptions, PopupLayout, TagBarOverflowPopup} from '../index';

export class TagBarOverflowPopupLayout extends PopupLayout {
  declare popup: TagBarOverflowPopup;

  override preferredLayoutSize($container: JQuery, options?: HtmlCompPrefSizeOptions): Dimension {
    let prefSize = super.preferredLayoutSize($container, options);
    // Use the width of the body element that uses display: inline.
    // Only inline elements are as width as its content if the content wraps.
    // This is the only purpose of the body.
    prefSize.width = graphics.size(this.popup.$body, {exact: true}).width;
    prefSize.width += this.popup.htmlComp.insets().horizontal();
    return prefSize;
  }
}
