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
  this._addAdapterProperties(['proposalChooser']);
};
scout.inherits(scout.SmartFieldAdapter, scout.ValueFieldAdapter);

scout.SmartFieldAdapter.prototype._onWidgetProposalTyped = function(event) {
  this._send('proposalTyped', {
    displayText: event.displayText
  }, {
    showBusyIndicator: false
  });
};

scout.SmartFieldAdapter.prototype._onWidgetEvent = function(event) {
  if (event.type === 'proposalTyped') {
    this._onWidgetProposalTyped(event);
  } else {
    scout.SmartFieldAdapter.parent.prototype._onWidgetEvent.call(this, event);
  }
};
