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
scout.MenuBarLayout = function(menuBar) {
  scout.MenuBarLayout.parent.call(this);
  this.menuBar = menuBar;
};
scout.inherits(scout.MenuBarLayout, scout.AbstractLayout);

/**
 * @override AbstractLayout.js
 */
scout.MenuBarLayout.prototype.layout = function($container) {
  var htmlContainer = scout.HtmlComponent.get($container),
    availableSize = htmlContainer.getAvailableSize()
      .subtract(htmlContainer.getInsets());

  // Temporarily add "overflow: hidden" to measure the available size. (The overflow
  // is not hidden in the CSS, otherwise the focus border could get cut off.)
  var oldStyle = $container.attr('style');
  $container.css('overflow', 'hidden');
  availableSize.width = $container.width();
  $container.attrOrRemove('style', oldStyle);

  var menuBoxLeft = this.menuBar.menuBoxLeft;
  var menuBoxRight = this.menuBar.menuBoxRight;
  var prefSizeLeft = menuBoxLeft.htmlComp.getPreferredSize();
  var prefSizeRight = menuBoxRight.htmlComp.getPreferredSize();
  // L = large, S = small
  var menuBoxL = menuBoxLeft;
  var menuBoxS = menuBoxRight;
  var prefSizeL = prefSizeLeft;
  var prefSizeS = prefSizeRight;
  if (prefSizeRight.width > prefSizeLeft.width) {
    menuBoxL = menuBoxRight;
    menuBoxS = menuBoxLeft;
    prefSizeL = prefSizeRight;
    prefSizeS = prefSizeLeft;
  }

  var sizeL = new scout.Dimension(0, availableSize.height);
  var sizeS = new scout.Dimension(availableSize.width, availableSize.height);

  var fns = [
    function() {
      prefSizeL = menuBoxL.htmlComp.layout.compactPrefSize();
    },
    function() {
      prefSizeS = menuBoxS.htmlComp.layout.compactPrefSize();
    },
    function() {
      prefSizeL = menuBoxL.htmlComp.layout.shrinkPrefSize();
    },
    function() {
      prefSizeS = menuBoxS.htmlComp.layout.shrinkPrefSize();
    }
  ];

  var diff = availableSize.width - prefSizeL.width - prefSizeS.width;
  while (diff < 0 && fns.length) {
    fns.shift().call();
    diff = availableSize.width - prefSizeL.width - prefSizeS.width;
  }

  sizeL.width = Math.max(prefSizeL.width + diff, 0);
  sizeS.width = Math.min(prefSizeS.width, availableSize.width);

  menuBoxL.htmlComp.setSize(sizeL);
  menuBoxS.htmlComp.setSize(sizeS);

  this.menuBar.updateDefaultMenu();
};

scout.MenuBarLayout.prototype.preferredLayoutSize = function($container) {
  // Menubar has an absolute css height set -> useCssSize = true
  var prefSize = scout.graphics.prefSize($container, false, {
    useCssSize: true
  });

  var insetsMenuBar = scout.graphics.getInsets($container);
  var menuBoxLeft = this.menuBar.menuBoxLeft;
  var menuBoxRight = this.menuBar.menuBoxRight;
  var prefSizeLeft = menuBoxLeft.htmlComp.getPreferredSize();
  var prefSizeRight = menuBoxRight.htmlComp.getPreferredSize();
  var marginsLeft = menuBoxLeft.htmlComp.getMargins();
  var marginsRight = menuBoxRight.htmlComp.getMargins();

  prefSize.width = insetsMenuBar.horizontal() + prefSizeLeft.width + marginsLeft.horizontal() + prefSizeRight.width + marginsRight.horizontal();

  return prefSize;
};

/* --- STATIC HELPERS ------------------------------------------------------------- */

/**
 * @memberOf scout.MenuBarLayout
 */
scout.MenuBarLayout.size = function(htmlMenuBar, containerSize) {
  var menuBarSize = htmlMenuBar.getPreferredSize();
  menuBarSize.width = containerSize.width;
  menuBarSize = menuBarSize.subtract(htmlMenuBar.getMargins());
  return menuBarSize;
};
