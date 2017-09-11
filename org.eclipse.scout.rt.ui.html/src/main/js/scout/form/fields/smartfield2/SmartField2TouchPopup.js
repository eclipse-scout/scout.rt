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

/**
 * Info: this class must have the same interface as SmartField2Popup. That's why there's some
 * copy/pasted code here, because we don't have multi inheritance.
 */
scout.SmartField2TouchPopup = function() {
  scout.SmartField2TouchPopup.parent.call(this);
};
scout.inherits(scout.SmartField2TouchPopup, scout.TouchPopup);

scout.SmartField2TouchPopup.prototype._init = function(options) {
  options.withFocusContext = false;
  options.smartField = options.parent; // alias for parent (required by proposal chooser)
  scout.SmartField2TouchPopup.parent.prototype._init.call(this, options);

  this.setLookupResult(options.lookupResult);
  this.setStatus(options.status);
  this.one('close', this._beforeClosePopup.bind(this));
  this.smartField.on('propertyChange', this._onPropertyChange.bind(this));
};

scout.SmartField2TouchPopup.prototype._initWidget = function(options) {
  this._widget = this._createProposalChooser();
  this._widget.on('lookupRowSelected', this._triggerEvent.bind(this));
  this._widget.on('activeFilterSelected', this._triggerEvent.bind(this));
};

scout.SmartField2TouchPopup.prototype._createProposalChooser = function() {
  var objectType = this.parent.browseHierarchy ? 'TreeProposalChooser2' : 'TableProposalChooser2';
  return scout.create(objectType, {
    parent: this,
    touch: true,
    smartField: this._field
  });
};

scout.SmartField2TouchPopup.prototype._fieldOverrides = function() {
  var obj = scout.SmartField2TouchPopup.parent.prototype._fieldOverrides.call(this);
  // Make sure proposal chooser does not get cloned, because it would not work (e.g. because selectedRows may not be cloned)
  // It would also generate a loop because field would try to render the chooser and the popup
  // -> The original smart field has to control the chooser
  obj.proposalChooser = null;
  return obj;
};

scout.SmartField2TouchPopup.prototype._onMouseDownOutside = function() {
  this._acceptInput(); // see: #_beforeClosePopup()
};

/**
 * Delegates the key event to the proposal chooser.
 */
scout.SmartField2TouchPopup.prototype.delegateKeyEvent = function(event) {
  event.originalEvent.smartFieldEvent = true;
  this._widget.delegateKeyEvent(event);
};

scout.SmartField2TouchPopup.prototype.getSelectedLookupRow = function() {
  return this._widget.getSelectedLookupRow();
};

scout.SmartField2TouchPopup.prototype._triggerEvent = function(event) {
  this.trigger(event.type, event);
};

scout.SmartField2TouchPopup.prototype.setLookupResult = function(result) {
  this._widget.setLookupResult(result);
};

scout.SmartField2TouchPopup.prototype.setStatus = function(status) {
  this._widget.setStatus(status);
};

scout.SmartField2TouchPopup.prototype.clearLookupRows = function() {
  this._widget.clearLookupRows();
};

scout.SmartField2TouchPopup.prototype.selectFirstLookupRow = function() {
  this._widget.selectFirstLookupRow();
};

scout.SmartField2TouchPopup.prototype.selectLookupRow = function() {
  this._widget.triggerLookupRowSelected();
};

scout.SmartField2TouchPopup.prototype._onPropertyChange = function(event) {
  if ('lookupStatus' === event.propertyName) {
    this._field.setLookupStatus(event.newValue);
  }
};

scout.SmartField2TouchPopup.prototype._beforeClosePopup = function(event) {
  var embeddedField = this._field;
  if (embeddedField._lookupInProgress) {
    embeddedField.one('acceptInput acceptInputFail', done.bind(this, embeddedField));
  } else {
    done.call(this, embeddedField);
  }

  function done(embeddedField) {
    this.smartField.acceptInputFromField(embeddedField);
  }
};
