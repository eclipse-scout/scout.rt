scout.TabBoxLayout = function(tabBox) {
  scout.TabBoxLayout.parent.call(this);
  this._tabBox = tabBox;
};
scout.inherits(scout.TabBoxLayout, scout.AbstractLayout);

scout.TabBoxLayout.prototype.layout = function($container) {
  var containerSize, tabContentSize, htmlTabArea, tabAreaSize,
    htmlContainer = scout.HtmlComponent.get($container),
    $tabArea = this._tabBox._$tabArea,
    $tabContent = this._tabBox._$tabContent,
    htmlTabContent = scout.HtmlComponent.get($tabContent),
    tabAreaHeight = 0;

  containerSize = htmlContainer.getAvailableSize()
    .subtract(htmlContainer.getInsets());

  if ($tabArea.isVisible()) {
    htmlTabArea = scout.HtmlComponent.get($tabArea);
    tabAreaSize = containerSize.subtract(htmlTabArea.getMargins());
    tabAreaHeight = $tabArea.outerHeight(true);
  }

  tabContentSize = containerSize.subtract(htmlTabContent.getMargins());
  tabContentSize.height -= tabAreaHeight;

  htmlTabContent.setSize(tabContentSize);
};

/**
 * Preferred size of the tab-box aligns every tab-item in a single line, so that each item is visible.
 */
scout.TabBoxLayout.prototype.preferredLayoutSize = function($container) {
  var prefSize,
    htmlContainer = scout.HtmlComponent.get($container),
    $tabArea = this._tabBox._$tabArea,
    $tabContent = this._tabBox._$tabContent,
    htmlTabContent = scout.HtmlComponent.get($tabContent),
    tabAreaSize = new scout.Dimension(),
    tabContentSize = new scout.Dimension();

  tabAreaSize = scout.graphics.getVisibleSize($tabArea, true);
  tabContentSize = htmlTabContent.getPreferredSize()
    .add(htmlContainer.getInsets())
    .add(htmlTabContent.getMargins());

  prefSize = new scout.Dimension(
    Math.max(tabAreaSize.width, tabContentSize.width),
    tabContentSize.height + tabAreaSize.height);

  return prefSize;
};
