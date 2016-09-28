scout.PropertyChangeEventFilter = function() {
  this._filterProperties = {};
};

scout.PropertyChangeEventFilter.prototype.addFilterForProperties = function(properties) {
  scout.objects.copyProperties(properties, this._filterProperties);
};

scout.PropertyChangeEventFilter.prototype.filter = function(propertyName, value) {
  if (!this._filterProperties.hasOwnProperty(propertyName)) {
    return false;
  }
  var filterPropertyValue = this._filterProperties[propertyName];
  return scout.objects.equals(filterPropertyValue, value);
};

scout.PropertyChangeEventFilter.prototype.reset = function() {
  this._filterProperties = {};
};
