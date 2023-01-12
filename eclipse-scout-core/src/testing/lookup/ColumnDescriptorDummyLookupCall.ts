/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {DummyLookupCall} from '../index';
import {LookupRow} from '../../index';

export class ColumnDescriptorDummyLookupCall extends DummyLookupCall {
  constructor() {
    super();
    this.multiline = false;
    this.showText = true;
    this.setDelay(250);
  }

  protected override _dataToLookupRow(data: any[]): LookupRow<number> {
    let lookupRow = super._dataToLookupRow(data);
    lookupRow.additionalTableRowData = {
      column1: lookupRow.text + ' column1',
      column2: lookupRow.text + ' column2'
    };
    return lookupRow;
  }
}
