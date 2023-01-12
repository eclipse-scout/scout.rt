/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Dimension, Insets, Point} from '../index';

export class Rectangle {
  x: number;
  y: number;
  width: number;
  height: number;

  /**
   * If no parameters are passed, all members are initialized with 0.
   */
  constructor(xOrRectangle?: number | Rectangle, y?: number, width?: number, height?: number) {
    if (xOrRectangle instanceof Rectangle) {
      this.x = xOrRectangle.x;
      this.y = xOrRectangle.y;
      this.width = xOrRectangle.width;
      this.height = xOrRectangle.height;
    } else {
      this.x = xOrRectangle || 0;
      this.y = y || 0;
      this.width = width || 0;
      this.height = height || 0;
    }
  }

  toString(): string {
    return 'Rectangle[x=' + this.x + ' y=' + this.y + ' width=' + this.width + ' height=' + this.height + ']';
  }

  equals(o: Rectangle): boolean {
    if (!o) {
      return false;
    }
    return (this.x === o.x && this.y === o.y && this.width === o.width && this.height === o.height);
  }

  clone(): Rectangle {
    return new Rectangle(this.x, this.y, this.width, this.height);
  }

  center(): Point {
    return new Point(this.x + this.width / 2, this.y + this.height / 2);
  }

  right(): number {
    return this.x + this.width;
  }

  bottom(): number {
    return this.y + this.height;
  }

  contains(x: number, y: number): boolean {
    return y >= this.y && y < this.y + this.height && x >= this.x && x < this.x + this.width;
  }

  /**
   * Tests whether the specified rectangle intersects this rectangle.
   * This means the two rectangles share at least one internal point.
   *
   * @param r the rectangle to test against
   * @returns true if the specified rectangle intersects this one
   */
  intersects(r: Rectangle): boolean {
    if (!r) {
      return false;
    }
    return r.width > 0 && r.height > 0 && this.width > 0 && this.height > 0 &&
      r.x < this.right() && r.right() > this.x &&
      r.y < this.bottom() && r.bottom() > this.y;
  }

  /**
   * Subtracts the given insets from the rectangle.
   */
  subtract(insets: Insets): Rectangle {
    return new Rectangle(
      this.x + insets.left,
      this.y + insets.top,
      this.width - insets.horizontal(),
      this.height - insets.vertical());
  }

  /**
   * Subtracts the insets only from the dimension properties (width and height)
   */
  subtractFromDimension(insets: Insets): Rectangle {
    return new Rectangle(
      this.x,
      this.y,
      this.width - insets.horizontal(),
      this.height - insets.vertical());
  }

  /**
   * Moves the rectangle the given distance.
   * <p>
   * @param dx the distance to move the rectangle along the x axis.
   * @param dy the distance to move the rectangle along the y axis.
   */
  translate(dx: number, dy: number): Rectangle {
    return new Rectangle(
      this.x + dx,
      this.y + dy,
      this.width,
      this.height);
  }

  /**
   * @returns property x and y of this instance as new {@link Point} instance.
   */
  point(): Point {
    return new Point(this.x, this.y);
  }

  /**
   * @returns property width and height of this instance as new {@link Dimension} instance.
   */
  dimension(): Dimension {
    return new Dimension(this.width, this.height);
  }

  union(r: Rectangle): Rectangle {
    let tx2 = this.width;
    let ty2 = this.height;
    if (tx2 < 0 || ty2 < 0) {
      // This rectangle has negative dimensions...
      // If r has non-negative dimensions then it is the answer.
      // If r is non-existant (has a negative dimension), then both
      // are non-existant and we can return any non-existant rectangle
      // as an answer.  Thus, returning r meets that criterion.
      // Either way, r is our answer.
      return new Rectangle(r.x, r.y, r.width, r.height);
    }
    let rx2 = r.width;
    let ry2 = r.height;
    if (rx2 < 0 || ry2 < 0) {
      return new Rectangle(this.x, this.y, this.width, this.height);
    }
    let tx1 = this.x;
    let ty1 = this.y;
    tx2 += tx1;
    ty2 += ty1;
    let rx1 = r.x;
    let ry1 = r.y;
    rx2 += rx1;
    ry2 += ry1;
    if (tx1 > rx1) {
      tx1 = rx1;
    }
    if (ty1 > ry1) {
      ty1 = ry1;
    }
    if (tx2 < rx2) {
      tx2 = rx2;
    }
    if (ty2 < ry2) {
      ty2 = ry2;
    }
    tx2 -= tx1;
    ty2 -= ty1;
    // tx2,ty2 will never underflow since both original rectangles
    // were already proven to be non-empty
    // they might overflow, though...
    if (tx2 > Number.MAX_VALUE) {
      tx2 = Number.MAX_VALUE;
    }
    if (ty2 > Number.MAX_VALUE) {
      ty2 = Number.MAX_VALUE;
    }
    return new Rectangle(tx1, ty1, tx2, ty2);
  }

  /**
   * Creates a copy and calls Math.floor() on each property.
   */
  floor(): Rectangle {
    return new Rectangle(Math.floor(this.x), Math.floor(this.y), Math.floor(this.width), Math.floor(this.height));
  }

  /**
   * Creates a copy and calls Math.ceil() on each property.
   */
  ceil(): Rectangle {
    return new Rectangle(Math.ceil(this.x), Math.ceil(this.y), Math.ceil(this.width), Math.ceil(this.height));
  }
}
