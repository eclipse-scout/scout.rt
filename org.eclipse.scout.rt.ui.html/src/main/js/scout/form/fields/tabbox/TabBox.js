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

/**
 * @override FormField.js
 */
scout.TabBox.prototype.init = function(model, session) {
  scout.TabBox.parent.prototype.init.call(this, model, session);
  this.tabItems[this.selectedTab].setTabActive(true);
};

scout.TabBox.prototype._render = function($parent) {
  this._$tabContentCache = []; // clear cache when tab-box is rendered anew
  this.addContainer($parent, 'tab-box', new scout.TabBoxLayout(this));

  this._$tabArea = this.$container
    .appendDiv('tab-area')
    .on('keydown', this._onKeyDown.bind(this));
  var htmlComp = new scout.HtmlComponent(this._$tabArea, this.session);
  htmlComp.setLayout(new scout.TabAreaLayout(this));

  this._$tabContent = this.$container.appendDiv('tab-content');
  htmlComp = new scout.HtmlComponent(this._$tabContent, this.session);
  htmlComp.setLayout(new scout.SingleLayout());
};

/**
 * @override FormField.js
 */
scout.TabBox.prototype._renderProperties = function() {
  scout.TabBox.parent.prototype._renderProperties.call(this);
  this._renderTabs();
  this._renderTabContent();
};

scout.TabBox.prototype._renderTabs = function() {
  this.tabItems.forEach(function(tabItem) {
    tabItem.renderTab(this._$tabArea);
  }, this);
};

scout.TabBox.prototype.rebuildTabs = function() {
  // FIXME AWE: (tab-box) refactor this and work with a clone in the TabBoxLayout - when we remove an existing
  // DOM element which currently has the focus - the focus is lost. An other solution would be, to render the
  // tab at the correct position but probably that's not so easy because the render impl. does always append.
  this.tabItems.forEach(function(tabItem) {
    if (tabItem._tabRendered) {
      tabItem.removeTab();
    }
  }, this);
  this._renderTabs();
};

scout.TabBox.prototype._selectTab = function(tabItem) {
  var tabIndex = this.tabItems.indexOf(tabItem);
  if (tabIndex != this.selectedTab) {
    $.log.debug('(TabBox#_selectTab) tabItem=' + tabItem + ' tabIndex=' + tabIndex);
    var oldSelectedTab = this.selectedTab;
    this.selectedTab = tabIndex;
    this.session.send(this.id, 'selected', {
      tabIndex: tabIndex
    });

    this.tabItems[oldSelectedTab].setTabActive(false);
    this.tabItems[this.selectedTab].setTabActive(true);

    scout.HtmlComponent.get(this._$tabArea).revalidate();

    // FIXME AWE: (tab-box) focus scheitert, wenn tab nicht gerendert ist (weil in overflow)
    // FIXME AWE: (tab-box) wieso braucht es hier NOCH ein setTimeout?
    setTimeout(function() {
      this.tabItems[this.selectedTab].focusTab();
    }.bind(this));

    var $tabContent = this._$tabContent.children().first();
    if ($tabContent.length > 0) {
      this.session.detachHelper.beforeDetach($tabContent);
      $tabContent.detach();
      this._$tabContentCache[oldSelectedTab] = $tabContent;
    }

    this._renderTabContent();
  }
};

// keyboard navigation in tab-box button area
scout.TabBox.prototype._onKeyDown = function(event) {
  var tabIndex, navigationKey =
    event.which === scout.keys.LEFT ||
    event.which === scout.keys.RIGHT;

  if (!navigationKey) {
    return true;
  }

  tabIndex = this.selectedTab;
  if (event.which === scout.keys.LEFT) {
    tabIndex--;
  }
  else if (event.which === scout.keys.RIGHT) {
    tabIndex++;
  }

  if (tabIndex >= 0 && tabIndex < this.tabItems.length) {
    setTimeout(function() {
      if (tabIndex >= 0 && tabIndex < this.tabItems.length) {
        var tabItem = this.tabItems[tabIndex];
        if (tabItem._tabRendered) {
          this._selectTab(tabItem);
        }
      }
    }.bind(this));
  }

  event.preventDefault();
};

scout.TabBox.prototype._renderTabContent = function($tabContent) {
  // add new tab-content (use from cache or render)
  var $cachedTabContent = this._$tabContentCache[this.selectedTab];
  if ($cachedTabContent) {
    $cachedTabContent.appendTo(this._$tabContent);
    this.session.detachHelper.afterAttach($cachedTabContent);
  } else {
    this.tabItems[this.selectedTab].render(this._$tabContent);

    /* in Swing there's some complicated logic dealing with borders and labels
     * that determines whether the first group-box in a tab-box has a title or not.
     * I decided to simply this and always set the title of the first group-box
     * to invisible.
     */
    this.tabItems[this.selectedTab]._renderLabelVisible(false);
  }
  if (this.rendered) {
    scout.HtmlComponent.get(this._$tabContent).revalidate();
  }
};

/**
 * @override CompositeField
 */
scout.TabBox.prototype.getFields = function() {
  return this.tabItems;
};
