// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

/**
 * Tab-area = where the 1-n tabs are placed (may have multiple runs = lines).
 * Tab-content = where the content of a single tab is displayed.
 */
scout.TabBox = function() {
  scout.TabBox.parent.call(this);
  this._addAdapterProperties(['groupBoxes']);
  this.selectedTab;
  this._$tabArea;
  this._$tabContent;

  // Contains detached tab-content, stored in order to be appended later
  this._$tabContentCache = [];
};
scout.inherits(scout.TabBox, scout.CompositeField);

scout.TabBox.prototype._render = function($parent) {
  this.addContainer($parent, 'TabBox', new scout.TabBoxLayout());
  this.$container.addClass('tab-box');

  this._$tabArea = this.$container.appendDiv('', 'tab-area');
  var htmlComp = new scout.HtmlComponent(this._$tabArea);
  htmlComp.setLayout(new scout.NullLayout());
  var i, groupBox, $tab;
  for (i = 0; i < this.groupBoxes.length; i++) {
    groupBox = this.groupBoxes[i];
    $tab = $('<button>').
      text(groupBox.label).
      appendTo(this._$tabArea).
      data('tabIndex', i).
      on('click', this._onTabClicked.bind(this));
  }

  // render 1st tab (currently hard-coded)
  this._$tabContent = this.$container.appendDiv('', 'tab-content');
  htmlComp = new scout.HtmlComponent(this._$tabContent);
  htmlComp.setLayout(new scout.SingleLayout());
};

scout.TabBox.prototype._renderProperties = function() {
  scout.TabBox.parent.prototype._renderProperties.call(this);
  this._renderSelectedTab(this.selectedTab);
};

scout.TabBox.prototype._onTabClicked = function(tab) {
  var tabIndex = $(tab.target).data('tabIndex');
  this.selectedTab = tabIndex;
  this.session.send('select', this.id, {'tabIndex':tabIndex});
  this._renderSelectedTab(tabIndex);
};


scout.TabBox.prototype._renderSelectedTab = function(selectedTab) {
  $.log.debug('(TabBox#_setSelectedTab) selectedTab='+selectedTab);
  var i, $tabs = this._$tabArea.children('button');
  for (i=0; i<$tabs.length; i++) {
    $($tabs[i]).removeClass('selected');
  }
  if (selectedTab >= 0 && selectedTab < $tabs.length) {
    $($tabs[selectedTab]).addClass('selected');
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
    this.groupBoxes[this.selectedTab].render(this._$tabContent);
    this._$tabContent.children().first().data('tabIndex', this.selectedTab);

    /* in Swing there's some complicated logic dealing with borders and labels
     * that determines whether the first group-box in a tab-box has a title or not.
     * I decided to simply this and always set the title of the first group-box
     * to invisible.
     */
    this.groupBoxes[this.selectedTab]._renderLabelVisible(false);

    // TODO AWE: (layout) beim initialen rendern ist das nicht nötig
    // schauen ob wir hier etwas unterdrücken müssen oder ob das
    // durch valid/invalidate bereits abgedeckt ist
    scout.HtmlComponent.get(this._$tabContent).layout();
  }
};

/**
 * @override CompositeField
 */
scout.CompositeField.prototype.getFields = function() {
  return this.groupBoxes;
};

