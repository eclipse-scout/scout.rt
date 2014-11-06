// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.TabItem = function() {
  scout.TabItem.parent.call(this);
  this._$tabItem;
};
scout.inherits(scout.TabItem, scout.GroupBox);


/**
 * This method has nothing to do with the regular rendering of the GroupBox. It is an additional method
 * to render a single tab for this tab-item. Since tab and tab-item share the same model.
 * @return tab as JQuery selector
 */
scout.TabItem.prototype.renderTab = function($parent, tabIndex) {
  this._$tabItem = $('<button>').
    text(this.label).
    appendTo($parent).
    data('tabIndex', tabIndex);
  this._renderMarked();
  return this._$tabItem;
};

scout.TabItem.prototype._renderMarked = function() {
  this._$tabItem.toggleClass('marked', this.marked);
};
