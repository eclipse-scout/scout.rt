/*******************************************************************************
 * Copyright (c) 2014-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
/**
 * KeyStroke which is bound to a range of keys, e.g. ctrl-1 ... ctrl-9.
 */
scout.RangeKeyStroke = function() {
  scout.RangeKeyStroke.parent.call(this);
  this.ranges = [];
};
scout.inherits(scout.RangeKeyStroke, scout.KeyStroke);

scout.RangeKeyStroke.prototype.registerRange = function(from, to) {
  this.ranges.push({
    from: from,
    to: to
  });
};

/**
 * @override KeyStroke.js
 */
scout.RangeKeyStroke.prototype._accept = function(event) {
  //event.ctrlKey||event.metaKey  --> some keystrokes with ctrl modifier are captured and suppressed by osx use in this cases command key
  if ((event.ctrlKey || event.metaKey) !== this.ctrl ||
    event.altKey !== this.alt ||
    event.shiftKey !== this.shift
  ) {
    return false;
  }

  return this.ranges.some(function(range) {
    return event.which >= this._getRangeFrom(range) && event.which <= this._getRangeTo(range);
  }, this);
};

/**
 * @override KeyStroke.js
 */
scout.RangeKeyStroke.prototype.keys = function() {
  var keys = [];
  this.ranges.forEach(function(range) {
    var from = this._getRangeFrom(range);
    var to = this._getRangeTo(range);

    for (var which = from; which <= to; which++) {
      keys.push(new scout.Key(this, which));
    }
  }, this);

  return keys;
};

scout.RangeKeyStroke.prototype._getRangeFrom = function(range) {
  return (typeof range.from === 'function' ? range.from() : range.from);
};

scout.RangeKeyStroke.prototype._getRangeTo = function(range) {
  return (typeof range.to === 'function' ? range.to() : range.to);
};
