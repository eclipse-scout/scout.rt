scout.DummyLookupCall = function() {
  scout.DummyLookupCall.parent.call(this);

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
