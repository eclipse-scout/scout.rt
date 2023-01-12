/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

export class Point {
  x: number;
  y: number;

  /**
   * If no parameters are passed, all members are initialized with 0.
   */
  constructor(xOrPoint?: number | Point, y?: number) {
    if (xOrPoint instanceof Point) {
      this.x = xOrPoint.x;
      this.y = xOrPoint.y;
    } else {
      this.x = xOrPoint || 0;
      this.y = y || 0;
    }
  }

  toString(): string {
    return 'Point[x=' + this.x + ' y=' + this.y + ']';
  }

  equals(o: Point): boolean {
    if (!o) {
      return false;
    }
    return (this.x === o.x && this.y === o.y);
  }

  clone(): Point {
    return new Point(this.x, this.y);
  }

  add(point: Point): Point {
    return new Point(this.x + point.x, this.y + point.y);
  }

  subtract(point: Point): Point {
    return new Point(this.x - point.x, this.y - point.y);
  }

  /**
   * Creates a copy and calls Math.floor() on each property.
   */
  floor(): Point {
    return new Point(Math.floor(this.x), Math.floor(this.y));
  }

  /**
   * Creates a copy and calls Math.ceil() on each property.
   */
  ceil(): Point {
    return new Point(Math.ceil(this.x), Math.ceil(this.y));
  }
}
