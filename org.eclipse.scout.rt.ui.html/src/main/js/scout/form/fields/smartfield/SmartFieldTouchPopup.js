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
scout.SmartFieldTouchPopup = function() {
  scout.SmartFieldTouchPopup.parent.call(this);
  this._addAdapterProperties('proposalChooser');
};
scout.inherits(scout.SmartFieldTouchPopup, scout.TouchPopup);

scout.SmartFieldTouchPopup.prototype._init = function(options) {
  scout.SmartFieldTouchPopup.parent.prototype._init.call(this, options);
  this._delegateEvents(['acceptProposal', 'proposalTyped', 'cancelProposal']);
  this._delegateDisplayTextChanges(['acceptProposal', 'proposalTyped', 'deleteProposal']);
};

scout.SmartFieldTouchPopup.prototype._fieldOverrides = function() {
  var obj = scout.SmartFieldTouchPopup.parent.prototype._fieldOverrides.call(this);
  // Make sure proposal chooser does not get cloned, because it would not work (e.g. because selectedRows may not be cloned)
  // It would also generate a loop because field would try to render the chooser and the popup
  // -> The original smart field has to control the chooser
  obj.proposalChooser = null;
  return obj;
};

scout.SmartFieldTouchPopup.prototype.setProposalChooser = function(proposalChooser) {
  this.setProperty('proposalChooser', proposalChooser);
};

scout.SmartFieldTouchPopup.prototype._renderProposalChooser = function() {
  this.proposalChooser.render(this._$widgetContainer);
  this.proposalChooser.$container.addClass('touch');
  this._widgetContainerHtmlComp.invalidateLayoutTree();
};

/**
 * @override Popup.js
 */
scout.SmartFieldTouchPopup.prototype._onMouseDownOutside = function(event) {
  // Sync display text first because accept input needs the correct display text
  this._delegateDisplayText();
  this._touchField.acceptInput();
  this.close();
};

// Info: cannot name this method _syncDisplayText because of naming-conflict with our _sync* functions from the Scout framework
scout.SmartFieldTouchPopup.prototype._delegateDisplayText = function() {
  this._touchField.setDisplayText(this._field.displayText);
};

scout.SmartFieldTouchPopup.prototype._delegateEvents = function(eventTypes) {
  var that = this;
  eventTypes.forEach(function(eventType) {
    that._field.on(eventType, function(event) {
      that._touchField.events.trigger(event.type, event);
    });
  });
};

scout.SmartFieldTouchPopup.prototype._delegateDisplayTextChanges = function(eventTypes) {
  eventTypes.forEach(function(eventType) {
    this._field.on(eventType, this._delegateDisplayText.bind(this));
  }, this);
};
