// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.PlannerField = function() {
  scout.PlannerField.parent.call(this);
  this._addAdapterProperties(['planner']);
};
scout.inherits(scout.PlannerField, scout.FormField);

scout.PlannerField.prototype._render = function($parent) {
  this.addContainer($parent, 'planner-field');
  this.addLabel();
  this.addStatus();
  if (this.planner) {
    this._renderPlanner();
  }
};

/**
 * Will also be called by model adapter on property change event
 */
scout.PlannerField.prototype._renderPlanner = function() {
  this.planner.render(this.$container);
  this.addField(this.planner.$container);
};

scout.PlannerField.prototype._removePlanner = function(oldPlanner) {
  oldPlanner.remove();
  this.removeField();
};

scout.PlannerField.prototype._renderSplitterPosition = function() {
};
