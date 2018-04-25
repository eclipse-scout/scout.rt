scout.RadioButtonGroupLayoutConfig = function(options) {
  scout.RadioButtonGroupLayoutConfig.parent.call(this, options);
  this.hgap = scout.HtmlEnvironment.smallColumnGap;
  this._extend(options);
};
scout.inherits(scout.RadioButtonGroupLayoutConfig, scout.LogicalGridLayoutConfig);

scout.RadioButtonGroupLayoutConfig.prototype.clone = function() {
  return new scout.RadioButtonGroupLayoutConfig(this);
};

scout.RadioButtonGroupLayoutConfig.ensure = function(layoutConfig) {
  if (!layoutConfig) {
    return layoutConfig;
  }
  if (layoutConfig instanceof scout.RadioButtonGroupLayoutConfig) {
    return layoutConfig;
  }
  return new scout.RadioButtonGroupLayoutConfig(layoutConfig);
};
