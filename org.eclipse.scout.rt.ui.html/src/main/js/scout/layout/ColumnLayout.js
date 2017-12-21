/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.ColumnLayout = function() {
  scout.ColumnLayout.parent.call(this);
};
scout.inherits(scout.ColumnLayout, scout.AbstractLayout);

scout.ColumnLayout.prototype.layout = function($container) {
  $container.children().each(function() {
    var htmlChild = scout.HtmlComponent.optGet($(this)),
      $child = $(this),
      childPrefSize;

    if (htmlChild) {
      childPrefSize = htmlChild.prefSize();
      htmlChild.setSize(childPrefSize);
    } else {
      childPrefSize = scout.graphics.prefSize($child, {
        useCssSize: true
      });
    }
  });
};

scout.ColumnLayout.prototype.preferredLayoutSize = function($container, options) {
  var prefSize = new scout.Dimension(),
    htmlContainer = scout.HtmlComponent.get($container);

  $container.children().filter(function() {
    return $(this).isVisible();
  }).each(function() {
    var childPrefSize,
      $child = $(this),
      htmlChild = scout.HtmlComponent.optGet($child);
    if (htmlChild) {
      childPrefSize = htmlChild.prefSize(options)
        .add(htmlChild.margins());
      prefSize.width = prefSize.width + childPrefSize.width;
      prefSize.height = Math.max(prefSize.height, childPrefSize.height);
    }
  });

  prefSize = prefSize.add(htmlContainer.insets());
  return prefSize;
};
