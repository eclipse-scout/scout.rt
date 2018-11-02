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
scout.TileGridLayout = function(tiles) {
  scout.TileGridLayout.parent.call(this, tiles);
  this.maxWidth = -1;
  this.containerPos = null;
  this.containerScrollTop = null;
  this.tiles = [];
};
scout.inherits(scout.TileGridLayout, scout.LogicalGridLayout);

scout.TileGridLayout.prototype.layout = function($container) {
  var htmlComp = this.widget.htmlComp;
  if (this.widget.scrolling) {
    // Try to layout only as much as needed while scrolling in virtual mode
    // Scroll top may be dirty when layout is validated before scrolling to a specific tile (see tileGrid.scrollTo)
    if (!this.widget.scrollTopDirty) {
      this.widget._renderViewPort();
    }
    this._layout($container);
    return;
  }

  // Animate only once on startup (if enabled) but animate every time on resize
  var animated = htmlComp.layouted || (this.widget.startupAnimationEnabled && !this.widget.startupAnimationDone) || this.widget.renderAnimationEnabled;
  this.tiles = this.widget.renderedTiles();

  // Make them invisible otherwise the influence scrollHeight (e.g. if grid is scrolled to the very bottom and tiles are filtered, scrollbar would still increase scroll height)
  scout.scrollbars.setVisible($container, false);

  // Store the current position of the tiles
  if (animated) {
    this._storeBounds(this.tiles);
  }

  this._updateMaxWidth();
  this._resetGridColumnCount();

  this.widget.invalidateLayout();
  this.widget.invalidateLogicalGrid(false);
  var contentFits = false;
  var containerWidth = $container.outerWidth();
  containerWidth = Math.max(containerWidth, this.minWidth);
  if (htmlComp.prefSize().width <= containerWidth) {
    this._layout($container);
    contentFits = true;
  }

  // If content does not fit, the columnCount will be reduced until it fits
  while (!contentFits && this.widget.gridColumnCount > 1) {
    this.widget.gridColumnCount--;
    this.widget.invalidateLayout();
    this.widget.invalidateLogicalGrid(false);
    if (htmlComp.prefSize().width <= containerWidth) {
      this._layout($container);
      contentFits = true;
    }
  }

  // If it does not fit, layout anyway (happens on small sizes if even one column is not sufficient)
  if (!contentFits) {
    this._layout($container);
  }

  if (!htmlComp.layouted) {
    this.widget._renderScrollTop();
  }
  if (this.widget.virtual && (!htmlComp.layouted || this._sizeChanged(htmlComp) || this.widget.withPlaceholders)) {
    // When changing size of the container, more or less tiles might be shown and some tiles might even change rows due to a new gridColumnCount -> ensure correct tiles are rendered in the range
    this.widget.setViewRangeSize(this.widget.calculateViewRangeSize(), false);
    var newTiles = this.widget._renderTileDelta();
    // Make sure newly rendered tiles are animated (if enabled) and layouted as well
    this._storeBounds(newTiles);
    scout.arrays.pushAll(this.tiles, newTiles);
    this._layout($container);
  }

  var promises = [];
  if (animated) {
    promises = this._animateTiles();
  }
  this.widget.startupAnimationDone = true;

  // When all animations have been finished, trigger event and update scrollbar
  if (promises.length > 0) {
    $.promiseAll(promises).done(this._onAnimationDone.bind(this));
  } else {
    this._onAnimationDone();
  }
};

scout.TileGridLayout.prototype._sizeChanged = function(htmlComp) {
  return htmlComp.sizeCached && !htmlComp.sizeCached.equals(htmlComp.size());
};

scout.TileGridLayout.prototype._storeBounds = function(tiles) {
  tiles.forEach(function(tile, i) {
    var bounds = scout.graphics.cssBounds(tile.$container);
    tile.$container.data('oldBounds', bounds);
    tile.$container.data('was-layouted', tile.htmlComp.layouted);
  }, this);
};

/**
 * @override
 */
scout.TileGridLayout.prototype._validateGridData = function(htmlComp) {
  htmlComp.$comp.removeClass('newly-rendered');
  return scout.TileGridLayout.parent.prototype._validateGridData.call(this, htmlComp);
};

/**
 * @override
 */
scout.TileGridLayout.prototype._layoutCellBounds = function(containerSize, containerInsets) {
  // Since the tiles are positioned absolutely it is necessary to add the height of the filler to the top insets
  if (this.widget.virtual) {
    containerInsets.top += this.widget.$fillBefore.outerHeight(true);
  }
  return scout.TileGridLayout.parent.prototype._layoutCellBounds.call(this, containerSize, containerInsets);
};

