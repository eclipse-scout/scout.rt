/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Column, LookupCall, scout, SmartField, strings} from '../../index';
import objects from '../../util/objects';

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
  }

  /**
   * @override
   */
  _init(model) {
    super._init(model);
    this._setLookupCall(this.lookupCall);
    this._setCodeType(this.codeType);
  }

  setLookupCall(lookupCall) {
    if (this.lookupCall === lookupCall) {
      return;
    }
    this._setLookupCall(lookupCall);
  }

  _setLookupCall(lookupCall) {
    this.lookupCall = LookupCall.ensure(lookupCall, this.session);
  }

  setCodeType(codeType) {
    if (this.codeType === codeType) {
      return;
    }
    this._setCodeType(codeType);
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

  _formatValue(value) {
    if (!this.lookupCall) {
      return strings.nvl(value) + '';
    }
    return this.lookupCall.textByKey(value);
  }

  _createEditor() {
    var field = scout.create('SmartField', {
      parent: this.table,
      codeType: this.codeType,
      lookupCall: this.lookupCall,
      browseHierarchy: this.browseHierarchy,
      browseMaxRowCount: this.browseMaxRowCount,
      browseAutoExpandAll: this.browseAutoExpandAll,
      browseLoadIncremental: this.browseLoadIncremental,
      activeFilterEnabled: this.activeFilterEnabled
    });

    field.on('prepareLookupCall', function(event) {
      this.trigger('prepareLookupCall', {
        lookupCall: event.lookupCall
      });
    }.bind(this));
    field.on('lookupCallDone', function(event) {
      this.trigger('lookupCallDone', {
        result: event.result
      });
    }.bind(this));

    return field;
  }

  /**
   * Since we don't know the type of the key from the lookup-row we must deal with numeric and string types here.
   */
  _hasCellValue(cell) {
    var value = cell.value;
    if (objects.isNumber(value)) {
      return !objects.isNullOrUndefined(value); // Zero (0) is valid too
    }
    return !!value;
  }
}
