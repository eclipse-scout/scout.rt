/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
export class Calculator {

  protected _tokens: string[];

  constructor() {
    this._tokens = [];
  }

  isFormula(input: string): RegExpMatchArray {
    return input.match(/^[\d()+\-*/.]+$/);
  }

  evalFormula(input: string): number {
    this._tokens = input
      .split(/([\d.]+|\(|\)|[+\-*/])/)
      .filter(e => {
        return e.length !== 0;
      });
    return this._expr();
  }

  protected _next(): string {
    if (this._tokens.length === 0) {
      return undefined;
    }
    return this._tokens[0];
  }

  protected _consumeNext(): string {
    let cur = this._tokens[0];
    this._tokens = this._tokens.slice(1, this._tokens.length);
    return cur;
  }

  protected _expr(): number {
    return this._sum();
  }

  // a+b+...
  protected _sum(): number {
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
  protected _prod(): number {
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
  protected _unary(): number {
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
  protected _group(): number {
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
