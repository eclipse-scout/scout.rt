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
  var popupWidth = scout.HtmlEnvironment.formColumnWidth,
    containerInsets = this.popup.htmlComp.getInsets(),
    fieldHtmlComp = this.popup._field.htmlComp,
    widgetContainerHtmlComp = this.popup._widgetContainerHtmlComp,
    fieldPrefSize = fieldHtmlComp.getPreferredSize(options)
      .add(fieldHtmlComp.getMargins()),
    widgetContainerPrefSize = widgetContainerHtmlComp.getPreferredSize(options)
      .add(widgetContainerHtmlComp.getMargins()),
    headerHeight = scout.graphics.getSize(this.popup._$header, true).height,
    popupHeight = headerHeight + fieldPrefSize.height + widgetContainerPrefSize.height + containerInsets.vertical();

  return new scout.Dimension(popupWidth, popupHeight);
};
