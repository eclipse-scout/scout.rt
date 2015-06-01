scout.DesktopTabBarLayout = function(desktop) {
  scout.DesktopTabBarLayout.parent.call(this);

  this.TAB_WIDTH_LARGE = 220;
  this.TAB_WIDTH_SMALL = 130;
  this._desktop = desktop;
};
scout.inherits(scout.DesktopTabBarLayout, scout.AbstractLayout);

/**
 * @override AbstractLayout.js
 */
scout.DesktopTabBarLayout.prototype.layout = function($container) {
  var $tabs = $container.find('.taskbar-tabs'),
    $tools = $container.find('.taskbar-tools'),
    $logo = $container.find('.taskbar-logo'),
    contWidth = scout.graphics.getSize($container).width,
    logoWidth = scout.graphics.getSize($logo, true).width,
    numTabs = this._desktop.tabCount(),
    largePrefTabsWidth = numTabs * this.TAB_WIDTH_LARGE,
    smallPrefTabsWidth = numTabs * this.TAB_WIDTH_SMALL,
    toolsWidth, tabsWidth;

  // create a clone to measure pref. width
  var $clone = $tools.clone();
  $('body').append($clone).width('auto');
  toolsWidth = scout.graphics.getSize($clone, true).width;
  $clone.remove();

  tabsWidth = contWidth - toolsWidth - logoWidth;

  $.log.info('numTabs=' + numTabs + ' contWidth=' + contWidth + ' toolsWidth=' + toolsWidth + ' logoWidth=' + logoWidth + ' --> tabsWidth=' + tabsWidth);

 if (smallPrefTabsWidth <= tabsWidth) {
    var tabWidth = Math.min(this.TAB_WIDTH_LARGE, Math.floor(tabsWidth / numTabs));
    $.log.info('MEEP tabWidth=' + tabWidth);
    // 2nd - all Tabs fit when they have small size
    $tabs.find('.taskbar-tab-item').each(function() {
      $(this).outerWidth(tabWidth);
    });
  }
  else {
    $.log.info('XXX');
    // small fit is not small enough -> put tabs into overflow menu
    // FIXME AWE:
  }


};
