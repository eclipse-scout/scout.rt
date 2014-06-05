// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.TabBox = function() {
  scout.TabBox.parent.call(this);
};
scout.inherits(scout.TabBox, scout.ModelAdapter);

scout.TabBox.prototype.init = function(model, session) {
  scout.TabBox.parent.prototype.init.call(this, model, session);

  this.groupBoxes = this.session.getOrCreateModelAdapters(this.model.groupBoxes, this);
};

scout.TabBox.prototype._render = function($parent) {
  this.$container = $parent.appendDiv(undefined, 'tab-box');

  var i, groupBox;
  for (i = 0; i < this.groupBoxes.length; i++) {
    this.groupBoxes[i].attach(this.$container);
  }
};
