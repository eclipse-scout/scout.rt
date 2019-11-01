/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {PopupLayout} from '../index';

export default class WidgetPopupLayout extends PopupLayout {

constructor(popup) {
  super( popup);
}


_setSize(prefSize) {
  super._setSize( prefSize);

  var htmlPopup = this.popup.htmlComp;
  var htmlWidget = this.popup.widget.htmlComp;
  var widgetSize = prefSize.subtract(htmlPopup.insets());
  htmlWidget.setSize(widgetSize.subtract(htmlWidget.margins()));
}

preferredLayoutSize($container, options) {
  var htmlComp = this.popup.htmlComp;
  var htmlWidget = this.popup.widget.htmlComp;

  var prefSize = htmlWidget.prefSize(options)
    .add(htmlComp.insets())
    .add(htmlWidget.margins());

  return prefSize;
}
}
