scout.TreeCompactLayout = function(tree) {
  scout.TreeCompactLayout.parent.call(this);
  this.tree = tree;
};
scout.inherits(scout.TreeCompactLayout, scout.AbstractLayout);

scout.TreeCompactLayout.prototype.layout = function($container) {
  var $filter = this.tree.$filter,
    $nodesWrapper = this.tree.$nodesWrapper,
    height = 0;

  height += scout.graphics.getSize($filter).height;
  height += $nodesWrapper.cssMarginTop() + $nodesWrapper.cssMarginBottom();

  $nodesWrapper.css('height', 'calc(100% - ' + height + 'px)');
  scout.scrollbars.update($nodesWrapper);
};

scout.TreeCompactLayout.prototype.preferredLayoutSize = function($comp) {
  return scout.graphics.getSize($comp);
};
