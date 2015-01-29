scout.TabBoxLayout = function() {
  scout.TabBoxLayout.parent.call(this);
};
scout.inherits(scout.TabBoxLayout, scout.AbstractLayout);

scout.TabBoxLayout.prototype.layout = function($container) {
  var containerSize, tabContentSize, htmlTabArea, tabAreaSize,
    htmlContainer = scout.HtmlComponent.get($container),
    $tabContent = $container.children('.tab-content'),
    htmlTabContent = scout.HtmlComponent.get($tabContent),
    $tabArea = $container.children('.tab-area'),
    tabAreaHeight = 0;

  containerSize = htmlContainer.getAvailableSize()
    .subtract(htmlContainer.getInsets());

  if ($tabArea.isVisible()) {
    htmlTabArea = scout.HtmlComponent.get($tabArea);
    tabAreaSize = containerSize.subtract(htmlTabArea.getMargins());
    $tabArea.cssWidth(tabAreaSize.width);
    tabAreaHeight = $tabArea.outerHeight(true);
  }

  tabContentSize = containerSize.subtract(htmlTabContent.getMargins());
  tabContentSize.height -= tabAreaHeight;

  htmlTabContent.setSize(tabContentSize);
};

scout.TabBoxLayout.prototype.preferredLayoutSize = function($container) {
  var htmlContainer = scout.HtmlComponent.get($container),
    $tabArea = $container.children('.tab-area'),
    $tabContent = $container.children('.tab-content'),
    htmlTabContent = scout.HtmlComponent.get($tabContent),
    tabAreaSize = scout.graphics.getVisibleSize($tabArea, true),
    tabContentPrefSize;

  tabContentPrefSize = htmlTabContent.getPreferredSize()
    .add(htmlContainer.getInsets())
    .add(htmlTabContent.getMargins());
  tabContentPrefSize.height += tabAreaSize.height;

  return tabContentPrefSize;
};
