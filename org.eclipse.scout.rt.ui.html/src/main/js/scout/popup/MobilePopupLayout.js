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
scout.MobilePopupLayout = function(popup) {
  scout.MobilePopupLayout.parent.call(this, popup);
};
scout.inherits(scout.MobilePopupLayout, scout.PopupLayout);

scout.MobilePopupLayout.prototype.layout = function($container) {
  scout.MobilePopupLayout.parent.prototype.layout.call(this, $container);

  var htmlPopup = this.popup.htmlComp,
    popupSize = htmlPopup.getSize(),
    htmlWidget = this.popup.widget.htmlComp,
    widgetSize = 0,
    $header = this.popup.$header,
    headerSize = 0;

  popupSize = popupSize.subtract(htmlPopup.getInsets());
  headerSize = scout.graphics.prefSize($header, true);
  scout.graphics.setLocation($header, new scout.Point(0, 0));

  widgetSize = popupSize.clone();
  widgetSize.height -= headerSize.height;
  htmlWidget.setLocation(new scout.Point(0, headerSize.height));
  htmlWidget.setSize(widgetSize.subtract(htmlWidget.getMargins()));

  // The first time it gets layouted, add shown class to be able to animate
  if (!htmlPopup.layouted) {
    htmlPopup.$comp.addClassForAnimation('animate-open');
  }
};

/**
 * @override AbstractLayout.js
 */
scout.MobilePopupLayout.prototype.preferredLayoutSize = function($container) {
  var $window = this.popup.$container.window(),
    windowSize = new scout.Dimension($window.width(), $window.height());

  return windowSize;
};
