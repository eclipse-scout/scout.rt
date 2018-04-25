scout.SequenceBoxLayoutConfig = function(options) {
  scout.SequenceBoxLayoutConfig.parent.call(this, options);
  this.hgap = scout.HtmlEnvironment.smallColumnGap;
  this._extend(options);
};
scout.inherits(scout.SequenceBoxLayoutConfig, scout.LogicalGridLayoutConfig);

scout.SequenceBoxLayoutConfig.prototype.clone = function() {
  return new scout.SequenceBoxLayoutConfig(this);
};

scout.SequenceBoxLayoutConfig.ensure = function(layoutConfig) {
  if (!layoutConfig) {
    return layoutConfig;
  }
  if (layoutConfig instanceof scout.SequenceBoxLayoutConfig) {
    return layoutConfig;
  }
  return new scout.SequenceBoxLayoutConfig(layoutConfig);
};
