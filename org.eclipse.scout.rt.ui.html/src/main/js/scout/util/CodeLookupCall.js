scout.CodeLookupCall = function() {
  scout.CodeLookupCall.parent.call(this);
  this.codeType;
};
scout.inherits(scout.CodeLookupCall, scout.LookupCall);

scout.CodeLookupCall.prototype._init = function(model) {
  scout.CodeLookupCall.parent.prototype._init.call(this, model);
  if (!this.session) {
    throw new Error('missing property \'session\'');
  }
};

scout.CodeLookupCall.prototype.textById = function(id) {
  if (id === null || id === undefined) {
    return $.Deferred().resolve(null);
  }
  var code = scout.codes.get(this.codeType, id);
  return $.Deferred().resolve(code.text(this.session.locale));
};
