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
 * Note: this class does intentionally NOT set any default values in the constructor.
 * This is required because there's a subtle logic in Column.js which checks if a value is set in a Cell instance
 * and, if not, uses the value from the Column. This class is tightly coupled with Columns.js.
 *
 * @class
 * @constructor
 */
scout.Cell = function() {
  this.$cell;
  this.cssClass;
  this.editable;
  this.errorStatus;
  this.horizontalAlignment;
  this.htmlEnabled;
  this.iconId;
  this.mandatory;
  this._cachedEncodedText;
  this.text;
  this.value;
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

scout.Cell.prototype.isContentValid = function() {
  var validByErrorStatus = !this.errorStatus || this.errorStatus.severity !== scout.Status.Severity.ERROR;
  var validByMandatory = !this.mandatory || !!this.value;
  return {
    valid: validByErrorStatus && validByMandatory,
    validByErrorStatus: validByErrorStatus,
    validByMandatory: validByMandatory
  };
};

