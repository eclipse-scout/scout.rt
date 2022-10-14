/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {DummyLookupCall} from '../index';

export default class ColumnDescriptorDummyLookupCall extends DummyLookupCall {
  constructor() {
    super();
    this.multiline = false;
    this.showText = true;
    this.setDelay(250);
  }

  _dataToLookupRow(data) {
    let lookupRow = super._dataToLookupRow(data);
    lookupRow.additionalTableRowData = {
      column1: lookupRow.text + ' column1',
      column2: lookupRow.text + ' column2'
    };
    return lookupRow;
  }
}
