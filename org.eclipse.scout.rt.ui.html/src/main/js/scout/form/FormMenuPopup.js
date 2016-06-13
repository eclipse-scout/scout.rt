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
};
scout.inherits(scout.FormMenuPopup, scout.PopupWithHead);

scout.FormMenuPopup.prototype._init = function(options) {
  this.formMenu = options.formMenu;
  options.initialFocus = this.formMenu.form._initialFocusElement.bind(this.formMenu.form);
  scout.FormMenuPopup.parent.prototype._init.call(this, options);

  this.$formMenu = this.formMenu.$container;
  this.$headBlueprint = this.$formMenu;
  this.form = this.formMenu.form;
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
  this.form.setParent(this);
};

scout.FormMenuPopup.prototype._renderHead = function() {
  scout.FormMenuPopup.parent.prototype._renderHead.call(this);
  if (this.formMenu._customCssClasses) {
    this._copyCssClassToHead(this.formMenu._customCssClasses);
  }
  if (this.formMenu.cssClass) {
    this._copyCssClassToHead(this.formMenu.cssClass);
  }
  this._copyCssClassToHead('unfocusable');
};
