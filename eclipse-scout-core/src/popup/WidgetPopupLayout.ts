/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Dimension, HtmlCompPrefSizeOptions, PopupLayout, WidgetPopup} from '../index';

export class WidgetPopupLayout extends PopupLayout {

  declare popup: WidgetPopup;

  constructor(popup: WidgetPopup) {
    super(popup);
  }

  protected override _setSize(prefSize: Dimension) {
    super._setSize(prefSize);

    let htmlPopup = this.popup.htmlComp;
    let htmlWidget = this.popup.content.htmlComp;
    let widgetSize = prefSize.subtract(htmlPopup.insets());
    htmlWidget.setSize(widgetSize.subtract(htmlWidget.margins()));
  }

  override preferredLayoutSize($container: JQuery, options?: HtmlCompPrefSizeOptions): Dimension {
    let htmlComp = this.popup.htmlComp;
    let htmlWidget = this.popup.content.htmlComp;

    return htmlWidget.prefSize(options)
      .add(htmlComp.insets())
      .add(htmlWidget.margins());
  }
}
