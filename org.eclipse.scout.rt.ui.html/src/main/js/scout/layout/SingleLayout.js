/*******************************************************************************
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
/**
 * Resizes the child so it has the same size as the container.<br>
 * If no child is provided, the first child in the container is used.
 */
scout.SingleLayout = function(htmlChild) {
  scout.SingleLayout.parent.call(this);
  this._htmlChild = htmlChild;
};
scout.inherits(scout.SingleLayout, scout.AbstractLayout);

scout.SingleLayout.prototype.layout = function($container) {
  var htmlContainer = scout.HtmlComponent.get($container);
  var childSize = htmlContainer.availableSize()
    .subtract(htmlContainer.insets()),
    htmlChild = this._htmlChild;

  if (!htmlChild) {
    htmlChild = this._getHtmlSingleChild($container);
  }
  if (htmlChild) {
    htmlChild.setSize(childSize);
  }
};

scout.SingleLayout.prototype.preferredLayoutSize = function($container, options) {
  var htmlChild = this._htmlChild;
  if (!htmlChild) {
    htmlChild = this._getHtmlSingleChild($container);
  }
  if (htmlChild) {
    return htmlChild.prefSize(options).add(scout.graphics.insets($container));
  }
  return new scout.Dimension(1, 1);
};

/**
 * @returns the first child html component of the given container or null if the container has no child with a html component or no children at all.
 */
scout.SingleLayout.prototype._getHtmlSingleChild = function($container) {
  var htmlComp = null;
  $container.children().each(function(i, child) {
    var htmlChild = scout.HtmlComponent.optGet($(child));
    if (htmlChild) {
      htmlComp = htmlChild;
      return false;
    }
  });
  return htmlComp;
};
