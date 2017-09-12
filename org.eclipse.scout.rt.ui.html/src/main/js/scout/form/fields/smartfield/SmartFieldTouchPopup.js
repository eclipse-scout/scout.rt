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
 * Info: this class must have the same interface as SmartFieldPopup. That's why there's some
 * copy/pasted code here, because we don't have multi inheritance.
 */
scout.SmartFieldTouchPopup = function() {
  scout.SmartFieldTouchPopup.parent.call(this);
};
scout.inherits(scout.SmartFieldTouchPopup, scout.TouchPopup);

scout.SmartFieldTouchPopup.prototype._init = function(options) {
  options.withFocusContext = false;
  options.smartField = options.parent; // alias for parent (required by proposal chooser)
  scout.SmartFieldTouchPopup.parent.prototype._init.call(this, options);

  this.setLookupResult(options.lookupResult);
  this.setStatus(options.status);
  this.one('close', this._beforeClosePopup.bind(this));
  this.smartField.on('propertyChange', this._onPropertyChange.bind(this));
};

scout.SmartFieldTouchPopup.prototype._initWidget = function(options) {
  this._widget = this._createProposalChooser();
  this._widget.on('lookupRowSelected', this._triggerEvent.bind(this));
  this._widget.on('activeFilterSelected', this._triggerEvent.bind(this));
};

scout.SmartFieldTouchPopup.prototype._createProposalChooser = function() {
  var objectType = this.parent.browseHierarchy ? 'TreeProposalChooser' : 'TableProposalChooser';
  return scout.create(objectType, {
    parent: this,
    touch: true,
    smartField: this._field
  });
};

scout.SmartFieldTouchPopup.prototype._fieldOverrides = function() {
  var obj = scout.SmartFieldTouchPopup.parent.prototype._fieldOverrides.call(this);
  // Make sure proposal chooser does not get cloned, because it would not work (e.g. because selectedRows may not be cloned)
  // It would also generate a loop because field would try to render the chooser and the popup
  // -> The original smart field has to control the chooser
  obj.proposalChooser = null;
  return obj;
};

scout.SmartFieldTouchPopup.prototype._onMouseDownOutside = function() {
  this._acceptInput(); // see: #_beforeClosePopup()
};

/**
 * Delegates the key event to the proposal chooser.
 */
scout.SmartFieldTouchPopup.prototype.delegateKeyEvent = function(event) {
  event.originalEvent.smartFieldEvent = true;
  this._widget.delegateKeyEvent(event);
};

scout.SmartFieldTouchPopup.prototype.getSelectedLookupRow = function() {
  return this._widget.getSelectedLookupRow();
};

scout.SmartFieldTouchPopup.prototype._triggerEvent = function(event) {
  this.trigger(event.type, event);
};

scout.SmartFieldTouchPopup.prototype.setLookupResult = function(result) {
  this._widget.setLookupResult(result);
};

scout.SmartFieldTouchPopup.prototype.setStatus = function(status) {
  this._widget.setStatus(status);
};

scout.SmartFieldTouchPopup.prototype.clearLookupRows = function() {
  this._widget.clearLookupRows();
};

scout.SmartFieldTouchPopup.prototype.selectFirstLookupRow = function() {
  this._widget.selectFirstLookupRow();
};

scout.SmartFieldTouchPopup.prototype.selectLookupRow = function() {
  this._widget.triggerLookupRowSelected();
};

scout.SmartFieldTouchPopup.prototype._onPropertyChange = function(event) {
  if ('lookupStatus' === event.propertyName) {
    this._field.setLookupStatus(event.newValue);
  }
};

scout.SmartFieldTouchPopup.prototype._beforeClosePopup = function(event) {
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