scout.TileGridLayout.prototype._animateTiles = function() {
  var htmlComp = this.widget.htmlComp;
  var $container = htmlComp.$comp;

  this.containerPos = htmlComp.offset();
  this.containerScrollTop = $container.scrollTop();

  // Hide scrollbar before the animation (does not look good if scrollbar is hidden after the animation)
  scout.scrollbars.setVisible($container, true);
  scout.scrollbars.opacity($container, 0);

  // Animate the position change of the tiles
  var promises = [];
  this.tiles.forEach(function(tile, i) {
    if (!tile.rendered) {
      // Only animate tiles which were there at the beginning of the layout
      // RenderViewPort may remove or render some, the removed ones cannot be animated because $container is missing and don't need to anyway, the rendered ones cannot because fromBounds are missing
      return;
    }

    var promise = this._animateTile(tile);
    if (promise) {
      scout.arrays.pushAll(promises, promise);
    }

    tile.$container.removeData('oldBounds');
    tile.$container.removeData('was-layouted');
  }, this);

  return promises;
};

scout.TileGridLayout.prototype._animateTile = function(tile) {
  var htmlComp = this.widget.htmlComp;

  // Stop running animations before starting the new ones to make sure existing promises are not resolved too early
  // It may also happen that while the animation of a tile is in progress, the layout is triggered again but the tile should not be animated anymore
  // (e.g. if it is not in the viewport anymore). In that case the animation must be stopped otherwise it may be placed at a wrong position
  tile.$container.stop();

  if (tile.$container.hasClass('animate-visible')) {
    // Don't animate tiles which are fading in (due to filtering), they should appear at the correct position.
    // Already visible tiles which were in the view port before will be moved from the old position. Tiles which were not in the view port before will fly in from the top left corner (same happens when sorting).
    // Reason: When sorting, if some tiles are in the viewport and some not, it is confusing if some tiles just appear and others are moved, even though all actually change position.
    return;
  }

  if (tile.$container.hasClass('invisible')) {
    // When tiles are inserted they are invisible because a dedicated insert animation will be started after the layouting,
    // the animation here is to animate the position change -> don't animate inserted tiles here
    return;
  }

  var bounds = scout.graphics.cssBounds(tile.$container);
  var fromBounds = tile.$container.data('oldBounds');
  if (tile instanceof scout.PlaceholderTile && !tile.$container.data('was-layouted')) {
    // Placeholders may not have fromBounds because they are added while layouting
    // Just let them appear at the correct position
    fromBounds = bounds.clone();
  }

  if (!htmlComp.layouted && this.widget.startupAnimationDone && this.widget.renderAnimationEnabled) {
    // This is a small, discreet render animation, just move the tiles a little
    // It will happen if the startup animation is disabled or done and every time the tiles are rendered anew
    fromBounds = new scout.Rectangle(bounds.x * 0.95, bounds.y * 0.95, bounds.width, bounds.height);
  }

  if (fromBounds.equals(bounds)) {
    // Don't animate if bounds are equals (otherwise promises would always resolve after 300ms even though no animation was visible)
    return;
  }

  if (!this._inViewport(bounds) && !this._inViewport(fromBounds)) {
    // If neither the new nor the old position is in the viewport don't animate the tile. This will affect the animation performance in a positive way if there are many tiles
    return;
  }

  if (!tile.$container.data('was-layouted') && !this._inViewport(bounds)) {
    // If a newly inserted tile will be rendered outside the view port, don't animate it. If it is rendered inside the view port it is fine if it will be moved from the top left corner
    return;
  }

  // Start animation
  return this._animateTileBounds(tile, fromBounds, bounds);
};

scout.TileGridLayout.prototype._inViewport = function(bounds) {
  bounds = bounds.translate(this.containerPos.x, this.containerPos.y).translate(0, -this.containerScrollTop);
  var topLeftPos = new scout.Point(bounds.x, bounds.y);
  var bottomRightPos = new scout.Point(bounds.x + bounds.width, bounds.y + bounds.height);
  var $scrollable = this.widget.$container.scrollParent();
  return scout.scrollbars.isLocationInView(topLeftPos, $scrollable) || scout.scrollbars.isLocationInView(bottomRightPos, $scrollable);
};

scout.TileGridLayout.prototype._onAnimationDone = function() {
  this._updateScrollbar();
  this.widget.trigger('layoutAnimationDone');
};

scout.TileGridLayout.prototype._animateTileBounds = function(tile, fromBounds, bounds) {
  var promises = [];

  tile.$container
    .cssLeftAnimated(fromBounds.x, bounds.x, {
      start: function(promise) {
        promises.push(promise);
      },
      queue: false
    })
    .cssTopAnimated(fromBounds.y, bounds.y, {
      start: function(promise) {
        promises.push(promise);
      },
      queue: false
    })
    .cssWidthAnimated(fromBounds.width, bounds.width, {
      start: function(promise) {
        promises.push(promise);
      },
      queue: false
    })
    .cssHeightAnimated(fromBounds.height, bounds.height, {
      start: function(promise) {
        promises.push(promise);
      },
      queue: false
    });
  return promises;
};

