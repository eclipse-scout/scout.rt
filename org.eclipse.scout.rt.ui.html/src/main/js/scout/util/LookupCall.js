scout.LookupCall = function() {
  this.session = null;
};

scout.LookupCall.prototype.init = function(model) {
  scout.assertParameter('session', model.session);
  this._init(model);
};

scout.LookupCall.prototype._init = function(model) {
  $.extend(this, model);
};

/**
 * @returns {Promise}
 */
scout.LookupCall.prototype.textById = function(id) {
  // To be implemented by subclasses
  return $.resolvedPromise(id);
};

/**
 * @returns {Promise}
 */
scout.LookupCall.prototype.allTexts = function() {
  // To be implemented by subclasses
  return $.resolvedPromise([]);
};
