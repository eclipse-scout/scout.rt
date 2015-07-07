scout.ViewButtonsLayout = function(htmlComp) {
  scout.ViewButtonsLayout.parent.call(this);
  this._htmlComp = htmlComp;
  this._$oldSelectedTab;
};
scout.inherits(scout.ViewButtonsLayout, scout.AbstractLayout);

scout.ViewButtonsLayout.prototype.layout = function($container) {
  var $selectedTab, selectedTabWidth,
    fixedWidth = 0,
    containerBounds = this._htmlComp.getBounds();
  $container.children().each(function() {
    var $tab = $(this);
    var oldStyle = $tab.attr('style');
    $tab
      .removeAttr('style')
      .removeClass('view-button-fadeout view-button-fadein');
    if ($tab.isSelected()) {
      $selectedTab = $tab;
    } else {
      fixedWidth += scout.graphics.getBounds($tab).width;
    }
    if (oldStyle) { // restore of style used for animation
      $tab.attr('style', oldStyle);
    }
  });
  if ($selectedTab) { // when no view-buttons exist
    selectedTabWidth = containerBounds.width - fixedWidth;
    if (this._$oldSelectedTab && this._$oldSelectedTab !== $selectedTab) {
      this._$oldSelectedTab
        .addClass('view-button-fadeout')
        .animate({width: '50px'}, 400);

      $selectedTab
        .addClass('view-button-fadein')
        .animate({width: selectedTabWidth}, 400);
    } else {
      scout.graphics.setSize($selectedTab, new scout.Dimension(selectedTabWidth, containerBounds.height));
    }
  }

  this._$oldSelectedTab = $selectedTab;
};

scout.ViewButtonsLayout.prototype.preferredLayoutSize = function($container) {
  return new scout.Dimension(0, 0);
};
