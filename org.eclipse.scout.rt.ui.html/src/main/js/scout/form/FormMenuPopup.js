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
scout.FormMenuPopup = function() {
  scout.FormMenuPopup.parent.call(this);
  this.formMenu;
  this._addAdapterProperties('form');
};
scout.inherits(scout.FormMenuPopup, scout.PopupWithHead);

scout.FormMenuPopup.prototype._init = function(options) {
  options.form = options.formMenu.form;
  options.initialFocus = options.formMenu.form._initialFocusElement.bind(options.formMenu.form);
  scout.FormMenuPopup.parent.prototype._init.call(this, options);

  this.$formMenu = this.formMenu.$container;
  this.$headBlueprint = this.$formMenu;
};

scout.FormMenuPopup.prototype._createLayout = function() {
  return new scout.FormMenuPopupLayout(this);
};

scout.FormMenuPopup.prototype._render = function($parent) {
  scout.FormMenuPopup.parent.prototype._render.call(this, $parent);
  this.$container.addClass('form-menu-popup');

  this.form.renderInitialFocusEnabled = false;
  this.form.render(this.$body);
  this.form.htmlComp.pixelBasedSizing = true;
};

scout.FormMenuPopup.prototype._renderHead = function() {
  scout.FormMenuPopup.parent.prototype._renderHead.call(this);
  if (this.formMenu.uiCssClass) {
    this._copyCssClassToHead(this.formMenu.uiCssClass);
  }
  if (this.formMenu.cssClass) {
    this._copyCssClassToHead(this.formMenu.cssClass);
  }
  this._copyCssClassToHead('unfocusable');
};
