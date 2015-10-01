scout.TableUserFilter = function() {};

scout.TableUserFilter.prototype.init = function(model) {
  this.session = model.session;
  if (!this.session) {
    throw new Error('Session expected: ' + this);
  }
  $.extend(this, model);
};

scout.TableUserFilter.prototype.createAddFilterEventData = function() {
  return {
    filterType: this.filterType
  };
};

scout.TableUserFilter.prototype.createRemoveFilterEventData = function() {
  return {
    filterType: this.filterType
  };
};

scout.TableUserFilter.prototype.createKey = function() {
  return this.filterType;
};

scout.TableUserFilter.prototype.createLabel = function() {
  // to be implemented by subclasses
  return '';
};

scout.TableUserFilter.prototype.accept = function($row) {
  // to be implemented by subclasses
};
