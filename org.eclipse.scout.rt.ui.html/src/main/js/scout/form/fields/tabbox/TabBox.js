/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
/**
 * Tab-area = where the 1-n tabs are placed (may have multiple runs = lines).
 * Tab-content = where the content of a single tab is displayed.
 */
scout.TabBox = function() {
  scout.TabBox.parent.call(this);

  this.gridDataHints.useUiHeight = true;
  this.gridDataHints.w = scout.FormField.FULL_WIDTH;
  this.selectedTab = null;
  this.tabItems = [];
  this._addWidgetProperties(['tabItems', 'selectedTab']);
  this._addPreserveOnPropertyChangeProperties(['selectedTab']); // TODO [7.0] awe: do this in Calendar too, for selectedComponent
  this._$tabContent = null;
};
scout.inherits(scout.TabBox, scout.CompositeField);

/**
 * @override FormField.js
 */
scout.TabBox.prototype._init = function(model) {
  scout.TabBox.parent.prototype._init.call(this, model);
  if (this.selectedTab) {
    this.selectedTab.setTabActive(true);
  }
  this.header = scout.create('TabBoxHeader', {
    parent: this,
    tabBox: this
  });
};

scout.TabBox.prototype._render = function() {
  this.addContainer(this.$parent, 'tab-box', new scout.TabBoxLayout(this));

  this.header.render(this.$container);
  this.addStatus();

  this._$tabContent = this.$container.appendDiv('tab-content');
  var htmlCompContent = scout.HtmlComponent.install(this._$tabContent, this.session);
  htmlCompContent.setLayout(new scout.SingleLayout());
};

/**
 * @override FormField.js
 */
scout.TabBox.prototype._renderProperties = function() {
  scout.TabBox.parent.prototype._renderProperties.call(this);
  this._renderTabContent();
};

/**
 * @override FormField.js
 */
scout.TabBox.prototype._remove = function() {
  scout.TabBox.parent.prototype._remove.call(this);
  this._removeTabContent();
};

scout.TabBox.prototype.setTabItems = function(tabItems) {
  this.setProperty('tabItems', tabItems);
};

scout.TabBox.prototype._removeTabContent = function() {
  this.tabItems.forEach(function(tabItem) {
    tabItem.remove();
  }, this);
};

scout.TabBox.prototype.selectTabById = function(tabId) {
  var tab = this.getTabItem(tabId);
  if (!tab) {
    throw new Error('Tab with ID \'' + tabId + '\' does not exist');
  }
  this.setSelectedTab(tab);
};

scout.TabBox.prototype.setSelectedTab = function(selectedTab) {
  this.setProperty('selectedTab', selectedTab);
};

scout.TabBox.prototype._setSelectedTab = function(tab) {
  if (this.selectedTab === tab) {
    return;
  }
  $.log.isDebugEnabled() && $.log.debug('(TabBox#_selectTab) tab=' + tab);
  var oldSelectedTab = this.selectedTab;
  var selectedTab = tab;
  if (oldSelectedTab) {
    oldSelectedTab.setTabActive(false);
  }
  if (selectedTab) {
    selectedTab.setTabActive(true);
  }
  this._setProperty('selectedTab', selectedTab);

  if (this.rendered) {
    if (oldSelectedTab) {
      oldSelectedTab.detach();
    }
    this._renderTabContent();
  }
};

scout.TabBox.prototype._renderTabContent = function() {
  // add new tab-content (use from cache or render)
  var selectedTab = this.selectedTab;
  if (selectedTab) {
    if (selectedTab.rendered && !selectedTab.attached) {
      selectedTab.attach();
    } else {
      // in Swing there's some complicated logic dealing with borders and labels
      // that determines whether the first group-box in a tab-box has a title or not.
      // I decided to simplify this and always set the title of the first group-box
      // to invisible.
      selectedTab.render(this._$tabContent);
    }
  }
  if (this.rendered) {
    scout.HtmlComponent.get(this._$tabContent).revalidateLayoutTree();
  }
};

scout.TabBox.prototype._renderStatusPosition = function() {
  if (this.statusPosition === scout.FormField.StatusPosition.TOP) {
    // move into title
    this.$status.appendTo(this.header.$container);
  } else {
    this.$status.appendTo(this.$container);
  }
  this.invalidateLayoutTree();
};

/**
 * @override CompositeField.js
 */
scout.TabBox.prototype.getFields = function() {
  return this.tabItems;
};

scout.TabBox.prototype.getTabItem = function(tabId) {
  return scout.arrays.find(this.tabItems, function(tabItem) {
    return tabItem.id === tabId;
  });
};

/**
 * @override FormField.js
 */
scout.TabBox.prototype.focus = function() {
  if (!this.rendered) {
    this._postRenderActions.push(this.focus.bind(this));
    return false;
  }
  if (this.selectedTab) {
    return this.selectedTab.focus();
  }
};
