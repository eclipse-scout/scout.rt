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
/**
 * Popup menu to switch between outlines.
 */
scout.ViewMenuPopup = function() {
  scout.ViewMenuPopup.parent.call(this);
  this.$tab;
  this.$headBlueprint;
  this.viewMenus;
  this.viewButtonBoxBounds;
  this._tooltip;
  this._addAdapterProperties('viewMenus');
};
scout.inherits(scout.ViewMenuPopup, scout.PopupWithHead);

scout.ViewMenuPopup.MAX_MENU_WIDTH = 300;

scout.ViewMenuPopup.prototype._init = function(options) {
  options.focusableContainer = true;
  scout.ViewMenuPopup.parent.prototype._init.call(this, options);

  this.$tab = options.$tab;
  this.$headBlueprint = this.$tab;
  this.viewButtonBoxBounds = options.naviBounds;
};

scout.ViewMenuPopup.prototype._createLayout = function() {
  return new scout.ViewMenuPopupLayout(this);
};

/**
 * @override Popup.js
 */
scout.ViewMenuPopup.prototype._initKeyStrokeContext = function(keyStrokeContext) {
  scout.ViewMenuPopup.parent.prototype._initKeyStrokeContext.call(this, keyStrokeContext);

  scout.menuNavigationKeyStrokes.registerKeyStrokes(keyStrokeContext, this, 'view-menu-item');
};

scout.ViewMenuPopup.prototype._render = function($parent) {
  scout.ViewMenuPopup.parent.prototype._render.call(this, $parent);

  this.viewMenus.forEach(function(viewMenu) {
    viewMenu.render(this.$body);
    viewMenu.afterSendDoAction = this.close.bind(this);
  }, this);

  // Add last marker to last visible item
  var lastVisibleMenu = scout.arrays.findFromPrev(this.viewMenus, this.viewMenus.length - 1, function(viewMenu) {
    return viewMenu.visible;
  }, this);
  lastVisibleMenu.$container.addClass('last');

  scout.scrollbars.install(this.$body, {
    parent: this,
    axis: 'y'
  });
};

/**
 * @override PopupWithHead.js
 */
scout.ViewMenuPopup.prototype._renderHead = function() {
  scout.ViewMenuPopup.parent.prototype._renderHead.call(this);

  this._copyCssClassToHead('view-button-tab');
  this._copyCssClassToHead('unfocusable');
  this.$head.removeClass('popup-head menu-item');
  this.$head.addClass('view-menu-popup-head');
};

/**
 * @override PopupWithHead.js
 */
scout.ViewMenuPopup.prototype._modifyBody = function() {
  this.$body.removeClass('popup-body');
  this.$body.addClass('view-menu-popup-body');
};

/**
 * @override PopupWithHead.js
 */
scout.ViewMenuPopup.prototype._modifyHeadChildren = function() {
  this.$head.find('.arrow-icon').addClass('menu-open');
};

scout.ViewMenuPopup.prototype.position = function() {
  var pos = this.$tab.offset(),
    headSize = scout.graphics.getSize(this.$tab, true),
    bodyTop = headSize.height;

  scout.graphics.setBounds(this.$head, pos.left, pos.top, headSize.width, headSize.height);

  this.$deco.cssLeft(pos.left);
  this.$deco.cssTop(0);
  this.$deco.cssWidth(headSize.width - 1);

  this.$head.cssTop(-bodyTop);
  this.$body.cssTop(0);
  this.$container.cssMarginTop(headSize.height);

  this.setLocation(new scout.Point(0, 0));
};
