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
  this.selected = false;
  this.visible = true;
  this.enabled = true;
  this.mandatory = false;
  this.menuStyle = 'button';
};
scout.inherits(scout.AbstractNavigationButton, scout.Menu);
// FIXME AWE: re-name to *Menu

/**
 * @override
 */
scout.AbstractNavigationButton.prototype._render = function($parent) {
  if (this._isDetail()) {
    this._onClickFunc = this._setDetailVisible.bind(this);
    this.text = this.session.text(this._text1);
  } else {
    this._onClickFunc = this._drill.bind(this);
    this.text = this.session.text(this._text2);
  }
  this.enabled = this._buttonEnabled();
  scout.AbstractNavigationButton.parent.prototype._render.call(this, $parent);
};

scout.AbstractNavigationButton.prototype._setDetailVisible = function() {
  var detailVisible = this._toggleDetail();
  $.log.debug('show detail-' + detailVisible ? 'form' : 'table');
  this.node.detailFormVisibleByUi = detailVisible;
  this.outline._updateOutlineTab(this.node);
};

scout.AbstractNavigationButton.prototype.doAction = function() {
  this._onClickFunc();
};

/**
 * Called when enabled state must be re-calculated and probably rendered.
 */
scout.AbstractNavigationButton.prototype.updateEnabled = function() {
  this.enabled = this._buttonEnabled();
  if (this.rendered) {
    this._renderEnabled(this.enabled);
  }
};

/**
 * @override
 */
scout.AbstractNavigationButton.prototype._registerButtonKeyStroke = function() {
  if (this.defaultKeyStroke) {
    this._unregisterButtonKeyStroke();
  }
  if (this.keyStroke) {
    // register buttons key stroke on root group-box
    this.defaultKeyStroke = new scout.ButtonKeyStroke(this, this.keyStroke);
    this.outline.keyStrokeAdapter.registerKeyStroke(this.defaultKeyStroke);
  }
};

/**
 * @override
 */
scout.AbstractNavigationButton.prototype._unregisterButtonKeyStroke = function() {
  // unregister buttons key stroke on root group-box
  if (this.defaultKeyStroke) {
    this.outline.keyStrokeAdapter.unregisterKeyStroke(this.defaultKeyStroke);
  }
};
