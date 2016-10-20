/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.ProposalChooserAdapter = function() {
  scout.ProposalChooserAdapter.parent.call(this);
  this._addAdapterProperties(['model']);
};
scout.inherits(scout.ProposalChooserAdapter, scout.ModelAdapter);

scout.ProposalChooserAdapter.prototype._postCreateWidget = function() {
  this._injectSendFunction();

  this.widget.model.on('rowClicked', this._onModelProposalSelected.bind(this));
};

scout.ProposalChooserAdapter.prototype._modelAdapter = function() {
  if (this.widget && this.widget.model) {
    return this.widget.model.modelAdapter;
  } else {
    return null;
  }
};

/**
 * This function is required because on slow connections issues. When a rowsSelected
 * event and an acceptProposal event is sent in the same request, the acceptProposal
 * event "wins". Which means, it sets the displayText to a wrong, old text. To
 * prevent this, we don't send the acceptProposal event at all, as long as the
 * displayText has not changed (in that case the displayText of the acceptProposal
 * event would still win).
 */
scout.ProposalChooserAdapter.prototype._onModelProposalSelected = function(event) {
  this.widget.owner.proposalSelected();
};

/**
 * We wrap the original _send method of the ModelAdapter
 */
scout.ProposalChooserAdapter.prototype._injectSendFunction = function() {
  var modelAdapter = this._modelAdapter();
  if (!modelAdapter) {
    return;
  }
  var origSendFunc = scout.objects.mandatoryFunction(modelAdapter, '_send');
  var sendFunc = function(type, data, options) {
    var extOptions = $.extend({}, options);
    extOptions.showBusyIndicator = false;
    origSendFunc.call(modelAdapter, type, data, extOptions);
  };
  modelAdapter._send = sendFunc;
};

scout.ProposalChooserAdapter.prototype._syncModel = function(model) {
  this._injectSendFunction();
};
