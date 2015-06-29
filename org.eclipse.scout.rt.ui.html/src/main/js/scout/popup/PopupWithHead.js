scout.PopupWithHead = function(session, options) {
  scout.PopupWithHead.parent.call(this, session, options);
  options = options || {};
  this.$head;
  this.$body;
  this.$deco;
};
scout.inherits(scout.PopupWithHead, scout.Popup);

scout.PopupWithHead.prototype._render = function($parent) {
  scout.PopupWithHead.parent.prototype._render.call(this, $parent);
  this.$body = this.$container.appendDiv('popup-body');
  this._renderHead();
};

scout.PopupWithHead.prototype.rerenderHead = function() {
  this._removeHead();
  this._renderHead();
};

/**
 * Copies html from this.$headBlueprint, if set
 */
scout.PopupWithHead.prototype._renderHead = function() {
  this.$deco = $.makeDiv('popup-deco');
  this.$head = $.makeDiv('popup-head');
  this.$container
    .prepend(this.$head)
    .append(this.$deco);
  this.$head.on('mousedown', '', this._onHeadMouseDown.bind(this));
  if (this.$headBlueprint) {
    this.$head.html(this.$headBlueprint.html());
  }
};

scout.PopupWithHead.prototype._removeHead = function() {
  if (this.$head) {
    this.$head.remove();
  }
  if (this.$deco) {
    this.$deco.remove();
  }
};

scout.PopupWithHead.prototype._copyCssClassToHead = function(className) {
  if (this.$headBlueprint && this.$headBlueprint.hasClass(className)) {
    this.$head.addClass(className);
  }
};

scout.PopupWithHead.prototype._onHeadMouseDown = function(event) {
  if (this.$head && this.$head.isOrHas(event.target)) {
    this.close();
  }
};

scout.PopupWithHead.prototype.appendToBody = function($element) {
  this.$body.append($element);
};

scout.PopupWithHead.prototype.addClassToBody = function(clazz) {
  this.$body.addClass(clazz);
};

scout.PopupWithHead.prototype.position = function() {
  this._position(this.$body);
};
