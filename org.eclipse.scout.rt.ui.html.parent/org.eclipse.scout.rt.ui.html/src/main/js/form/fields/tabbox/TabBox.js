// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.TabBox = function() {
  scout.TabBox.parent.call(this);
  this._addAdapterProperties(['groupBoxes', 'selectedTab']);
};
scout.inherits(scout.TabBox, scout.FormField);

scout.TabBox.prototype._render = function($parent) {
  this.$container = $parent.appendDiv(undefined, 'tab-box');

  var i, groupBox;
  for (i = 0; i < this.groupBoxes.length; i++) {
    this.groupBoxes[i].render(this.$container);
  }
};
