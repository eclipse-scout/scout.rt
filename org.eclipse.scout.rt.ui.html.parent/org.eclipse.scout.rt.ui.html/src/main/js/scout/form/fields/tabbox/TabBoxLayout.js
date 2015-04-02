scout.TabBoxLayout = function(tabBox) {
  scout.TabBoxLayout.parent.call(this);
  this._tabBox = tabBox;
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
  var htmlContainer = scout.HtmlComponent.get($container),
    htmlTabContent = scout.HtmlComponent.get($container.children('.tab-content')),
    tabButtonInsets = new scout.Insets(1, 20, 3, 20),
    // FIXME AWE: (layout) cannot read insets dynamically because items are not rendered yet
    // tabButtonInsets = scout.graphics.getInsets($container.children('.tab-area > button'), {includeMargin: true}),
    tabAreaSize = new scout.Dimension(),
    tabContentSize = new scout.Dimension(),
    prefSize, textSize;

  // tab-area
  this._tabBox.tabItems.forEach(function(tabItem) {
    textSize = scout.graphics.measureString(tabItem.label, ['font-text-normal', 'make-bold']);
    tabAreaSize.width += textSize.width + tabButtonInsets.left + tabButtonInsets.right;
    tabAreaSize.height = 32;
    // FIXME AWE: (layout) cannot read height dynamically because items are not rendered yet
    // tabAreaSize.height = Math.max(tabAreaSize.height, textSize.height + tabButtonInsets.top + tabButtonInsets.bottom);
  });
  $.log.info('tabAreaSize='+tabAreaSize);

  // tab-content
  tabContentSize = htmlTabContent.getPreferredSize()
    .add(htmlContainer.getInsets())
    .add(htmlTabContent.getMargins());

  prefSize = new scout.Dimension(
      Math.max(tabAreaSize.width, tabContentSize.width),
      Math.max(tabAreaSize.height, tabContentSize.height));

  $.log.trace('(TabBoxLayout#preferredLayoutSize)='+prefSize);

  return prefSize;
};
