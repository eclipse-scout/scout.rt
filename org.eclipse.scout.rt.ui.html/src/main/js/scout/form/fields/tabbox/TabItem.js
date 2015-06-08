// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.TabItem = function() {
  scout.TabItem.parent.call(this);
  this.$tabContainer;
  this._tabRendered = false;
  this._tabSelected = false;
  this._tabTabbable = false;
//  this._tabIndex    = 0;
};
scout.inherits(scout.TabItem, scout.GroupBox);

/**
 * This method has nothing to do with the regular rendering of the GroupBox. It is an additional method
 * to render a single tab for this tab-item. Since tab and tab-item share the same model.
 *
 * @return tab button as JQuery object
 */
scout.TabItem.prototype.renderTab = function($parent) {
  if (this._tabRendered) {
    throw new Error('Tab already rendered');
  }
  this.$tabContainer = $('<button>')
    .text(scout.strings.removeAmpersand(this.label))
    .appendTo($parent)
    .data('tabItem', this)
    .on('mousedown', this._onTabMouseDown.bind(this));

  this._renderTabTabbable();
  this._renderTabSelected();
  this._tabRendered = true;
  this._updateTabButton();
};

scout.TabItem.prototype._onTabMouseDown = function(event) {
  this.parent._selectTab(this);
};



scout.TabItem.prototype.focusTab = function() {
  $.log.info('focusTab =' + this + ' tabindex=' + this.$tabContainer.attr('tabindex'));
  this.$tabContainer.focus();
};

scout.TabItem.prototype.setTabSelected = function(tabSelected) {
  var oldTabSelected = this._tabSelected;
  this._tabSelected = tabSelected;
  if (oldTabSelected != tabSelected) {
    this._renderTabSelected();
  }
};

scout.TabItem.prototype._renderTabSelected = function() {
  this.$tabContainer.select(this._tabSelected);
};

scout.TabItem.prototype.setTabTabbable = function(tabTabbable) {
  var oldTabTabbable = this._tabTabbable;
  this._tabTabbable = tabTabbable;
  if (oldTabTabbable != tabTabbable) {
    this._renderTabTabbable();
  }
};

scout.TabItem.prototype._renderTabTabbable = function() {
  if (this._tabTabbable) {
    this.$tabContainer.removeAttr('tabindex');
  } else {
    this.$tabContainer.attr('tabindex', -1);
  }
};

scout.TabItem.prototype.removeTab = function() {
  $.log.info('removeTab =' + this);
  this.$tabContainer.remove();
  this._tabRendered = false;
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
  this.$tabContainer.toggleClass('marked', this.marked);
  this.$tabContainer.setVisible(this.visible);
};
