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
  this.actionStyle = scout.Action.ActionStyle.BUTTON;
  /**
   * Additional CSS class to be applied in _render method.
   */
  this._additionalCssClass = '';
};
scout.inherits(scout.AbstractNavigationButton, scout.Menu);

// FIXME AWE: re-name to *Menu

/**
 * @override
 */
scout.AbstractNavigationButton.prototype._render = function($parent) {
  if (this._isDetail()) {
    this._onClickFunc = this._setDetailVisible.bind(this);
  } else {
    this._onClickFunc = this._drill.bind(this);
  }
  if (this.overflow) {
    this.text = this.session.text(this._defaultText);
    this.iconId = null;
  } else {
    this.text = null;
    this.iconId = this._defaultIconId;
  }
  this.enabled = this._buttonEnabled();
  scout.AbstractNavigationButton.parent.prototype._render.call(this, $parent);
  this.$container.addClass('small');
  this.$container.addClass(this._additionalCssClass);
  this.outline.keyStrokeContext.registerKeyStroke(this);
};

/**
 * @override Action.js
 */
scout.AbstractNavigationButton.prototype._remove = function() {
  scout.AbstractNavigationButton.parent.prototype._remove.call(this);
  this.outline.keyStrokeContext.unregisterKeyStroke(this);
};

scout.AbstractNavigationButton.prototype._setDetailVisible = function() {
  var detailVisible = this._toggleDetail();
  $.log.debug('show detail-' + detailVisible ? 'form' : 'table');
  this.node.detailFormVisibleByUi = detailVisible;
  this.outline._updateOutlineNode(this.node, true);
};

/**
 * @override Menu.js
 */
scout.AbstractNavigationButton.prototype.doAction = function() {
  this._onClickFunc();
  return true;
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
