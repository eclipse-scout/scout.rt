/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */

/**
 * JavaScript port from java.awt.Insets.
 */
export default class Insets {

  constructor(vararg, right, bottom, left) {
    if (vararg instanceof Insets) {
      this.top = vararg.top;
      this.right = vararg.right;
      this.bottom = vararg.bottom;
      this.left = vararg.left;
    } else {
      this.top = vararg || 0;
      this.right = right || 0;
      this.bottom = bottom || 0;
      this.left = left || 0;
    }
  }

  toString() {
    return 'Insets[top=' + this.top + ' right=' + this.right + ' bottom=' + this.bottom + ' left=' + this.left + ']';
  }

  equals(o) {
    return this.top === o.top &&
      this.right === o.right &&
      this.bottom === o.bottom &&
      this.left === o.left;
  }

  clone() {
    return new Insets(this.top, this.right, this.bottom, this.left);
  }

  horizontal() {
    return this.right + this.left;
  }

  vertical() {
    return this.top + this.bottom;
  }

  floor() {
    return new Insets(Math.floor(this.top), Math.floor(this.right), Math.floor(this.bottom), Math.floor(this.left));
  }

  ceil() {
    return new Insets(Math.ceil(this.top), Math.ceil(this.right), Math.ceil(this.bottom), Math.ceil(this.left));
  }
}
