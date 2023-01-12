/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {comparators, objects} from '../index';

/**
 * JavaScript port from java.util.TreeSet.
 */
export class TreeSet<T extends string | number> {
  array: T[];
  properties: { [key: string | number]: boolean };
  comparator: { compare: (a: T, b: T) => number };

  constructor() {
    this.array = [];
    this.properties = objects.createMap();
    this.comparator = comparators.ALPHANUMERIC;
  }

  add(value: T) {
    if (!this.contains(value)) {
      this.array.push(value);
      this.array.sort((a, b) => this.comparator.compare(a, b));
      this.properties[value] = true;
    }
  }

  size(): number {
    return this.array.length;
  }

  contains(value: T): boolean {
    return (value in this.properties);
  }

  last(): T {
    return this.array[this.array.length - 1];
  }
}
