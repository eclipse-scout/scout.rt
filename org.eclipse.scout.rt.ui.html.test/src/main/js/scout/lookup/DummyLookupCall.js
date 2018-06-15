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
scout.DummyLookupCall = function() {
  scout.DummyLookupCall.parent.call(this);

  this.multiline = false;
  this.showText = true;
  this.setDelay(250);
};
scout.inherits(scout.DummyLookupCall, scout.StaticLookupCall);

scout.DummyLookupCall.prototype._data = function() {
  return [
    [1, 'Foo', 0],
    [2, 'Bar', 1],
    [3, 'Baz', 1]
  ];
};

scout.DummyLookupCall.prototype._dataToLookupRow = function(data) {
  var lookupRow = scout.DummyLookupCall.parent.prototype._dataToLookupRow.call(this, data);
  lookupRow.cssClass = lookupRow.text.toLowerCase();
  if (!this.showText) {
    lookupRow.text = null;
  } else if (this.multiline) {
    lookupRow.text = '1:' + lookupRow.text + '\n2:' + lookupRow.text;
  }
  return lookupRow;
};
