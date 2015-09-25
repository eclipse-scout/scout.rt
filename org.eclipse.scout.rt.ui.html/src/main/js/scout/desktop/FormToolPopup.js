scout.FormToolPopup = function(formToolButton, session, options) {
  options = options || {};
  options.initialFocus = formToolButton.form._initialFocusElement.bind(formToolButton.form);
  scout.FormToolPopup.parent.call(this, session, options);
  this.$formToolButton = formToolButton.$container;
  this.$headBlueprint = this.$formToolButton;
  this.formToolButton = formToolButton;
  formToolButton.form.rootGroupBox.menuBar.bottom();
};
scout.inherits(scout.FormToolPopup, scout.PopupWithHead);

scout.FormToolPopup.prototype._render = function($parent) {
  scout.FormToolPopup.parent.prototype._render.call(this, $parent);
  this.$container.addClass('form-tool-popup');

  var form = this.formToolButton.form;
  form.renderInitialFocusEnabled = false;
  form.render(this.$body);
  form.htmlComp.pixelBasedSizing = true;
  form.htmlComp.pack();
  this.addChild(form);
};

scout.FormToolPopup.prototype._renderHead = function() {
  scout.FormToolPopup.parent.prototype._renderHead.call(this);
  if (this.formToolButton._customCssClasses) {
    this._copyCssClassToHead(this.formToolButton._customCssClasses);
  }
  this._copyCssClassToHead('unfocusable');
};
