scout.ViewButtonsLayout = function(htmlComp) {
  scout.ViewButtonsLayout.parent.call(this);
  this._htmlComp = htmlComp;
};
scout.inherits(scout.ViewButtonsLayout, scout.AbstractLayout);

scout.ViewButtonsLayout.prototype.layout = function($container) {
  var containerWidth = this._htmlComp.getBounds().width;
  var $selectedTab, selectedTabWidth, fixedWidth = 0;
  $container.children().each(function() {
    var $tab = $(this);
    $tab.removeAttr('style');
    if ($tab.isSelected()) {
      $selectedTab = $tab;
    } else {
      fixedWidth += scout.graphics.getBounds($tab).width;
    }
  });
  selectedTabWidth = containerWidth - fixedWidth;
  scout.graphics.setSize($selectedTab, new scout.Dimension(selectedTabWidth, 50)); // FIXME AWE
};

scout.ViewButtonsLayout.prototype.preferredLayoutSize = function($container) {
  return new scout.Dimension(0, 0);
};
