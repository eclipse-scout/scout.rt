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
scout.LookupBox = function() {
  scout.LookupBox.parent.call(this);
  this.filterBox = null;
  this.gridDataHints.weightY = 1.0;
  this.gridDataHints.h = 2;
  this.value = [];

  this.lookupCall = null;
  this._pendingLookup = null;
  this._currentLookupCall = null;
  this._pendingLookup = null;
  this._lookupExecuted = false;
  this._valueSyncing = false; // true when value is either syncing to table or table to value

  this._addCloneProperties(['lookupCall']);
};
scout.inherits(scout.LookupBox, scout.ValueField);

scout.LookupBox.ErrorCode = {
  NO_DATA: 1
};

scout.LookupBox.prototype._init = function(model) {
  scout.LookupBox.parent.prototype._init.call(this, model);
  if (this.filterBox) {
    this.filterBox.enabledComputed = true; // filter is always enabled
    this.filterBox.recomputeEnabled(true);
    this.filterBox.on('propertyChange', this._onFilterBoxPropertyChange.bind(this));
  }
};

scout.LookupBox.prototype._initValue = function(value) {
  if (this.lookupCall) {
    this._setLookupCall(this.lookupCall);
  }
  this._initStructure(value);
  scout.LookupBox.parent.prototype._initValue.call(this, value);
};

scout.LookupBox.prototype._render = function() {
  this.addContainer(this.$parent, 'lookup-box');
  this.addLabel();
  this.addMandatoryIndicator();
  this.addStatus();
  this.addFieldContainer(this.$parent.makeDiv());

  var htmlComp = scout.HtmlComponent.install(this.$fieldContainer, this.session);
  htmlComp.setLayout(this._createLayout());

  this._ensureLookupCallExecuted();
  this._renderStructure();
  this.$field.addDeviceClass();
  this.$field.addClass('structure');
  this._renderFilterBox();
};

scout.LookupBox.prototype._renderFilterBox = function() {
  if (!this.filterBox || !this.filterBox.visible) {
    return;
  }
  this.filterBox.render(this.$fieldContainer);
};

scout.LookupBox.prototype._ensureValue = function(value) {
  return scout.arrays.ensure(value);
};

scout.LookupBox.prototype._updateEmpty = function() {
  this.empty = scout.arrays.empty(this.value);
};

scout.LookupBox.prototype._lookupByAll = function() {
  if (!this.lookupCall) {
    return;
  }
  this._clearPendingLookup();

  var deferred = $.Deferred();
  var doneHandler = function(result) {
    this._lookupByAllDone(result);
    deferred.resolve(result);
  }.bind(this);

  this._executeLookup(this.lookupCall.cloneForAll(), true)
    .done(doneHandler);

  return deferred.promise();
};

scout.LookupBox.prototype._clearPendingLookup = function() {
  if (this._pendingLookup) {
    clearTimeout(this._pendingLookup);
    this._pendingLookup = null;
  }
};

scout.LookupBox.prototype._executeLookup = function(lookupCall, abortExisting) {
  this.setLoading(true);

  if (abortExisting && this._currentLookupCall) {
    this._currentLookupCall.abort();
  }
  this._currentLookupCall = lookupCall;
  this.trigger('prepareLookupCall', {
    lookupCall: lookupCall
  });

  return lookupCall
    .execute()
    .always(function() {
      this._currentLookupCall = null;
      this._lookupExecuted = true;
      this.setLoading(false);
      this._clearLookupStatus();
    }.bind(this));
};

scout.LookupBox.prototype._lookupByAllDone = function(result) {
  try {
    if (result.exception) {
      // Oops! Something went wrong while the lookup has been processed.
      this.setErrorStatus(scout.Status.error({
        message: result.exception
      }));
      return false;
    }

    // 'No data' case
    if (result.lookupRows.length === 0) {
      this.setLookupStatus(scout.Status.warning({
        message: this.session.text('SmartFieldNoDataFound'),
        code: scout.LookupBox.ErrorCode.NO_DATA
      }));
      return false;
    }

    return true;
  } finally {
    this.trigger('lookupCallDone', {
      result: result
    });
  }
};

scout.LookupBox.prototype._clearLookupStatus = function() {
  this.setLookupStatus(null);
};

scout.LookupBox.prototype._errorStatus = function() {
  return this.lookupStatus || this.errorStatus;
};

scout.LookupBox.prototype.setLookupStatus = function(lookupStatus) {
  this.setProperty('lookupStatus', lookupStatus);
  if (this.rendered) {
    this._renderErrorStatus();
  }
};

scout.LookupBox.prototype.clearErrorStatus = function() {
  this.setErrorStatus(null);
  this._clearLookupStatus();
};

scout.LookupBox.prototype._clearLookupStatus = function() {
  this.setLookupStatus(null);
};

scout.LookupBox.prototype.setLookupCall = function(lookupCall) {
  this.setProperty('lookupCall', lookupCall);
};

scout.LookupBox.prototype._setLookupCall = function(lookupCall) {
  this._setProperty('lookupCall', scout.LookupCall.ensure(lookupCall, this.session));
  this._lookupExecuted = false;
  if (this.rendered) {
    this._ensureLookupCallExecuted();
  }
};

scout.LookupBox.prototype.refreshLookup = function() {
  this._lookupExecuted = false;
  this._ensureLookupCallExecuted();
};

/**
 * @return true if a lookup call execution has been scheduled now. false otherwise.
 */
scout.LookupBox.prototype._ensureLookupCallExecuted = function() {
  if (this._lookupExecuted) {
    return false;
  }
  this._lookupByAll();
  return true;
};

scout.LookupBox.prototype._formatValue = function(value) {
  if (scout.objects.isNullOrUndefined(value)) {
    return '';
  }

  return this._formatLookupRows(this.getCheckedLookupRows());
};

scout.LookupBox.prototype._formatLookupRows = function(lookupRows) {
  lookupRows = scout.arrays.ensure(lookupRows);
  if (lookupRows.length === 0) {
    return '';
  }

  var formatted = [];
  lookupRows.forEach(function(row) {
    formatted.push(row.text);
  });
  return scout.strings.join(', ', formatted);
};

scout.LookupBox.prototype._readDisplayText = function() {
  return this.displayText;
};

scout.LookupBox.prototype._clear = function() {
  this.setValue(null);
};

scout.LookupBox.prototype._onFilterBoxPropertyChange = function(event) {
  if (event.propertyName === 'visible') {
    if (!this.rendered) {
      return;
    }
    if (this.filterBox.visible) {
      this._renderFilterBox();
    } else {
      this.filterBox.remove();
    }
  }
};
