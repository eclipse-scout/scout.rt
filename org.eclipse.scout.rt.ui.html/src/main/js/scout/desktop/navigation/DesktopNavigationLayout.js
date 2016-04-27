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
    toolBar = this.navigation.toolBar,
    viewButtonBox = this.navigation.viewButtonBox,
    viewButtonBoxHeight = 0,
    viewButtonBoxWidth = 0;

  containerSize = containerSize.subtract(htmlContainer.getInsets());

  if (viewButtonBox) {
    viewButtonBoxPrefSize = viewButtonBox.htmlComp.getPreferredSize();
    viewButtonBoxHeight = viewButtonBoxPrefSize.height;
    viewButtonBoxWidth = containerSize.width;
    if (toolBar) {
      viewButtonBoxWidth = viewButtonBoxPrefSize.width;
    }

    viewButtonBoxSize = new scout.Dimension(viewButtonBoxWidth, viewButtonBoxHeight)
      .subtract(viewButtonBox.htmlComp.getMargins());
    viewButtonBox.htmlComp.setSize(viewButtonBoxSize);
  }

  if (toolBar) {
    toolBar.$container.cssLeft(viewButtonBoxWidth);
    toolBar.htmlComp.setSize(new scout.Dimension(containerSize.width - viewButtonBoxWidth, viewButtonBoxHeight));
  }

  bodySize = new scout.Dimension(containerSize.width, containerSize.height - viewButtonBoxHeight)
    .subtract(htmlBody.getMargins());
  htmlBody.setSize(bodySize);
  htmlBody.$comp.cssTop(viewButtonBoxHeight);
};
