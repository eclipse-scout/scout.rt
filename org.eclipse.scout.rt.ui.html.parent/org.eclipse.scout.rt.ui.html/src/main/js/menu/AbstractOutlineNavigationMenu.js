/**
 * The outline navigation works mostly browser-side. The navigation logic is implemented in JavaScript.
 * When a navigation button is clicked, we process that click browser-side first and send an event to
 * the server which nodes have been selected. We do that for better user experience. In a first attempt
 * the whole navigation logic was on the server, which caused a lag and flickering in the UI.
 */
scout.AbstractOutlineNavigationMenu = function(outline, node) {
  this.node = node;
  this.outline = outline;
  this.session = outline.session;
  this._onClick;
  this.childMenus = [];
  this.visible = true;
};

scout.AbstractOutlineNavigationMenu.prototype.render = function($parent) {
  this._initMenu();
  this.$container = $('<button>')
    .appendTo($parent)
    .addClass('menu-button')
    .text(this.text)
    .on('click', '', this._onClick.bind(this))
    .setEnabled(this._menuEnabled());

  if (this.defaultMenu) {
    this.$container.addClass('default-button');
  }
};

scout.AbstractOutlineNavigationMenu.prototype._initMenu = function() {
  if (this.node.detailForm && this._isDetail()) {
    this._onClick = this._setDetailVisible.bind(this);
    this.text = this.session.text(this._text1);
  } else {
    this._onClick = this._drill.bind(this);
    this.text = this.session.text(this._text2);
  }
};

scout.AbstractOutlineNavigationMenu.prototype._setDetailVisible = function() {
  var detailVisible = this._toggleDetail();
  $.log.debug('show detail-' + detailVisible ? 'form' : 'table');
  this.node.detailFormVisible = detailVisible;
  this.outline._updateOutlineTab(this.node);
};

scout.AbstractOutlineNavigationMenu.prototype.remove = function() {
  this.$container.remove();
};
