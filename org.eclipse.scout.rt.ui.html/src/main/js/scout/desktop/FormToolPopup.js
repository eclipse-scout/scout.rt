scout.FormToolPopup = function(formToolButton, session) {
  scout.FormToolPopup.parent.call(this, session, {
      initialFocus: formToolButton.form._initialFocusElement.bind(formToolButton.form)
  });
  this.$formToolButton = formToolButton.$container;
  this.$headBlueprint = this.$formToolButton;
  this.formToolButton = formToolButton;
  formToolButton.form.rootGroupBox.menuBar.bottom();
};
scout.inherits(scout.FormToolPopup, scout.PopupWithHead);

scout.FormToolPopup.prototype._render = function($parent) {
  scout.FormToolPopup.parent.prototype._render.call(this, $parent);
  this.$container.addClass('form-tool-popup');
  this.$body.addClass('down'); // FormToolButtons always open the popup downwards

  var form = this.formToolButton.form;
  form.renderInitialFocusEnabled = false;
  form.render(this.$body);
  form.htmlComp.pixelBasedSizing = true;
  form.htmlComp.pack();

  this.alignTo();
};

scout.FormToolPopup.prototype._renderHead = function() {
  scout.FormToolPopup.parent.prototype._renderHead.call(this);
  this._copyCssClassToHead('taskbar-tool-item');
  this._copyCssClassToHead('unfocusable');
  this.$head.addClass('selected');
};

scout.FormToolPopup.prototype.detach = function() {
  this._detachCloseHandler();
  this._uninstallKeyStrokeAdapter();

  this.session.detachHelper.beforeDetach(this.$container);
  this.$container.detach();
};

scout.FormToolPopup.prototype.attach = function() {
  this.session.$entryPoint.append(this.$container);
  this.formToolButton.form.validateLayout(); // Form layout may have become invalid during detach (i.e. closing the popup)
  this.alignTo();

  this._triggerPopupOpenEvent();
  this._attachCloseHandler();
  this._installKeyStrokeAdapter();

  this.session.detachHelper.afterAttach(this.$container);

};

scout.FormToolPopup.prototype._onMouseDownOutside = function(event) {
  // close popup only if source of event is not this button or it's child (icon).
  if (this.formToolButton.$container.isOrHas(event.target)) {
    return;
  }

  this.close();
};

scout.FormToolPopup.prototype.alignTo = function() {
  // FIXME NBU: add hack to trigger this function after resources loaded(fonts);
  var pos = this.$formToolButton.offset(),
    headSize = scout.graphics.getSize(this.$head, true),
    bodyWidth = scout.graphics.getSize(this.$body, true).width;

  // horiz. alignment
  var left = pos.left,
    top = pos.top,
    headInsets = scout.graphics.getInsets(this.$head),
    bodyTop = headSize.height;

  $.log.debug('bodyWidth=' + bodyWidth + ' pos=[left' + pos.left + ' top=' + pos.top + '] headSize=' + headSize +
    ' headInsets=' + headInsets + ' left=' + left + ' top=' + top);
  this.$body.cssTop(bodyTop);
  var containerOffsetBounds = scout.graphics.offsetBounds(this.formToolButton.$container),
    right = 0 - containerOffsetBounds.width;
  this.$body
    .css('right', right);
  this.$deco.cssTop(bodyTop);
  this.$head.cssLeft(0);
  this.$deco.cssLeft(1).width(headSize.width - 2);

  this.setLocation(new scout.Point(left, top));
};

/**
 * @override Popup.js
 */
scout.FormToolPopup.prototype.close = function() {
  this.formToolButton.setSelected(false);
};
