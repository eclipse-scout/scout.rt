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
scout.DesktopNavigationHandle = function() {
  scout.DesktopNavigationHandle.parent.call(this);
  this._addEventSupport();
  this.leftVisible = false;
  this.rightVisible = false;
};
scout.inherits(scout.DesktopNavigationHandle, scout.Widget);

scout.DesktopNavigationHandle.prototype._init = function(model) {
  scout.DesktopNavigationHandle.parent.prototype._init.call(this, model);

  var defaults = {
    leftVisible: true,
    rightVisible: true
  };
  $.extend(this, defaults, model);
};

scout.DesktopNavigationHandle.prototype._initKeyStrokeContext = function(keyStrokeContext) {
  scout.DesktopNavigationHandle.parent.prototype._initKeyStrokeContext.call(this, keyStrokeContext);

  // Bound to desktop
  this.desktopKeyStrokeContext = new scout.KeyStrokeContext();
  this.desktopKeyStrokeContext.invokeAcceptInputOnActiveValueField = true;
  this.desktopKeyStrokeContext.$bindTarget = this.session.desktop.$container;
  this.desktopKeyStrokeContext.$scopeTarget = this.$container;
  this.desktopKeyStrokeContext.registerKeyStroke([
    new scout.ShrinkNavigationKeyStroke(this),
    new scout.EnlargeNavigationKeyStroke(this)
  ]);
};

scout.DesktopNavigationHandle.prototype._render = function($parent) {
  this.$container = $parent.appendDiv('desktop-navigation-handle');
  this.$container.on('mousedown', this._onMouseDown.bind(this));

  this.$left = this.$container.appendDiv('desktop-navigation-handle-body left');
  this.$right = this.$container.appendDiv('desktop-navigation-handle-body right');
  this.session.keyStrokeManager.installKeyStrokeContext(this.desktopKeyStrokeContext);
};

scout.DesktopNavigationHandle.prototype._remove = function() {
  scout.DesktopNavigationHandle.parent.prototype._remove.call(this);
  this.session.keyStrokeManager.uninstallKeyStrokeContext(this.desktopKeyStrokeContext);
};

scout.DesktopNavigationHandle.prototype._renderProperties = function() {
  this._renderLeftVisible();
  this._renderRightVisible();
};

scout.DesktopNavigationHandle.prototype._renderLeftVisible = function() {
  this.$left.setVisible(this.leftVisible);
  this._updateVisibilityClasses();
};

scout.DesktopNavigationHandle.prototype._renderRightVisible = function() {
  this.$right.setVisible(this.rightVisible);
  this._updateVisibilityClasses();
};

scout.DesktopNavigationHandle.prototype._updateVisibilityClasses = function() {
  var bothVisible = this.leftVisible && this.rightVisible;
  this.$container.toggleClass('both-visible', bothVisible);
  this.$left.toggleClass('both-visible', bothVisible);
  this.$right.toggleClass('both-visible', bothVisible);
  this.$container.toggleClass('one-visible', (this.leftVisible || this.rightVisible) && !bothVisible);
};

scout.DesktopNavigationHandle.prototype.setLeftVisible = function(visible) {
  if (this.leftVisible === visible) {
    return;
  }
  this._setProperty('leftVisible', visible);
  if (this.rendered) {
    this._renderLeftVisible();
  }
};

scout.DesktopNavigationHandle.prototype.setRightVisible = function(visible) {
  if (this.rightVisible === visible) {
    return;
  }
  this._setProperty('rightVisible', visible);
  if (this.rendered) {
    this._renderRightVisible();
  }
};

scout.DesktopNavigationHandle.prototype._onLeftMouseDown = function(event) {
  this.trigger('action', {
    left: true
  });
};

scout.DesktopNavigationHandle.prototype._onRightMouseDown = function(event) {
  this.trigger('action', {
    right: true
  });
};

scout.DesktopNavigationHandle.prototype._onMouseDown = function(event) {
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
