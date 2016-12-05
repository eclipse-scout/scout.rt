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
  var htmlContainer = this.tree.htmlComp;

  this._layout($container);
  scout.scrollbars.update(this.tree.$data);
};

scout.TreeLayout.prototype._layout = function($container) {
  var menuBarSize, containerSize, heightOffset,
    menuBar = this.tree.menuBar,
    htmlMenuBar = menuBar.htmlComp,
    htmlContainer = this.tree.htmlComp;

  containerSize = htmlContainer.getAvailableSize()
    .subtract(htmlContainer.getInsets());

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

  // Check if width has changed
  if (htmlContainer.size && htmlContainer.size.width !== htmlContainer.getSize().width) {
    if (this.tree.isHorizontalScrollingEnabled()) {
      // Width is only relevant if horizontal scrolling is enabled -> mark as dirty
      this.tree.nodeWidthDirty = true;
      this.tree.maxNodeWidth = 0;
    } else {
      // Nodes may contain wrapped text (with breadcrumb style-or if nodes contain html) -> update heights
      this.tree.updateNodeHeights();
      this.tree._renderFiller();
    }
  }

  this.tree.setViewRangeSize(this.tree.calculateViewRangeSize());
  // Always render viewport (not only when viewRangeSize changes), because view range depends on scroll position and data height
  this.tree._renderViewport();
};

scout.TreeLayout.prototype._setDataHeight = function(heightOffset) {
  var $data = this.tree.$data;

  heightOffset += $data.cssMarginTop() + $data.cssMarginBottom();

  $data.css('height', (heightOffset === 0 ? '100%' : 'calc(100% - ' + heightOffset + 'px)'));
};

scout.TreeLayout.prototype.preferredLayoutSize = function($container) {
  // Make sure viewport is up to date before calculating pref size.
  // This is necessary because the tree does not render the view port on any change (like insert or delete nodes). Instead it just invalidates the layout.
  this.tree._renderViewport();

  return scout.graphics.prefSize($container);
};
