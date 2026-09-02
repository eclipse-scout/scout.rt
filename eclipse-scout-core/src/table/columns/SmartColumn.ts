/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {BatchCall, BatchCallResult, Cell, codes, CodeType, InitModelOf, LookupCallColumn, LookupCallOrModel, LookupRow, objects, scout, SmartColumnEventMap, SmartColumnModel, SmartField, TableRow} from '../../index';

/**
 * Column where each cell fetches its value using a lookup call.
 *
 * A 'prepareLookupCall' event gets triggered before executing the lookup call and contains two properties, 'lookupCall' and 'row'. Here, 'lookupCall' is the
 * lookup call which is used to fetch one or more values for a cell. 'row' is the row containing the cell and usually corresponds to the selected row.
 * It should be used instead of the property selectedRows from Table.js which must not be used here.
 * 'row' can be null or undefined in some cases. Hence, some care is needed when listening to this event.
 */
export class SmartColumn<TValue> extends LookupCallColumn<TValue> implements SmartColumnModel<TValue> {
  declare model: SmartColumnModel<TValue>;
  declare eventMap: SmartColumnEventMap<TValue>;
  declare self: SmartColumn<any>;

  browseAutoExpandAll: boolean;
  browseLoadIncremental: boolean;
  activeFilterEnabled: boolean;

  constructor() {
    super();
    this.browseAutoExpandAll = true;
    this.browseLoadIncremental = false;
    this.activeFilterEnabled = false;
  }

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    this._batchFormat = new BatchCall<TValue, string>(this._batchFormatValues.bind(this));
  }

  protected override _initCell(cell: Cell<TValue>): Cell<TValue> {
    super._initCell(cell);
    cell.sortCode = this._calculateCellSortCode(cell);
    return cell;
  }

  protected _calculateCellSortCode(cell: Cell<TValue>): number {
    if (!this.codeType) {
      return cell.sortCode;
    }
    let codeType = codes.get(this.codeType);
    if (!codeType) {
      return null;
    }
    let code = codeType.get(cell.value);
    return code ? code.sortCode : null;
  }

  protected _updateAllCellSortCodes() {
    this.table.rows.map(row => this.cell(row)).forEach(cell => cell.setSortCode(this._calculateCellSortCode(cell)));
  }

  protected override _setLookupCall(lookupCall: LookupCallOrModel<TValue>) {
    super._setLookupCall(lookupCall);
    if (this.initialized) {
      this._updateAllCellSortCodes();
    }
  }

  protected _batchFormatValues(keys: TValue[]): JQuery.Promise<Map<TValue, string>> {
    const lookupCall = this.lookupCall.cloneForKeys(keys);
    this.trigger('prepareLookupCall', {lookupCall});
    return lookupCall.execute().then(response => {
      if (!objects.isArray(response?.lookupRows)) {
        return null;
      }
      return new Map(response.lookupRows.map(row => [row.key, row.text]));
    });
  }

  protected override _setCodeType(codeType: string | (new() => CodeType<TValue>)) {
    super._setCodeType(codeType);
    if (this.initialized) {
      this._updateAllCellSortCodes();
    }
  }

  setBrowseAutoExpandAll(browseAutoExpandAll: boolean) {
    this.setProperty('browseAutoExpandAll', browseAutoExpandAll);
  }

  setBrowseLoadIncremental(browseLoadIncremental: boolean) {
    this.setProperty('browseLoadIncremental', browseLoadIncremental);
  }

  setActiveFilterEnabled(activeFilterEnabled: boolean) {
    this.setProperty('activeFilterEnabled', activeFilterEnabled);
  }

  protected override _formatValue(value: TValue, row?: TableRow): string | JQuery.Promise<BatchCallResult<TValue, unknown>> {
    if (objects.isNullOrUndefined(value)) {
      return '';
    }
    if (!this.lookupCall) {
      return value + '';
    }
    if (this.lookupCall.batch) {
      // use column batch feature
      return super._formatValue(value, row);
    }
    // LookupCall doesn't support batch: resolve each cell separately
    return this._prepareAndExecuteLookupCallCloneAsync(this.lookupCall.cloneForKey(value), row);
  }

  /**
   * Create and set the lookup-row instead of call setValue() as this would execute a lookup by key
   * which is not necessary, since the cell already contains text and value. This also avoids a problem
   * with multiple lookups running at once, see ticket 236960.
   */
  protected override _updateEditorFromValidCell(field: SmartField<TValue>, cell: Cell<TValue>) {
    if (objects.isNullOrUndefined(cell.value)) {
      field.setValue(null);
      return;
    }

    let lookupRow: LookupRow<TValue> = new LookupRow();
    lookupRow.key = cell.value;
    lookupRow.text = cell.text;
    field.setLookupRow(lookupRow);
  }

  protected override _createEditor(row: TableRow): SmartField<TValue> {
    let field = scout.create(SmartField, {
      parent: this.table,
      codeType: this.codeType,
      lookupCall: this.lookupCall ? this.lookupCall.clone() : null,
      browseHierarchy: this.browseHierarchy,
      browseMaxRowCount: this.browseMaxRowCount,
      browseAutoExpandAll: this.browseAutoExpandAll,
      browseLoadIncremental: this.browseLoadIncremental,
      activeFilterEnabled: this.activeFilterEnabled
    }) as SmartField<TValue>;

    field.on('prepareLookupCall', event => {
      this.trigger('prepareLookupCall', {
        lookupCall: event.lookupCall,
        row: row
      });
    });
    field.on('lookupCallDone', event => {
      this.trigger('lookupCallDone', {
        result: event.result
      });
    });

    return field;
  }

  /**
   * Since we don't know the type of the key from the lookup-row we must deal with numeric and string types here.
   */
  protected override _hasCellValue(cell: Cell<TValue>): boolean {
    let value = cell.value;
    if (objects.isNumber(value)) {
      return !objects.isNullOrUndefined(value); // Zero (0) is valid too
    }
    return !!value;
  }

  protected override _setCellValue(row: TableRow, value: TValue, cell: Cell<TValue>) {
    super._setCellValue(row, value, cell);
    cell.setSortCode(this._calculateCellSortCode(cell));
  }
}
