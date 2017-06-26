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

  this.maxLength = -1;
  this.trimText = true;
  this.autoCloseProposalChooser = false;

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

scout.ProposalField2.prototype._acceptInputFail = function(result) {
  this.setValue(result.searchText);
  this._inputAccepted();
};

scout.ProposalField2.prototype.cssClassName = function() {
  return 'proposal-field';
};

scout.ProposalField2.prototype._lookupByTextOrAllDone = function(result) {
  if (result.lookupRows.length === 0) {
    this.hideLookupInProgress();
    this.closePopup();
    return;
  }
  scout.ProposalField2.parent.prototype._lookupByTextOrAllDone.call(this, result);
};

scout.ProposalField2.prototype._formatValue = function(value) {
  return scout.nvl(value, '');
};

scout.ProposalField2.prototype._acceptByText = function(searchText) {
  $.log.debug('(ProposalField2#_acceptByText) searchText=', searchText);
  if (this.lookupOnAcceptByText) {
    scout.ProposalField2.parent.prototype._acceptByText.call(this, searchText);
  } else {
    this.setValue(searchText);
    this._inputAccepted();
  }
};

/**
 * Only used in case lookupOnAcceptByText is true. It's basically the same code
 * as in the smart-field but without the error handling.
 */
scout.ProposalField2.prototype._acceptInputDone = function(result) {
  this._userWasTyping = false;
  this._extendResult(result);

  // when there's exactly one result, we accept that lookup row
  if (result.numLookupRows === 1) {
    var lookupRow = result.singleMatch;
    if (this._isLookupRowActive(lookupRow)) {
      this.setLookupRow(lookupRow);
    } else {
      this.setValue(result.searchText);
    }
  }

  this._inputAccepted();
};

scout.ProposalField2.prototype.getValueForSelection = function() {
  if (this.lookupRow) {
    return this.lookupRow.key;
  }
  return null;
};
