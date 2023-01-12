/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {LookupRow, StaticLookupCall} from '../../index';

export class DummyLookupCall extends StaticLookupCall<number> {
  multiline: boolean;
  showText: boolean;

  constructor() {
    super();
    this.multiline = false;
    this.showText = true;
    this.setDelay(250);
  }

  protected override _data(): any[] {
    return [
      [1, 'Foo'],
      [2, 'Bar', 1],
      [3, 'Baz', 1]
    ];
  }

  protected override _dataToLookupRow(data: any[]): LookupRow<number> {
    let lookupRow = super._dataToLookupRow(data);
    lookupRow.cssClass = lookupRow.text.toLowerCase();
    if (!this.showText) {
      lookupRow.text = null;
    } else if (this.multiline) {
      lookupRow.text = '1:' + lookupRow.text + '\n2:' + lookupRow.text;
    }
    return lookupRow;
  }
}
