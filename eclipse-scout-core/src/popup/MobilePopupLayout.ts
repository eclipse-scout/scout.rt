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
import {Dimension, graphics, HtmlCompPrefSizeOptions, MobilePopup, Point, PopupLayout} from '../index';

export default class MobilePopupLayout extends PopupLayout {

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
