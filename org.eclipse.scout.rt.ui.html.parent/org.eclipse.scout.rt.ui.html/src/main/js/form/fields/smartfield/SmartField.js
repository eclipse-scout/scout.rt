scout.SmartField = function(lookupStrategy) {
  scout.SmartField.parent.call(this, lookupStrategy);
  this.options;
};
scout.inherits(scout.SmartField, scout.AbstractSmartField);
