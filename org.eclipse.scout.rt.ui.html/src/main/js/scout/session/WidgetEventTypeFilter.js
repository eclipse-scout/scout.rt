scout.WidgetEventTypeFilter = function() {
  this.filters = [];
};

scout.WidgetEventTypeFilter.prototype.addFilter = function(filterFunc) {
  this.filters.push(filterFunc);
};

scout.WidgetEventTypeFilter.prototype.addFilterForEventType = function(eventType) {
  this.filters.push(function(event) {
    return event.type === eventType;
  });
};

scout.WidgetEventTypeFilter.prototype.filter = function(event) {
  return this.filters.some(function(filterFunc) {
    return filterFunc(event);
  });
};

scout.WidgetEventTypeFilter.prototype.reset = function() {
  this.filters = [];
};
