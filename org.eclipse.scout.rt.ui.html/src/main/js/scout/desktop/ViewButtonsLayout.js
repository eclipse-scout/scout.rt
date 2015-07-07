scout.ViewButtonsLayout = function(htmlComp) {
  scout.ViewButtonsLayout.parent.call(this);
  this._htmlComp = htmlComp;
  this._$oldSelectedTab;
};
scout.inherits(scout.ViewButtonsLayout, scout.AbstractLayout);

/**
 * Should be the same as in DesktopNavigation.css .view-button-tab > min-width.
 */
scout.ViewButtonsLayout.MIN_TAB_WIDTH = '50px';
scout.ViewButtonsLayout.ANIMATION_DURATION = 300; // ms

scout.ViewButtonsLayout.prototype.layout = function($container) {
  var $selectedTab, selectedTabWidth,
    fixedWidth = 0,
    containerBounds = this._htmlComp.getBounds();
  $container.children().each(function() {
    var $tab = $(this);
    var oldStyle = $tab.attr('style');
    $tab.removeAttr('style');
    if ($tab.isSelected()) {
      $selectedTab = $tab;
    } else {
      fixedWidth += scout.graphics.getBounds($tab).width;
    }
    if (oldStyle) { // restore style  (required for animation)
      $tab.attr('style', oldStyle);
    }
  });
  if ($selectedTab) { // when no view-buttons exist
    selectedTabWidth = containerBounds.width - fixedWidth;
    if (this._$oldSelectedTab && !this._$oldSelectedTab.is($selectedTab)) {
      this._$oldSelectedTab.animate({width: scout.ViewButtonsLayout.MIN_TAB_WIDTH}, scout.ViewButtonsLayout.ANIMATION_DURATION);
      $selectedTab.animate({width: selectedTabWidth}, scout.ViewButtonsLayout.ANIMATION_DURATION);
    } else {
      scout.graphics.setSize($selectedTab, new scout.Dimension(selectedTabWidth, containerBounds.height));
    }
  }
  this._$oldSelectedTab = $selectedTab;
};
