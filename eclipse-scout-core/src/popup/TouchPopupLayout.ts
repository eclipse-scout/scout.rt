/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
