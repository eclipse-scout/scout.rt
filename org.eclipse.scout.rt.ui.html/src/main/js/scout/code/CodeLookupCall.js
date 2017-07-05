scout.CodeLookupCall = function() {
  scout.CodeLookupCall.parent.call(this);
  this.codeType;
};
scout.inherits(scout.CodeLookupCall, scout.LookupCall);

scout.CodeLookupCall.prototype._init = function(model) {
  scout.assertParameter('session', model.session);
  scout.CodeLookupCall.parent.prototype._init.call(this, model);
};

scout.CodeLookupCall.prototype._textByKey = function(key) {
  var code = scout.codes.optGet(this.codeType, key);
  return $.resolvedDeferred(code ? code.text(this.session.locale) : this._textCodeUndefined(key));
};

scout.CodeLookupCall.prototype._textCodeUndefined = function(id) {
  return this.session.text('ui.CodeUndefined');
};
