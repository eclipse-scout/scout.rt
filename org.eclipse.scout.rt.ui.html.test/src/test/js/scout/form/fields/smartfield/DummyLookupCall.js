scout.DummyLookupCall = function() {
  scout.DummyLookupCall.parent.call(this);

  this.setDelay(250);
};
scout.inherits(scout.DummyLookupCall, scout.StaticLookupCall);

scout.DummyLookupCall.prototype._data = function() {
  return [
    ['Foo', 1],
    ['Bar', 2],
    ['Baz', 3]
  ];
};
