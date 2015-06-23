scout.Range = function(from, to) {
  this.from = from;
  this.to = to;
};

// FIXME AWE: (calendar) unit-test scout.Range
scout.Range.prototype.equals = function(other) {
  if (other.from === undefined || other.to === undefined) {
    return false;
  }
  var fromEquals = this._dateEquals(this.from, other.from);
  var toEquals = this._dateEquals(this.to, other.to);
  return fromEquals && toEquals;
};

scout.Range.prototype._dateEquals = function(a, b) {
  if (a === null && b === null) {
    return true;
  }
  if (a === null && b !== null) {
    return false;
  }
  if (a !== null && b === null) {
    return false;
  }
  return a.valueOf() === b.valueOf();
};

scout.Range.prototype.toString = function() {
  return 'scout.Range[' +
    'from=' + (this.from === null ? 'null' : this.from.toUTCString()) +
    ' to=' + (this.to === null ? 'null' : this.to.toUTCString()) + ']';
};
