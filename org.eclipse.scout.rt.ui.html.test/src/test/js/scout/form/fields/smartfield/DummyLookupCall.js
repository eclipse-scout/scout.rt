scout.DummyLookupCall = function() {
  scout.DummyLookupCall.parent.call(this);
  this.multiline = false;
  this.setDelay(250);
};
scout.inherits(scout.DummyLookupCall, scout.StaticLookupCall);

scout.DummyLookupCall.prototype._data = function() {
  return [
    [1, line.call(this, 'Foo')],
    [2, line.call(this, 'Bar')],
    [3, line.call(this, 'Baz')]
  ];

  function line(text) {
    if (this.multiline) {
      return '1:' + text + '\n2:' + text;
    } else {
      return text;
    }
  }
};
