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
  var outlineSize, viewButtonsSize, viewButtonsHeight,
    htmlContainer = this.navigation.htmlComp,
    containerSize = htmlContainer.getSize(),
    htmlOutline = this.navigation.outline.htmlComp,
    htmlViewButtons = this.navigation.htmlViewButtons;

  this.navigation.setBreadcrumbEnabled(containerSize.width <= scout.DesktopNavigation.BREADCRUMB_SWITCH_WIDTH);

  containerSize = containerSize.subtract(htmlContainer.getInsets());
  viewButtonsHeight = this.navigation.$viewButtons.outerHeight(true);
  viewButtonsSize = new scout.Dimension(containerSize.width, viewButtonsHeight)
    .subtract(htmlContainer.getMargins());
  htmlViewButtons.setSize(viewButtonsSize);

  outlineSize = new scout.Dimension(containerSize.width, containerSize.height - viewButtonsHeight)
    .subtract(htmlContainer.getMargins());
  htmlOutline.setSize(outlineSize);
};
