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
scout.ViewButtonBoxLayout = function(viewButtonBox) {
  scout.ViewButtonBoxLayout.parent.call(this);
  this.viewButtonBox = viewButtonBox;
};
scout.inherits(scout.ViewButtonBoxLayout, scout.AbstractLayout);

scout.ViewButtonBoxLayout.prototype.layout = function($container) {
  var tabs = this.viewButtonBox.viewTabs.slice(),
    viewMenuTab = this.viewButtonBox.viewMenuTab,
    htmlComp = this.viewButtonBox.htmlComp,
    containerWidth = htmlComp.size().width,
    tabWidth = containerWidth / tabs.length;

  if (viewMenuTab.visible) {
    if (viewMenuTab.selectedButton) {
      tabWidth = (containerWidth - scout.graphics.size(viewMenuTab.dropdown.$container, {
        exact: true
      }).width) / (tabs.length + 1);
      viewMenuTab.selectedButton.$container.cssWidth(tabWidth);
    }

    containerWidth -= scout.graphics.size(viewMenuTab.$container, {
      exact: true
    }).width;
  }

  tabs.forEach(function(tab, index) {
    if (tabs.length - 1 === index) {
      // to avoid pixel fault due to rounding issues calculate the rest for the last tab.
      tab.$container.cssWidth(containerWidth);
    } else {
      tab.$container.cssWidth(tabWidth);
      containerWidth -= tab.$container.cssWidth();
    }
  }, this);
};

scout.ViewButtonBoxLayout.prototype.preferredLayoutSize = function($container) {
  // View buttons have an absolute css height set -> useCssSize = true
  return scout.graphics.prefSize($container, {
    useCssSize: true
  });
};
