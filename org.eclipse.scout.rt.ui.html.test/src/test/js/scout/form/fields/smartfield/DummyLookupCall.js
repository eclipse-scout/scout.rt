scout.DummyLookupCall = function() {
  scout.DummyLookupCall.parent.call(this);

  this.showText = true;
  this.setDelay(250);
};
scout.inherits(scout.DummyLookupCall, scout.StaticLookupCall);

scout.DummyLookupCall.prototype._data = function() {
  var showText = this.showText;

  return [
    [1, text('Foo')],
    [2, text('Bar')],
    [3, text('Baz')]
  ];

  function text(text) {
    return showText ? text : null;
  }
};

