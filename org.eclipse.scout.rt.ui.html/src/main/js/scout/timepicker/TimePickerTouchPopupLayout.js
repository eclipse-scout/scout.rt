/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.TimePickerTouchPopupLayout = function(popup) {
  scout.TimePickerTouchPopupLayout.parent.call(this, popup);
};
scout.inherits(scout.TimePickerTouchPopupLayout, scout.TouchPopupLayout);

scout.TimePickerTouchPopupLayout.prototype.layout = function($container) {
  scout.TimePickerTouchPopupLayout.parent.prototype.layout.call(this, $container);
  var htmlPicker = this.popup.getTimePicker().htmlComp;
  htmlPicker.setSize(this.popup._widgetContainerHtmlComp.getSize());
};

/**
 * @override
 */
scout.TimePickerTouchPopupLayout.prototype.preferredLayoutSize = function($container, options) {
  var containerInsets = this.popup.htmlComp.insets(),
    fieldHtmlComp = this.popup._field.htmlComp,
    widgetContainerHtmlComp = this.popup._widgetContainerHtmlComp,
    fieldPrefSize = fieldHtmlComp.prefSize(options)
    .add(fieldHtmlComp.margins()),
    widgetContainerPrefSize = widgetContainerHtmlComp.prefSize(options)
    .add(widgetContainerHtmlComp.margins()),
    headerHeight = scout.graphics.size(this.popup._$header, {
      includeMargin: true
    }).height,
    popupHeight = headerHeight + fieldPrefSize.height + widgetContainerPrefSize.height + containerInsets.vertical(),
    popupWidth = Math.max(fieldPrefSize.width, widgetContainerPrefSize.width);

  return new scout.Dimension(popupWidth, popupHeight);
};
