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
scout.Cell = function() {
  this.$cell;
  this.checked = false;
  this.cssClass;
  this.editable = false;
  this.errorStatus;
  this.horizontalAlignment = -1;
  this.htmlEnabled = false;
  this.iconId;
  this._cacheEncodedText;
  this.text;
  this.value;
};

scout.Cell.prototype.init = function(model) {
  this._init(model);
};

scout.Cell.prototype._init = function(model) {
  if (!model.parent) {
    throw new Error('missing property \'parent\'');
  }
  $.extend(this, model);
  scout.defaultValues.applyTo(this);
};

scout.Cell.prototype.update = function(model) {
  var oldText = this.text;
  $.extend(this, model);

  // reset cached encodedText, so when encodedText() is called the next time
  // will be set to the a new value
  if (oldText !== this.text) {
    this._cacheEncodedText = null;
  }
};

scout.Cell.prototype.encodedText = function() {
  if (!this._cacheEncodedText) {
    // Encode text and cache it, encoding is expensive
    this._cacheEncodedText = scout.strings.encode(this.text);
  }
  return this._cacheEncodedText;
};


