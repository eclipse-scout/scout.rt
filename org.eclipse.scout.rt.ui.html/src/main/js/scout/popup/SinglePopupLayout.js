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
/**
 * Layout for a popup with a single child.<br>
 * If no child is provided, the first child in the container is used.
 */
scout.SinglePopupLayout = function(popup, childWidget) {
  scout.SinglePopupLayout.parent.call(this, popup);
  this.childWidget = childWidget;
};
scout.inherits(scout.SinglePopupLayout, scout.PopupLayout);

scout.SinglePopupLayout.prototype.layout = function($container) {
  scout.SinglePopupLayout.parent.prototype.layout.call(this, $container);

  var childSize, htmlChild,
    htmlContainer = this.popup.htmlComp;

  if (this.childWidget) {
    htmlChild = this.childWidget.htmlChild;
  }
  if (!htmlChild) {
    htmlChild = this._getHtmlSingleChild($container);
  }
  if (htmlChild) {
    childSize = htmlContainer.getSize()
      .subtract(htmlContainer.getInsets())
      .subtract(htmlChild.getMargins());
    htmlChild.setSize(childSize);
  }
};

scout.SinglePopupLayout.prototype.preferredLayoutSize = function($container) {
  var htmlChild;
  if (this.childWidget) {
    htmlChild = this.childWidget.htmlChild;
  }
  if (!htmlChild) {
    htmlChild = this._getHtmlSingleChild($container);
  }
  if (htmlChild) {
    var prefSize,
      htmlContainer = this.popup.htmlComp;

    prefSize = htmlChild.getPreferredSize()
      .add(htmlContainer.getInsets())
      .add(htmlChild.getMargins());

    return prefSize;
  }
  return new scout.Dimension(1, 1);
};

/**
 * @returns the first found html component of the given container or null if the container has no children linked with a html component.
 */
scout.SinglePopupLayout.prototype._getHtmlSingleChild = function($container) {
  var htmlComp = null;

  $container.children().each(function (idx, elem) {
    var $comp = $(elem);
    htmlComp = scout.HtmlComponent.optGet($comp);
    if (htmlComp) {
      return false;
    }
  });

  return htmlComp;
};
