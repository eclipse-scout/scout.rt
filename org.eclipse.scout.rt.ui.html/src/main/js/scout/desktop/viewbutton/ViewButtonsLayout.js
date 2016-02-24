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
scout.ViewButtonsLayout = function(viewButtons) {
  scout.ViewButtonsLayout.parent.call(this);
  this.viewButtons = viewButtons;
};
scout.inherits(scout.ViewButtonsLayout, scout.AbstractLayout);

scout.ViewButtonsLayout.prototype.layout = function($container) {
  var htmlComp = this.viewButtons.htmlComp,
    containerBounds = htmlComp.getBounds(),
    tabs = $container.children().length,
    tabWidth = (containerBounds.width / tabs);
  $container.children().each(function() {
    var $tab = $(this);
    $tab.removeAttr('style');
    scout.graphics.setSize($tab, new scout.Dimension(tabWidth, containerBounds.height));
  });
};
