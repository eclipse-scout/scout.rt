/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
export class VennCircle {
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

