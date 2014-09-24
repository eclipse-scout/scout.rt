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
};
scout.inherits(scout.TabBox, scout.FormField);

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
  this.groupBoxes[this.selectedTab].render(this._$tabContent);

  /* in Swing there's some complicated logic dealing with borders and labels
   * that determines whether the first group-box in a tab-box has a title or not.
   * I decided to simply this and always set the title of the first group-box
   * to invisible.
   */
  this.groupBoxes[this.selectedTab]._setLabelVisible(false);

  // TODO AWE: (tab-box) improv. implementation - currently very hacky
  // in Swing hat das JTabbedPane 2'500 lines of code! use JQuery UI plugin?
//  this._uiSetSelectedTab(this.selectedTab);
};

// TODO AWE: rename _renderProperties
scout.TabBox.prototype._callSetters = function() {
  scout.TabBox.parent.prototype._callSetters.call(this);
  this._setSelectedTab(this.selectedTab);
};

scout.TabBox.prototype._onTabClicked = function(tab) {
  var tabIndex = $(tab.target).data('tabIndex');
  this.selectedTab = tabIndex;
  this.session.send('select', this.id, {'tabIndex':tabIndex});
  this._setSelectedTab(tabIndex);
  // TODO AWE: (tab-box) send to server? or make everything client-side
};

//  this._$tabContent.children().first().detach();
//  this.groupBoxes[tabIndex].render(this._$tabContent);

// TODO AWE: rename _setXxx --> _renderXxx
scout.TabBox.prototype._setSelectedTab = function(selectedTab) {
  var i, $tabs = this._$tabArea.children('button');
  for (i=0; i<$tabs.length; i++) {
    $($tabs[i]).removeClass('selected');
  }
  if (selectedTab >= 0 && selectedTab < $tabs.length) {
    $($tabs[selectedTab]).addClass('selected');
  }
  $.log.debug('selectedTab='+selectedTab);
};
