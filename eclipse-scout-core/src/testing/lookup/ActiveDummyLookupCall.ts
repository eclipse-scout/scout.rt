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
import {LookupRow, StaticLookupCall} from '../../index';

export default class ActiveDummyLookupCall extends StaticLookupCall<number> {
  constructor() {
    super();
    this.setDelay(250);
  }

  protected override _data() {
    return [
      [1, 'Foo', true],
      [2, 'Bar', false],
      [3, 'Baz', null]
    ];
  }

  protected override _dataToLookupRow(data: any[]): LookupRow<number> {
    let lookupRow = super._dataToLookupRow(data);
    lookupRow.active = data[2];
    return lookupRow;
  }
}
