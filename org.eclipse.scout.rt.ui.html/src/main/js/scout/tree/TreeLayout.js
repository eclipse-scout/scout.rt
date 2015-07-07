scout.TreeLayout = function(tree) {
  scout.TreeLayout.parent.call(this);
  this.tree = tree;
};
scout.inherits(scout.TreeLayout, scout.AbstractLayout);

scout.TreeLayout.prototype.layout = function($container) {
  var menuBarSize,
    menuBar = this.tree.menuBar,
    $data = this.tree.$data,
    height = 0,
    htmlMenuBar = scout.HtmlComponent.get(menuBar.$container),
    htmlContainer = this.tree.htmlComp,
    containerSize = htmlContainer.getAvailableSize()
      .subtract(htmlContainer.getInsets());

  if (menuBar.$container.isVisible()) {
    menuBarSize = scout.MenuBarLayout.size(htmlMenuBar, containerSize);
    htmlMenuBar.setSize(menuBarSize);
    height += menuBarSize.height;
  }
  height += $data.cssMarginTop() + $data.cssMarginBottom();

  $data.css('height', 'calc(100% - ' + height + 'px)');
  scout.scrollbars.update($data);
};
