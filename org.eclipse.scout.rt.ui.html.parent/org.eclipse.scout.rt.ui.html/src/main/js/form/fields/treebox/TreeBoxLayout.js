scout.TreeBoxLayout = function(tree, filterBox) {
  scout.TreeBoxLayout.parent.call(this);
  this.tree = tree;
  this.filterBox = filterBox;
};
scout.inherits(scout.TreeBoxLayout, scout.AbstractLayout);

scout.TreeBoxLayout.prototype.layout = function($container) {
  var htmlContainer = scout.HtmlComponent.get($container),
    size = htmlContainer.getSize(),
    height = size.height;

  if (this.filterBox && this.filterBox.$container.isVisible()) {
    height -= 30;
  }

  scout.graphics.setSize(this.tree.$container, size.width, height);

  if (this.filterBox && this.filterBox.$container.isVisible()) {
    var htmlFilterBox = scout.HtmlComponent.get(this.filterBox.$container);
    htmlFilterBox.setSize(new scout.Dimension(size.width, 30));
  }
};

scout.TreeBoxLayout.prototype.preferredLayoutSize = function($container) {
  // TODO Tree | Preferred size
  return new scout.Dimension(300, 300);
};
