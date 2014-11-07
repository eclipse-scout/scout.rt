// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

/**
 * Tab-area = where the 1-n tabs are placed (may have multiple runs = lines).
 * Tab-content = where the content of a single tab is displayed.
 */
scout.TabBox = function() {
  scout.TabBox.parent.call(this);
  this._addAdapterProperties(['tabItems']);
  this.selectedTab;
  this._$tabArea;
  this._$tabContent;

  // Contains detached tab-content, stored in order to be appended later
  this._$tabContentCache = [];
};
scout.inherits(scout.TabBox, scout.CompositeField);

scout.TabBox.prototype._render = function($parent) {
  this.addContainer($parent, 'tab-box', new scout.TabBoxLayout());

  this._$tabArea = this.$container.appendDiv('', 'tab-area');
  var htmlComp = new scout.HtmlComponent(this._$tabArea, this.session);
  htmlComp.setLayout(new scout.NullLayout());
  var i, tabItem, $tab;
  for (i = 0; i < this.tabItems.length; i++) {
    tabItem = this.tabItems[i];
    $tab = tabItem.renderTab(this._$tabArea, i).
      on('mousedown', this.onMousedown.bind(this)).
      on('keydown', this._onKeydown.bind(this));
    // only the selected tab is focusable
    if (i != this.selectedTab) {
      $tab.attr('tabindex', -1);
    }
  }

  this._$tabContent = this.$container.appendDiv('', 'tab-content');
  htmlComp = new scout.HtmlComponent(this._$tabContent, this.session);
  htmlComp.setLayout(new scout.SingleLayout());
};

scout.TabBox.prototype._renderProperties = function() {
  scout.TabBox.parent.prototype._renderProperties.call(this);
  this._renderSelectedTab(this.selectedTab);
};

scout.TabBox.prototype.onMousedown = function(e) {
  var tabIndex = $(e.target).data('tabIndex');
  this._selectTab(tabIndex);
};

scout.TabBox.prototype._selectTab = function(tabIndex) {
  this.selectedTab = tabIndex;
  this.session.send('select', this.id, {'tabIndex':tabIndex});
  this._renderSelectedTab(tabIndex);
};

// keyboard navigation in tab-box button area
scout.TabBox.prototype._onKeydown = function(e) {
  var tabIndex, navigationKey =
    e.which === scout.keys.LEFT ||
    e.which === scout.keys.RIGHT;
  if (!navigationKey) {
    return true;
  }
  tabIndex = $(e.target).data('tabIndex');
  if (e.which === scout.keys.LEFT) { tabIndex--; }
  if (e.which === scout.keys.RIGHT) { tabIndex++; }
  if (tabIndex >= 0 && tabIndex < this.tabItems.length) {
    setTimeout(function() {
      if (tabIndex >= 0 && tabIndex < this.tabItems.length) {
        this._selectTab(tabIndex);
        var $tabButton = this._$tabArea.children('button').get(tabIndex);
        $tabButton.focus();
      }
    }.bind(this));
  }
  e.preventDefault();
};

scout.TabBox.prototype._renderSelectedTab = function(selectedTab) {
  $.log.debug('(TabBox#_setSelectedTab) selectedTab='+selectedTab);
  var i, $tabButton, $oldTabButton, $selectedTabButton, $tabs = this._$tabArea.children('button');
  for (i=0; i<$tabs.length; i++) {
    $tabButton = $($tabs[i]);
    if ($tabButton.hasClass('selected')) {
      $oldTabButton = $tabButton;
      $oldTabButton.removeClass('selected');
    }
  }
  if (selectedTab >= 0 && selectedTab < $tabs.length) {
    $selectedTabButton = $($tabs[selectedTab]);
    $selectedTabButton.addClass('selected');
  }

  // deal with HTML attr 'tabindex' used for focus handling
  // don't confuse jquery data 'tabIndex' and HTML attr 'tabindex' here.
  // the former is used for internal widget logic, the later for focus handling
  if ($oldTabButton && $selectedTabButton) {
    $oldTabButton.attr('tabindex', -1);
    $selectedTabButton.removeAttr('tabindex');
  }

  // replace tab-content
  var $oldTab = this._$tabContent.children().first().detach();
  if ($oldTab.data('tabIndex') !== undefined) {
    var oldTabIndex = $oldTab.data('tabIndex');
    this._$tabContentCache[oldTabIndex] = $oldTab;
  }

  var $cachedTabContent = this._$tabContentCache[this.selectedTab];
  if ($cachedTabContent) {
    $cachedTabContent.appendTo(this._$tabContent);
  } else {
    this.tabItems[this.selectedTab].render(this._$tabContent);
    this._$tabContent.children().first().data('tabIndex', this.selectedTab);

    /* in Swing there's some complicated logic dealing with borders and labels
     * that determines whether the first group-box in a tab-box has a title or not.
     * I decided to simply this and always set the title of the first group-box
     * to invisible.
     */
    this.tabItems[this.selectedTab]._renderLabelVisible(false);

    if (this.rendered) {
      var htmlComp = scout.HtmlComponent.get(this._$tabContent);
      htmlComp.revalidate();
    }
  }
};

/**
 * @override CompositeField
 */
scout.CompositeField.prototype.getFields = function() {
  return this.tabItems;
};

