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
/**
 * @class
 * @constructor
 */
scout.Cell = function() {
  this.$cell = null;
  this.cssClass = null;
  this.editable = null; /* do not initialize with false. This is required because there's a subtle logic in Column.js (_initCell) which checks if a value is set. */
  this.errorStatus = null;
  this.horizontalAlignment = null; /* do not initialize with -1. This is required because there's a subtle logic in Column.js (_initCell) which checks if a value is set. */
  this.htmlEnabled = null; /* do not initialize with false. This is required because there's a subtle logic in Column.js (_initCell) which checks if a value is set. */
  this.iconId = null;
  this.mandatory = null;  /* do not initialize with false. This is required because there's a subtle logic in Column.js (_initCell) which checks if a value is set. */
  this._cachedEncodedText = null;
  this.text = null;
  this.value = null;
  this.tooltipText = null;
};

scout.Cell.prototype.init = function(model) {
  this._init(model);
};

scout.Cell.prototype._init = function(model) {
  $.extend(this, model);
};

scout.Cell.prototype.update = function(model) {
  this.setText(model.text);
  $.extend(this, model);
};

scout.Cell.prototype.setEditable = function(editable) {
  this.editable = editable;
};

scout.Cell.prototype.setHorizontalAlignment = function(hAlign) {
  this.horizontalAlignment = hAlign;
};

scout.Cell.prototype.setValue = function(value) {
  this.value = value;
};

scout.Cell.prototype.setText = function(text) {
  var oldText = this.text;
  this.text = text;

  // reset cached encodedText, so when encodedText() is called the next time
  // will be set to the a new value
  if (oldText !== this.text) {
    this._cachedEncodedText = null;
  }
};

scout.Cell.prototype.encodedText = function() {
  if (!this._cachedEncodedText) {
    // Encode text and cache it, encoding is expensive
    this._cachedEncodedText = scout.strings.encode(this.text);
  }
  return this._cachedEncodedText;
};

scout.Cell.prototype.setCssClass = function(cssClass) {
  this.cssClass = cssClass;
};

scout.Cell.prototype.isContentValid = function() {
  var validByErrorStatus = !this.errorStatus || this.errorStatus.severity !== scout.Status.Severity.ERROR;
  var validByMandatory = !this.mandatory || !!this.value;
  return {
    valid: validByErrorStatus && validByMandatory,
    validByErrorStatus: validByErrorStatus,
    validByMandatory: validByMandatory
  };
};
