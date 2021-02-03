/*
 * Copyright (c) 2014-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {codes, Column, LookupCall, LookupRow, scout, SmartField, strings} from '../../index';
import objects from '../../util/objects';

/**
 * Column where each cell fetches its value using a lookup call.
 *
 * A 'prepareLookupCall' event gets triggered before executing the lookup call and contains two properties, 'lookupCall' and 'row'. Here, 'lookupCall' is the
 * lookup call which is used to fetch one ore more values for a cell. 'row' is the row containing the cell and usually corresponds to the selected row.
 * It should be used instead of the property selectedRows from Table.js which must not be used here.
 * 'row' can be null or undefined in some cases. Hence some care is needed when listening to this event.
 */
export default class SmartColumn extends Column {

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

  /**
   * @override
   */
  _init(model) {
    super._init(model);
    this._setLookupCall(this.lookupCall);
    this._setCodeType(this.codeType);
  }

  _initCell(cell) {
    super._initCell(cell);
    cell.sortCode = this._calculateCellSortCode(cell);
    return cell;
  }

  _calculateCellSortCode(cell) {
    if (!this.codeType) {
      return null;
    }
    let code = codes.get(this.codeType, cell.value);
    return code ? code.sortCode : null;
  }

  _updateAllCellSortCodes() {
    this.table.rows.map(row => this.cell(row)).forEach(cell => cell.setSortCode(this._calculateCellSortCode(cell)));
  }

  setLookupCall(lookupCall) {
    if (this.lookupCall === lookupCall) {
      return;
    }
    this._setLookupCall(lookupCall);
    this._updateAllCellSortCodes();
  }

  _setLookupCall(lookupCall) {
    this.lookupCall = LookupCall.ensure(lookupCall, this.session);
  }

  setCodeType(codeType) {
    if (this.codeType === codeType) {
      return;
    }
    this._setCodeType(codeType);
    this._updateAllCellSortCodes();
  }

  _setCodeType(codeType) {
    this.codeType = codeType;
    if (!codeType) {
      return;
    }
    this.lookupCall = scout.create('CodeLookupCall', {
      session: this.session,
      codeType: codeType
    });
  }

  setBrowseHierarchy(browseHierarchy) {
    this.browseHierarchy = browseHierarchy;
  }

  setBrowseMaxRowCount(browseMaxRowCount) {
    this.browseMaxRowCount = browseMaxRowCount;
  }

  setBrowseAutoExpandAll(browseAutoExpandAll) {
    this.browseAutoExpandAll = browseAutoExpandAll;
  }

  setBrowseLoadIncremental(browseLoadIncremental) {
    this.browseLoadIncremental = browseLoadIncremental;
  }

  setActiveFilterEnabled(activeFilterEnabled) {
    this.activeFilterEnabled = activeFilterEnabled;
  }

  _formatValue(value, row) {
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
  _batchFormatValue(key) {
    if (objects.isNullOrUndefined(key)) {
      return $.resolvedPromise('');
    }

    let currentBatchContext = this._lookupCallBatchContext;
    if (!currentBatchContext) {
      // create new batch context for this column
      const batchResult = $.Deferred();
      currentBatchContext = {
        keySet: {},
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
        lookupCall.textsByKeys(Object.keys(currentBatchContext.keySet))
          .then(textMap => batchResult.resolve(textMap)) // resolve result in current batch context
          .catch(e => batchResult.reject(e)); // reject any errors
      });
    }

    // add key to current batch
    currentBatchContext.keySet[key] = true;

    // return text for current key
    return currentBatchContext.result.then(textMap => textMap[key] || '');
  }

  /**
   * Create and set the lookup-row instead of call setValue() as this would execute a lookup by key
   * which is not necessary, since the cell already contains text and value. This also avoids a problem
   * with multiple lookups running at once, see ticket 236960.
   */
  _initEditorField(field, cell) {
    if (objects.isNullOrUndefined(cell.value)) {
      field.setValue(null);
      return;
    }

    let lookupRow = new LookupRow();
    lookupRow.key = cell.value;
    lookupRow.text = cell.text;
    field.setLookupRow(lookupRow);
  }

  _createEditor(row) {
    let field = scout.create('SmartField', {
      parent: this.table,
      codeType: this.codeType,
      lookupCall: this.lookupCall ? this.lookupCall.clone() : null,
      browseHierarchy: this.browseHierarchy,
      browseMaxRowCount: this.browseMaxRowCount,
      browseAutoExpandAll: this.browseAutoExpandAll,
      browseLoadIncremental: this.browseLoadIncremental,
      activeFilterEnabled: this.activeFilterEnabled
    });

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
   * @override
   */
  updateCellFromEditor(row, field) {
    this.setCellErrorStatus(row, field.errorStatus);
    if (field.errorStatus) {
      this.setCellText(row, field.displayText);
    } else {
      let cell = this.cell(row);
      if (cell.value === field.value) {
        // If value did not change do nothing (important if column formats the value in a different way than the field)
        return;
      }
      // Always set the text even the value will be set
      // This prevents flickering when display text is updated async.
      // In most of the cases the text computed by the column will be the same as the one from the field.
      this.setCellText(row, field.displayText);
      this.setCellValue(row, field.value);
    }
  }

  /**
   * Since we don't know the type of the key from the lookup-row we must deal with numeric and string types here.
   */
  _hasCellValue(cell) {
    let value = cell.value;
    if (objects.isNumber(value)) {
      return !objects.isNullOrUndefined(value); // Zero (0) is valid too
    }
    return !!value;
  }

  setCellValue(row, value) {
    super.setCellValue(row, value);
    let cell = this.cell(row);
    cell.setSortCode(this._calculateCellSortCode(cell));
  }

  setCellText(row, text, cell) {
    if (!cell) {
      cell = this.cell(row);
    }
    if (cell.text === text) {
      // Break if text did not change.
      // This should actually be in Column.js but some columns never use a text but still need updateRows to be called (e.g. BooleanColumn)
      return;
    }
    super.setCellText(row, text, cell);
  }
}
