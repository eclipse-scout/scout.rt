// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.TabBox = function() {
  scout.TabBox.parent.call(this);
};
scout.inherits(scout.TabBox, scout.ModelAdapter);

scout.TabBox.prototype._render = function($parent) {
  this.$container = $parent.appendDiv(undefined, 'tab-box');

  if (this.model.groupBoxes) {
    var i, groupBoxModel, groupBoxWidget;
    for (i = 0; i < this.model.groupBoxes.length; i++) {
      groupBoxModel = this.model.groupBoxes[i];
      groupBoxWidget = this.session.modelAdapterRegistry[groupBoxModel.id];
      if (!groupBoxWidget) {
        groupBoxWidget = this.session.objectFactory.create(groupBoxModel);
      }
      groupBoxWidget.attach(this.$container);
    }
  }
};

scout.TabBox.prototype.onModelPropertyChange = function(event) {
};
