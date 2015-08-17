scout.UserTableFilter = function() {};

scout.UserTableFilter.prototype.init = function(model, session) {
  this.session = session;
  $.extend(this, model);
};

scout.UserTableFilter.prototype.createAddFilterEventData = function() {
  return {
    filterType: this.filterType
  };
};

scout.UserTableFilter.prototype.createRemoveFilterEventData = function() {
  return {
    filterType: this.filterType
  };
};

scout.UserTableFilter.prototype.createKey = function() {
  return this.filterType;
};

scout.UserTableFilter.prototype.createLabel = function() {
  // to be implemented by subclasses
  return '';
};

scout.UserTableFilter.prototype.accept = function($row) {
  // to be implemented by subclasses
};
