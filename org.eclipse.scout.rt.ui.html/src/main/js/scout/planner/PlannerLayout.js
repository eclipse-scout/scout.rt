scout.PlannerLayout = function(planner) {
  scout.PlannerLayout.parent.call(this);
  this.planner = planner;
};
scout.inherits(scout.PlannerLayout, scout.AbstractLayout);

scout.PlannerLayout.prototype.layout = function($container) {
  scout.scrollbars.update(this.planner.$grid);
};

scout.PlannerLayout.prototype.preferredLayoutSize = function($comp) {
  return scout.graphics.getSize($comp);
};
