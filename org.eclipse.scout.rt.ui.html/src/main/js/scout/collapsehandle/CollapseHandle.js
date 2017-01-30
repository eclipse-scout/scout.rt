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
scout.CollapseHandle = function() {
  scout.CollapseHandle.parent.call(this);
  this.leftVisible = true;
  this.rightVisible = true;
  this.horizontalAlignment = scout.CollapseHandle.HorizontalAlignment.LEFT;
};
scout.inherits(scout.CollapseHandle, scout.Widget);

scout.CollapseHandle.HorizontalAlignment = {
  LEFT: 'left',
  RIGHT: 'right'
};

scout.CollapseHandle.prototype._render = function($parent) {
  this.$container = $parent.appendDiv('collapse-handle');
  this.$container.on('mousedown', this._onMouseDown.bind(this));

  this.$left = this.$container.appendDiv('collapse-handle-body left');
  this.$right = this.$container.appendDiv('collapse-handle-body right');
};

scout.CollapseHandle.prototype._renderProperties = function() {
  scout.CollapseHandle.parent.prototype._renderProperties.call(this);
  this._renderLeftVisible();
  this._renderRightVisible();
  this._renderHorizontalAlignment();
};

scout.CollapseHandle.prototype.setHorizontalAlignment = function(alignment) {
  this.setProperty('horizontalAlignment', alignment);
};

scout.CollapseHandle.prototype._renderHorizontalAlignment = function() {
  this.$container.removeClass('left-aligned right-aligned');
  if (this.horizontalAlignment === scout.CollapseHandle.HorizontalAlignment.LEFT) {
    this.$container.addClass('left-aligned');
  } else if (this.horizontalAlignment === scout.CollapseHandle.HorizontalAlignment.RIGHT) {
    this.$container.addClass('right-aligned');
  }
};

scout.CollapseHandle.prototype.setLeftVisible = function(visible) {
  this.setProperty('leftVisible', visible);
};

scout.CollapseHandle.prototype._renderLeftVisible = function() {
  this.$left.setVisible(this.leftVisible);
  this._updateVisibilityClasses();
};

scout.CollapseHandle.prototype.setRightVisible = function(visible) {
  this.setProperty('rightVisible', visible);
};

scout.CollapseHandle.prototype._renderRightVisible = function() {
  this.$right.setVisible(this.rightVisible);
  this._updateVisibilityClasses();
};

scout.CollapseHandle.prototype._updateVisibilityClasses = function() {
  var bothVisible = this.leftVisible && this.rightVisible;
  this.$container.toggleClass('both-visible', bothVisible);
  this.$left.toggleClass('both-visible', bothVisible);
  this.$right.toggleClass('both-visible', bothVisible);
  this.$container.toggleClass('one-visible', (this.leftVisible || this.rightVisible) && !bothVisible);
};

scout.CollapseHandle.prototype._onLeftMouseDown = function(event) {
  this.trigger('action', {
    left: true
  });
};

scout.CollapseHandle.prototype._onRightMouseDown = function(event) {
  this.trigger('action', {
    right: true
  });
};

scout.CollapseHandle.prototype._onMouseDown = function(event) {
  var target = event.target;
  if (this.$left.isOrHas(target)) {
    this.trigger('action', {
      left: true
    });
    return;
  }
  if (this.$right.isOrHas(target)) {
    this.trigger('action', {
      right: true
    });
    return;
  }

  // If there is only one box visible, trigger also when container was clicked
  // Mainly used to make the pixel on the left clickable, when the handle is visible in bench mode
  if (this.$container.hasClass('one-visible')) {
    this.trigger('action', {
      left: this.leftVisible,
      right: this.rightVisible
    });
  }
};
