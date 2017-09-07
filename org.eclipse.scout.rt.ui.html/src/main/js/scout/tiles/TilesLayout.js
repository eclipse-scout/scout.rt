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
    var pos = tile.$container.position();
    tile.$container.data('oldLeft', pos.left);
    tile.$container.data('oldTop', pos.top);
  }, this);

  if (this.widget.prefGridColumnCount === undefined) {
    // Store the preferred column count
    this.widget.prefGridColumnCount = this.widget.gridColumnCount;
  }
  this.widget.gridColumnCount = this.widget.prefGridColumnCount;

  this.widget.invalidateLayout();
  this.widget.invalidateLogicalGrid(false);
  if (this.widget.htmlComp.prefSize().width <= $container.width()) {
    this._layout(this.widget.$container);
    contentFits = true;
  }

  // If content does not fit, the columnCount will be reduced until it fits
  while (!contentFits && this.widget.gridColumnCount > 1) {
    this.widget.gridColumnCount--;
    this.widget.invalidateLayout();
    this.widget.invalidateLogicalGrid(false);
    if (this.widget.htmlComp.prefSize().width <= $container.width()) {
      this._layout(this.widget.$container);
      contentFits = true;
    }
  }

  // animate the position change of the tiles
  var promises = [];
  this.widget.tiles.forEach(function(tile, i) {
    var deferred = $.Deferred(),
      pos = tile.$container.position(),
      fromLeft = tile.$container.data('oldLeft') || 0,
      fromTop = tile.$container.data('oldTop') || 0;

    if (!animated) {
      fromLeft = (pos.left - fromLeft) * 0.95;
      fromTop = (pos.top - fromTop) * 0.95;
    }

    promises.push(deferred.promise());

    tile.$container
      .cssLeftAnimated(fromLeft, pos.left, {
        queue: false
      })
      .cssTopAnimated(fromTop, pos.top, {
        queue: false,
        complete: function() {
          deferred.resolve();
        }
      });
    tile.$container.removeData('oldLeft');
    tile.$container.removeData('oldTop');
  }, this);
  this.widget.initialAnimationDone = true;

  // when all animations have been finished update scrollbar of the first scrollable parent
  if (promises.length > 0) {
    $.promiseAll(promises).done(function() {
      var htmlComp = this.widget.htmlComp;
      while (htmlComp) {
        if (htmlComp.scrollable) {
          scout.scrollbars.update(htmlComp.$comp);
          break;
        }
        htmlComp = htmlComp.getParent();
      }
    }.bind(this));
  }
};
