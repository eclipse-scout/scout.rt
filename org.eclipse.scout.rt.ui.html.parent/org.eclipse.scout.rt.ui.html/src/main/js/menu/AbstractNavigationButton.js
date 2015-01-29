/**
 * The outline navigation works mostly browser-side. The navigation logic is implemented in JavaScript.
 * When a navigation button is clicked, we process that click browser-side first and send an event to
 * the server which nodes have been selected. We do that for better user experience. In a first attempt
 * the whole navigation logic was on the server, which caused a lag and flickering in the UI.
 */
scout.AbstractNavigationButton = function(outline, node) {
  scout.AbstractNavigationButton.parent.call(this);
  this.node = node;
  this.outline = outline;
  this.session = outline.session;
  this._onClickFunc;
  this.visible = true;
  this.enabled = true;
  this.mandatory = false;
};
scout.inherits(scout.AbstractNavigationButton, scout.Button);

// @override
scout.AbstractNavigationButton.prototype._onClick = function() {
  this._onClickFunc();
};

// @override
scout.AbstractNavigationButton.prototype._render = function($parent) {
  if (this.node.detailForm && this._isDetail()) {
    this._onClickFunc = this._setDetailVisible.bind(this);
    this.label = this.session.text(this._text1);
  } else {
    this._onClickFunc = this._drill.bind(this);
    this.label = this.session.text(this._text2);
  }
  this.enabled = this._buttonEnabled();
  scout.AbstractNavigationButton.parent.prototype._render.call(this, $parent);
};

scout.AbstractNavigationButton.prototype._setDetailVisible = function() {
  var detailVisible = this._toggleDetail();
  $.log.debug('show detail-' + detailVisible ? 'form' : 'table');
  this.node.detailFormVisible = detailVisible;
  this.outline._updateOutlineTab(this.node);
};

