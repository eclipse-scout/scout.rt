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
scout.FormToolPopup = function() {
  scout.FormToolPopup.parent.call(this);
};
scout.inherits(scout.FormToolPopup, scout.PopupWithHead);

scout.FormToolPopup.prototype._init = function(options) {
  this.formToolButton = options.formToolButton;
  options.initialFocus = this.formToolButton.form._initialFocusElement.bind(this.formToolButton.form);
  scout.FormToolPopup.parent.prototype._init.call(this, options);

  this.$formToolButton = this.formToolButton.$container;
  this.$headBlueprint = this.$formToolButton;
  this.form = this.formToolButton.form;
  this.form.rootGroupBox.menuBar.bottom();
};

scout.FormToolPopup.prototype._createLayout = function() {
  return new scout.FormToolPopupLayout(this);
};

scout.FormToolPopup.prototype._render = function($parent) {
  scout.FormToolPopup.parent.prototype._render.call(this, $parent);
  this.$container.addClass('form-tool-popup');

  this.form.renderInitialFocusEnabled = false;
  this.form.render(this.$body);
  this.form.htmlComp.pixelBasedSizing = true;
  this.form.setParent(this);
};

scout.FormToolPopup.prototype._renderHead = function() {
  scout.FormToolPopup.parent.prototype._renderHead.call(this);
  if (this.formToolButton._customCssClasses) {
    this._copyCssClassToHead(this.formToolButton._customCssClasses);
  }
  if (this.formToolButton.cssClass) {
    this._copyCssClassToHead(this.formToolButton.cssClass);
  }
  this._copyCssClassToHead('unfocusable');
};
