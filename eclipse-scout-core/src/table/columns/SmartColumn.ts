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
import {Cell, CodeLookupCall, codes, Column, Event, LookupCall, LookupRow, scout, SmartColumnEventMap, SmartColumnModel, SmartField, strings, TableRow} from '../../index';
import objects from '../../util/objects';
import LookupCallModel from '../../lookup/LookupCallModel';
import {ObjectType} from '../../ObjectFactory';
import {EventMapOf, EventModel} from '../../events/EventEmitter';

export type SmartColumnBatchContext = {
  keySet: Set<any>;
  result: JQuery.Promise<Record<string, string>>;
};

/**
 * Column where each cell fetches its value using a lookup call.
 *
 * A 'prepareLookupCall' event gets triggered before executing the lookup call and contains two properties, 'lookupCall' and 'row'. Here, 'lookupCall' is the
 * lookup call which is used to fetch one ore more values for a cell. 'row' is the row containing the cell and usually corresponds to the selected row.
 * It should be used instead of the property selectedRows from Table.js which must not be used here.
 * 'row' can be null or undefined in some cases. Hence some care is needed when listening to this event.
 */
export default class SmartColumn extends Column implements SmartColumnModel {
  declare model: SmartColumnModel;
  declare eventMap: SmartColumnEventMap;

  codeType: string;
  lookupCall: LookupCall<any>;
  browseHierarchy: boolean;
  browseMaxRowCount: number;
  browseAutoExpandAll: boolean;
  browseLoadIncremental: boolean;
  activeFilterEnabled: boolean;

  protected _lookupCallBatchContext: SmartColumnBatchContext;

  constructor() {
    super();
    this.codeType = null;
    this.lookupCall = null;
    this.browseHierarchy = false;
    this.browseMaxRowCount = SmartField.DEFAULT_BROWSE_MAX_COUNT;
    this.browseAutoExpandAll = true;
    this.browseLoadIncremental = false;
    this.activeFilterEnabled = false;
    this._lookupCallBatchContext = null;
  }

  protected override _init(model: SmartColumnModel) {
    super._init(model);
    this._setLookupCall(this.lookupCall);
    this._setCodeType(this.codeType);
  }

  protected override _initCell(cell: Cell): Cell {
    super._initCell(cell);
    cell.sortCode = this._calculateCellSortCode(cell);
    return cell;
  }

  protected _calculateCellSortCode(cell: Cell): number {
    if (!this.codeType) {
      return null;
    }
    let code = codes.get(this.codeType, cell.value);
    return code ? code.sortCode : null;
  }

  protected _updateAllCellSortCodes() {
    this.table.rows.map(row => this.cell(row)).forEach(cell => cell.setSortCode(this._calculateCellSortCode(cell)));
  }

  setLookupCall(lookupCall: LookupCall<any> | LookupCallModel<any> & { objectType: ObjectType<LookupCall<any>> } | string) {
    this.setProperty('lookupCall', lookupCall);
  }

  protected _setLookupCall(lookupCall: LookupCall<any> | LookupCallModel<any> & { objectType: ObjectType<LookupCall<any>> } | string) {
    let call = LookupCall.ensure(lookupCall, this.session);
    this._setProperty('lookupCall', call);
    if (this.initialized) {
      this._updateAllCellSortCodes();
    }
  }

  setCodeType(codeType: string) {
    this.setProperty('codeType', codeType);
  }

  protected _setCodeType(codeType: string) {
    this._setProperty('codeType', codeType);
    if (codeType) {
      this.lookupCall = scout.create(CodeLookupCall, {
        session: this.session,
        codeType: codeType
      });
    }
    if (this.initialized) {
      this._updateAllCellSortCodes();
    }
  }

  setBrowseHierarchy(browseHierarchy: boolean) {
    this.setProperty('browseHierarchy', browseHierarchy);
  }

