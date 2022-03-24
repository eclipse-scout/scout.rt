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
import {objects, scout, SmartField, strings} from '../../../index';
import $ from 'jquery';

export default class ProposalField extends SmartField {

  constructor() {
    super();

    this.maxLength = 4000;
    this.trimText = true;

    /**
     * If this flag is set to true the proposal field performs a lookup by text when
     * accept proposal is called. The behavior is similar to what the smart-field does
     * in that case, but without the need to have a valid single match as the result
     * from the lookup.
     */
    this.lookupOnAcceptByText = false;
  }

  _getValueFromLookupRow(lookupRow) {
    return lookupRow.text;
  }

  _getLastSearchText() {
    return this.value;
  }

  cssClassName() {
    return 'proposal-field';
  }

  _handleEnterKey(event) {
    // The state of 'this.popup' is different on various browsers. On some browsers (IE11) we don't
    // do CSS animations. This means IE11 sets the popup to null immediately whereas other browsers
    // use a timeout. Anyway: in case the popup is open at the time the user presses enter, we must
    // stop propagation (e.g. to avoid calls of other registered enter key-shortcuts, like the default
    // button on a form). See Widget.js for details about removing with or without CSS animations.
    let hasPopup = !!this.popup;
    this.acceptInput();
    if (this.popup) {
      this.closePopup();
    }
    if (hasPopup) {
      event.stopPropagation();
    }
  }

  _lookupByTextOrAllDone(result) {
    if (super._handleException(result)) {
      return;
    }
    if (result.lookupRows.length === 0) {
      this.setLoading(false);
      this._handleEmptyResult();
      return;
    }
    super._lookupByTextOrAllDone(result);
  }

  _formatValue(value) {
    return scout.nvl(value, '');
  }

  _validateValue(value) {
    if (objects.isNullOrUndefined(value)) {
      return value;
    }
    let validValue = strings.asString(value);
    if (this.trimText) {
      validValue = validValue.trim();
    }
    if (validValue === '') {
      validValue = null;
    }
    return validValue;
  }

  _ensureValue(value) {
    return strings.asString(value);
  }

  /**
   * When 'clear' has been clicked (searchText is empty), we want to call customTextAccepted,
   * so the new value is sent to the server #221199.
   */
  _acceptByText(sync, searchText) {
    $.log.isDebugEnabled() && $.log.debug('(ProposalField#_acceptByText) searchText=', searchText);
    let async = !sync;

    // In case sync=true we cannot wait for the results of the lookup-call,
    // that's why we simply accept the text that's already in the field
    if (async && this.lookupOnAcceptByText && strings.hasText(searchText)) {
      super._acceptByTextAsync(searchText);
    } else {
      this._customTextAccepted(searchText);
    }
  }

  /**
   * Only used in case lookupOnAcceptByText is true. It's basically the same code
   * as in the smart-field but without the error handling.
   */
  _acceptByTextDone(result) {
    this._userWasTyping = false;
    this._extendResult(result);

    // when there's exactly one result, we accept that lookup row
    if (result.uniqueMatch) {
      let lookupRow = result.uniqueMatch;
      if (this._isLookupRowActive(lookupRow)) {
        this.setLookupRow(lookupRow);
        this._inputAccepted();
        return;
      }
    }

    this._customTextAccepted(result.text);
  }

  _checkResetLookupRow(value) {
    return this.lookupRow && this.lookupRow.text !== value;
  }

  _checkSearchTextChanged(searchText) {
    return this._checkDisplayTextChanged(searchText);
  }

  _customTextAccepted(searchText) {
    this._setLookupRow(null); // only reset property lookup
    this._setValue(searchText);
    this._inputAccepted(true, false);
  }

  getValueForSelection() {
    return this._showSelection() ? this.lookupRow.key : null;
  }

  /**
   * This function is overridden by ProposalField because it has a different behavior than the smart-field.
   */
  _acceptLookupRowAndValueFromField(otherField) {
    if (this.lookupRow !== otherField.lookupRow) {
      this.setLookupRow(otherField.lookupRow);
    }
  }

  /**
   * In ProposalField value and display-text is the same. When a custom text has been entered,
   * the value is set and the lookup-row is null.
   */
  _copyValuesFromField(otherField) {
    if (this.lookupRow !== otherField.lookupRow) {
      this.setLookupRow(otherField.lookupRow);
    }
    if (this.value !== otherField.value) {
      this.setValue(otherField.value);
    }
  }

  _acceptInput(sync, searchText, searchTextEmpty, searchTextChanged, selectedLookupRow) {
    // Do nothing when search text is equals to the text of the current lookup row
    if (!selectedLookupRow && this.lookupRow && this.lookupRow.text === searchText) {
      $.log.isDebugEnabled() && $.log.debug('(ProposalField#_acceptInput) unchanged: text is equals. Close popup');
      this._inputAccepted(false);
      return;
    }

    // 2.) proposal chooser is open -> use the selected row as value
    if (selectedLookupRow) {
      $.log.isDebugEnabled() && $.log.debug('(ProposalField#_acceptInput) lookup-row selected. Set lookup-row, close popup lookupRow=', selectedLookupRow.toString());
      this.clearErrorStatus();
      this.setLookupRow(selectedLookupRow);
      this._inputAccepted();
      return;
    }

    // 3.) proposal chooser is not open -> try to accept the current display text
    // this causes a lookup which may fail and open a new proposal chooser (property
    // change for 'result').
    if (searchTextChanged) {
      this.clearErrorStatus();
      this._acceptByText(sync, searchText);
    } else if (!this._hasUiError()) {
      this._inputAccepted(false);
    } else {
      // even though there's nothing to do, someone could wait for our promise to be resolved
      this._acceptInputDeferred.resolve();
    }

    return this._acceptInputDeferred.promise();
  }

  setTrimText(trimText) {
    this.setProperty('trimText', trimText);
  }

  /**
   * @override ValueField.js
   */
  _updateEmpty() {
    this.empty = strings.empty(this.value);
  }
}
