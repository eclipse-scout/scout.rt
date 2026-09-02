/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  arrays, BatchCall, BatchCallResult, Cell, ColumnValidationResult, InitModelOf, LookupBox, LookupCallColumn, LookupColumnEventMap, LookupColumnModel, LookupEditor, LookupRow, scout, Status, strings, TableRow, ValueField
} from '../../index';

/**
 * This column is a multivalued LookupCallColumn. If editable it opens a popup containing a {@link LookupBox} in order to select multiple values.
 */
export class LookupColumn<TValue> extends LookupCallColumn<TValue[], TValue> implements LookupColumnModel<TValue> {
  declare model: LookupColumnModel<TValue>;
  declare eventMap: LookupColumnEventMap<TValue>;

  distinct = false;

  /** @see LookupColumnModel.distinct */
  setDistinct(distinct: boolean) {
    this.setProperty('distinct', distinct);
  }

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    this._batchFormat = new BatchCall<TValue[], string>(this._batchFormatValues.bind(this));
  }

  protected _batchFormatValues(keys: TValue[][]): JQuery.Promise<Map<TValue[], string>> {
    const allKeys = [...new Set(keys.flat())];
    return LookupEditor.formatValues(allKeys, this.lookupCall, lookupCall => this.trigger('prepareLookupCall', {lookupCall})).then(rows => {
      const rowsByKey = new Map<TValue, LookupRow<TValue>>(rows.map(r => [r.key, r]));
      return new Map<TValue[], string>(keys.map(k => [k, strings.join(', ', ...k.map(e => rowsByKey.get(e)?.text))]));
    });
  }

  protected override _formatValue(value: TValue[], row?: TableRow): string | JQuery.Promise<BatchCallResult<TValue[], unknown>> {
    if (!value?.length || !this.lookupCall) {
      return '';
    }
    if (this.lookupCall.batch) {
      // use column batch feature
      return super._formatValue(value, row);
    }
    return this._prepareAndExecuteLookupCallCloneAsync(this.lookupCall.cloneForKeys(value), row);
  }

  protected override _createEditor(row: TableRow): LookupEditor<TValue> {
    const editor = scout.create(LookupEditor<TValue>, {
      parent: this.table,
      lookupCall: this.lookupCall,
      codeType: this.codeType,
      browseHierarchy: this.browseHierarchy,
      browseMaxRowCount: this.browseMaxRowCount,
      row: row
    });

    editor.on('lookupCallDone', e => {
      if (!this.distinct) {
        return;
      }

      const valuesInOtherRows = [];
      this.table.rows.forEach(r => {
        if (r === row) {
          return;
        }
        valuesInOtherRows.push(...arrays.ensure(this.cellValue(r)));
      });
      arrays.removeAll(valuesInOtherRows, this.cellValue(row));
      e.result.lookupRows.forEach(lookupRow => {
        if (scout.isOneOf(lookupRow.key, valuesInOtherRows)) {
          lookupRow.setEnabled(false);
        }
      });
    });

    // pass editor events to column
    editor.on('prepareLookupCall', e => this.trigger('prepareLookupCall', {
      lookupCall: e.lookupCall,
      row
    }));
    editor.on('lookupCallDone', e => this.trigger('lookupCallDone', {
      result: e.result
    }));

    return editor;
  }

  protected override _updateEditorFromValidCell(field: ValueField<TValue[]>, cell: Cell<TValue[]>) {
    super._updateEditorFromValidCell(field, cell);
    field.setDisplayText(cell.text);
  }

  override isContentValid(row: TableRow): ColumnValidationResult {
    const validationResult = super.isContentValid(row);

    if (!this.distinct) {
      return validationResult;
    }

    const cell = this.cell(row);
    if (!this.table.rows.some(r => r !== row && arrays.containsAny(cell.value, this.cellValue(r)))) {
      return validationResult;
    }

    const distinctErrorStatus = Status.error(this.session.text('ui.LookupColumnDistinctErrorMessage'));
    const errorStatus = validationResult.errorStatus ? Status.error({children: [distinctErrorStatus, validationResult.errorStatus]}) : distinctErrorStatus;
    return {
      ...validationResult,
      valid: errorStatus.isValid(),
      errorStatus
    };
  }
}

