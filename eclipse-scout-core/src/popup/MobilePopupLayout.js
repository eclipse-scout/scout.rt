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
import {Dimension, graphics, Point, PopupLayout} from '../index';

export default class MobilePopupLayout extends PopupLayout {

  constructor(popup) {
    super(popup);
    this.doubleCalcPrefSize = false;
  }

  layout($container) {
    super.layout($container);

    let htmlPopup = this.popup.htmlComp,
      popupSize = htmlPopup.size(),
      htmlWidget = this.popup.widget.htmlComp,
      widgetSize = 0,
      $header = this.popup.$header,
      headerSize = 0;

    popupSize = popupSize.subtract(htmlPopup.insets());
    headerSize = graphics.prefSize($header, true);
    graphics.setLocation($header, new Point(0, 0));

    widgetSize = popupSize.clone();
    widgetSize.height -= headerSize.height;
    htmlWidget.setLocation(new Point(0, headerSize.height));
    htmlWidget.setSize(widgetSize.subtract(htmlWidget.margins()));
  }

  /**
   * @override AbstractLayout.js
   */
  preferredLayoutSize($container) {
    let $window = this.popup.$container.window(),
      windowSize = new Dimension($window.width(), $window.height());

    return windowSize;
  }
}
