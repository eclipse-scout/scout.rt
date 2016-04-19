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
  var bodySize, viewButtonsSize, viewButtonsHeight, viewButtonsWidth,
    htmlContainer = this.navigation.htmlComp,
    containerSize = htmlContainer.getSize(),
    htmlBody = this.navigation.htmlCompBody,
    htmlViewButtons = this.navigation.viewButtons.htmlComp,
    viewButtonsPrefSize = htmlViewButtons.getPreferredSize();

  containerSize = containerSize.subtract(htmlContainer.getInsets());
  viewButtonsHeight = viewButtonsPrefSize.height;
  viewButtonsWidth = containerSize.width;
  if (this.navigation.toolBarVisible) {
    viewButtonsWidth = viewButtonsPrefSize.width;
  }

  viewButtonsSize = new scout.Dimension(viewButtonsWidth, viewButtonsHeight)
    .subtract(htmlViewButtons.getMargins());
  htmlViewButtons.setSize(viewButtonsSize);

  if (this.navigation.toolBarVisible) {
    this.navigation.toolBar.$container.cssLeft(viewButtonsWidth);
    this.navigation.toolBar.htmlComp.setSize(new scout.Dimension(containerSize.width - viewButtonsWidth, viewButtonsHeight));
  }

  bodySize = new scout.Dimension(containerSize.width, containerSize.height - viewButtonsHeight)
    .subtract(htmlBody.getMargins());
  htmlBody.setSize(bodySize);
  htmlBody.$comp.cssTop(viewButtonsHeight);
};
