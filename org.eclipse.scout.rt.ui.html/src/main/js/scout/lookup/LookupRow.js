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
scout.LookupRow = function() {
  this.key = null;
  this.text = null;
  this.parentKey = null;
  this.enabled = true;
  this.active = true;
};

scout.LookupRow.prototype.init = function(model) {
  $.extend(this, model);
};

scout.LookupRow.prototype.setKey = function(key) {
  this.key = key;
};

scout.LookupRow.prototype.setText = function(text) {
  this.text = text;
};

scout.LookupRow.prototype.setParentKey = function(parentKey) {
  this.parentKey = parentKey;
};

scout.LookupRow.prototype.toString = function() {
  return 'scout.LookupRow[key=' + this.key + ' text=' + this.text + ']';
};
