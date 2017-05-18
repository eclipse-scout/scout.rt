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
};
scout.inherits(scout.ProposalField2, scout.SmartField2);

scout.ProposalField2.prototype._getValueFromLookupRow = function(lookupRow) {
  return lookupRow.text;
};

scout.ProposalField2.prototype._handleInvalidLookup = function(result, numLookupRows, searchText) {
  this.setValue(searchText);
};

scout.ProposalField2.prototype.openPopup2 = function(result) {
  if (result.lookupRows.length === 0) {
    return;
  }
  scout.ProposalField2.parent.prototype.openPopup2.call(this, result);
};

scout.ProposalField2.prototype._formatValue = function(value) {
  return scout.nvl(value, '');
};

scout.ProposalField2.prototype._handleLookupDone = function(result) {
  if (result.lookupRows.length === 0) {
    this.closePopup();
  } else {
    this.popup.setLookupResult(result);
  }
};


