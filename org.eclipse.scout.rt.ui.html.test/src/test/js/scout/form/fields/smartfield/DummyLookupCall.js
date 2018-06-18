scout.DummyLookupCall = function() {
  scout.DummyLookupCall.parent.call(this);

  this.showText = true;
  this.setDelay(250);
};
scout.inherits(scout.DummyLookupCall, scout.StaticLookupCall);

scout.DummyLookupCall.prototype._data = function() {
  return [
    [1, 'Foo'],
    [2, 'Bar'],
    [3, 'Baz']
  ];
};

scout.DummyLookupCall.prototype._dataToLookupRow = function(data) {
  var lookupRow = scout.DummyLookupCall.parent.prototype._dataToLookupRow.call(this, data);
  lookupRow.cssClass = lookupRow.text.toLowerCase();
  if (!this.showText) {
    lookupRow.text = null;
  }
  return lookupRow;
};
