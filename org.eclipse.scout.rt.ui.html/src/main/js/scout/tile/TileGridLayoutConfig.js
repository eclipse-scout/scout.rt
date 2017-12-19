scout.TileGridLayoutConfig = function(options) {
  scout.TileGridLayoutConfig.parent.call(this, options);
  this.hgap = 15;
  this.vgap = 20;
  this.columnWidth = 200;
  this.rowHeight = 150;
  this.maxWidth = -1;

  this._extend(options);
};
scout.inherits(scout.TileGridLayoutConfig, scout.LogicalGridLayoutConfig);

scout.TileGridLayoutConfig.prototype._extend = function(options) {
  scout.TileGridLayoutConfig.parent.prototype._extend.call(this, options);
  options = options || {};
  if (options.maxWidth > -2) {
    this.maxWidth = options.maxWidth;
  }
};

scout.TileGridLayoutConfig.prototype.clone = function() {
  return new scout.TileGridLayoutConfig(this);
};

scout.TileGridLayoutConfig.ensure = function(layoutConfig) {
  if (!layoutConfig) {
    return layoutConfig;
  }
  if (layoutConfig instanceof scout.TileGridLayoutConfig) {
    return layoutConfig;
  }
  return new scout.TileGridLayoutConfig(layoutConfig);
};
