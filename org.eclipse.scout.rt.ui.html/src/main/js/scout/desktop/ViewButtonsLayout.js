scout.ViewButtonsLayout = function(htmlComp) {
  scout.ViewButtonsLayout.parent.call(this);
  this._htmlComp = htmlComp;
};
scout.inherits(scout.ViewButtonsLayout, scout.AbstractLayout);

scout.ViewButtonsLayout.prototype.layout = function($container) {
  var $selectedTab, selectedTabWidth,
    fixedWidth = 0,
    containerBounds = this._htmlComp.getBounds();
  $container.children().each(function() {
    var $tab = $(this);
    $tab.removeAttr('style');
    if ($tab.isSelected()) {
      $selectedTab = $tab;
    } else {
      fixedWidth += scout.graphics.getBounds($tab).width;
    }
  });
  if ($selectedTab) { // when no view-buttons exist
    // FIXME awe handle form-only mode
    selectedTabWidth = containerBounds.width - fixedWidth;
    scout.graphics.setSize($selectedTab, new scout.Dimension(selectedTabWidth, containerBounds.height));
  }
};

scout.ViewButtonsLayout.prototype.preferredLayoutSize = function($container) {
  return new scout.Dimension(0, 0);
};
