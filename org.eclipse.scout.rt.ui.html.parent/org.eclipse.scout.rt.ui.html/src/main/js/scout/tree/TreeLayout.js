scout.TreeLayout = function(tree) {
  scout.TreeLayout.parent.call(this);
  this.tree = tree;
};
scout.inherits(scout.TreeLayout, scout.AbstractLayout);

scout.TreeLayout.prototype.layout = function($container) {
  var menuBar = this.tree.menuBar,
    $data = this.tree.$data,
    height = 0;

  if (menuBar.$container.isVisible()) {
    height += scout.graphics.getSize(menuBar.$container).height;
  }
  height += $data.cssMarginTop() + $data.cssMarginBottom();
  $data.css('height', 'calc(100% - ' + height + 'px)');

  scout.scrollbars.update(this.tree.$data);
};

scout.TreeLayout.prototype.preferredLayoutSize = function($comp) {
  return scout.graphics.getSize($comp);
};
