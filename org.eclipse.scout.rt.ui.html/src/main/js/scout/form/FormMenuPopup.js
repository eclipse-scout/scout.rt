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
  this.formMenuPropertyChangeHandler = this._onFormMenuPropertyChange.bind(this);
  this._addWidgetProperties('form');
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

scout.FormMenuPopup.prototype._render = function() {
  scout.FormMenuPopup.parent.prototype._render.call(this);
  this.$container.addClass('form-menu-popup');

  this.form.renderInitialFocusEnabled = false;
  this.form.render(this.$body);
  this.form.htmlComp.pixelBasedSizing = true;

  // We add this here for symmetry reasons (because _removeHead is not called on remove())
  if (this._headVisible) {
    this.formMenu.on('propertyChange', this.formMenuPropertyChangeHandler);
  }
};

scout.FormMenuPopup.prototype._remove = function() {
  scout.FormMenuPopup.parent.prototype._remove.call(this);

  if (this._headVisible) {
    this.formMenu.off('propertyChange', this.formMenuPropertyChangeHandler);
  }
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

scout.FormMenuPopup.prototype._onFormMenuPropertyChange = function(event) {
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
