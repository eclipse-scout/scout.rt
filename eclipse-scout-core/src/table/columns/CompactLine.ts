/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Cell, CompactLineBlock, scout} from '../../index';

export class CompactLine {

  labelBlock: CompactLineBlock;
  textBlock: CompactLineBlock;

  constructor(labelOrHeaderCell: Cell | string, textOrCell: Cell | string) {
    this.labelBlock = null;
    this.textBlock = null;
    if (labelOrHeaderCell instanceof Cell) {
      this.setLabelBlock(this.convertHeaderCellToBlock(labelOrHeaderCell));
    } else {
      this.setLabelBlock(new CompactLineBlock(labelOrHeaderCell));
    }
    if (textOrCell instanceof Cell) {
      this.setTextBlock(this.convertCellToBlock(textOrCell));
    } else {
      this.setTextBlock(new CompactLineBlock(textOrCell));
    }
  }

  convertHeaderCellToBlock(cell: Cell): CompactLineBlock {
    return this.convertCellToBlock(cell);
  }

  convertCellToBlock(cell: Cell): CompactLineBlock {
    let block = new CompactLineBlock();
    if (cell != null) {
      block.setText(cell.text);
      block.setIcon(cell.iconId);
      block.setEncodeHtmlEnabled(!cell.htmlEnabled);
    }
    return block;
  }

  setLabelBlock(block: CompactLineBlock) {
    scout.assertParameter('block', block);
    this.labelBlock = block;
  }

  setTextBlock(block: CompactLineBlock) {
    scout.assertParameter('block', block);
    this.textBlock = block;
  }

  build(): string {
    let label = this.labelBlock.build();
    if (label) {
      label += ': ';
    }
    let value = this.textBlock.build();
    if (!value) {
      return '';
    }
    return `
<div class="compact-cell-line">
  <span class="compact-cell-line-label">${label}</span>
  <span class="compact-cell-line-value">${value}</span>
</div>`;
  }
}
