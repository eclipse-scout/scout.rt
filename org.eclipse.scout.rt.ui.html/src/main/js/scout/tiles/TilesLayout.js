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
scout.TilesLayout = function(tiles) {
  scout.TilesLayout.parent.call(this, tiles);
  this.maxWidth = -1;
  this.containerPos = null;
  this.containerScrollTop = null;
};
scout.inherits(scout.TilesLayout, scout.LogicalGridLayout);

scout.TilesLayout.prototype.layout = function($container) {
  var contentFits;
  var htmlComp = this.widget.htmlComp;
  // Animate only once on startup (if enabled) but animate every time on resize
  var animated = htmlComp.layouted || (this.widget.startupAnimationEnabled && !this.widget.startupAnimationDone) || this.widget.renderAnimationEnabled;

  // Store the current position of the tiles
  if (animated) {
    this.widget.filteredTiles.forEach(function(tile, i) {
      var bounds = scout.graphics.cssBounds(tile.$container);
      tile.$container.data('oldBounds', bounds);
    }, this);
  }

  this._updateMaxWidth();
  this._resetGridColumnCount();

  this.widget.invalidateLayout();
  this.widget.invalidateLogicalGrid(false);
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

scout.TilesLayout.prototype._animateTiles = function() {
  var htmlComp = this.widget.htmlComp;
  var $container = htmlComp.$comp;

  this.containerPos = htmlComp.offset();
  this.containerScrollTop = $container.scrollTop();

  // Hide scrollbar before the animation (does not look good if scrollbar is hidden after the animation)
  scout.scrollbars.opacity($container, 0);

  // Animate the position change of the tiles
  var promises = [];
  this.widget.filteredTiles.forEach(function(tile, i) {

    // Stop running animations before starting the new ones to make sure existing promises are not resolved too early
    // It may also happen that while the animation of a tile is in progress, the layout is triggered again but the tile should not be animated anymore
    // (e.g. if it is not in the viewport anymore). In that case the animation must be stopped otherwise it may be placed at a wrong position
    tile.$container.stop();

    if (tile.$container.hasClass('invisible')) {
      // When tiles are inserted they are invisible because a dedicated insert animation will be started after the layouting,
      // the animation here is to animate the position change -> don't animate inserted tiles here
      tile.$container.removeData('oldBounds');
      return;
    }

    var bounds = scout.graphics.cssBounds(tile.$container);
    var fromBounds = tile.$container.data('oldBounds');
    if (tile instanceof scout.PlaceholderTile && !fromBounds) {
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
      tile.$container.removeData('oldBounds');
      return;
    }

    if (!this._inViewport(bounds) && !this._inViewport(fromBounds)) {
      // If neither the new nor the old position is in the viewport don't animate the tile. This will affect the animation performance in a positive way if there are many tiles
      tile.$container.removeData('oldBounds');
      return;
    }

    // Start animation
    scout.arrays.pushAll(promises, this._animateTileBounds(tile, fromBounds, bounds));

    tile.$container.removeData('oldBounds');
  }, this);

  return promises;
};

scout.TilesLayout.prototype._inViewport = function(bounds) {
  bounds = bounds.translate(this.containerPos.x, this.containerPos.y).translate(0, -this.containerScrollTop);
  var topLeftPos = new scout.Point(bounds.x, bounds.y);
  var bottomRightPos = new scout.Point(bounds.x + bounds.width, bounds.y + bounds.height);
  var $scrollable = this.widget.$container;
  return scout.scrollbars.isLocationInView(topLeftPos, $scrollable) || scout.scrollbars.isLocationInView(bottomRightPos, $scrollable);
};

scout.TilesLayout.prototype._onAnimationDone = function() {
  this._updateScrollbar();
  this.widget.trigger('layoutAnimationDone');
};

scout.TilesLayout.prototype._animateTileBounds = function(tile, fromBounds, bounds) {
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

scout.TilesLayout.prototype._updateScrollbar = function() {
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
scout.TilesLayout.prototype._updateMaxWidth = function() {
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

scout.TilesLayout.prototype._resetGridColumnCount = function() {
  this.widget.gridColumnCount = this.widget.prefGridColumnCount;
};

scout.TilesLayout.prototype.preferredLayoutSize = function($container, options) {
  options = $.extend({}, options);
  if (options.widthHint) {
    return this.prefSizeForWidth(options.widthHint);
  }
  return scout.TilesLayout.parent.prototype.preferredLayoutSize.call(this, $container, options);
};

scout.TilesLayout.prototype.prefSizeForWidth = function(width) {
  var prefSize,
    htmlComp = this.widget.htmlComp,
    contentFits = false;

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
  return prefSize;
};
