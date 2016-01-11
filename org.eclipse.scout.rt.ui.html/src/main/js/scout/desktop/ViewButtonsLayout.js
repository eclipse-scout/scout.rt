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
scout.ViewButtonsLayout = function(htmlComp) {
  scout.ViewButtonsLayout.parent.call(this);
  this._htmlComp = htmlComp;
};
scout.inherits(scout.ViewButtonsLayout, scout.AbstractLayout);

/**
 * Should be the same as in DesktopNavigation.css .view-button-tab > min-width.
 */
scout.ViewButtonsLayout.MIN_TAB_WIDTH = '50px';

scout.ViewButtonsLayout.prototype.layout = function($container) {
  var containerBounds = this._htmlComp.getBounds(),
    tabs = $container.children().length,
    tabWidth = (containerBounds.width / tabs);
  $container.children().each(function() {
    var $tab = $(this);
    $tab.removeAttr('style');
    scout.graphics.setSize($tab, new scout.Dimension(tabWidth, containerBounds.height));
  });
};
