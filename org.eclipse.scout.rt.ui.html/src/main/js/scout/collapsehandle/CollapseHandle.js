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
  this._addEventSupport();
  this.leftVisible = false;
  this.rightVisible = false;
};
scout.inherits(scout.CollapseHandle, scout.Widget);

scout.CollapseHandle.prototype._init = function(model) {
  scout.CollapseHandle.parent.prototype._init.call(this, model);

  var defaults = {
    leftVisible: true,
    rightVisible: true
  };
  $.extend(this, defaults, model);
};

scout.CollapseHandle.prototype._initKeyStrokeContext = function(keyStrokeContext) {
  scout.CollapseHandle.parent.prototype._initKeyStrokeContext.call(this, keyStrokeContext);

  // Bound to desktop
  this.desktopKeyStrokeContext = new scout.KeyStrokeContext();
  this.desktopKeyStrokeContext.invokeAcceptInputOnActiveValueField = true;
  this.desktopKeyStrokeContext.$bindTarget = function() { return this.$container; }.bind(this); // this.session.desktop.$container; // FIXME awe, cgu: desktop not ready yet?
  this.desktopKeyStrokeContext.$scopeTarget = function() { return this.$container; }.bind(this);
  this.desktopKeyStrokeContext.registerKeyStroke([
    new scout.ShrinkNavigationKeyStroke(this),
    new scout.EnlargeNavigationKeyStroke(this)
  ]);
};

scout.CollapseHandle.prototype._render = function($parent) {
  this.$container = $parent.appendDiv('desktop-navigation-handle');
  this.$container.on('mousedown', this._onMouseDown.bind(this));

  this.$left = this.$container.appendDiv('desktop-navigation-handle-body left');
  this.$right = this.$container.appendDiv('desktop-navigation-handle-body right');
  this.session.keyStrokeManager.installKeyStrokeContext(this.desktopKeyStrokeContext);
};

scout.CollapseHandle.prototype._remove = function() {
  scout.CollapseHandle.parent.prototype._remove.call(this);
  this.session.keyStrokeManager.uninstallKeyStrokeContext(this.desktopKeyStrokeContext);
};

scout.CollapseHandle.prototype._renderProperties = function() {
  this._renderLeftVisible();
  this._renderRightVisible();
};

scout.CollapseHandle.prototype._renderLeftVisible = function() {
  this.$left.setVisible(this.leftVisible);
  this._updateVisibilityClasses();
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

scout.CollapseHandle.prototype.setLeftVisible = function(visible) {
  if (this.leftVisible === visible) {
    return;
  }
  this._setProperty('leftVisible', visible);
  if (this.rendered) {
    this._renderLeftVisible();
  }
};

scout.CollapseHandle.prototype.setRightVisible = function(visible) {
  if (this.rightVisible === visible) {
    return;
  }
  this._setProperty('rightVisible', visible);
  if (this.rendered) {
    this._renderRightVisible();
  }
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
