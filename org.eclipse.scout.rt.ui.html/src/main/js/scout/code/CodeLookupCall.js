scout.CodeLookupCall = function() {
  scout.CodeLookupCall.parent.call(this);
  this.codeType;
};
scout.inherits(scout.CodeLookupCall, scout.LookupCall);

scout.CodeLookupCall.prototype._init = function(model) {
  scout.assertParameter('session', model.session);
  scout.CodeLookupCall.parent.prototype._init.call(this, model);
};

scout.CodeLookupCall.prototype._textById = function(id) {
  var code = scout.codes.get(this.codeType, id);
  return $.Deferred().resolve(code.text(this.session.locale));
};
