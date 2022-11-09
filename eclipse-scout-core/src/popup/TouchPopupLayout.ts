/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Dimension, HtmlCompPrefSizeOptions, HtmlEnvironment, PopupLayout, TouchPopup} from '../index';

export class TouchPopupLayout extends PopupLayout {

  declare popup: TouchPopup;

  constructor(popup: TouchPopup) {
    super(popup);
    this.doubleCalcPrefSize = false;
  }

  protected override _setSize(prefSize: Dimension) {
    super._setSize(prefSize);

    let htmlPopup = this.popup.htmlComp;
    let htmlBody = this.popup.htmlBody;
    let bodySize = prefSize.subtract(htmlPopup.insets());
    htmlBody.setSize(bodySize.subtract(htmlBody.margins()));
  }

  override preferredLayoutSize($container: JQuery, options?: HtmlCompPrefSizeOptions): Dimension {
    let htmlComp = this.popup.htmlComp;
    let htmlBody = this.popup.htmlBody;

    let prefSize = htmlBody.prefSize(options)
      .add(htmlComp.insets());
    prefSize.width = HtmlEnvironment.get().formColumnWidth;
    return prefSize.add(htmlBody.margins());
  }
}
