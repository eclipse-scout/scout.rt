/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays} from '../index';

export class Range {
  from: number;
  to: number;

  constructor(from?: number, to?: number) {
    this.from = from;
    this.to = to;
  }

  equals(other: Range): boolean {
    return this.from === other.from && this.to === other.to;
  }

  /**
   * Subtracts the given range and returns an array of the remaining ranges.
   */
  subtract(other: Range): Range[] {
    // other is empty
    if (other.size() === 0) {
      return [new Range(this.from, this.to)];
    }
    // other is greater
    if (this.from >= other.from && this.to <= other.to) {
      return [new Range(0, 0)];
    }
    // other is contained completely
    if (other.from >= this.from && other.to <= this.to) {
      let range1 = new Range(this.from, other.from);
      let range2 = new Range(other.to, this.to);
      if (range1.size() === 0) {
        return [range2];
      }
      if (range2.size() === 0) {
        return [range1];
      }
      return [range1, range2];
    }
    // other overlaps on the bottom
    if (other.from > this.from && other.from < this.to) {
      return [new Range(this.from, other.from)];
    }
    // other overlaps on the top
    if (this.from > other.from && this.from < other.to) {
      return [new Range(other.to, this.to)];
    }
    // other is outside
    return [new Range(this.from, this.to)];
  }

  /**
   * Subtracts every given range and returns an array of the remaining ranges.
   */
  subtractAll(others: Range[]): Range[] {
    let other = others.shift();
    let remains: Range[] = [this];
    let newRemains = [];
    // Subtract every other element from the remains of every subtraction
    while (other) {
      remains.forEach(subtract.bind(other));
      remains = newRemains;
      newRemains = [];
      other = others.shift();
    }
    // Remove empty ranges
    remains = remains.filter(remainingElem => {
      return remainingElem.size() > 0;
    });
    // If nothing is left add one empty range to be consistent with .subtract()
    if (remains.length === 0) {
      remains.push(new Range(0, 0));
    }

    function subtract(remainingElem) {
      arrays.pushAll(newRemains, remainingElem.subtract(other));
    }

    return remains;
  }

  shrink(other: Range): Range {
    // other is empty
    if (other.size() === 0) {
      return new Range(this.from, this.to);
    }
    // other is greater
    if (this.from >= other.from && this.to <= other.to) {
      return new Range(0, 0);
    }
    // other is contained completely
    if (other.from >= this.from && other.to <= this.to) {
      return new Range(this.from, other.to);
    }
    // other overlaps on the bottom
    if (other.from >= this.from && other.from < this.to) {
      return new Range(this.from, other.from);
    }
    // other overlaps on the top
    if (this.from > other.from && this.from < other.to) {
      return new Range(other.to, this.to);
    }
    if (other.to < this.from) {
      return new Range(this.from - other.size() - 1, this.to - other.size() - 1);
    }
    // other is outside
    return new Range(this.from, this.to);
  }

  union(other: Range): Range[] {
    if (this.to < other.from || other.to < this.from) {
      let range1 = new Range(this.from, this.to);
      let range2 = new Range(other.from, other.to);
      if (range1.size() === 0) {
        return [range2];
      }
      if (range2.size() === 0) {
        return [range1];
      }
      return [range1, range2];
    }
    return [new Range(Math.min(this.from, other.from), Math.max(this.to, other.to))];
  }

  add(other: Range): Range {
    if (this.to < other.from || other.to < this.from) {
      let range1 = new Range(this.from, this.to);
      let range2 = new Range(other.from, other.to);
      if (range1.size() === 0) {
        return range2;
      }
      if (range2.size() === 0) {
        return range1;
      }
      throw new Error('Range to add has to border on the existing range. ' + this + ', ' + other);
    }
    return new Range(Math.min(this.from, other.from), Math.max(this.to, other.to));
  }

  intersect(other: Range): Range {
    if (this.to <= other.from || other.to <= this.from) {
      return new Range(0, 0);
    }
    return new Range(Math.max(this.from, other.from), Math.min(this.to, other.to));
  }

  size(): number {
    return this.to - this.from;
  }

  contains(value: number): boolean {
    return this.from <= value && value < this.to;
  }

  toString(): string {
    return 'scout.Range[' +
      'from=' + (this.from === null ? 'null' : this.from) +
      ' to=' + (this.to === null ? 'null' : this.to) + ']';
  }
}
