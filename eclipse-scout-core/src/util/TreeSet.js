/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {comparators, objects} from '../index';

/**
 * JavaScript port from java.util.TreeSet.
 */
export default class TreeSet {

  constructor() {
    this.array = [];
    this.properties = objects.createMap();
    this.comparator = comparators.ALPHANUMERIC;
  }

  add(value) {
    if (!this.contains(value)) {
      this.array.push(value);
      this.array.sort((a, b) => this.comparator.compare(a, b));
      this.properties[value] = true;
    }
  }

  size() {
    return this.array.length;
  }

  contains(value) {
    return (value in this.properties);
  }

  last() {
    return this.array[this.array.length - 1];
  }
}
