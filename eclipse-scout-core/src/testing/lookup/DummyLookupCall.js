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
import {StaticLookupCall} from '../../index';

export default class DummyLookupCall extends StaticLookupCall {
  constructor() {
    super();
    this.multiline = false;
    this.showText = true;
    this.setDelay(250);
  }

  _data() {
    return [
      [1, 'Foo'],
      [2, 'Bar', 1],
      [3, 'Baz', 1]
    ];
  }

  _dataToLookupRow(data) {
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
