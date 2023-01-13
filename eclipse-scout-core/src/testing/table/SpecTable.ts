/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Column, DoubleClickSupport, EventListener, EventSupport, Range, Table, TableFooter, TableRow} from '../../index';

export class SpecTable extends Table {
  declare _filteredRows: TableRow[];
  declare _animationRowLimit: number;
  declare events: EventSupport & { _eventListeners: EventListener[] };
  declare footer: TableFooter & { _$infoTableStatus: JQuery; _$infoTableStatusIcon: JQuery };
  declare _doubleClickSupport: DoubleClickSupport & { _lastTimestamp: number };

  override _resizeToFit(column: Column<any>, maxWidth?: number, calculatedSize?: number) {
    super._resizeToFit(column, maxWidth, calculatedSize);
  }

  override _calculateCurrentViewRange(): Range {
    return super._calculateCurrentViewRange();
  }

  override _calculateViewRangeForRowIndex(rowIndex: number): Range {
    return super._calculateViewRangeForRowIndex(rowIndex);
  }

  override _unwrapText(text?: string): string {
    return super._unwrapText(text);
  }

  override _selectedRowsToText(): string {
    return super._selectedRowsToText();
  }
}
