scout.PlannerLayout = function(planner) {
  scout.PlannerLayout.parent.call(this);
  this.planner = planner;
};
scout.inherits(scout.PlannerLayout, scout.AbstractLayout);

scout.PlannerLayout.prototype.layout = function($container) {
  var $header = this.planner.$header,
    $scale = this.planner.$scale,
    $grid = this.planner.$grid,
    $yearContainer = this.planner.$year.parent(),
    height = 0,
    gridTop = 0,
    scaleTop = 0;

  if ($header.isVisible()) {
    scaleTop += scout.graphics.getSize($header).height;
  }
  $scale.css('top', scaleTop);
  $yearContainer.css('height', 'calc(100% - '+ (scaleTop + $yearContainer.cssMarginY()) + 'px)');

  gridTop += scaleTop + scout.graphics.getSize($scale).height;
  $grid.css('top', gridTop)
    .css('height', 'calc(100% - '+ (gridTop + $grid.cssMarginY()) + 'px)');

  scout.scrollbars.update(this.planner.$grid);
};

scout.PlannerLayout.prototype.preferredLayoutSize = function($comp) {
  return scout.graphics.getSize($comp);
};
