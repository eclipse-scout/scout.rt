/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
// circle

export default class VennCircle {

  constructor($circle) {
    this.$circle = $circle;
    this.x;
    this.y;
    this.r;
    this.legend;
    this.legendR;
    this.legendH;
    this.legendV;
    this.labels = [];
  }

  setLegend(legend, horizontal, vertical, r) {
    this.legend = legend;
    this.legendH = horizontal;
    this.legendV = vertical;
    this.legendR = r || this.r;
  }

  addLabel(text, x, y) {
    this.labels.push({
      text: text,
      x: x,
      y: y
    });
  }
}
