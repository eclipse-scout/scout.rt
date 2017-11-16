scout.LogicalGridLayoutConfig = function(options) {
  var env = scout.HtmlEnvironment;
  this.hgap = env.formColumnGap;
  this.vgap = env.formRowGap;
  this.columnWidth = env.formColumnWidth;
  this.rowHeight = env.formRowHeight;
  this.minWidth = 0;

  this._extend(options);
};

scout.LogicalGridLayoutConfig.prototype._extend = function(options) {
  // -1 means use the UI defaults
  options = options || {};
  if (options.hgap > -1) {
    this.hgap = options.hgap;
  }
  if (options.vgap > -1) {
    this.vgap = options.vgap;
  }
  if (options.columnWidth > -1) {
    this.columnWidth = options.columnWidth;
  }
  if (options.rowHeight > -1) {
    this.rowHeight = options.rowHeight;
  }
  if (options.minWidth > -1) {
    this.minWidth = options.minWidth;
  }
};

scout.LogicalGridLayoutConfig.prototype.clone = function() {
  return new scout.LogicalGridLayoutConfig(this);
};

scout.LogicalGridLayoutConfig.ensure = function(layoutConfig) {
  if (!layoutConfig) {
    return layoutConfig;
  }
  if (layoutConfig instanceof scout.LogicalGridLayoutConfig) {
    return layoutConfig;
  }
  return new scout.LogicalGridLayoutConfig(layoutConfig);
};
