scout.PopupWithHead = function(session, options) {
  scout.PopupWithHead.parent.call(this, session, options);
  options = options || {};
  this.$head;
  this.$body;
  this.$deco;
  this._headVisible = true;
};
scout.inherits(scout.PopupWithHead, scout.Popup);

scout.PopupWithHead.prototype._render = function($parent) {
  scout.PopupWithHead.parent.prototype._render.call(this, $parent);
  this.$body = this.$container.appendDiv('popup-body');
  if (this._headVisible) {
    this._renderHead();
  }
  this._modifyBody();
};

scout.PopupWithHead.prototype.rerenderHead = function() {
  this._removeHead();
  this._renderHead();
};

/**
 * Copies html from this.$headBlueprint, if set.
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
    this._modifyHeadChildren();
  }
};

/**
 * Sets CSS classes or CSS-properties on the copied children in the head.
 */
scout.PopupWithHead.prototype._modifyHeadChildren = function() {
  // NOP
};

/**
 * Sets CSS classes or CSS-properties on the body.
 */
scout.PopupWithHead.prototype._modifyBody = function() {
  // NOP
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
