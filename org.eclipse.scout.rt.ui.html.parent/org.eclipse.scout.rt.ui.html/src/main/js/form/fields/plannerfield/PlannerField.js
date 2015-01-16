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
  // TODO Implement rendering!
  if (this.resourceTable) {
    this.resourceTable.render(this.$container);
    this.addField(this.resourceTable.$container);
  }
  if (this.activityMap) {
    this.activityMap.render(this.$container);
    this.addField(this.activityMap.$container);
  }
};
