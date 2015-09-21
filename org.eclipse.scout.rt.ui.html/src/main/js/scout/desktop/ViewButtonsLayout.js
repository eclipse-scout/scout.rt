scout.ViewButtonsLayout = function(htmlComp) {
  scout.ViewButtonsLayout.parent.call(this);
  this._htmlComp = htmlComp;
};
scout.inherits(scout.ViewButtonsLayout, scout.AbstractLayout);

/**
 * Should be the same as in DesktopNavigation.css .view-button-tab > min-width.
 */
scout.ViewButtonsLayout.MIN_TAB_WIDTH = '50px';

scout.ViewButtonsLayout.prototype.layout = function($container) {
  var $selectedTab, selectedTabWidth,
    fixedWidth = 0,
    containerBounds = this._htmlComp.getBounds(),
    tabs = $container.children().length,
    tabWidth = (containerBounds.width/tabs);
  $container.children().each(function() {
    var $tab = $(this);
    var oldStyle = $tab.attr('style');
    $tab.removeAttr('style');
    scout.graphics.setSize($tab, new scout.Dimension(tabWidth, containerBounds.height));
    if (oldStyle) { // restore style  (required for animation)
      $tab.attr('style', oldStyle);
    }
  });
};
