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

  var field = this.popup._field,
    fieldMargins = new scout.Insets(4, 6, 5, 4),
    popupSize = this.popup.htmlComp.getSize(),
    fieldHeight = field.htmlComp.getPreferredSize().height,
    fieldWidth = popupSize.width - fieldMargins.horizontal(),
    widgetVerticalOffset = fieldHeight + fieldMargins.vertical();

  field.htmlComp.setBounds(new scout.Rectangle(fieldMargins.left, fieldMargins.top, fieldWidth, fieldHeight));
  this.popup._widgetContainerHtmlComp.setBounds(
    new scout.Rectangle(0, widgetVerticalOffset, popupSize.width, popupSize.height - widgetVerticalOffset));
};

/**
 * @override AbstractLayout.js
 */
scout.TouchPopupLayout.prototype.preferredLayoutSize = function($container) {
  var $document = $container.document(),
    screenWidth = $document.width(),
    screenHeight = $document.height(),
    minPopupWidth = scout.HtmlEnvironment.formColumnWidth / 2,
    maxPopupHeight = scout.HtmlEnvironment.formRowHeight * 15,
    popupWidth = scout.HtmlEnvironment.formColumnWidth,
    popupHeight = screenHeight / 2 - scout.TouchPopup.TOP_MARGIN;

  popupWidth = Math.max(popupWidth, minPopupWidth);
  popupHeight = Math.min(popupHeight, maxPopupHeight);

  return new scout.Dimension(popupWidth, popupHeight);
};
