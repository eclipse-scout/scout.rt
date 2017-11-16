scout.TilesLayoutConfig = function(options) {
  scout.TilesLayoutConfig.parent.call(this, options);
  this.hgap = 15;
  this.vgap = 20;
  this.columnWidth = 200;
  this.rowHeight = 150;
  this.maxWidth = -1;

  this._extend(options);
};
scout.inherits(scout.TilesLayoutConfig, scout.LogicalGridLayoutConfig);

scout.TilesLayoutConfig.prototype._extend = function(options) {
  scout.TilesLayoutConfig.parent.prototype._extend.call(this, options);
  options = options || {};
  if (options.maxWidth > -2) {
    this.maxWidth = options.maxWidth;
  }
};

scout.TilesLayoutConfig.prototype.clone = function() {
  return new scout.TilesLayoutConfig(this);
};

scout.TilesLayoutConfig.ensure = function(layoutConfig) {
  if (!layoutConfig) {
    return layoutConfig;
  }
  if (layoutConfig instanceof scout.TilesLayoutConfig) {
    return layoutConfig;
  }
  return new scout.TilesLayoutConfig(layoutConfig);
};
