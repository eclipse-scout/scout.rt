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
  labels: Label[];
  $circle: JQuery<SVGElement>;
  x: number;
  y: number;
  r: number;
  legend: string;
  legendR: number;
  legendH: -1 | 1;
  legendV: -1 | 1;

  constructor($circle: JQuery<SVGElement>) {
    this.$circle = $circle;
    this.labels = [];
  }

  setLegend(legend: string, horizontal: -1 | 1, vertical: -1 | 1, r?: number) {
    this.legend = legend;
    this.legendH = horizontal;
    this.legendV = vertical;
    this.legendR = r || this.r;
  }

  addLabel(text: number, x: number, y: number) {
    this.labels.push({
      text: text,
      x: x,
      y: y
    });
  }
}

export type Label = {
  text: number;
  x: number;
  y: number;
};

