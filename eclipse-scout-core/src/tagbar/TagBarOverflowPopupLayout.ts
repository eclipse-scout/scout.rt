import {graphics, PopupLayout} from '../index';
/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
export default class TagBarOverflowPopupLayout extends PopupLayout {

  preferredLayoutSize($container, options) {
    let prefSize = super.preferredLayoutSize($container, options);
    // Use the width of the body element that uses display: inline.
    // Only inline elements are as width as its content if the content wraps.
    // This is the only purpose of the body.
    prefSize.width = graphics.size(this.popup.$body, {exact: true}).width;
    prefSize.width += this.popup.htmlComp.insets().horizontal();
    return prefSize;
  }
}
