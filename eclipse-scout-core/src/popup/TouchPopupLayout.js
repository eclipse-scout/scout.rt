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
import {Dimension, graphics, HtmlEnvironment, PopupLayout, Rectangle} from '../index';

export default class TouchPopupLayout extends PopupLayout {

  constructor(popup) {
    super(popup);
    this.doubleCalcPrefSize = false;
  }

  layout($container) {
    super.layout($container);

    let popupSize = this.popup.htmlComp.size().subtract(this.popup.htmlComp.insets()),
      headerHeight = graphics.size(this.popup._$header, true).height,
      field = this.popup._field,
      fieldHeight = field.htmlComp.prefSize().height,
      fieldMargins = field.htmlComp.margins(),
      fieldWidth = popupSize.width - fieldMargins.horizontal(),
      widgetVerticalOffset = headerHeight + fieldHeight + fieldMargins.vertical();

    field.htmlComp.setBounds(new Rectangle(0, headerHeight, fieldWidth, fieldHeight));
    this.popup._widgetContainerHtmlComp.setBounds(
      new Rectangle(0, widgetVerticalOffset, popupSize.width, popupSize.height - widgetVerticalOffset));
  }

  /**
   * @override AbstractLayout.js
   */
  preferredLayoutSize($container) {
    let popupWidth = HtmlEnvironment.get().formColumnWidth,
      popupHeight = HtmlEnvironment.get().formRowHeight * 15;

    return new Dimension(popupWidth, popupHeight);
  }
}
