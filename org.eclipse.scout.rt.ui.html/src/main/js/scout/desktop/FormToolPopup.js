scout.FormToolPopup = function(formToolButton, session) {
  scout.FormToolPopup.parent.call(this, session);
  this.$formToolButton = formToolButton.$container;
  this.formToolButton = formToolButton;
  this.$head;
  this.$deco;
};
scout.inherits(scout.FormToolPopup, scout.Popup);

scout.FormToolPopup.prototype._render = function($parent) {
  scout.FormToolPopup.parent.prototype._render.call(this, $parent);
  this.$head = $.makeDiv('popup-head');
  this.$deco = $.makeDiv('popup-deco');
  this.$container
    .prepend(this.$head)
    .append(this.$deco);

  var text = this.$formToolButton.text(),
    dataIcon = this.$formToolButton.attr('data-icon');

  this.$head.text(text);
  if (dataIcon) {
    this.$head.attr('data-icon', dataIcon);
  }
  this._copyCssClass('taskbar-tool-item');
  this.$head.addClass('selected');

  this.formToolButton.form.rootGroupBox.menuBarPosition = 'bottom';
  this.formToolButton.form.render(this.$body);
  this.formToolButton.form.htmlComp.pixelBasedSizing = true;
  this.formToolButton.form.htmlComp.pack();
  this.alignTo();
};

scout.FormToolPopup.prototype._copyCssClass = function(className) {
  if (this.$formToolButton.hasClass(className)) {
    this.$head.addClass(className);
  }
};

scout.FormToolPopup.prototype.detach = function() {
  this._detachCloseHandler();
  this._uninstallKeyStrokeAdapter();
  this.$container.uninstallFocusContext(this.session.uiSessionId);
  this.$container.detach();
};

scout.FormToolPopup.prototype.attach = function() {
  var $docBody = this.session.$entryPoint;
  $docBody.append(this.$container);
  this._attachCloseHandler();
  this._installKeyStrokeAdapter();
  setTimeout(function() {
    this.$container.installFocusContext('auto', this.session.uiSessionId);
    this.$container.focus();
  }.bind(this));
  this.alignTo();
};

scout.FormToolPopup.prototype._onMouseDownOutside = function(event) {
  // close popup only if source of event is not this button.
  if (this.formToolButton.$container[0] === event.target) {
    return;
  }
  this.formToolButton.setSelected(false);
  this.detach();
};

scout.FormToolPopup.prototype.alignTo = function() {
  //TODO nbu add hack to trigger this function after resources loaded(fonts);
  var pos = this.$formToolButton.offset(),
    headSize = scout.graphics.getSize(this.$head, true),
    bodyWidth = scout.graphics.getSize(this.$body, true).width;

  // horiz. alignment
  var left = pos.left,
    top = pos.top,
    headInsets = scout.graphics.getInsets(this.$head),
    bodyTop = headSize.height - 1;

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

scout.FormToolPopup.prototype.closePopup = function() {
  this.formToolButton.setSelected(false);
};

scout.FormToolPopup.prototype._onMouseDown = function(event) {
  if(this.$head && this.$head[0]===event.target){
    this.closePopup();
  }
};
