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
import {Column, DoubleClickSupport, EventListener, EventSupport, Range, Table, TableControl, TableFooter, TableRow} from '../../index';

export default class SpecTable extends Table {
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

  override _setTableControls(controls: TableControl[]) {
    super._setTableControls(controls);
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
