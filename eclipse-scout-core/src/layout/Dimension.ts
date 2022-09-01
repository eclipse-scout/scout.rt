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

import {Insets} from '../index';

export default class Dimension {
  width: number;
  height: number;

  /**
   * If no parameters are passed, all members are initialized with 0.
   */
  constructor(widthOrDimension?: number | Dimension, height?: number) {
    if (widthOrDimension instanceof Dimension) {
      this.width = widthOrDimension.width;
      this.height = widthOrDimension.height;
    } else {
      this.width = widthOrDimension || 0;
      this.height = height || 0;
    }
  }

  toString(): string {
    return 'Dimension[width=' + this.width + ' height=' + this.height + ']';
  }

  equals(o: Dimension): boolean {
    if (!o) {
      return false;
    }
    return (this.width === o.width && this.height === o.height);
  }

  clone(): Dimension {
    return new Dimension(this.width, this.height);
  }

  subtract(insets: Insets): Dimension {
    return new Dimension(
      this.width - insets.horizontal(),
      this.height - insets.vertical());
  }

  add(insets: Insets): Dimension {
    return new Dimension(
      this.width + insets.horizontal(),
      this.height + insets.vertical());
  }

  /**
   * Creates a copy and calls Math.floor() on each property.
   */
  floor(): Dimension {
    return new Dimension(Math.floor(this.width), Math.floor(this.height));
  }

  /**
   * Creates a copy and calls Math.ceil() on each property.
   */
  ceil(): Dimension {
    return new Dimension(Math.ceil(this.width), Math.ceil(this.height));
  }
}
