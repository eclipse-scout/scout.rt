/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {CodeLookupCall, CodeType, Column, InitModelOf, LookupCall, LookupCallColumnEventMap, LookupCallColumnModel, LookupCallOrModel, scout, SmartField, TableRow, ValueField} from '../../index';

export class LookupCallColumn<TValue, TKey = TValue> extends Column<TValue> implements LookupCallColumnModel<TValue, TKey> {
  declare model: LookupCallColumnModel<TValue, TKey>;
  declare eventMap: LookupCallColumnEventMap<TValue, TKey>;

  lookupCall: LookupCall<TKey> = null;
  codeType: string | (new() => CodeType<TKey>) = null;
  browseHierarchy = false;
  browseMaxRowCount = SmartField.DEFAULT_BROWSE_MAX_COUNT;

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    this._setLookupCall(this.lookupCall);
    this._setCodeType(this.codeType);
  }

  setLookupCall(lookupCall: LookupCallOrModel<TKey>) {
    this.setProperty('lookupCall', lookupCall);
  }

  protected _setLookupCall(lookupCall: LookupCallOrModel<TKey>) {
    let call = LookupCall.ensure(lookupCall, this.session);
    this._setProperty('lookupCall', call);
  }

  setCodeType(codeType: string | (new() => CodeType<TKey>)) {
    this.setProperty('codeType', codeType);
  }

  protected _setCodeType(codeType: string | (new() => CodeType<TKey>)) {
    this._setProperty('codeType', codeType);
    if (codeType) {
      this.lookupCall = scout.create(CodeLookupCall<TKey>, {
        session: this.session,
        codeType
      });
    }
  }

  setBrowseHierarchy(browseHierarchy: boolean) {
    this.setProperty('browseHierarchy', browseHierarchy);
  }

  setBrowseMaxRowCount(browseMaxRowCount: number) {
    this.setProperty('browseMaxRowCount', browseMaxRowCount);
  }

  protected override _updateCellFromValidEditor(row: TableRow, field: ValueField<TValue>) {
    // The following code is only necessary to prevent flickering because the text is updated async.
    // Instead of only calling setCellValue which itself would update the display text, we set the text manually before calling setCellValue.
    // This works because in most of the cases the text computed by the column will be the same as the one computed by the editor field.

    // Clear error status first (regular behavior)
    this.setCellErrorStatus(row, null);

    // Update cell text
    // We cannot use setCellText to not trigger updateRows yet -> it has to be done after the value and row.status are updated correctly.
    let cell = this.cell(row);
    let oldText = cell.text;
    let newText = field.displayText;
    cell.setText(newText);

    // Update cell value
    // We cannot use setCellValue since it would add the update event to the updateBuffer, but we need the row update to be sync to prevent the flickering
    this._setCellValue(row, field.value, cell);

    // Update row -> Render row, trigger update event
    // Only trigger update row event if text has changed (same as setCellText would do)
    if (row.initialized && oldText !== newText && cell.text === newText) {
      this.table.updateRow(row);
    }

    // Ensure display text is correct (for the rare case that the column computes a different text than the editor field).
    this._updateCellText(row, cell);
  }
}

