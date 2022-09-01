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
import {Dimension, HtmlCompPrefSizeOptions, PopupLayout, WidgetPopup} from '../index';

export default class WidgetPopupLayout extends PopupLayout {

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
