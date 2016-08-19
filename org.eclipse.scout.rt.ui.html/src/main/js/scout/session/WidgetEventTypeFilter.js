scout.WidgetEventTypeFilter = function() {
  this._filterEventTypes = [];
};

scout.WidgetEventTypeFilter.prototype.addFilterForEventType = function(eventType) {
  if (this._filterEventTypes.indexOf(eventType) === -1) {
    this._filterEventTypes.push(eventType);
  }
};

scout.WidgetEventTypeFilter.prototype.filter = function(event) {
  return this._filterEventTypes.some(function(filterEventType) {
    return filterEventType === event.type;
  });
};

scout.WidgetEventTypeFilter.prototype.reset = function() {
  this._filterEventTypes = [];
};
