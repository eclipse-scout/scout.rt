scout.LookupCall = function() {
};

scout.LookupCall.prototype.init = function(model) {
  this._init(model);
};

scout.LookupCall.prototype._init = function(model) {
  $.extend(this, model);
};

/**
 * @returns Promise
 */
scout.LookupCall.prototype.textById = function(id) {
  // to be implemented by subclasses
};
