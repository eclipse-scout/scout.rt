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
  this.encodedText;
  this.editable = false;
  this.errorStatus;
  this.horizontalAlignment = -1;
  this.htmlEnabled = false;
  this.iconId;
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
