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
scout.RowLayout = function() {
  scout.RowLayout.parent.call(this);
};
scout.inherits(scout.RowLayout, scout.AbstractLayout);

scout.RowLayout.prototype.layout = function($container) {
  $container.children().each(function() {
    var htmlComp = scout.HtmlComponent.optGet($(this));
    if (htmlComp) {
      htmlComp.setSize(htmlComp.prefSize());
    }
  });
};

scout.RowLayout.prototype.preferredLayoutSize = function($container) {
  var prefSize = new scout.Dimension(),
    htmlContainer = scout.HtmlComponent.get($container),
    maxWidth = 0;

  $container.children().each(function() {
    var htmlChildPrefSize,
      htmlChild = scout.HtmlComponent.optGet($(this));
    if (htmlChild) {
      htmlChildPrefSize = htmlChild.prefSize()
        .add(htmlChild.margins());
      maxWidth = Math.max(htmlChildPrefSize.width, maxWidth);
      prefSize.height += htmlChildPrefSize.height;
      prefSize.width = maxWidth;
    }
  });

  prefSize = prefSize.add(htmlContainer.insets());
  return prefSize;
};
