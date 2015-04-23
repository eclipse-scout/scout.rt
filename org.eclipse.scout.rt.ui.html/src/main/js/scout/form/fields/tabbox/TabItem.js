// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.TabItem = function() {
  scout.TabItem.parent.call(this);
  this._$tabButton;
  this._tabButtonRendered = false;
};
scout.inherits(scout.TabItem, scout.GroupBox);

/**
 * This method has nothing to do with the regular rendering of the GroupBox. It is an additional method
 * to render a single tab for this tab-item. Since tab and tab-item share the same model.
 *
 * @return tab button as JQuery object
 */
scout.TabItem.prototype.renderTab = function($parent, tabIndex) {
  this._$tabButton = $('<button>').
  text(scout.strings.removeAmpersand(this.label)).
  appendTo($parent).
  data('tabIndex', tabIndex);
  this._tabButtonRendered = true;
  this._updateTabButton();
  return this._$tabButton;
};

scout.TabItem.prototype._syncMarked = function(marked) {
  this.marked = marked;
  // Special case: If the group box part of the TabItem is NOT (yet) rendered, but the
  // tabButton IS, render the properties that affect the tab button.
  if (!this.rendered && this._tabButtonRendered) {
    this._updateTabButton();
  }
};

scout.TabItem.prototype._renderMarked = function(marked) {
  this._updateTabButton();
};

scout.TabItem.prototype._syncVisible = function(visible) {
  this.visible = visible;
  // Special case: If the group box part of the TabItem is NOT (yet) rendered, but the
  // tabButton IS, render the properties that affect the tab button.
  if (!this.rendered && this._tabButtonRendered) {
    this._updateTabButton();
  }
};

scout.TabItem.prototype._renderVisible = function(visible) {
  scout.TabItem.parent.prototype._renderVisible.call(this, visible);
  this._updateTabButton();
};

scout.TabItem.prototype._updateTabButton = function() {
  this._$tabButton.toggleClass('marked', this.marked);
  this._$tabButton.setVisible(this.visible);
};
