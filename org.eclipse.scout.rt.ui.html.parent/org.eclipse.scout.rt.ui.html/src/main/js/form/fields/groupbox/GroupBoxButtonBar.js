scout.GroupBoxButtonBar = function(processButtons) {
  this.processButtons = processButtons;
  this.$container;
  this.$processDiv;
  this.$systemDiv;
};

scout.GroupBoxButtonBar.prototype.render = function($parent) {
  var i, btn;
  this.$container = $parent.appendDIV('button-bar');
  this.$processDiv = this.$container.appendDIV('process');
  this.$systemDiv = this.$container.appendDIV('system');
  for (i=0; i<this.processButtons.length; i++) {
    btn = this.processButtons[i];
    if (btn.gridData.horizontalAlignment <= 0) {
      btn.render(this.$processDiv);
    } else {
      btn.render(this.$systemDiv);
    }
  }
};
