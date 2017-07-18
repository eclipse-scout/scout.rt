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
scout.ProposalField2 = function() {
  scout.ProposalField2.parent.call(this);

  this.maxLength = -1; // FIXME [awe] 7.0 - SF2: implement maxLength and trimText
  this.trimText = true;

  /**
   * If this flag is set to true the proposal field performs a lookup by text when
   * accept proposal is called. The behavior is similar to what the smart-field does
   * in that case, but without the need to have a valid single match as the result
   * from the lookup.
   */
  this.lookupOnAcceptByText = false;
};
scout.inherits(scout.ProposalField2, scout.SmartField2);

scout.ProposalField2.prototype._getValueFromLookupRow = function(lookupRow) {
  return lookupRow.text;
};

scout.ProposalField2.prototype.cssClassName = function() {
  return 'proposal-field';
};

scout.SmartField2.prototype._handleEnterKey = function(event) {
  this.acceptInput();
  event.stopPropagation();
};

scout.ProposalField2.prototype._lookupByTextOrAllDone = function(result) {
  if (result.lookupRows.length === 0) {
    this.setLoading(false);
    this._handleEmptyResult();
    return;
  }
  scout.ProposalField2.parent.prototype._lookupByTextOrAllDone.call(this, result);
};

scout.ProposalField2.prototype._formatValue = function(value) {
  return scout.nvl(value, '');
};

scout.ProposalField2.prototype._ensureValue = function(value) {
  return scout.strings.asString(value);
};

scout.ProposalField2.prototype._acceptByText = function(searchText) {
  $.log.debug('(ProposalField2#_acceptByText) searchText=', searchText);
  if (this.lookupOnAcceptByText) {
    scout.ProposalField2.parent.prototype._acceptByText.call(this, searchText);
  } else {
    this._customTextAccepted(searchText);
  }
};

/**
 * Only used in case lookupOnAcceptByText is true. It's basically the same code
 * as in the smart-field but without the error handling.
 */
scout.ProposalField2.prototype._acceptByTextDone = function(result) {
  this._userWasTyping = false;
  this._extendResult(result);

  // when there's exactly one result, we accept that lookup row
  if (result.numLookupRows === 1) {
    var lookupRow = result.singleMatch;
    if (this._isLookupRowActive(lookupRow)) {
      this.setLookupRow(lookupRow);
      this._inputAccepted();
      return;
    }
  }

  this._customTextAccepted(result.searchText);
};

scout.ProposalField2.prototype._customTextAccepted = function(searchText) {
  this._setLookupRow(null); // only reset property lookup
  this._setValue(searchText);
  this._inputAccepted(true, false);
};

scout.ProposalField2.prototype.getValueForSelection = function() {
  if (!this._showSelection()) {
    return null;
  }
  return this.lookupRow.key;
};