scout.TileGridLayout.prototype._updateScrollbar = function() {
  scout.scrollbars.setVisible(this.widget.$container, true);
  scout.scrollbars.opacity(this.widget.$container, 1);

  // Update first scrollable parent (if widget itself is not scrollable, maybe a parent is)
  var htmlComp = this.widget.htmlComp;
  while (htmlComp) {
    if (htmlComp.scrollable) {
      // Update immediately to prevent flickering (scrollbar is made visible on the top of this function)
      scout.scrollbars.update(htmlComp.$comp, true);
      break;
    }
    htmlComp = htmlComp.getParent();
  }
};

/**
 * When max. width should be enforced, add a padding to the container if necessary
 * (to make sure, scrollbar position is not changed)
 */
scout.TileGridLayout.prototype._updateMaxWidth = function() {
  // Reset padding-right set by layout
  var htmlComp = this.widget.htmlComp;
  htmlComp.$comp.cssPaddingRight(null);

  if (this.maxWidth <= 0) {
    return;
  }

  // Measure current padding-right (by CSS)
  var cssPaddingRight = htmlComp.$comp.cssPaddingRight();

  // Calculate difference between current with and max. width
  var containerSize = htmlComp.size();
  var oldWidth = containerSize.width;
  var newWidth = Math.min(containerSize.width, this.maxWidth);
  var diff = oldWidth - newWidth - htmlComp.$comp.cssPaddingLeft() - htmlComp.$comp.cssBorderWidthX();
  if (diff > cssPaddingRight) {
    htmlComp.$comp.cssPaddingRight(diff);
  }
};

scout.TileGridLayout.prototype._resetGridColumnCount = function() {
  this.widget.gridColumnCount = this.widget.prefGridColumnCount;
};

scout.TileGridLayout.prototype.preferredLayoutSize = function($container, options) {
  options = $.extend({}, options);

  if (this.widget.virtual) {
    return this.virtualPrefSize($container, options);
  }
  if (options.widthHint) {
    return this.prefSizeForWidth(options.widthHint);
  }
  return scout.TileGridLayout.parent.prototype.preferredLayoutSize.call(this, $container, options);
};

/**
 * Calculates the preferred size only based on the grid column count, row count and layout config. Does not use rendered elements.
 * Therefore only works if all tiles are of the same size (which is a precondition for the virtual scrolling anyway).
 */
scout.TileGridLayout.prototype.virtualPrefSize = function($container, options) {
  var rowCount, columnCount;
  var insets = scout.HtmlComponent.get($container).insets();
  var prefSize = new scout.Dimension();
  var columnWidth = this.widget.layoutConfig.columnWidth;
  var rowHeight = this.widget.layoutConfig.rowHeight;
  var hgap = this.widget.layoutConfig.hgap;
  var vgap = this.widget.layoutConfig.vgap;

  if (options.widthHint) {
    columnCount = Math.floor(options.widthHint / (columnWidth + hgap));
    var width = columnCount * (columnWidth + hgap);
    if (options.widthHint - width > columnWidth) {
      // The last column does not have a hgap -> Correct the grid column count if another column would fit in
      columnCount++;
    }
    columnCount = Math.max(Math.min(this.widget.prefGridColumnCount, columnCount), 1);

    rowCount = this.widget.rowCount(columnCount);
    prefSize.width = options.widthHint;
    prefSize.height = Math.max(rowCount * rowHeight + (rowCount - 1) * vgap, 0);
    prefSize.width += insets.horizontal();
    prefSize.height += insets.vertical();
    return prefSize;
  }

  columnCount = this.widget.gridColumnCount;
  rowCount = this.widget.rowCount();
  prefSize.width = Math.max(columnCount * columnWidth + (columnCount - 1) * hgap, 0);
  prefSize.height = Math.max(rowCount * rowHeight + (rowCount - 1) * vgap, 0);
  prefSize.width += insets.horizontal();
  prefSize.height += insets.vertical();
  return prefSize;
};

scout.TileGridLayout.prototype.prefSizeForWidth = function(width) {
  var prefSize,
    htmlComp = this.widget.htmlComp,
    contentFits = false,
    gridColumnCount = this.widget.gridColumnCount;

  width += htmlComp.insets().horizontal();
  this._resetGridColumnCount();

  this.widget.invalidateLayout();
  this.widget.invalidateLogicalGrid(false);
  prefSize = htmlComp.prefSize();
  if (prefSize.width <= width) {
    contentFits = true;
  }

  while (!contentFits && this.widget.gridColumnCount > 1) {
    this.widget.gridColumnCount--;
    this.widget.invalidateLayout();
    this.widget.invalidateLogicalGrid(false);
    prefSize = htmlComp.prefSize();
    if (prefSize.width <= width) {
      contentFits = true;
    }
  }
  // Reset to previous gridColumnCount (prefSize should not modify properties)
  this.widget.gridColumnCount = gridColumnCount;
  return prefSize;
};
