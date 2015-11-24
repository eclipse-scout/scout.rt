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
scout.Splitter = function() {
  scout.Splitter.parent.call(this);

  this.splitHorizontal;
  this.$anchor;
  this._$root;
  this._maxRatio;
  this._oldRatio;
  this.position; // current splitter position in pixels, updated by updatePosition()
  this._addEventSupport();
};
scout.inherits(scout.Splitter, scout.Widget);

scout.Splitter.prototype._init = function(options) {
  scout.BusyIndicator.parent.prototype._init.call(this, options);

  this.splitHorizontal = scout.nvl(options.splitHorizontal, true);
  this.$anchor = options.$anchor;
  this._$root = options.$root;
  this._maxRatio = options.maxRatio;
};

scout.Splitter.prototype._render = function($parent) {
  this.$container = $parent.appendDiv('splitter')
    .addClass(this.splitHorizontal ? 'x-axis' : 'y-axis')
    .on('mousedown', this._onMouseDown.bind(this));
  this._$window = $parent.window();
  this._$body = $parent.body();
};

/**
 * Sets the splitter position to the specified newSize (in pixels). If the newSize is
 * not specified, the size is calculated automatically by reading the this.$anchor
 * element's bounds.
 *
 * Note: This function does not fire any events.
 */
scout.Splitter.prototype.updatePosition = function(newPosition) {
  if (!$.isNumeric(newPosition)) {
    // Get size automatically from $anchor element's bounds
    var anchorBounds = scout.graphics.offsetBounds(this.$anchor);
    if (this.splitHorizontal) {
      newPosition = anchorBounds.x + anchorBounds.width;
    } else {
      newPosition = anchorBounds.y + anchorBounds.height;
    }
  }

  // Set the new position (center splitter around 'newPosition')
  var splitterSize = scout.graphics.getVisibleSize(this.$container, true);
  if (this.splitHorizontal) {
    this.$container.cssLeft(newPosition - (splitterSize.width / 2));
  } else {
    this.$container.cssTop(newPosition - (splitterSize.height / 2));
  }
  this.position = newPosition;
};

scout.Splitter.prototype._onMouseDown = function(event) {
  // Add listeners (we add them to the window to make sure we get the mouseup event even when the cursor it outside the window)
  this._$window
    .on('mousemove.splitter', this._onMouseMove.bind(this))
    .one('mouseup', this._onMouseUp.bind(this));
  // Ensure the correct cursor is always shown while moving
  this._$body.addClass(this.splitHorizontal ? 'col-resize' : 'row-resize');
  this.trigger('resizestart', event);
  // Prevent text selection in a form
  event.preventDefault();
};

scout.Splitter.prototype._ratio = function(event) {
  var splitterBounds = scout.graphics.offsetBounds(this.$container),
    rootBounds = scout.graphics.offsetBounds(this._$root);
  var ratio, rootSize;
  if (this.splitHorizontal) {
    rootSize = rootBounds.width;
    ratio = (event ? event.pageX : splitterBounds.x) / rootBounds.width;
  } else {
    rootSize = rootBounds.height;
    ratio = (event ? event.pageY : splitterBounds.y) / rootBounds.height;
  }
  return {
    ratio: ratio,
    rootSize: rootSize
  };
};

scout.Splitter.prototype._onMouseMove = function(event) {
  var obj = this._ratio(event),
    ratio = obj.ratio;
  if (ratio >= this._maxRatio) {
    ratio = this._maxRatio;
  }
  if (ratio !== this._oldRatio) {
    var newPosition = Math.floor(ratio * obj.rootSize);
    this.updatePosition(newPosition);
    this.trigger('resize', {
      data: newPosition
    });
    this._oldRatio = ratio;
  }
};

scout.Splitter.prototype._onMouseUp = function(event) {
  // Remove listeners and reset cursor
  this._$window.off('mousemove.splitter');
  this._$body.removeClass((this.splitHorizontal ? 'col-resize' : 'row-resize'));
  this.trigger('resizeend', {
    data: this.position
  });
};
