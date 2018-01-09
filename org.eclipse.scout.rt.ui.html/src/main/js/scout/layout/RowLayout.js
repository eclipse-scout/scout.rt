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
scout.RowLayout = function(pixelSizedLayouting) {
  scout.RowLayout.parent.call(this);
  this.pixelSizedLayouting = pixelSizedLayouting;
};
scout.inherits(scout.RowLayout, scout.AbstractLayout);

scout.RowLayout.prototype.layout = function($container) {
  var htmlComp = scout.HtmlComponent.get($container);
  var containerSize = htmlComp.availableSize()
    .subtract(htmlComp.insets());

  $container.children().each(function() {
    var htmlChild = scout.HtmlComponent.optGet($(this));
    if (htmlChild) {
      if (!this.pixelSizedLayouting) {
        var prefSize = htmlChild.prefSize({
          widthHint: containerSize.width
        });
        // All elements in a row layout have the same width which is the width of the container
        prefSize.width = containerSize.width;
        htmlChild.setSize(prefSize);
      } else {
        htmlChild.validateLayout();
      }
    }
  }.bind(this));
};

scout.RowLayout.prototype.preferredLayoutSize = function($container, options) {
  var prefSize = new scout.Dimension(),
    htmlContainer = scout.HtmlComponent.get($container),
    maxWidth = 0;

  $container.children().each(function() {
    var htmlChildPrefSize,
      htmlChild = scout.HtmlComponent.optGet($(this));
    if (htmlChild) {
      htmlChildPrefSize = htmlChild.prefSize(options)
        .add(htmlChild.margins());
      maxWidth = Math.max(htmlChildPrefSize.width, maxWidth);
      prefSize.height += htmlChildPrefSize.height;
      prefSize.width = maxWidth;
    }
  });

  prefSize = prefSize.add(htmlContainer.insets());
  return prefSize;
};
