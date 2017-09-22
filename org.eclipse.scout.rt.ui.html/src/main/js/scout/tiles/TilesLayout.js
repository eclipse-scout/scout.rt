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
scout.TilesLayout = function(tiles) {
  scout.TilesLayout.parent.call(this, tiles);
};
scout.inherits(scout.TilesLayout, scout.LogicalGridLayout);

scout.TilesLayout.prototype.layout = function($container) {
  var contentFits;
  // Animate only once initially but animate every time on resize
  var animated = this.widget.htmlComp.layouted || !this.widget.initialAnimationDone;

  // Store the current position of the tiles
  this.widget.tiles.forEach(function(tile, i) {
    var pos = scout.graphics.location(tile.$container);
    tile.$container.data('oldLeft', pos.x);
    tile.$container.data('oldTop', pos.y);
  }, this);

  this._resetGridColumnCount();

  this.widget.invalidateLayout();
  this.widget.invalidateLogicalGrid(false);
  if (this.widget.htmlComp.prefSize().width <= $container.width()) {
    this._layout($container);
    contentFits = true;
  }

  // If content does not fit, the columnCount will be reduced until it fits
  while (!contentFits && this.widget.gridColumnCount > 1) {
    this.widget.gridColumnCount--;
    this.widget.invalidateLayout();
    this.widget.invalidateLogicalGrid(false);
    if (this.widget.htmlComp.prefSize().width <= $container.width()) {
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
  this.widget.tiles.forEach(function(tile, i) {
    var pos = scout.graphics.location(tile.$container),
      fromLeft = tile.$container.data('oldLeft') || 0,
      fromTop = tile.$container.data('oldTop') || 0;

    if (!animated) {
      fromLeft = (pos.x - fromLeft) * 0.95;
      fromTop = (pos.y - fromTop) * 0.95;
    }

    // Stop running animations before starting the new ones to make sure existing promises are not resolved too early
    tile.$container
      .stop()
      .cssLeftAnimated(fromLeft, pos.x, {
        start: function(promise) {
          promises.push(promise);
        },
        queue: false
      })
      .cssTopAnimated(fromTop, pos.y, {
        start: function(promise) {
          promises.push(promise);
        },
        queue: false
      });
    tile.$container.removeData('oldLeft');
    tile.$container.removeData('oldTop');
  }, this);

  this.widget.initialAnimationDone = true;

  // When all animations have been finished update scrollbar
  if (promises.length > 0) {
    $.promiseAll(promises).done(this._updateScrollbar.bind(this));
  }
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
