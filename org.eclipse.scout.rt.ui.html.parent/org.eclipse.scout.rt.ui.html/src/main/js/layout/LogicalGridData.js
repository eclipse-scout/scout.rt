scout.LogicalGridData = function(template) {
  this.gridx = 0;
  this.gridy = 0;
  this.gridw = 1;
  this.gridh = 1;
  this.weightx = 0.0;
  this.weighty = 0.0;
  this.useUiWidth = false;
  this.useUiHeight = false;
  this.widthHint = 0;
  this.heightHint = 0;
  this.horizontalAlignment = -1;
  this.verticalAlignment = -1;
  this.fillHorizontal = true;
  this.fillVertical = true;
  this.topInset = 0;
  if (template) {
    this.gridx = template.gridx;
    this.gridy = template.gridy;
    this.gridw = template.gridw;
    this.gridh = template.gridh;
    this.weightx = template.weightx;
    this.weighty = template.weighty;
    this.useUiWidth = template.useUiWidth;
    this.useUiHeight = template.useUiHeight;
    this.widthHint = template.widthHint;
    this.heightHint = template.heightHint;
    this.horizontalAlignment = template.horizontalAlignment;
    this.verticalAlignment = template.verticalAlignment;
    this.fillHorizontal = template.fillHorizontal;
    this.fillVertical = template.fillVertical;
    this.topInset = template.topInset;
  }
};

scout.LogicalGridData.prototype.validate = function() {
  // TODO AWE: prüfen ob wir das validate() hier brauchen - sieht mir in Swing ziemlich hacky aus
};
