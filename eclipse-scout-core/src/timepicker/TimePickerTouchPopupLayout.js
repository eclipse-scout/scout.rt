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
import {Dimension, graphics, TouchPopupLayout} from '../index';

export default class TimePickerTouchPopupLayout extends TouchPopupLayout {

  constructor(popup) {
    super(popup);
  }

  layout($container) {
    super.layout($container);
    let htmlPicker = this.popup.getTimePicker().htmlComp;
    htmlPicker.setSize(this.popup._widgetContainerHtmlComp.size());
  }

  /**
   * @override
   */
  preferredLayoutSize($container, options) {
    let containerInsets = this.popup.htmlComp.insets(),
      fieldHtmlComp = this.popup._field.htmlComp,
      widgetContainerHtmlComp = this.popup._widgetContainerHtmlComp;

    let fieldPrefSize = fieldHtmlComp.prefSize(options)
      .add(fieldHtmlComp.margins());
    let widgetContainerPrefSize = widgetContainerHtmlComp.prefSize(options)
      .add(widgetContainerHtmlComp.margins());

    let headerHeight = graphics.size(this.popup._$header, true).height;
    let popupHeight = headerHeight + fieldPrefSize.height + widgetContainerPrefSize.height + containerInsets.vertical();
    let popupWidth = Math.max(fieldPrefSize.width, widgetContainerPrefSize.width);

    return new Dimension(popupWidth, popupHeight);
  }
}
