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
scout.ViewButtonBoxLayout = function(viewButtonBox) {
  scout.ViewButtonBoxLayout.parent.call(this);
  this.viewButtonBox = viewButtonBox;
};
scout.inherits(scout.ViewButtonBoxLayout, scout.AbstractLayout);

scout.ViewButtonBoxLayout.prototype.layout = function($container) {
  var htmlComp = this.viewButtonBox.htmlComp,
    containerBounds = htmlComp.getBounds(),
    $visibleTabs = $container.children(':visible'),
    tabCount = $visibleTabs.length,
    tabWidth = (containerBounds.width / tabCount);

  $visibleTabs.each(function() {
    var $tab = $(this);
    // only set width, use css height
    $tab.cssWidth(tabWidth);
  });
};

scout.ViewButtonBoxLayout.prototype.preferredLayoutSize = function($container) {
  // View buttons have an absolute css height set -> useCssSize = true
  return scout.graphics.prefSize($container, false, {
    useCssSize: true
  });
};
