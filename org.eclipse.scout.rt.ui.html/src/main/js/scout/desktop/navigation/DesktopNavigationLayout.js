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
scout.DesktopNavigationLayout = function(navigation) {
  scout.DesktopNavigationLayout.parent.call(this);
  this.navigation = navigation;
};
scout.inherits(scout.DesktopNavigationLayout, scout.AbstractLayout);

scout.DesktopNavigationLayout.prototype.layout = function($container) {
  var bodySize, viewButtonBoxSize, viewButtonBoxPrefSize,
    htmlContainer = this.navigation.htmlComp,
    containerSize = htmlContainer.getSize(),
    htmlBody = this.navigation.htmlCompBody,
    toolBox = this.navigation.toolBox,
    viewButtonBox = this.navigation.viewButtonBox,
    viewButtonBoxHeight = 0,
    viewButtonBoxWidth = 0;

  containerSize = containerSize.subtract(htmlContainer.getInsets());

  if (viewButtonBox) {
    viewButtonBoxPrefSize = viewButtonBox.htmlComp.getPreferredSize();
    viewButtonBoxHeight = viewButtonBoxPrefSize.height;
    viewButtonBoxWidth = containerSize.width;
    if (toolBox) {
      viewButtonBoxWidth = viewButtonBoxPrefSize.width;
    }

    viewButtonBoxSize = new scout.Dimension(viewButtonBoxWidth, viewButtonBoxHeight)
      .subtract(viewButtonBox.htmlComp.getMargins());
    viewButtonBox.htmlComp.setSize(viewButtonBoxSize);
  }

  if (toolBox) {
    toolBox.$container.cssLeft(viewButtonBoxWidth);
    toolBox.htmlComp.setSize(new scout.Dimension(containerSize.width - viewButtonBoxWidth, viewButtonBoxHeight));
  }

  bodySize = new scout.Dimension(containerSize.width, containerSize.height - viewButtonBoxHeight)
    .subtract(htmlBody.getMargins());
  htmlBody.setSize(bodySize);
  htmlBody.$comp.cssTop(viewButtonBoxHeight);
};

scout.DesktopNavigationLayout.prototype.preferredLayoutSize = function($container) {
  var htmlContainer = this.navigation.htmlComp,
    htmlBody = this.navigation.htmlCompBody,
    toolBox = this.navigation.toolBox,
    viewButtonBox = this.navigation.viewButtonBox;

  var prefSize = htmlBody.getPreferredSize();

  var prefSizeBoxes = new scout.Dimension(0, 0);
  if (viewButtonBox) {
    var prefSizeViewButtonBox = viewButtonBox.htmlComp.getPreferredSize();
    prefSizeBoxes.width += prefSizeViewButtonBox.width;
    prefSizeBoxes.height = Math.max(prefSizeBoxes.height, prefSizeViewButtonBox.height);
  }
  if (toolBox) {
    var prefSizeToolBox = toolBox.htmlComp.getPreferredSize();
    prefSizeBoxes.width += prefSizeToolBox.width;
    prefSizeBoxes.height = Math.max(prefSizeBoxes.height, prefSizeToolBox.height);
  }

  prefSize.height += prefSizeBoxes.height;
  prefSize.width = Math.max(prefSize.width, prefSizeBoxes.width);
  prefSize.add(htmlContainer.getInsets());

  return prefSize;
};
