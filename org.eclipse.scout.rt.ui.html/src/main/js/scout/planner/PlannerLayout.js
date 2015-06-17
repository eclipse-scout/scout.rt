scout.PlannerLayout = function(planner) {
  scout.PlannerLayout.parent.call(this);
  this.planner = planner;
};
scout.inherits(scout.PlannerLayout, scout.AbstractLayout);

scout.PlannerLayout.prototype.layout = function($container) {
  var $header = this.planner.$header,
    $scale = this.planner.$scale,
    $grid = this.planner.$grid,
    menuBar = this.planner.menuBar,
    $yearContainer = this.planner.$year.parent(),
    menuBarHeight = 0,
    gridHeight = 0,
    yearContainerHeight = 0,
    gridTop = 0,
    scaleTop = 0;

  if (menuBar.$container.isVisible()) {
    var htmlMenuBar = scout.HtmlComponent.get(menuBar.$container),
    menuBarSize = htmlMenuBar.getPreferredSize();
    htmlMenuBar.setSize(menuBarSize);
    menuBarHeight = menuBarSize.height;
    if (menuBar.position === 'top') {
      scaleTop += menuBarHeight;
    }
  }

  if ($header.isVisible()) {
    scaleTop += scout.graphics.getSize($header).height;
  }
  $scale.css('top', scaleTop);
  gridTop += scaleTop + scout.graphics.getSize($scale).height;
  $grid.css('top', gridTop);

  yearContainerHeight = scaleTop + $yearContainer.cssMarginY();
  gridHeight = gridTop + $grid.cssMarginY();

  if (menuBar.$container.isVisible() && menuBar.position === 'bottom') {
    yearContainerHeight += menuBarHeight;
    gridHeight += menuBarHeight;
  }

  $yearContainer.css('height', 'calc(100% - '+ yearContainerHeight + 'px)');
  $grid.css('height', 'calc(100% - '+ gridHeight + 'px)');

  scout.scrollbars.update(this.planner.$grid);
};

scout.PlannerLayout.prototype.preferredLayoutSize = function($comp) {
  return scout.graphics.getSize($comp);
};
