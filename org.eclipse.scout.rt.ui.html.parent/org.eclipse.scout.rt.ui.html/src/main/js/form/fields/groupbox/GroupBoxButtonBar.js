scout.GroupBoxButtonBar = function(processButtons) {
  this.processButtons = processButtons;
  this.$container;
  this.$processDiv;
  this.$systemDiv;
};

scout.GroupBoxButtonBar.prototype.render = function($parent) {
  this.$container = $parent.appendDiv('', 'button-bar');
  this.$processDiv = this.$container.appendDiv('', 'process');
  this.$systemDiv = this.$container.appendDiv('', 'system');

  var i, btn;
  for (i=0; i<this.processButtons.length; i++) {
    btn = this.processButtons[i];
    if (btn.gridData.horizontalAlignment <= 0) {
      btn.render(this.$processDiv);
    } else {
      btn.render(this.$systemDiv);
    }
  }
};
