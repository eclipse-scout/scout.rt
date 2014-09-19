// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

/**
 * Tab-area = where the 1-n tabs are placed (may have multiple runs = lines).
 * Tab-content = where the content of a single tab is displayed.
 */
scout.TabBox = function() {
  scout.TabBox.parent.call(this);
  this._addAdapterProperties(['groupBoxes', 'selectedTab']);
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
      appendTo(this._$tabArea);
  }

  // render 1st tab (currently hard-coded)
  this._$tabContent = this.$container.appendDiv('', 'tab-content');
  htmlComp = new scout.HtmlComponent(this._$tabContent);
  htmlComp.setLayout(new scout.SingleLayout());
  this.groupBoxes[0].render(this._$tabContent);

  /* in Swing there's some complicated logic dealing with borders and labels
   * that determines whether the first group-box in a tab-box has a title or not.
   * I decided to simply this and always set the title of the first group-box
   * to invisible.
   */
  this.groupBoxes[0]._setLabelVisible(false);

  // TODO AWE: (tab-box) improv. implementation - currently very hacky
  // in Swing hat das JTabbedPane 2'500 lines of code!
};
