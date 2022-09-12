/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Key, KeyStroke, ScoutKeyboardEvent} from '../index';

/**
 * KeyStroke which is bound to a range of keys, e.g. ctrl-1 ... ctrl-9.
 */
export default class RangeKeyStroke extends KeyStroke {
  ranges: KeyStrokeRange[];

  constructor() {
    super();
    this.ranges = [];
  }

  registerRange(from: number | (() => number), to: number | (() => number)) {
    this.ranges.push({
      from: from,
      to: to
    });
  }

  protected override _accept(event: ScoutKeyboardEvent): boolean {
    // event.ctrlKey||event.metaKey  --> some keystrokes with ctrl modifier are captured and suppressed by osx use in this cases command key
    if ((event.ctrlKey || event.metaKey) !== this.ctrl || event.altKey !== this.alt || event.shiftKey !== this.shift) {
      return false;
    }
    return this.ranges.some(range => event.which >= this._getRangeFrom(range) && event.which <= this._getRangeTo(range));
  }

  override keys() {
    let keys = [];
    this.ranges.forEach(range => {
      let from = this._getRangeFrom(range);
      let to = this._getRangeTo(range);
      for (let which = from; which <= to; which++) {
        keys.push(new Key(this, which));
      }
    });
    return keys;
  }

  protected _getRangeFrom(range: KeyStrokeRange): number {
    return typeof range.from === 'function' ? range.from() : range.from;
  }

  protected _getRangeTo(range: KeyStrokeRange): number {
    return typeof range.to === 'function' ? range.to() : range.to;
  }
}

export interface KeyStrokeRange {
  from: number | (() => number);
  to: number | (() => number);
}
