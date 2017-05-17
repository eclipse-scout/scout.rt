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
scout.SmartFieldAdapter = function() {
  scout.SmartFieldAdapter.parent.call(this);
};
scout.inherits(scout.SmartFieldAdapter, scout.ValueFieldAdapter);

scout.SmartFieldAdapter.prototype._onWidgetProposalTyped = function(event) {
  this._send('proposalTyped', {
    displayText: event.displayText
  }, {
    showBusyIndicator: false
  });
};

scout.SmartFieldAdapter.prototype._onWidgetAcceptProposal = function(event) {
  this._sendAcceptProposal(event.displayText, event.chooser, event.forceClose);
};

/**
 * Note: we set showBusyIndicator=false in this request, because without it it could cause two calls
 * to send/acceptProposal when the user presses Enter. The first one because of the keyDown event
 * and the second one because of the blur event caused by the busy indicator.
 */
scout.SmartFieldAdapter.prototype._sendAcceptProposal = function(displayText, chooser, forceClose) {
  this._send('acceptProposal', {
    displayText: displayText,
    chooser: chooser,
    forceClose: forceClose
  }, {
    showBusyIndicator: false,
    coalesce: function(previous) {
      return this.target === previous.target && this.type === previous.type;
    }
  });
};

scout.SmartFieldAdapter.prototype._onWidgetCancelProposal = function(event) {
  this._send('cancelProposal');
};

scout.SmartFieldAdapter.prototype._onWidgetDeleteProposal = function(event) {
  this._send('deleteProposal');
};

scout.SmartFieldAdapter.prototype._onWidgetOpenProposal = function(event) {
  this._send('openProposal', {
    displayText: event.displayText,
    selectCurrentValue: event.selectCurrentValue,
    browseAll: event.browseAll
  });
};

scout.SmartFieldAdapter.prototype._onWidgetEvent = function(event) {
  if (event.type === 'proposalTyped') {
    this._onWidgetProposalTyped(event);
  } else if (event.type === 'acceptProposal') {
    this._onWidgetAcceptProposal(event);
  } else if (event.type === 'cancelProposal') {
    this._onWidgetCancelProposal(event);
  } else if (event.type === 'deleteProposal') {
    this._onWidgetDeleteProposal(event);
  } else if (event.type === 'openProposal') {
    this._onWidgetOpenProposal(event);
  } else {
    scout.SmartFieldAdapter.parent.prototype._onWidgetEvent.call(this, event);
  }
};

/**
 * We must reset the flag _requestedProposal in the _sync function of the adapter
 * Because the _sync function of the widget is only called, when the value has
 * changed. However, we must always set the flag to false.
 *
 * Note: we should try to move this flag to the adapter completely. It does not
 * belong to the widget.
 */
scout.SmartFieldAdapter.prototype._syncProposalChooser = function(proposalChooser) {
  $.log.debug('(SmartFieldAdapter#_syncProposalChooser) set _requestedProposal to false');
  this.widget._requestedProposal = false;
  this.widget.callSetter('proposalChooser', proposalChooser);
};
