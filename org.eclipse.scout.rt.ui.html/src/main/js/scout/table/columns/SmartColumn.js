/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.SmartColumn = function() {
  scout.SmartColumn.parent.call(this);
  this.codeType = null;
  this.lookupCall = null;
  this.browseHierarchy = false;
  this.browseMaxRowCount = scout.SmartField.DEFAULT_BROWSE_MAX_COUNT;
  this.browseAutoExpandAll = true;
  this.browseLoadIncremental = false;
  this.activeFilterEnabled = false;
};
scout.inherits(scout.SmartColumn, scout.Column);

/**
 * @override
 */
scout.SmartColumn.prototype._init = function(model) {
  scout.SmartColumn.parent.prototype._init.call(this, model);
  this._setLookupCall(this.lookupCall);
  this._setCodeType(this.codeType);
};

scout.SmartColumn.prototype.setLookupCall = function(lookupCall) {
  if (this.lookupCall === lookupCall) {
    return;
  }
  this._setLookupCall(lookupCall);
};

scout.SmartColumn.prototype._setLookupCall = function(lookupCall) {
  this.lookupCall = scout.LookupCall.ensure(lookupCall, this.session);
};

scout.SmartColumn.prototype.setCodeType = function(codeType) {
  if (this.codeType === codeType) {
    return;
  }
  this._setCodeType(codeType);
};

scout.SmartColumn.prototype._setCodeType = function(codeType) {
  this.codeType = codeType;
  if (!codeType) {
    return;
  }
  this.lookupCall = scout.create('CodeLookupCall', {
    session: this.session,
    codeType: codeType
  });
};

scout.SmartColumn.prototype.setBrowseHierarchy = function(browseHierarchy) {
  this.browseHierarchy = browseHierarchy;
};

scout.SmartColumn.prototype.setBrowseMaxRowCount = function(browseMaxRowCount) {
  this.browseMaxRowCount = browseMaxRowCount;
};

scout.SmartColumn.prototype.setBrowseAutoExpandAll = function(browseAutoExpandAll) {
  this.browseAutoExpandAll = browseAutoExpandAll;
};

scout.SmartColumn.prototype.setBrowseLoadIncremental = function(browseLoadIncremental) {
  this.browseLoadIncremental = browseLoadIncremental;
};

scout.SmartColumn.prototype.setActiveFilterEnabled = function(activeFilterEnabled) {
  this.activeFilterEnabled = activeFilterEnabled;
};

scout.SmartColumn.prototype._formatValue = function(value) {
  if (!this.lookupCall) {
    return scout.strings.nvl(value) + '';
  }
  return this.lookupCall.textByKey(value);
};

/**
 * Create and set the lookup-row instead of call setValue() as this would execute a lookup by key
 * which is not necessary, since the cell already contains text and value. This also avoids a problem
 * with multiple lookups running at once, see ticket 236960.
 */
scout.SmartColumn.prototype._initEditorField = function(field, cell) {
  var lookupRow = new scout.LookupRow();
  lookupRow.key = cell.value;
  lookupRow.text = cell.text;
  field.setLookupRow(lookupRow);
};

scout.SmartColumn.prototype._createEditor = function() {
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

  return field;
};
