/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
scout.ColumnDescriptorDummyLookupCall = function() {
  scout.ColumnDescriptorDummyLookupCall.parent.call(this);

  this.multiline = false;
  this.showText = true;
  this.setDelay(250);
};
scout.inherits(scout.ColumnDescriptorDummyLookupCall, scout.DummyLookupCall);

scout.ColumnDescriptorDummyLookupCall.prototype._dataToLookupRow = function(data) {
  var lookupRow = scout.DummyLookupCall.parent.prototype._dataToLookupRow.call(this, data);

  var additionalTableRowData = {
    column1: lookupRow.text + ' column1',
    column2: lookupRow.text + ' column2'
  };

  lookupRow.additionalTableRowData = additionalTableRowData;

  return lookupRow;
};
