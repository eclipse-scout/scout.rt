/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Rectangle} from '../../index';
import $ from 'jquery';

export class LogicalGridLayoutSpy {
  canvas: HTMLCanvasElement;

  drawCellBounds($container: JQuery, rows: Rectangle[][]) {
    this._prepareCanvas($container);

    for (let row of rows) {
      for (let cell of row) {
        this._drawGridCell(cell);
      }
    }
  }

  dispose() {
    this.canvas?.remove();
    this.canvas = null;
  }

  protected _prepareCanvas($container: JQuery) {
    if (this.canvas) {
      const context = this.canvas.getContext('2d');
      context.clearRect(0, 0, this.canvas.width, this.canvas.height);
    } else {
      let $canvas = $container.appendElement('<canvas>', 'logical-grid-layout-spy') as JQuery<HTMLCanvasElement>;
      this.canvas = $canvas[0];
    }
    $(this.canvas).attr({
      width: $container.outerWidth(),
      height: $container.outerHeight()
    });
  }

  protected _drawGridCell(cell: Rectangle) {
    this._drawCellLabel(cell);

    const ctx = this.canvas.getContext('2d');
    ctx.beginPath();
    ctx.setLineDash([10, 2]);
    ctx.strokeStyle = '#FF00FF';
    ctx.rect(cell.x, cell.y, cell.width, cell.height);
    ctx.stroke();
    ctx.closePath();
  }

  protected _drawCellLabel(cell: Rectangle) {
    const ctx = this.canvas.getContext('2d');
    let label = `x: ${cell.x}, y: ${cell.y}, w: ${cell.width}, h: ${cell.height}`;
    let labelWidth = ctx.measureText(label).width;
    let labels = [label];
    let labelHeight = 12;
    let lineHeight = 12;
    if (cell.height > cell.width) {
      labels = label.split(', ');
      labelHeight *= labels.length;
      labelWidth = labels
        .map(lbl => ctx.measureText(lbl).width)
        .reduce((prev, curr) => Math.max(prev, curr), 0);
    }
    ctx.beginPath();
    ctx.fillStyle = '#FFF';
    ctx.fillRect(cell.x, cell.y, labelWidth + 2, labelHeight);
    for (let i = 0; i < labels.length; i++) {
      ctx.fillStyle = '#000';
      ctx.fillText(labels[i], cell.x + 2, cell.y + 10 + i * lineHeight);
    }
  }
}
