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
scout.WidgetPopupLayout = function(popup) {
  scout.WidgetPopupLayout.parent.call(this, popup);
};
scout.inherits(scout.WidgetPopupLayout, scout.PopupLayout);

scout.WidgetPopupLayout.prototype._setSize = function(prefSize) {
  scout.WidgetPopupLayout.parent.prototype._setSize.call(this, prefSize);

  var htmlPopup = this.popup.htmlComp;
  var htmlWidget = this.popup.widget.htmlComp;
  var widgetSize = prefSize.subtract(htmlPopup.insets());
  htmlWidget.setSize(widgetSize.subtract(htmlWidget.margins()));
};

scout.WidgetPopupLayout.prototype.preferredLayoutSize = function($container, options) {
  var htmlComp = this.popup.htmlComp;
  var htmlWidget = this.popup.widget.htmlComp;

  var prefSize = htmlWidget.prefSize(options)
    .add(htmlComp.insets())
    .add(htmlWidget.margins());

  return prefSize;
};
