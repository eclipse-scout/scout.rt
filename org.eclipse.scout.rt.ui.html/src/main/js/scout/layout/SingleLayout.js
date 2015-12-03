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
  var childSize = htmlContainer.getAvailableSize()
    .subtract(htmlContainer.getInsets()),
    htmlChild = this._htmlChild;

  if (!htmlChild) {
    htmlChild = this._getHtmlSingleChild($container);
  }
  if (htmlChild) {
    htmlChild.setSize(childSize);
  }
};

scout.SingleLayout.prototype.preferredLayoutSize = function($container) {
  var htmlChild = this._htmlChild;
  if (!htmlChild) {
    htmlChild = this._getHtmlSingleChild($container);
  }
  if (htmlChild) {
    return htmlChild.getPreferredSize();
  } else {
    return new scout.Dimension(1, 1);
  }
};

/**
 * @returns a HtmlComponent instance for the first child of the given container or null if the container has no children.
 */
scout.SingleLayout.prototype._getHtmlSingleChild = function($container) {
  var $firstChild = $container.children().first();
  if ($firstChild.length) {
    return scout.HtmlComponent.get($firstChild);
  } else {
    return null;
  }
};
