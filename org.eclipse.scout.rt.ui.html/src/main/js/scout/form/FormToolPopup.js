scout.FormToolPopup = function() {
  scout.FormToolPopup.parent.call(this);
};
scout.inherits(scout.FormToolPopup, scout.PopupWithHead);

scout.FormToolPopup.prototype._init = function(options) {
  options = options || {};
  this.formToolButton = options.formToolButton;
  options.initialFocus = this.formToolButton.form._initialFocusElement.bind(this.formToolButton.form);
  scout.FormToolPopup.parent.prototype._init.call(this, options);

  this.$formToolButton = this.formToolButton.$container;
  this.$headBlueprint = this.$formToolButton;
  this.formToolButton.form.rootGroupBox.menuBar.bottom();
};

scout.FormToolPopup.prototype._render = function($parent) {
  scout.FormToolPopup.parent.prototype._render.call(this, $parent);
  this.$container.addClass('form-tool-popup');

  var form = this.formToolButton.form;
  form.renderInitialFocusEnabled = false;
  form.render(this.$body);
  form.htmlComp.pixelBasedSizing = true;
  form.htmlComp.pack();
  form.setParent(this);
};

scout.FormToolPopup.prototype._renderHead = function() {
  scout.FormToolPopup.parent.prototype._renderHead.call(this);
  if (this.formToolButton._customCssClasses) {
    this._copyCssClassToHead(this.formToolButton._customCssClasses);
  }
  this._copyCssClassToHead('unfocusable');
};
