scout.TileGridLayoutConfig = function(options) {
  scout.TileGridLayoutConfig.parent.call(this, options);
  var dim = scout.TileGridLayoutConfig.getTileDimensions();
  this.hgap = dim.x;
  this.vgap = dim.y;
  this.columnWidth = dim.width;
  this.rowHeight = dim.height;
  this.maxWidth = -1;

  this._extend(options);
};
scout.inherits(scout.TileGridLayoutConfig, scout.LogicalGridLayoutConfig);

scout.TileGridLayoutConfig._SIZE = undefined;

scout.TileGridLayoutConfig.prototype._extend = function(options) {
  scout.TileGridLayoutConfig.parent.prototype._extend.call(this, options);
  options = options || {};
  if (options.maxWidth > -2) {
    this.maxWidth = options.maxWidth;
  }
};

scout.TileGridLayoutConfig.prototype.applyToLayout = function(layout) {
  scout.TileGridLayoutConfig.parent.prototype.applyToLayout.call(this, layout);
  layout.maxWidth = this.maxWidth;
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

scout.TileGridLayoutConfig.getTileDimensions = function() {
  if (!(scout.TileGridLayoutConfig._SIZE instanceof scout.Rectangle)) {
    var h = scout.styles.getSize('tile-grid-layout-config', 'height', 'height', -1);
    var w = scout.styles.getSize('tile-grid-layout-config', 'width', 'width', -1);
    var horizontalGap = scout.styles.getSize('tile-grid-layout-config', 'margin-left', 'marginLeft', -1);
    var verticalGap = scout.styles.getSize('tile-grid-layout-config', 'margin-top', 'marginTop', -1);
    scout.TileGridLayoutConfig._SIZE = new scout.Rectangle(horizontalGap, verticalGap, w, h);
  }
  return scout.TileGridLayoutConfig._SIZE;
};
