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
};
scout.inherits(scout.SmartFieldTouchPopup, scout.TouchPopup);

scout.SmartFieldTouchPopup.prototype._init = function(options) {
  scout.SmartFieldTouchPopup.parent.prototype._init.call(this, options);
  this._field.on('acceptProposal', this._onFieldAcceptOrDeleteProposal.bind(this));
  this._field.on('deleteProposal', this._onFieldAcceptOrDeleteProposal.bind(this));
};

scout.SmartFieldTouchPopup.prototype._fieldOverrides = function() {
  var obj = scout.SmartFieldTouchPopup.parent.prototype._fieldOverrides.call(this);
  // Make sure proposal chooser does not get cloned, because it would not work (e.g. because selectedRows may not be cloned)
  // It would also generate a loop because field would try to render the chooser and the popup
  // -> The original smart field has to control the chooser
  obj.proposalChooser = null;
  return obj;
};

scout.SmartFieldTouchPopup.prototype._renderProposalChooser = function(proposalChooser) {
  proposalChooser.render(this._$widgetContainer);
  proposalChooser.setParent(this);
  proposalChooser.$container.addClass('touch');
  this._widgetContainerHtmlComp.invalidateLayoutTree();
};

/**
 * @override Popup.js
 */
scout.SmartFieldTouchPopup.prototype._onMouseDownOutside = function(event) {
  // Sync display text first because accept input needs the correct display text
  this._touchField.setDisplayText(this._field.displayText);
  this._touchField.acceptInput();
  this.close();
};

scout.SmartFieldTouchPopup.prototype._onFieldAcceptOrDeleteProposal = function(event) {
  // Delegate to original field
  this._touchField.setDisplayText(event.displayText);
};
