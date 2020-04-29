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
/*
 * A simple calculator similar to eval but safe in use.
 * Supports the following format:
 * group character ( )
 * digits 0-9
 * operators + - * /
 * decimal separator .
 * no grouping character
 * no whitespace
 */
export default class Calculator {

  constructor() {
  }

  isFormula(input) {
    return input.match(/^[\d()+\-*/.]+$/);
  }

  evalFormula(input) {
    this._tokens = input
      .split(/([\d.]+|\(|\)|[+\-*/])/)
      .filter(e => {
        return e.length !== 0;
      });
    return this._expr();
  }

  _next() {
    if (this._tokens.length === 0) {
      return undefined;
    }
    return this._tokens[0];
  }

  _consumeNext() {
    let cur = this._tokens[0];
    this._tokens = this._tokens.slice(1, this._tokens.length);
    return cur;
  }

  _expr() {
    return this._sum();
  }

  // a+b+...
  _sum() {
    let v = this._prod();
    while (this._next() === '+' || this._next() === '-') {
      switch (this._consumeNext()) { // NOSONAR
        case '+':
          v = v + this._prod();
          break;
        case '-':
          v = v - this._prod();
          break;
      }
    }
    return v;
  }

  // a*b*...
  _prod() {
    let v = this._unary();
    while (this._next() === '*' || this._next() === '/') {
      switch (this._consumeNext()) { // NOSONAR
        case '*':
          v = v * this._unary();
          break;
        case '/':
          v = v / this._unary();
          break;
      }
    }
    return v;
  }

  // [+-]123, [+-](a)
  _unary() {
    let qualifier = 1;
    if (this._next() === '+') {
      this._consumeNext();
    } else if (this._next() === '-') {
      this._consumeNext();
      qualifier = -1;
    }
    let v;
    if ((v = this._group()) !== undefined) {
      return qualifier * v;
    }
    // must be num
    v = this._consumeNext();
    return qualifier * v;
  }

  // (a)
  _group() {
    if (this._next() === '(') {
      this._consumeNext();
      let v = this._expr();
      if (this._next() !== ')') {
        throw 'missing closing bracket';
      }
      this._consumeNext();
      return v;
    }
    return undefined;
  }
}
