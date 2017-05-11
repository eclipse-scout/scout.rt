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
 * The MenuBarPopup is a special Popup that is used in the menu-bar. It is tightly coupled with a menu-item and shows a header
 * which has a different size than the popup-body.
 */
scout.MenuBarPopup = function() {
  scout.MenuBarPopup.parent.call(this);
  this.menu;
  this.$headBlueprint;
  this.ignoreEvent;
  this._headVisible = true;
  this.parentMenuPropertyChangeHandler = this._onParentMenuPropertyChange.bind(this);
};
scout.inherits(scout.MenuBarPopup, scout.ContextMenuPopup);

scout.MenuBarPopup.prototype._init = function(options) {
  options.$anchor = options.menu.$container;
  scout.MenuBarPopup.parent.prototype._init.call(this, options);

  this.$headBlueprint = this.menu.$container;
};

/**
 * @override ContextMenuPopup.js
 */
scout.MenuBarPopup.prototype._getMenuItems = function() {
  return this.menu.childActions || this.menu.menus;
};

/**
 * @override Popup.js
 */
scout.MenuBarPopup.prototype.close = function(event) {
  if (!event || !this.ignoreEvent || event.originalEvent !== this.ignoreEvent.originalEvent) {
    scout.MenuBarPopup.parent.prototype.close.call(this, event);
  }
};

scout.MenuBarPopup.prototype._render = function() {
  scout.MenuBarPopup.parent.prototype._render.call(this);
  this.menu.on('propertyChange', this.parentMenuPropertyChangeHandler);
};

scout.MenuBarPopup.prototype._remove = function() {
  this.menu.off('propertyChange', this.parentMenuPropertyChangeHandler);
  scout.MenuBarPopup.parent.prototype._remove.call(this);
};

/**
 * @override PopupWithHead.js
 */
scout.MenuBarPopup.prototype._renderHead = function() {
  scout.MenuBarPopup.parent.prototype._renderHead.call(this);

  // TODO [7.0] awe: throws exception if this.menu is a button because button is not rendered (MenuButtonAdapter is)
  if (this.menu.$container.parent().hasClass('main-menubar')) {
    this.$head.addClass('in-main-menubar');
  }

  if (this.menu.uiCssClass) {
    this._copyCssClassToHead(this.menu.uiCssClass);
  }
  this._copyCssClassToHead('unfocusable');
  this._copyCssClassToHead('button');
};

scout.MenuBarPopup.prototype._onParentMenuPropertyChange = function(event) {
  this.session.layoutValidator.schedulePostValidateFunction(function() {
    // Because this post layout validation function is executed asynchronously,
    // we have to check again if the popup is still rendered.
    if (!this.rendered) {
      return;
    }
    this.rerenderHead();
    this.position();
  }.bind(this));
};
