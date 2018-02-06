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
      childPrefSize;
    if (htmlChild) {
      childPrefSize = htmlChild.prefSize({
        useCssSize: true
      });
      // use layout data width if set.
      if (htmlChild.layoutData && htmlChild.layoutData.widthHint) {
        childPrefSize.width = htmlChild.layoutData.widthHint;
      }
      htmlChild.setSize(childPrefSize);
    }
  });
};

scout.ColumnLayout.prototype.preferredLayoutSize = function($container, options) {
  var prefSize = new scout.Dimension(),
    htmlContainer = scout.HtmlComponent.get($container),
    childOptions = {
      useCssSize: true
    };

  $container.children().filter(function() {
    return $(this).isVisible();
  }).each(function() {
    var childPrefSize,
      $child = $(this),
      htmlChild = scout.HtmlComponent.optGet($child);
    if (htmlChild) {
      childPrefSize = htmlChild.prefSize(childOptions);
      // use layout data width if set.
      if (htmlChild.layoutData && htmlChild.layoutData.widthHint) {
        childPrefSize.width = htmlChild.layoutData.widthHint;
      }
      childPrefSize = childPrefSize.add(htmlChild.margins());
      prefSize.width = prefSize.width + childPrefSize.width;
      prefSize.height = Math.max(prefSize.height, childPrefSize.height);
    }
  });

  prefSize = prefSize.add(htmlContainer.insets());
  return prefSize;
};
