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
  this.splitHorizontal = true;
  this.$anchor;
  this.$root;
  this.position = null; // current splitter position in pixels, updated by updatePosition()
  this.orientation = 'top'; // Direction set to position the splitter inside the root element ('top', 'right', 'bottom' or 'left')
  this._cursorOffset = 0; // distance from cursor to splitter, makes resizing smoother by preventing initial 'jump'
  this._mouseDownHandler;
};
scout.inherits(scout.Splitter, scout.Widget);

scout.Splitter.prototype._init = function(model) {
  scout.Splitter.parent.prototype._init.call(this, model);
  this.setPosition(this.position);
};

scout.Splitter.prototype._render = function() {
  this.$container = this.$parent.appendDiv('splitter')
    .addClass(this.splitHorizontal ? 'x-axis' : 'y-axis');
  this.htmlComp = scout.HtmlComponent.install(this.$container, this.session);
  this._$window = this.$parent.window();
  this._$body = this.$parent.body();
};

scout.Splitter.prototype._renderProperties = function() {
  scout.Splitter.parent.prototype._renderProperties.call(this);
  this._renderPosition();
};

scout.Splitter.prototype._renderEnabled = function() {
  scout.Splitter.parent.prototype._renderEnabled.call(this);
  if (this.enabled) {
    if (!this._mouseDownHandler) {
      this._mouseDownHandler = this._onMouseDown.bind(this);
      this.$container.on('mousedown', this._mouseDownHandler);
    }
  } else {
    if (this._mouseDownHandler) {
      this.$container.off('mousedown', this._mouseDownHandler);
      this._mouseDownHandler = null;
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

/**
 * Sets the splitter position to the specified newSize (in pixels). If the newSize is
 * not specified, the size is calculated automatically by reading the this.$anchor
 * element's bounds.
 *
 * @returns the effective position in pixel.
 */
scout.Splitter.prototype.setPosition = function(position) {
  if (!$.isNumeric(position)) {
    position = this._derivePositionFromAnchor();
  }
  if (position === this.position) {
    return;
  }
  this._setPosition(position);
  return position;
};

/**
 * Derives the position from $anchor element's bounds
 */
scout.Splitter.prototype._derivePositionFromAnchor = function() {
  if (!this.$anchor) {
    return null;
  }
  var anchorBounds = scout.graphics.offsetBounds(this.$anchor, {
    exact: true
  });
  if (this.splitHorizontal) {
    return anchorBounds.x + anchorBounds.width;
  } else {
    return anchorBounds.y + anchorBounds.height;
  }
};

scout.Splitter.prototype._setPosition = function(position) {
  if (!$.isNumeric(position)) {
    return;
  }
  if (position === this.position) {
    return;
  }
  this.position = position;
  var event = {
    position: position
  };
  this.trigger('positionChange', event);
  if (this.rendered) {
    this._renderPosition();
  }
};

scout.Splitter.prototype._renderPosition = function() {
  if (this.position === null) {
    return;
  }

  // center splitter around this.position
  var splitterSize = scout.graphics.getVisibleSize(this.$container, true);
  if (this.splitHorizontal) {
    var x = this.position - (splitterSize.width / 2);
    if (this.orientation === 'right') {
      this.$container.cssRight(x);
    } else {
      this.$container.cssLeft(x);
    }
  } else {
    var y = this.position - (splitterSize.height / 2);
    if (this.orientation === 'bottom') {
      this.$container.cssBottom(y);
    } else {
      this.$container.cssTop(y);
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
  var rootBounds = scout.graphics.offsetBounds(this.$root);
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
    setPosition: function(position) {
      this.position = position;
    }
  };
  this.trigger('move', moveEvent);
  if (moveEvent.defaultPrevented) {
    return;
  }
  this._setPosition(moveEvent.position);
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
