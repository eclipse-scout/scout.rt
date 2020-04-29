/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Key, KeyStroke} from '../index';

/**
 * KeyStroke which is bound to a range of keys, e.g. ctrl-1 ... ctrl-9.
 */
export default class RangeKeyStroke extends KeyStroke {

  constructor() {
    super();
    this.ranges = [];
  }

  registerRange(from, to) {
    this.ranges.push({
      from: from,
      to: to
    });
  }

  /**
   * @override KeyStroke.js
   */
  _accept(event) {
    // event.ctrlKey||event.metaKey  --> some keystrokes with ctrl modifier are captured and suppressed by osx use in this cases command key
    if ((event.ctrlKey || event.metaKey) !== this.ctrl ||
      event.altKey !== this.alt ||
      event.shiftKey !== this.shift
    ) {
      return false;
    }

    return this.ranges.some(function(range) {
      return event.which >= this._getRangeFrom(range) && event.which <= this._getRangeTo(range);
    }, this);
  }

  /**
   * @override KeyStroke.js
   */
  keys() {
    let keys = [];
    this.ranges.forEach(function(range) {
      let from = this._getRangeFrom(range);
      let to = this._getRangeTo(range);

      for (let which = from; which <= to; which++) {
        keys.push(new Key(this, which));
      }
    }, this);

    return keys;
  }

  _getRangeFrom(range) {
    return (typeof range.from === 'function' ? range.from() : range.from);
  }

  _getRangeTo(range) {
    return (typeof range.to === 'function' ? range.to() : range.to);
  }
}
