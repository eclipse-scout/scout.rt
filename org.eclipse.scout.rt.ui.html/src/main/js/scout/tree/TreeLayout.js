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
  this.nodeDimensionsDirty = false;
};
scout.inherits(scout.TreeLayout, scout.AbstractLayout);

scout.TreeLayout.prototype.layout = function($container) {
  this._layout($container);
  scout.scrollbars.update(this.tree.$data);
};

scout.TreeLayout.prototype._layout = function($container) {
  var menuBarSize, containerSize, heightOffset,
    menuBar = this.tree.menuBar,
    htmlMenuBar = menuBar.htmlComp,
    htmlContainer = this.tree.htmlComp;

  containerSize = htmlContainer.availableSize({
      exact: true
    })
    .subtract(htmlContainer.insets());

  if (this.tree.toggleBreadcrumbStyleEnabled) {
    this.tree.setBreadcrumbStyleActive(Math.floor(containerSize.width) <= this.tree.breadcrumbTogglingThreshold);
  }

  heightOffset = 0;
  if (menuBar.$container.isVisible()) {
    menuBarSize = scout.MenuBarLayout.size(htmlMenuBar, containerSize);
    htmlMenuBar.setSize(menuBarSize);
    heightOffset += menuBarSize.height;
  }

  this._setDataHeight(heightOffset);

  // recalculate ViewRangeSize before any rendering is done
  this.tree.setViewRangeSize(this.tree.calculateViewRangeSize());

  // Check if width has changed
  this.nodeDimensionsDirty = this.nodeDimensionsDirty ||
    (htmlContainer.sizeCached && htmlContainer.sizeCached.width !== htmlContainer.size().width);
  if (this.nodeDimensionsDirty) {
    this.nodeDimensionsDirty = false;
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

  if (!htmlContainer.layouted) {
    this.tree._renderScrollTop();
  }

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

  // Node dimensions were fixed when calling _renderViewport using the current size, but that size might change during layout
  // Only necessary the first time it is layouted, afterwards htmlContainer.sizeCached will be set
  if (!this.tree.htmlComp.layouted) {
    this.nodeDimensionsDirty = true;
  }
  return scout.graphics.prefSize($container);
};
