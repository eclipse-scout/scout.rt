/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

export class Insets {
  top: number;
  right: number;
  bottom: number;
  left: number;

  /**
   * If no parameters are passed, all members are initialized with 0.
   */
  constructor(topOrInsets?: number | Insets, right?: number, bottom?: number, left?: number) {
    if (topOrInsets instanceof Insets) {
      this.top = topOrInsets.top;
      this.right = topOrInsets.right;
      this.bottom = topOrInsets.bottom;
      this.left = topOrInsets.left;
    } else {
      this.top = topOrInsets || 0;
      this.right = right || 0;
      this.bottom = bottom || 0;
      this.left = left || 0;
    }
  }

  toString(): string {
    return 'Insets[top=' + this.top + ' right=' + this.right + ' bottom=' + this.bottom + ' left=' + this.left + ']';
  }

  equals(o: Insets): boolean {
    return this.top === o.top &&
      this.right === o.right &&
      this.bottom === o.bottom &&
      this.left === o.left;
  }

  clone(): Insets {
    return new Insets(this.top, this.right, this.bottom, this.left);
  }

  horizontal(): number {
    return this.right + this.left;
  }

  vertical(): number {
    return this.top + this.bottom;
  }

  /**
   * Creates a copy and calls Math.floor() on each property.
   */
  floor(): Insets {
    return new Insets(Math.floor(this.top), Math.floor(this.right), Math.floor(this.bottom), Math.floor(this.left));
  }

  /**
   * Creates a copy and calls Math.ceil() on each property.
   */
  ceil(): Insets {
    return new Insets(Math.ceil(this.top), Math.ceil(this.right), Math.ceil(this.bottom), Math.ceil(this.left));
  }
}
