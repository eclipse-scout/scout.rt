/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Dimension, graphics, HtmlCompPrefSizeOptions, MobilePopup, Point, PopupLayout} from '../index';

export class MobilePopupLayout extends PopupLayout {

  declare popup: MobilePopup;

  constructor(popup: MobilePopup) {
    super(popup);
    this.doubleCalcPrefSize = false;
  }

  override layout($container: JQuery) {
    super.layout($container);

    let htmlPopup = this.popup.htmlComp,
      popupSize = htmlPopup.size(),
      htmlWidget = this.popup.content.htmlComp,
      widgetSize,
      $header = this.popup.$header,
      headerSize;

    popupSize = popupSize.subtract(htmlPopup.insets());
    headerSize = graphics.prefSize($header, true);
    graphics.setLocation($header, new Point(0, 0));

    widgetSize = popupSize.clone();
    widgetSize.height -= headerSize.height;
    htmlWidget.setLocation(new Point(0, headerSize.height));
    htmlWidget.setSize(widgetSize.subtract(htmlWidget.margins()));
  }

  override preferredLayoutSize($container: JQuery, options?: HtmlCompPrefSizeOptions): Dimension {
    let $window = this.popup.$container.window();
    return new Dimension($window.width(), $window.height());
  }
}
