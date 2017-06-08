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
scout.TouchPopupLayout = function(popup) {
  scout.TouchPopupLayout.parent.call(this, popup);
};
scout.inherits(scout.TouchPopupLayout, scout.PopupLayout);

scout.TouchPopupLayout.prototype.layout = function($container) {
  scout.TouchPopupLayout.parent.prototype.layout.call(this, $container);

  var popupSize = this.popup.htmlComp.getSize().subtract(this.popup.htmlComp.getInsets()),
    headerHeight = scout.graphics.getSize(this.popup._$header, true).height,
    field = this.popup._field,
    fieldHeight = field.htmlComp.getPreferredSize().height,
    fieldMargins = field.htmlComp.getMargins(),
    fieldWidth = popupSize.width - fieldMargins.horizontal(),
    widgetVerticalOffset = headerHeight + fieldHeight + fieldMargins.vertical();

  field.htmlComp.setBounds(new scout.Rectangle(0, headerHeight, fieldWidth, fieldHeight));
  this.popup._widgetContainerHtmlComp.setBounds(
    new scout.Rectangle(0, widgetVerticalOffset, popupSize.width, popupSize.height - widgetVerticalOffset));
};

/**
 * @override AbstractLayout.js
 */
scout.TouchPopupLayout.prototype.preferredLayoutSize = function($container) {
  var popupWidth = scout.HtmlEnvironment.formColumnWidth,
    popupHeight = scout.HtmlEnvironment.formRowHeight * 15;

  return new scout.Dimension(popupWidth, popupHeight);
};
