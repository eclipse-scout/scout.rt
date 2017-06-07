scout.GridData = function(model) {
  model = model || {};
  this.x = -1;
  this.y = -1;
  this.w = 1;
  this.h = 1;
  this.weightX = -1;
  this.weightY = -1;
  this.useUiWidth = false;
  this.useUiHeight = false;
  this.horizontalAlignment = -1;
  this.verticalAlignment = -1;
  this.fillHorizontal = true;
  this.fillVertical = true;
  this.widthInPixel = 0;
  this.heightInPixel = 0;

  $.extend(this, model);
};

scout.GridData.createFromHints = function(field, gridColumnCount) {
  var data = new scout.GridData(field.gridDataHints);
  if (data.w === scout.FormField.FULL_WIDTH) {
    data.w = gridColumnCount;
  }
  return data;
};

scout.GridData.ensure = function(gridData) {
  if (!gridData) {
    return gridData;
  }
  if (gridData instanceof scout.GridData) {
    return gridData;
  }
  return new scout.GridData(gridData);
};
