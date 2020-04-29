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
import {graphics, PopupWithHeadLayout} from '../index';

export default class FormMenuPopupLayout extends PopupWithHeadLayout {

  constructor(popup) {
    super(popup);
    this.popup = popup;
  }

  _setSize(prefSize) {
    let popupSize,
      htmlForm = this.popup.form.htmlComp;

    super._setSize(prefSize);

    popupSize = graphics.size(this.popup.$body);

    // set size of form
    popupSize = popupSize.subtract(graphics.insets(this.popup.$body));
    htmlForm.setSize(popupSize);
  }

  preferredLayoutSize($container) {
    let htmlComp = this.popup.htmlComp,
      htmlForm = this.popup.form.htmlComp,
      prefSize;

    prefSize = htmlForm.prefSize()
      .add(htmlComp.insets())
      .add(graphics.insets(this.popup.$body, true))
      .add(htmlForm.margins());

    return prefSize;
  }
}
