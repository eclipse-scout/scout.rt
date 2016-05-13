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
scout.TreeLayout = function(tree) {
  scout.TreeLayout.parent.call(this);
  this.tree = tree;
};
scout.inherits(scout.TreeLayout, scout.AbstractLayout);

scout.TreeLayout.prototype.layout = function($container) {
  var menuBarSize, containerSize, heightOffset,
    menuBar = this.tree.menuBar,
    htmlMenuBar = menuBar.htmlComp,
    htmlContainer = this.tree.htmlComp,
    $data = this.tree.$data;

  containerSize = htmlContainer.getAvailableSize()
    .subtract(htmlContainer.getInsets());

  //FIXME CGU/AWE remove this check as soon as HtmlComp.validateLayout checks for invisible components
  if (!htmlContainer.isAttachedAndVisible() || !htmlContainer.$comp.isEveryParentVisible()) {
    return;
  }

  if (this.tree.autoToggleBreadcrumbStyle) {
    this.tree.setBreadcrumbStyleActive(containerSize.width <= this.tree.breadcrumbTogglingThreshold);
  }

  heightOffset = 0;
  if (menuBar.$container.isVisible()) {
    menuBarSize = scout.MenuBarLayout.size(htmlMenuBar, containerSize);
    htmlMenuBar.setSize(menuBarSize);
    heightOffset += menuBarSize.height;
  }

  this._setDataHeight(heightOffset);
  scout.scrollbars.update($data);
};

scout.TreeLayout.prototype._setDataHeight = function(heightOffset) {
  var $data = this.tree.$data;

  heightOffset += $data.cssMarginTop() + $data.cssMarginBottom();

  $data.css('height', (heightOffset === 0 ? '100%' : 'calc(100% - ' + heightOffset + 'px)'));
  this.tree.nodeWidthDirty = true;
  this.tree.maxNodeWidth=0;

  this.tree.setViewRangeSize(this.tree.calculateViewRangeSize());
  // Always render viewport (not only when viewRangeSize changes), because view range depends on scroll position and data height
  this.tree._renderViewport();
};
