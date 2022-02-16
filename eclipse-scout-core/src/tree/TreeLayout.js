/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {AbstractLayout, graphics, MenuBarLayout, scrollbars} from '../index';

export default class TreeLayout extends AbstractLayout {

  constructor(tree) {
    super();
    this.tree = tree;
    this.nodeDimensionsDirty = false;
  }

  layout($container) {
    this._layout($container);
    scrollbars.update(this.tree.$data);
  }

  _layout($container) {
    let menuBarSize, containerSize, heightOffset,
      menuBar = this.tree.menuBar,
      htmlMenuBar = menuBar.htmlComp,
      htmlContainer = this.tree.htmlComp;

    containerSize = htmlContainer.availableSize({
      exact: true
    })
      .subtract(htmlContainer.insets());

    if (this.tree.toggleBreadcrumbStyleEnabled && this._sizeChanged(htmlContainer)) {
      this.tree.setBreadcrumbStyleActive(Math.floor(containerSize.width) <= this.tree.breadcrumbTogglingThreshold);
    }

    heightOffset = 0;
    if (menuBar.$container.isVisible()) {
      menuBarSize = MenuBarLayout.size(htmlMenuBar, containerSize);
      htmlMenuBar.setSize(menuBarSize);
      heightOffset += menuBarSize.height;
    }
    $container.css('--menubar-height', ((menuBarSize || {}).height || 0) + 'px');

    this._setDataHeight(heightOffset);

    // recalculate ViewRangeSize before any rendering is done
    this.tree.setViewRangeSize(this.tree.calculateViewRangeSize());

    // Check if width has changed
    this.nodeDimensionsDirty = this.nodeDimensionsDirty || this._sizeChanged(htmlContainer);
    if (this.nodeDimensionsDirty) {
      this.nodeDimensionsDirty = false;
      if (this.tree.isHorizontalScrollingEnabled()) {
        // Width is only relevant if horizontal scrolling is enabled -> mark as dirty
        this.tree.nodeWidthDirty = true;
        this.tree.maxNodeWidth = 0;
      } else {
        // Nodes may contain wrapped text (with breadcrumb style-or if nodes contain html) -> update heights
        this.tree.updateNodeHeights();
        this.tree._renderViewport(); // Ensure viewRangeRendered is up to date and matches visibleNodesFlat (can diverge after filtering)
        this.tree._renderFiller();
      }
    }

    if (!htmlContainer.layouted) {
      this.tree._renderScrollTop();
    }

    // Always render viewport (not only when viewRangeSize changes), because view range depends on scroll position and data height
    this.tree._renderViewport();

    // Render scroll top again to make sure the data is really at the correct position after rendering viewport, see tree.setScrollTop for details
    if (!htmlContainer.layouted) {
      this.tree._renderScrollTop();
    }
  }

  _sizeChanged(htmlContainer) {
    // Ceil because sizeCached is exact but .size() is not)
    return htmlContainer.sizeCached && Math.ceil(htmlContainer.sizeCached.width) !== Math.ceil(htmlContainer.size().width);
  }

  _setDataHeight(heightOffset) {
    let $data = this.tree.$data;

    heightOffset += $data.cssMarginTop() + $data.cssMarginBottom();

    $data.css('height', (heightOffset === 0 ? '100%' : 'calc(100% - ' + heightOffset + 'px)'));
  }

  preferredLayoutSize($container, options) {
    // Make sure viewport is up to date before calculating pref size.
    // This is necessary because the tree does not render the view port on any change (like insert or delete nodes). Instead it just invalidates the layout.
    this.tree._renderViewport();

    // Node dimensions were fixed when calling _renderViewport using the current size, but that size might change during layout
    // Only necessary the first time it is layouted, afterwards htmlContainer.sizeCached will be set
    if (!this.tree.htmlComp.layouted) {
      this.nodeDimensionsDirty = true;
    }
    return graphics.prefSize($container, options);
  }
}
