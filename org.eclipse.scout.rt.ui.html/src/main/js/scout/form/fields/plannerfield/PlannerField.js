// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.PlannerField = function() {
  scout.PlannerField.parent.call(this);
  this._addAdapterProperties(['resourceTable', 'activityMap']);
};
scout.inherits(scout.PlannerField, scout.FormField);

scout.PlannerField.prototype._render = function($parent) {
  this.addContainer($parent, 'planner-field');
  this.addLabel();
  this.addStatus();
  this.addFieldContainer($.makeDiv());
  // TODO Implement rendering!
  if (this.resourceTable) {
    this.resourceTable.render(this.$fieldContainer);
    this.resourceTable.$container.cssWidth(150);
  }
  if (this.activityMap) {
    this.activityMap.render(this.$fieldContainer);
    this.activityMap.$container
      .cssTop(0)
      .cssLeft(150);
  }
};

scout.PlannerField.prototype._renderMiniCalendarCount = function() {
};

scout.PlannerField.prototype._renderSplitterPosition = function() {
};
