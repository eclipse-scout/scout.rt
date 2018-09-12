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
scout.ColumnLayout = function(options) {
  scout.ColumnLayout.parent.call(this);
  options = options || {};
  this.stretch = scout.nvl(options.stretch, true);
};
scout.inherits(scout.ColumnLayout, scout.AbstractLayout);

scout.ColumnLayout.prototype.layout = function($container) {
  var htmlComp = scout.HtmlComponent.get($container);
  var containerSize = htmlComp.availableSize()
    .subtract(htmlComp.insets());

  $container.children().each(function(i, elem) {
    var $elem = $(elem);
    var htmlChild = scout.HtmlComponent.optGet($elem);
    if (!htmlChild || !$elem.isVisible()) {
      return;
    }

    var childPrefSize = htmlChild.prefSize({
      useCssSize: true
    });

    if (this.stretch) {
      // All elements in a column layout have the same height which is the height of the container
      childPrefSize.height = containerSize.height;
    }

    // Use layout data width if set
    if (htmlChild.layoutData && htmlChild.layoutData.widthHint) {
      childPrefSize.width = htmlChild.layoutData.widthHint;
    }
    htmlChild.setSize(childPrefSize);
  }.bind(this));
};

scout.ColumnLayout.prototype.preferredLayoutSize = function($container, options) {
  var prefSize = new scout.Dimension(),
    htmlContainer = scout.HtmlComponent.get($container),
    childOptions = {
      useCssSize: true
    };

  $container.children().each(function(i, elem) {
    var $elem = $(elem);
    var htmlChild = scout.HtmlComponent.optGet($elem);
    if (!htmlChild || !$elem.isVisible()) {
      return;
    }

    var childPrefSize = htmlChild.prefSize(childOptions);
    // Use layout data width if set
    if (htmlChild.layoutData && htmlChild.layoutData.widthHint) {
      childPrefSize.width = htmlChild.layoutData.widthHint;
    }
    childPrefSize = childPrefSize.add(htmlChild.margins());
    prefSize.width = prefSize.width + childPrefSize.width;
    prefSize.height = Math.max(prefSize.height, childPrefSize.height);
  }.bind(this));

  prefSize = prefSize.add(htmlContainer.insets());
  return prefSize;
};
