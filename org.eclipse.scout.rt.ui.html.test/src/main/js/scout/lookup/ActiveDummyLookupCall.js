/*******************************************************************************
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.ActiveDummyLookupCall = function() {
  scout.ActiveDummyLookupCall.parent.call(this);
  this.setDelay(250);
};
scout.inherits(scout.ActiveDummyLookupCall, scout.StaticLookupCall);

scout.ActiveDummyLookupCall.prototype._data = function() {
  return [
    [1, 'Foo', true],
    [2, 'Bar', false],
    [3, 'Baz', null]
  ];
};

scout.ActiveDummyLookupCall.prototype._dataToLookupRow = function(data) {
  var lookupRow = scout.ActiveDummyLookupCall.parent.prototype._dataToLookupRow.call(this, data);
  lookupRow.active = data[2];
  return lookupRow;
};
