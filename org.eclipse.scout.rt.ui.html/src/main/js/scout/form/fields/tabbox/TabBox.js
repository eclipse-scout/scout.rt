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
  this._$tabContentCache = []; // clear cache when tab-box is rendered anew
  this.addContainer($parent, 'tab-box', new scout.TabBoxLayout(this));

  this._$tabArea = this.$container.appendDiv('tab-area');
  var htmlComp = new scout.HtmlComponent(this._$tabArea, this.session);
  htmlComp.setLayout(new scout.NullLayout());
  var tabItem, $tab;
  for (var i = 0; i < this.tabItems.length; i++) {
    tabItem = this.tabItems[i];
    $tab = tabItem.renderTab(this._$tabArea, i).
      on('mousedown', this._onMouseDown.bind(this)).
      on('keydown', this._onKeyDown.bind(this));
    // only the selected tab is focusable
    if (i !== this.selectedTab) {
      $tab.attr('tabindex', -1);
    }
  }

  this._$tabContent = this.$container.appendDiv('tab-content');
  htmlComp = new scout.HtmlComponent(this._$tabContent, this.session);
  htmlComp.setLayout(new scout.SingleLayout());
};

scout.TabBox.prototype._renderProperties = function() {
  scout.TabBox.parent.prototype._renderProperties.call(this);
  this._renderSelectedTab(this.selectedTab);
};

scout.TabBox.prototype._onMouseDown = function(e) {
  var tabIndex = $(e.target).data('tabIndex');
  this._selectTab(tabIndex);
};

scout.TabBox.prototype._selectTab = function(tabIndex) {
  this.selectedTab = tabIndex;
  this.session.send(this.id, 'selected', {
    tabIndex: tabIndex
  });
  this._renderSelectedTab(tabIndex);
};

// keyboard navigation in tab-box button area
scout.TabBox.prototype._onKeyDown = function(e) {
  var tabIndex, navigationKey =
    e.which === scout.keys.LEFT ||
    e.which === scout.keys.RIGHT;
  if (!navigationKey) {
    return true;
  }

  tabIndex = $(e.target).data('tabIndex');
  if (e.which === scout.keys.LEFT) {
    tabIndex--;
  }

  if (e.which === scout.keys.RIGHT) {
    tabIndex++;
  }

  if (tabIndex >= 0 && tabIndex < this.tabItems.length) {
    setTimeout(function() {
      if (tabIndex >= 0 && tabIndex < this.tabItems.length) {
        this._selectTab(tabIndex);
        var $tabButton = this._$tabArea.children('button').eq(tabIndex);
        $tabButton.focus();
      }
    }.bind(this));
  }
  e.preventDefault();
};

scout.TabBox.prototype._renderSelectedTab = function(selectedTab) {
  $.log.debug('(TabBox#_renderSelectedTab) selectedTab='+selectedTab);
  var $tabs = this._$tabArea.children('button');

  var $oldTabButton;
  for (var i = 0; i < $tabs.length; i++) {
    var $tabButton = $($tabs[i]);
    if ($tabButton.hasClass('selected')) {
      $oldTabButton = $tabButton;
      $oldTabButton.removeClass('selected');
    }
  }
  var $selectedTabButton;
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
  var $tabContent = this._$tabContent.children().first();
  if ($tabContent.length > 0) {
    this.session.detachHelper.beforeDetach($tabContent);
    $tabContent.detach();
    var oldTabIndex = $tabContent.data('tabIndex');
    if (oldTabIndex !== undefined) {
      this._$tabContentCache[oldTabIndex] = $tabContent;
    }
  }

  var $cachedTabContent = this._$tabContentCache[this.selectedTab];
  if ($cachedTabContent) {
    $cachedTabContent.appendTo(this._$tabContent);
    this.session.detachHelper.afterAttach($cachedTabContent);
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
      scout.HtmlComponent.get(this._$tabContent).revalidate();
    }
  }
};

/**
 * @override CompositeField
 */
scout.TabBox.prototype.getFields = function() {
  return this.tabItems;
};
