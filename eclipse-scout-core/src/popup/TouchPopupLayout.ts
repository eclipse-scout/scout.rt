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
import {HtmlEnvironment, PopupLayout} from '../index';

export default class TouchPopupLayout extends PopupLayout {

  constructor(popup) {
    super(popup);
    this.doubleCalcPrefSize = false;
  }

  _setSize(prefSize) {
    super._setSize(prefSize);

    let htmlPopup = this.popup.htmlComp;
    let htmlBody = this.popup.htmlBody;
    let bodySize = prefSize.subtract(htmlPopup.insets());
    htmlBody.setSize(bodySize.subtract(htmlBody.margins()));
  }

  preferredLayoutSize($container, options) {
    let htmlComp = this.popup.htmlComp;
    let htmlBody = this.popup.htmlBody;

    let prefSize = htmlBody.prefSize(options)
      .add(htmlComp.insets());
    prefSize.width = HtmlEnvironment.get().formColumnWidth;
    return prefSize.add(htmlBody.margins());
  }
}
