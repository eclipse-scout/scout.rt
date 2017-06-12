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
  this.ratio;
  this.position; // current splitter position in pixels, updated by updatePosition()
  this.orientation; // Direction sed to position the splitter inside the root element ('top', 'right', 'bottom' or 'left')
  this._cursorOffset = 0; // distance from cursor to splitter, makes resizing smoother by preventing initial 'jump'
  this._handleMouseDown;
};
scout.inherits(scout.Splitter, scout.Widget);

scout.Splitter.prototype._init = function(options) {
  scout.Splitter.parent.prototype._init.call(this, options);
  this.splitHorizontal = scout.nvl(options.splitHorizontal, true);
  this.$anchor = options.$anchor;
  this._$root = options.$root;
  this.orientation = scout.nvl(options.orientation, (this.splitHorizontal ? 'top' : 'left'));
};

scout.Splitter.prototype._render = function() {
  this.$container = this.$parent.appendDiv('splitter')
    .addClass(this.splitHorizontal ? 'x-axis' : 'y-axis');
  this.htmlComp = scout.HtmlComponent.install(this.$container, this.session);
  this._$window = this.$parent.window();
  this._$body = this.$parent.body();
};

scout.Splitter.prototype._renderEnabled = function() {
  scout.Splitter.parent.prototype._renderEnabled.call(this);
  if (this.enabled) {
    if (!this._handleMouseDown) {
      this._handleMouseDown = this._onMouseDown.bind(this);
      this.$container.on('mousedown', this._handleMouseDown);
    }
  } else {
    if (this._handleMouseDown) {
      this.$container.off('mousedown', this._handleMouseDown);
      this._handleMouseDown = null;
    }
  }
};

scout.Splitter.prototype.setLayoutData = function(layoutData) {
  scout.Splitter.parent.prototype.setLayoutData.call(this, layoutData);
  this.layoutData = layoutData;
};

scout.Splitter.prototype.getLayoutData = function() {
  return this.layoutData;
};

scout.Splitter.prototype.getPosition = function() {
  return this.position;
};

scout.Splitter.prototype.getRatio = function() {
  return this.ratio;
};

/**
 * Sets the splitter position to the specified newSize (in pixels). If the newSize is
 * not specified, the size is calculated automatically by reading the this.$anchor
 * element's bounds.
 *
 * @returns the effective position in pixel.
 * Note: This function does not fire any events.
 */
scout.Splitter.prototype.setPosition = function(newPosition, updateRatio, firePositionChange) {
  if (!$.isNumeric(newPosition)) {
    // Get size automatically from $anchor element's bounds
    var anchorBounds = scout.graphics.offsetBounds(this.$anchor);
    if (this.splitHorizontal) {
      newPosition = anchorBounds.x + anchorBounds.width;
    } else {
      newPosition = anchorBounds.y + anchorBounds.height;
    }
  }
  if (newPosition === this.position) {
    return;
  }

  this._setPosition(newPosition, updateRatio, firePositionChange);
  return newPosition;
};

scout.Splitter.prototype._setPosition = function(newPosition, updateRatio, firePositionChange) {
  firePositionChange = scout.nvl(firePositionChange, true);
  if (!$.isNumeric(newPosition)) {
    return;
  }
  if (newPosition === this.position) {
    return;
  }
  if (updateRatio) {
    if (this.splitHorizontal) {
      this.ratio = newPosition / this._$root.outerWidth(true);
    } else {
      this.ratio = newPosition / this._$root.outerHeight(true);
    }
  }
  this.position = newPosition;
  var positionChangeEvent = {
    position: newPosition
  };
  if (firePositionChange) {
    this.trigger('positionChange', positionChangeEvent);
  }
  if (this.rendered) {
    // Set the new position (center splitter around 'newPosition')
    var splitterSize = scout.graphics.size(this.$container, true);
    if (this.splitHorizontal) {
      var x = newPosition - (splitterSize.width / 2);
      if (this.orientation === 'right') {
        this.$container.cssRight(x);
      } else {
        this.$container.cssLeft(x);
      }
    } else {
      var y = newPosition - (splitterSize.height / 2);
      if (this.orientation === 'bottom') {
        this.$container.cssBottom(y);
      } else {
        this.$container.cssTop(y);
      }
    }
  }
};
scout.Splitter.prototype._onMouseDown = function(event) {
  var splitterCenter = scout.graphics.offsetBounds(this.$container, true).center();

  // Add listeners (we add them to the window to make sure we get the mouseup event even when the cursor it outside the window)
  this._$window
    .on('mousemove.splitter', this._onMouseMove.bind(this))
    .one('mouseup', this._onMouseUp.bind(this));
  // Ensure the correct cursor is always shown while moving
  this._$body.addClass(this.splitHorizontal ? 'col-resize' : 'row-resize');
  $('iframe').addClass('dragging-in-progress');
  this._cursorOffset = {
    left: splitterCenter.x - event.pageX,
    top: splitterCenter.y - event.pageY
  };
  this.trigger('moveStart', {
    position: this._getSplitterPosition(event)
  });
  // Prevent text selection in a form
  event.preventDefault();
};

scout.Splitter.prototype._getSplitterPosition = function(event) {
  var rootBounds = scout.graphics.offsetBounds(this._$root);
  if (this.splitHorizontal) {
    var x = event.pageX + this._cursorOffset.left - rootBounds.x;
    return (this.orientation === 'right' ? rootBounds.width - x : x);
  } else {
    var y = event.pageY + this._cursorOffset.top - rootBounds.y;
    return (this.orientation === 'bottom' ? rootBounds.height - y : y);
  }
};

scout.Splitter.prototype._onMouseMove = function(event) {
  var splitterPosition = this._getSplitterPosition(event);
  // fire event
  var moveEvent = {
    position: splitterPosition,
    defaultPrevented: false,
    preventDefault: function() {
      this.defaultPrevented = true;
    },
    setPosition: function(newPosition) {
      this.position = newPosition;
    }
  };
  this.trigger('move', moveEvent);
  if (moveEvent.defaultPrevented) {
    return;
  }
  this._setPosition(moveEvent.position, true);
};

scout.Splitter.prototype._onMouseUp = function(event) {
  // Remove listeners and reset cursor
  this._$window.off('mousemove.splitter');
  this._$body.removeClass((this.splitHorizontal ? 'col-resize' : 'row-resize'));
  $('iframe').removeClass('dragging-in-progress');
  this.trigger('moveEnd', {
    position: this.position
  });
};
