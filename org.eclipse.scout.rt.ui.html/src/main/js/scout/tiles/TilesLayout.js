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
  this.maxContentWidth = -1;
};
scout.inherits(scout.TilesLayout, scout.LogicalGridLayout);

scout.TilesLayout.prototype.layout = function($container) {
  var contentFits;
  var htmlComp = this.widget.htmlComp;
  // Animate only once on startup (if enabled) but animate every time on resize
  var animated = htmlComp.layouted || (this.widget.startupAnimationEnabled && !this.widget.startupAnimationDone);

  // Store the current position of the tiles
  this.widget.filteredTiles.forEach(function(tile, i) {
    var bounds = scout.graphics.cssBounds(tile.$container);
    tile.$container.data('oldBounds', bounds);
  }, this);

  this._updateMaxContentWidth();
  this._resetGridColumnCount();

  this.widget.invalidateLayout();
  this.widget.invalidateLogicalGrid(false);
  var containerWidth = $container.outerWidth();
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

  if (animated) {
    // Hide scrollbar before the animation (does not look good if scrollbar is shown while the animation is running)
    scout.scrollbars.setVisible($container, false);

    // The animation of the position change won't look good if the box is scrolled down -> scroll up first
    $container[0].scrollTop = 0;
  }

  // Animate the position change of the tiles
  var promises = [];
  this.widget.filteredTiles.forEach(function(tile, i) {
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

    if (!animated) {
      // This is a small, discreet startup animation, just move the tiles a little
      // It will happen if the startup animation is disabled, or every time the tiles are rendered anew
      fromBounds = new scout.Rectangle(bounds.x * 0.95, bounds.y * 0.95, bounds.width, bounds.height);
    }

    if (fromBounds.equals(bounds)) {
      // Don't animate if bounds are equals (otherwise promises would always resolve after 300ms even though no animation was visible)
      tile.$container.removeData('oldBounds');
      return;
    }

    // Start animation
    scout.arrays.pushAll(promises, this._animateTileBounds(tile, fromBounds, bounds));

    tile.$container.removeData('oldBounds');
  }, this);

  this.widget.startupAnimationDone = true;

  // When all animations have been finished, trigger event and update scrollbar
  if (promises.length > 0) {
    $.promiseAll(promises).done(this._onAnimationDone.bind(this));
  } else {
    this._onAnimationDone();
  }
};

scout.TilesLayout.prototype._onAnimationDone = function() {
  this.widget.trigger('layoutAnimationDone');
  this._updateScrollbar();
};

scout.TilesLayout.prototype._animateTileBounds = function(tile, fromBounds, bounds) {
  var promises = [];

  // Stop running animations before starting the new ones to make sure existing promises are not resolved too early
  tile.$container
    .stop()
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
  scout.scrollbars.setVisible(this.widget.$container, true);

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
 * When max. content width should be enforced, add a padding to the container if necessary
 * (to make sure, scrollbar position is not changed)
 */
scout.TilesLayout.prototype._updateMaxContentWidth = function() {
  // Reset padding-right set by layout
  var htmlComp = this.widget.htmlComp;
  var containerSize = htmlComp.size();
  htmlComp.$comp.cssPaddingRight(null);
  if (this.maxContentWidth <= 0) {
    return;
  }

  // Measure current padding-right (by CSS)
  var cssPaddingRight = htmlComp.$comp.cssPaddingRight();

  // Calculate difference between current with and max. width
  var oldWidth = containerSize.width;
  var newWidth = Math.min(containerSize.width, this.maxContentWidth);
  var diff = oldWidth - newWidth - htmlComp.$comp.cssPaddingLeft() - htmlComp.$comp.cssBorderWidthX();
  if (diff > cssPaddingRight) {
    htmlComp.$comp.cssPaddingRight(diff);
  }
};

scout.TilesLayout.prototype._resetGridColumnCount = function() {
  this.widget.gridColumnCount = this.widget.prefGridColumnCount;
};

scout.TilesLayout.prototype.prefSizeForWidth = function(width) {
  var prefSize,
    contentFits = false;

  this._resetGridColumnCount();

  this.widget.invalidateLayout();
  this.widget.invalidateLogicalGrid(false);
  prefSize = this.widget.htmlComp.prefSize();
  if (prefSize.width <= width) {
    contentFits = true;
  }

  while (!contentFits && this.widget.gridColumnCount > 1) {
    this.widget.gridColumnCount--;
    this.widget.invalidateLayout();
    this.widget.invalidateLogicalGrid(false);
    prefSize = this.widget.htmlComp.prefSize();
    if (prefSize.width <= width) {
      contentFits = true;
    }
  }
  return prefSize;
};
