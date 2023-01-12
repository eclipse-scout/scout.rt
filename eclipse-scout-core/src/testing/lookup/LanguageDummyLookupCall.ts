/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {StaticLookupCall} from '../../index';

export class LanguageDummyLookupCall extends StaticLookupCall<number> {
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
      [100, line.call(this, 'English')],
      [200, line.call(this, 'German')],
      [300, line.call(this, 'Italian')],
      [400, line.call(this, 'French')],
      [500, line.call(this, 'Swiss-German')]
    ];

    function line(text) {
      if (!this.showText) {
        return null;
      }
      if (this.multiline) {
        return '1:' + text + '\n2:' + text;
      }
      return text;
    }
  }
}
