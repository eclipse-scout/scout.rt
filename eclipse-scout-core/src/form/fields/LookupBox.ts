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
import {arrays, HtmlComponent, LookupCall, objects, Status, strings, ValueField} from '../../index';
import $ from 'jquery';

export default class LookupBox extends ValueField {

  constructor() {
    super();
    this.filterBox = null;
    this.gridDataHints.weightY = 1.0;
    this.gridDataHints.h = 2;
    this.value = [];
    this.clearable = ValueField.Clearable.NEVER;

    this.lookupCall = null;
    this._pendingLookup = null;
    this._currentLookupCall = null;
    this._pendingLookup = null;
    this._lookupExecuted = false;
    this._valueSyncing = false; // true when value is either syncing to table or table to value

    this._addCloneProperties(['lookupCall']);
  }

  static ErrorCode = {
    NO_DATA: 1
  };

  _init(model) {
    super._init(model);
    if (this.filterBox) {
      this.filterBox.enabledComputed = true; // filter is always enabled
      this.filterBox.recomputeEnabled(true);
      this.filterBox.on('propertyChange', this._onFilterBoxPropertyChange.bind(this));
    }
  }

  _initValue(value) {
    if (this.lookupCall) {
      this._setLookupCall(this.lookupCall);
    }
    this._initStructure(value);
    super._initValue(value);
  }

  _render() {
    this.addContainer(this.$parent, 'lookup-box');
    this.addLabel();
    this.addMandatoryIndicator();
    this.addStatus();
    this.addFieldContainer(this.$parent.makeDiv());

    let htmlComp = HtmlComponent.install(this.$fieldContainer, this.session);
    htmlComp.setLayout(this._createFieldContainerLayout());

    this._ensureLookupCallExecuted();
    this._renderStructure();
    this.$field.addDeviceClass();
    this.$field.addClass('structure');
    this._renderFilterBox();

    this.$container.css('--inactive-lookup-row-suffix-text', `'${this.session.text('InactiveState')}'`);
  }

  _renderFilterBox() {
    if (!this.filterBox || !this.filterBox.visible) {
      return;
    }
    this.filterBox.render(this.$fieldContainer);
  }

  _ensureValue(value) {
    return arrays.ensure(value);
  }

  _updateEmpty() {
    this.empty = arrays.empty(this.value);
  }

  _lookupByAll() {
    if (!this.lookupCall) {
      return;
    }
    this._clearPendingLookup();

    let deferred = $.Deferred();
    let doneHandler = function(result) {
      this._lookupByAllDone(result);
      deferred.resolve(result);
    }.bind(this);

    this._executeLookup(this.lookupCall.cloneForAll(), true)
      .done(doneHandler);

    return deferred.promise();
  }

  _clearPendingLookup() {
    if (this._pendingLookup) {
      clearTimeout(this._pendingLookup);
      this._pendingLookup = null;
    }
  }

  _executeLookup(lookupCall, abortExisting) {
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
      .always(() => {
        this._currentLookupCall = null;
        this._lookupExecuted = true;
        this.setLoading(false);
        this._clearLookupStatus();
      });
  }

  _lookupByAllDone(result) {
    try {
      if (result.exception) {
        // Oops! Something went wrong while the lookup has been processed.
        this.setErrorStatus(Status.error({
          message: result.exception
        }));
        return false;
      }

      // 'No data' case
      if (result.lookupRows.length === 0) {
        this.setLookupStatus(Status.warning({
          message: this.session.text('SmartFieldNoDataFound'),
          code: LookupBox.ErrorCode.NO_DATA
        }));
        return false;
      }

      return true;
    } finally {
      this.trigger('lookupCallDone', {
        result: result
      });
    }
  }

  _errorStatus() {
    return this.lookupStatus || this.errorStatus;
  }

  setLookupStatus(lookupStatus) {
    this.setProperty('lookupStatus', lookupStatus);
    if (this.rendered) {
      this._renderErrorStatus();
    }
  }

  clearErrorStatus() {
    this.setErrorStatus(null);
    this._clearLookupStatus();
  }

  _clearLookupStatus() {
    this.setLookupStatus(null);
  }

  setLookupCall(lookupCall) {
    this.setProperty('lookupCall', lookupCall);
  }

  _setLookupCall(lookupCall) {
    this._setProperty('lookupCall', LookupCall.ensure(lookupCall, this.session));
    this._lookupExecuted = false;
    if (this.rendered) {
      this._ensureLookupCallExecuted();
    }
  }

  refreshLookup() {
    this._lookupExecuted = false;
    this._ensureLookupCallExecuted();
  }

  /**
   * @return {boolean} true if a lookup call execution has been scheduled now. false otherwise.
   */
  _ensureLookupCallExecuted() {
    if (this._lookupExecuted) {
      return false;
    }
    this._lookupByAll();
    return true;
  }

  _formatValue(value) {
    if (objects.isNullOrUndefined(value)) {
      return '';
    }

    return this._formatLookupRows(this.getCheckedLookupRows());
  }

  _formatLookupRows(lookupRows) {
    lookupRows = arrays.ensure(lookupRows);
    if (lookupRows.length === 0) {
      return '';
    }

    let formatted = [];
    lookupRows.forEach(row => {
      formatted.push(row.text);
    });
    return strings.join(', ', formatted);
  }

  _readDisplayText() {
    return this.displayText;
  }

  _clear() {
    this.setValue(null);
  }

  _onFilterBoxPropertyChange(event) {
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
  }
}
