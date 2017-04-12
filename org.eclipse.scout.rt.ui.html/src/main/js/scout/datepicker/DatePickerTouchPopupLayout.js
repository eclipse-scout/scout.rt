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
scout.DatePickerTouchPopupLayout = function(popup) {
  scout.DatePickerTouchPopupLayout.parent.call(this, popup);
};
scout.inherits(scout.DatePickerTouchPopupLayout, scout.TouchPopupLayout);

/**
 * @override
 */
scout.DatePickerTouchPopupLayout.prototype.preferredLayoutSize = function($container, options) {
  var popupWidth = scout.HtmlEnvironment.formColumnWidth,
    popupHeight = 0;

  var containerInsets = this.popup.htmlComp.getInsets();
  var fieldHtmlComp = this.popup._field.htmlComp;
  var widgetContainerHtmlComp = this.popup._widgetContainerHtmlComp;
  var fieldPrefSize = fieldHtmlComp.getPreferredSize(options)
    .add(fieldHtmlComp.getMargins());
  var widgetContainerPrefSize = widgetContainerHtmlComp.getPreferredSize(options)
    .add(widgetContainerHtmlComp.getMargins());

  popupHeight = fieldPrefSize.height + widgetContainerPrefSize.height + containerInsets.vertical();
  return new scout.Dimension(popupWidth, popupHeight);
};