  setBrowseMaxRowCount(browseMaxRowCount: number) {
    this.setProperty('browseMaxRowCount', browseMaxRowCount);
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

  protected override _formatValue(value: any, row?: TableRow): string | JQuery.Promise<string> {
    if (!this.lookupCall) {
      return strings.nvl(value) + '';
    }

    if (this.lookupCall.batch) {
      return this._batchFormatValue(value);
    }

    let lookupCall = this.lookupCall.clone();
    this.trigger('prepareLookupCall', {
      lookupCall: lookupCall,
      row: row
    });

    return lookupCall.textByKey(value);
  }

  /**
   * Defers all invocations of the lookup call for the duration of the current event handler.
   * Once the current event handler completes, all lookup calls are resolved in a single batch.
   */
  protected _batchFormatValue(key: any): JQuery.Promise<string> {
    if (objects.isNullOrUndefined(key)) {
      return $.resolvedPromise('');
    }

    let currentBatchContext = this._lookupCallBatchContext;
    if (!currentBatchContext) {
      // create new batch context for this column
      const batchResult = $.Deferred();
      currentBatchContext = {
        keySet: new Set(),
        result: batchResult.promise()
      };
      this._lookupCallBatchContext = currentBatchContext;

      setTimeout(() => {
        // reset batch context for next batch run
        this._lookupCallBatchContext = null;

        let lookupCall = this.lookupCall.clone();
        this.trigger('prepareLookupCall', {
          lookupCall: lookupCall
        });

        // batch lookup texts
        lookupCall.textsByKeys([...currentBatchContext.keySet])
          .then(textMap => batchResult.resolve(textMap)) // resolve result in current batch context
          .catch(e => batchResult.reject(e)); // reject any errors
      });
    }

    // add key to current batch
    currentBatchContext.keySet.add(key);

    // return text for current key
    return currentBatchContext.result.then(textMap => textMap[objects.ensureValidKey(key)] || '');
  }

  /**
   * Create and set the lookup-row instead of call setValue() as this would execute a lookup by key
   * which is not necessary, since the cell already contains text and value. This also avoids a problem
   * with multiple lookups running at once, see ticket 236960.
   */
  protected override _updateEditorFromValidCell(field: SmartField, cell: Cell) {
    if (objects.isNullOrUndefined(cell.value)) {
      field.setValue(null);
      return;
    }

    let lookupRow = new LookupRow();
    lookupRow.key = cell.value;
    lookupRow.text = cell.text;
    field.setLookupRow(lookupRow);
  }

  protected override _createEditor(row: TableRow): SmartField {
    let field = scout.create(SmartField, {
      parent: this.table,
      codeType: this.codeType,
      lookupCall: this.lookupCall ? this.lookupCall.clone() : null,
      browseHierarchy: this.browseHierarchy,
      browseMaxRowCount: this.browseMaxRowCount,
      browseAutoExpandAll: this.browseAutoExpandAll,
      browseLoadIncremental: this.browseLoadIncremental,
      activeFilterEnabled: this.activeFilterEnabled
    });

    // FIXME TS: add correct events as soon as SmartField has been migrated.
    field.on('prepareLookupCall', event => {
      this.trigger('prepareLookupCall', {
        // @ts-ignore
        lookupCall: event.lookupCall,
        row: row
      });
    });
    // FIXME TS: add correct events as soon as SmartField has been migrated.
    field.on('lookupCallDone', event => {
      this.trigger('lookupCallDone', {
        // @ts-ignore
        result: event.result
      });
    });

    return field;
  }

  protected override _updateCellFromValidEditor(row: TableRow, field: SmartField) {
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
    // We cannot use setCellValue since it would add the update event to the updateBuffer but we need the row update to be sync to prevent the flickering
    this._setCellValue(row, field.value, cell);

    // Update row -> Render row, trigger update event
    // Only trigger update row event if text has changed (same as setCellText would do)
    if (row.initialized && oldText !== newText && cell.text === newText) {
      this.table.updateRow(row);
    }

    // Ensure display text is correct (for the rare case that the column computes a different text than the editor field).
    this._updateCellText(row, cell);
  }

  /**
   * Since we don't know the type of the key from the lookup-row we must deal with numeric and string types here.
   */
  protected override _hasCellValue(cell: Cell): boolean {
    let value = cell.value;
    if (objects.isNumber(value)) {
      return !objects.isNullOrUndefined(value); // Zero (0) is valid too
    }
    return !!value;
  }

  protected override _setCellValue(row: TableRow, value: any, cell: Cell) {
    super._setCellValue(row, value, cell);
    cell.setSortCode(this._calculateCellSortCode(cell));
  }

  override trigger<K extends string & keyof EventMapOf<SmartColumn>>(type: K, eventOrModel?: Event | EventModel<EventMapOf<SmartColumn>[K]>): EventMapOf<SmartColumn>[K] {
    return super.trigger(type, eventOrModel);
  }
}
